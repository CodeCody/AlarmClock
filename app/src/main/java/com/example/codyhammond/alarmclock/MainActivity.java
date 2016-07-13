package com.example.codyhammond.alarmclock;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager manager=getSupportFragmentManager();
        StaticWakeLock.acquire(this);
        Intent intent=getIntent();
        if(intent.hasExtra(Alarm.ALARM_KEY))
        {
            playAlarm(intent);
        }
        if(savedInstanceState==null)
        {
            Fragment fragment=new AlarmList();
            manager.beginTransaction().add(R.id.activity_main,fragment).commit();
        }

    }

    @Override
    public void onNewIntent(Intent intent)
    {
        StaticWakeLock.acquire(this);
        if(intent.hasExtra(Alarm.ALARM_KEY)) {
            playAlarm(intent);
        }
    }

    public void playAlarm(Intent intent)
    {
        final Alarm alarm=intent.getParcelableExtra(Alarm.ALARM_KEY);

        final MediaPlayer mediaPlayer=new MediaPlayer();
        AlertDialog.Builder alertDialogBuilder= new AlertDialog.Builder(this);
        if(alarm.isSnoozeOn()) {
            alertDialogBuilder.setNegativeButton("Snooze", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alarm.scheduleAlarmSnooze(getApplicationContext());
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    StaticWakeLock.release();
                }
            });
        }

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.stop();
                mediaPlayer.release();
                StaticWakeLock.release();
                if(alarm.getDays().size()==0)
                {
                    alarm.setAlarmOnOff(false);
                }
            }
        });

        alertDialogBuilder.setTitle(alarm.getLabel());


        alertDialogBuilder.show();
        if(alarm!=null)
        {
            Log.i("Alarming..","going off..");
            Uri uri=Uri.parse(alarm.getAlarmSoundPath());

            try {
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            }
            catch (IOException io)
            {
                Log.i("onNewIntent",io.getMessage());
            }
        }

        telephonyCheck(mediaPlayer);

    }

    public void telephonyCheck(final MediaPlayer mediaPlayer)
    {

        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(getClass().getSimpleName(), "Incoming call: "
                                + incomingNumber);
                        try {
                            mediaPlayer.pause();
                        } catch (IllegalStateException e) {

                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(getClass().getSimpleName(), "Call State Idle");
                        try {
                            mediaPlayer.start();
                        } catch (IllegalStateException e) {

                        }
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        StaticWakeLock.release();
    }
}
