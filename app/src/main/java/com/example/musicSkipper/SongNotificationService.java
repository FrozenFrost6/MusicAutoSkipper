package com.example.musicSkipper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class SongNotificationService extends NotificationListenerService {
    private static final String TAG = "SongNotificationService";
    private static final String SONG_INFO_BROADCAST = "com.example.musicSkipper.SONG_INFO";
    private static final String UPDATE_SKIP_PERCENT = "com.example.musicSkipper.UPDATE_SKIP_PERCENT";
    private static final String STOP_SERVICE = "com.example.musicSkipper.STOP_SERVICE"; // New action
    private static final String START_SERVICE = "com.example.musicSkipper.START_SERVICE"; // New action

    private boolean terminated = false; // Flag to control termination

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable currentRunnable;
    private MediaController mediaController;
    private double percentSkip = 0.05;



    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate called");

        IntentFilter filter = new IntentFilter(UPDATE_SKIP_PERCENT);
        registerReceiver(percentSkipReceiver, filter);

        // Register a receiver to listen for the termination command
        IntentFilter stopFilter = new IntentFilter(STOP_SERVICE);
        registerReceiver(stopReceiver, stopFilter);

        // Register a receiver to listen for the start command
        IntentFilter startFilter = new IntentFilter(START_SERVICE);
        registerReceiver(startReceiver, startFilter);


    }


    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (STOP_SERVICE.equals(intent.getAction())) {
                Log.d(TAG, "Termination command received. Stopping service.");
                terminated = true;  // Set the flag to true
                if (currentRunnable != null) {
                    handler.removeCallbacks(currentRunnable);
                }

            }
        }
    };

    private BroadcastReceiver startReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (START_SERVICE.equals(intent.getAction())) {
                Log.d(TAG, "Start command received. Starting service.");
                terminated = false;  // Set the flag to true
            }
        }
    };


    private BroadcastReceiver percentSkipReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.musicSkipper.UPDATE_SKIP_PERCENT".equals(intent.getAction())) {
                double newPercentSkip = intent.getDoubleExtra("percentSkip", 0.6);
                percentSkip = newPercentSkip;  // Update the variable
                Log.d(TAG, "Updated percentSkip to: " + percentSkip);
            }
        }
    };



    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if(terminated) {
            Log.d(TAG, "terminated = true");
            return;
        }

        if (sbn.getPackageName().equals("com.google.android.apps.youtube.music")) {
            Notification notification = sbn.getNotification();
            MediaSession.Token mediaSessionToken = (MediaSession.Token) notification.extras.get("android.mediaSession");

            if (mediaSessionToken != null) {
                mediaController = new MediaController(this, mediaSessionToken);

                MediaMetadata metadata = mediaController.getMetadata();
                if (metadata == null) {
                    Log.e(TAG, "Metadata is null. Skipping.");
                    return;
                }

                if(mediaController.getPlaybackState().getState() != PlaybackState.STATE_PAUSED) {
                    if (currentRunnable != null) {
                        handler.removeCallbacks(currentRunnable); // Pause skipping when song is paused
                        Log.d(TAG, "Song paused, skipping paused.");
                    }
                }

                if(mediaController.getPlaybackState().getState() != PlaybackState.STATE_PLAYING) {
                    Log.d(TAG, "playbackState: not skip song: "  + mediaController.getPlaybackState().getState());
                    return;
                }

                long songDurationMillis = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
                String songTitle = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                String songArtist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);

                Log.d(TAG, "Song Title: " + (songTitle != null ? songTitle : "Unknown"));
                Log.d(TAG, "Song Artist: " + (songArtist != null ? songArtist : "Unknown"));
                Log.d(TAG, "Song Duration: " + songDurationMillis);

                Intent intent = new Intent(SONG_INFO_BROADCAST);
                intent.putExtra("songName", songTitle != null ? songTitle : "Unknown");
                intent.putExtra("songArtist", songArtist != null ? songArtist : "Unknown");
                intent.putExtra("songDuration", String.valueOf(songDurationMillis));
                sendBroadcast(intent);

                // Schedule song skipping
                if (songDurationMillis > 0) {
                    if (currentRunnable != null) {
                        handler.removeCallbacks(currentRunnable);
                    }
                    long skipTimeMillis = (long) (percentSkip * songDurationMillis);
                    Log.d(TAG, "Skipping song after " + skipTimeMillis / 1000 + " seconds.");
                    currentRunnable = this::skipSong;
                    handler.postDelayed(currentRunnable, skipTimeMillis);
                }


            }
        }
    }

    private void skipSong() {
        if (mediaController != null) {
            Log.d(TAG, "Skipping song...");
            mediaController.getTransportControls().skipToNext();
        } else {
            Log.e(TAG, "MediaController is null. Cannot skip.");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy called");
        unregisterReceiver(percentSkipReceiver);
        if (currentRunnable != null) {
            handler.removeCallbacks(currentRunnable);
        }
        stopSelf();
    }




}


