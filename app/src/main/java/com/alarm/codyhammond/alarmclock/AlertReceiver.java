package com.alarm.codyhammond.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;

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
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle=intent.getBundleExtra(Alarm.ALARM_KEY);
        if(bundle==null)
        {
            Log.i("onReceive","Null Extra");
            return;
        }
        else
        {
            if(bundle.getParcelable(Alarm.ALARM_KEY)==null) {
                return;
            }
            Log.i("onReceive","Non null Extra");
        }
        mainIntent.putExtra(Alarm.ALARM_KEY,bundle);

        context.startActivity(mainIntent);
    }
}
