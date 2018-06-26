package com.alarm.codyhammond.alarmclock;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    private volatile Stack<Alarm> activatedAlarms=new Stack<>();
     Window window;
     MediaPlayer mediaPlayer;

    Vibrator vibrator;

    Set<Alarm> alarmQueue = new TreeSet<>(new Comparator<Alarm>() {
        @Override
        public int compare(Alarm lhs, Alarm rhs) {
            int result = 0;
            long diff = lhs.getMillisecondTime() - rhs.getMillisecondTime();
            if(diff>0){
                return 1;
            }else if (diff < 0){
                return -1;
            }
            return result;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(am!=null)
        am.setMode(AudioManager.MODE_NORMAL);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        vibrator=(Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);



        FragmentManager manager=getSupportFragmentManager();

        Intent intent=getIntent();

        if(savedInstanceState==null)
        {
            Fragment fragment=new AlarmList();
            manager.beginTransaction().add(R.id.activity_main,fragment).commit();
        }

        if(intent.hasExtra(Alarm.ALARM_KEY))
        {
            if(intent.getBundleExtra(Alarm.ALARM_KEY).getParcelable(Alarm.ALARM_KEY)!=null) {
               Alarm alarm=(Alarm)intent.getBundleExtra(Alarm.ALARM_KEY).getParcelable(Alarm.ALARM_KEY);
               if(alarm!=null) {
                   restoreMediaPlayer(alarm.getAlarmSoundPath());

                   playAlarm((Alarm) intent.getBundleExtra(Alarm.ALARM_KEY).getParcelable(Alarm.ALARM_KEY));
               }
            }

        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onNewIntent(Intent intent)
    {

        Log.i("onNewIntent","beforeKeyCheck");

        if(intent.hasExtra(Alarm.ALARM_KEY)) {

            Alarm alarm=(Alarm)intent.getBundleExtra(Alarm.ALARM_KEY).get(Alarm.ALARM_KEY);
            if(alarm==null)
                return;

            Log.i("onNewIntent","called");

            restoreMediaPlayer(alarm.getAlarmSoundPath());

           if(!mediaPlayer.isPlaying()) {
               Log.i("onNewIntent","empty");
                playAlarm(alarm);
            }
            else {
               Log.i("onNewIntent","!empty");
             activatedAlarms.push(alarm);
            }
        }
    }

    public void playAlarm(final Alarm alarm)
    {
        if(alarm == null) {
            return;
        }

        activatedAlarms.push(alarm);

        window=getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        StaticWakeLock.acquire(getApplicationContext());
        buildAlertDialog(alarm);


            Log.i("Alarming..","going off..");
            Uri uri=Uri.parse(alarm.getAlarmSoundPath());




        telephonyCheck(mediaPlayer);

    }

    private void restoreMediaPlayer(String path) {

        Uri uri=Uri.parse(path);

        if(mediaPlayer==null) {
            mediaPlayer = new MediaPlayer();
        }
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
        } catch (IOException | IllegalStateException exception) {
            if(exception.getLocalizedMessage()!=null) {
                Log.e("restoreMediaPlayer", exception.getLocalizedMessage());
            }
        }
    }

    private void buildAlertDialog(final Alarm alarm) {

        AlertDialog.Builder alertDialogBuilder= new AlertDialog.Builder(this);
        if(alarm.isSnoozeOn()) {

            alertDialogBuilder.setNegativeButton("Snooze", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   alarm.setActiveSnooze(true);

                 //  preparePostAlarmSettings(alarm,true);
                }
            });
        }

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

               // preparePostAlarmSettings(alarm,false);
                alarm.setActiveSnooze(false);

            }
        });

        alertDialogBuilder.setTitle(alarm.getLabel());

        alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {


                dismissAlarm();
                Log.i("OKButton","dismissed");


                if(alarm.isSnoozing()) {
                    setPostAlarmSnoozeSettings(alarm,true);
                }
                else {
                    setPostAlarmSnoozeSettings(alarm, false);
                }

                checkDeactivationCandidates(alarm);


                StaticWakeLock.release();

                AlarmScheduleService.updateAlarmSchedule(getApplicationContext());

            }
        });


        alertDialogBuilder.show();
    }



    private void setPostAlarmSnoozeSettings(Alarm alarm,boolean flag) {
        List<Alarm> alarms=Database.getInstance(getApplicationContext()).getAlarms();
        for(Alarm alarm1 : alarms) {
            if(alarm1.getAlarmID().equals(alarm.getAlarmID()) ) {
                alarm1.setActiveSnooze(flag);
                alarm1.setHasSnoozeBeenScheduled(!flag);
            }
        }

    }

    private void checkDeactivationCandidates(Alarm alarm) {
        if(alarm.getDays().size()==0)
        {
            deactivateAlarms();

        }
    }

    private void stopRingTone() {
        Log.i("stopRingTone","stopped");
        if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    private void stopVibration() {
        vibrator.cancel();
    }

    private void clearWindowFlags() {

        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void dismissAlarm() {
        stopRingTone();
        stopVibration();
        clearWindowFlags();
    }

    private void deactivateAlarms() {
        AlarmList fragment=((AlarmList) getSupportFragmentManager().getFragments().get(0));
        Alarm alarm;
        Log.i("MainActivity","deactivateAlarms");
        Log.i("beforeLoop", String.valueOf(activatedAlarms.size()));
        while(!activatedAlarms.empty()) {
            alarm=activatedAlarms.pop();
            if(!alarm.isSnoozing()) {
                alarm.removeFromSchedule(this);
                fragment.showDeactivatedAlarmView(alarm.getAlarmTimeinMilliseconds());
                Log.i("duringLoop", String.valueOf(activatedAlarms.size()));
                Log.i("pop", String.valueOf(alarm.getTime()));
            }
        }
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

        if(telephonyManager!=null)
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
        if(vibrator.hasVibrator()) {
            vibrator.vibrate(new long[]{2000,2000},0);
        }

    }

    private void registerReceiver() {
        ComponentName componentName=new ComponentName(getApplicationContext(),AlarmBootUpReceiver.class);

        getApplicationContext().getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
    }

    private void unregisterReceiver() {
        ComponentName componentName=new ComponentName(getApplicationContext(),AlarmBootUpReceiver.class);

        getApplicationContext().getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        unregisterReceiver();

    }
    @Override
    protected void onPause() {
        super.onPause();
        registerReceiver();

    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }
}
