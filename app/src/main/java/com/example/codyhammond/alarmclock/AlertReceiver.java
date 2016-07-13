package com.example.codyhammond.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by codyhammond on 6/29/16.
 */
public class AlertReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i("AlrmRcvr","Intent received");
        Intent mainIntent=new Intent(context,MainActivity.class);
        Intent broadcastIntent=new Intent(context,AlarmBootUpService.class);
        context.sendBroadcast(broadcastIntent);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(intent.getParcelableExtra(Alarm.ALARM_KEY)==null)
        {
            Log.i("onReceive","Null Extra");
        }
        else
        {
            Log.i("onReceive","Non null Extra");
        }
        mainIntent.putExtra(Alarm.ALARM_KEY,intent.getParcelableExtra(Alarm.ALARM_KEY));

        context.startActivity(mainIntent);
    }
}
