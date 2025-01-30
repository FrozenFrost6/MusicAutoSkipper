package com.example.musicSkipper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView textSongName, textSongArtist, textSongDuration, textSkipTime, textSkipTimePercent;
    private Handler handler = new Handler(); // Initialize the Handler

    private static final String TAG = "MainActivity";

    private SeekBar seekBar;

    private double skipPercent = 0.6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Views
        textSongName = findViewById(R.id.textSongName);
        textSongArtist = findViewById(R.id.textSongArtist);
        textSongDuration = findViewById(R.id.textSongDuration);
        textSkipTime = findViewById(R.id.textSkipTime);
        textSkipTimePercent = findViewById(R.id.textSkipTimePercent);

        seekBar = findViewById(R.id.seekBarSkip);

        // Register the receiver for the song information
        IntentFilter filter = new IntentFilter("com.example.musicSkipper.SONG_INFO");
        registerReceiver(songInfoReceiver, filter);



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                skipPercent = progress / 100.0;
                textSkipTimePercent.setText("Skip Time: " + progress + "%");
                updateSkipPercentage(skipPercent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // Check if the app has notification listener permission
        if (!isNotificationListenerEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }

        Intent startService = new Intent("com.example.musicSkipper.START_SERVICE");
        sendBroadcast(startService);

    }


    private boolean isNotificationListenerEnabled() {
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(flat);
        while (colonSplitter.hasNext()) {
            String componentName = colonSplitter.next();
            if (componentName.equals(new ComponentName(this, SongNotificationService.class).flattenToString())) {
                return true;
            }
        }
        return false;
    }


    // Update the skip percentage in the service class
    private void updateSkipPercentage(double newPercentage) {
        Intent intent = new Intent("com.example.musicSkipper.UPDATE_SKIP_PERCENT");
        intent.putExtra("percentSkip", newPercentage);
        sendBroadcast(intent);
    }


    // BroadcastReceiver to listen for the song information
    private final BroadcastReceiver songInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract song information from the broadcast
            String songName = intent.getStringExtra("songName");
            String songArtist = intent.getStringExtra("songArtist");
            String songDuration = intent.getStringExtra("songDuration");
            long millis = Long.parseLong(songDuration);

            // Convert to minutes and seconds
            long minutes = (millis / 1000) / 60;
            long seconds = (millis / 1000) % 60;

            // Format the result as "mm:ss"`
            String formattedDuration = String.format("%02d:%02d", minutes, seconds);



            // Update the UI with the new song information
            textSongName.setText("🎵 " + songName);
            textSongArtist.setText("🎤 " + songArtist);
            textSongDuration.setText("⏱ Duration: " + formattedDuration);
            textSkipTime.setText(String.format("Skipping in: " + (int)((millis/1000)*skipPercent) +" seconds."));



        }
    };





    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy mainactivity, deleting class: called");
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(songInfoReceiver);

        Intent stopService = new Intent("com.example.musicSkipper.STOP_SERVICE");
        sendBroadcast(stopService);

        Intent stopServiceIntent = new Intent(this, SongNotificationService.class);
        stopService(stopServiceIntent);

    }


}

