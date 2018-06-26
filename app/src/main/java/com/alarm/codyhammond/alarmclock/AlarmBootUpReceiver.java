package com.alarm.codyhammond.alarmclock;

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
        if(intent.getAction()!=null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent bootUpIntent = new Intent(context, AlarmScheduleService.class);
            context.startService(bootUpIntent);
        }
    }
}
