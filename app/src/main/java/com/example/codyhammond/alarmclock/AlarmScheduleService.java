package com.example.codyhammond.alarmclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by codyhammond on 6/29/16.
 */
public class AlarmScheduleService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int id)
    {
        try {
            Alarm alarm = getAlarm();
            if (alarm != null) {
                StaticWakeLock.acquire(getApplicationContext());
                alarm.scheduleAlarm(getApplicationContext());
              //  Toast.makeText(getApplicationContext(), alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }


     //  Toast.makeText(getApplicationContext(), "Toasty Toast", Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
    }

    public static void updateAlarmSchedule(Context context)
    {
        Intent intent=new Intent(context,AlarmScheduleService.class);
        context.startService(intent);
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

        List<Alarm> alarms=alarmDatabase.getAlarmsForService();


        for(Alarm alarm : alarms)
        {
            if(alarm.isAlarmOn())
            {
                alarmQueue.add(alarm);
            }
        }

        if(alarmQueue.iterator().hasNext())
        {
          //  Toast.makeText(getApplicationContext(),"Got alarms",Toast.LENGTH_LONG).show();
            return alarmQueue.iterator().next();
        }
        else
        {

         //   Toast.makeText(getApplicationContext(),"no alarms",Toast.LENGTH_LONG).show();
            return null;
        }

    }

}
