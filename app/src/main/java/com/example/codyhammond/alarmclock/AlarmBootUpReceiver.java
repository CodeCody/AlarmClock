package com.example.codyhammond.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by codyhammond on 6/29/16.
 */
public class AlarmBootUpReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
       Intent bootUpIntent=new Intent(context,AlarmScheduleService.class);
        context.startService(bootUpIntent);
    }
}
