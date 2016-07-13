package com.example.codyhammond.alarmclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by codyhammond on 6/29/16.
 */
public class AlarmBootUpService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int id)
    {
        Alarm alarm=getAlarm();
        StaticWakeLock.acquire(getApplicationContext());
        alarm.scheduleAlarm(getApplicationContext());
        Toast.makeText(getApplicationContext(), "a", Toast.LENGTH_SHORT).show();

        return START_NOT_STICKY;
    }


    public Alarm getAlarm()
    {
        Database alarmDatabase=Database.getInstance(getApplicationContext());

        Set<Alarm> alarmQueue = new TreeSet<>(new Comparator<Alarm>() {
            @Override
            public int compare(Alarm lhs, Alarm rhs) {
                int result = 0;
                long diff = lhs.getTimeInMilliseconds() - rhs.getTimeInMilliseconds();
                if(diff>0){
                    return 1;
                }else if (diff < 0){
                    return -1;
                }
                return result;
            }
        });

        List<Alarm> alarms=alarmDatabase.getAlarms();

        for(Alarm alarm : alarms)
        {
            if(alarm.isAlarmOn())
            {
                alarmQueue.add(alarm);
            }
        }

        if(alarmQueue.iterator().hasNext())
        {
            return alarmQueue.iterator().next();
        }
        else
        {
            return null;
        }

    }

}
