package com.example.codyhammond.alarmclock;

import android.content.Context;
import android.os.PowerManager;
import android.view.WindowManager;

/**
 * Created by codyhammond on 7/10/16.
 */
public class StaticWakeLock {
    private static PowerManager.WakeLock wakeLock=null;
    public static int variable;
    public static void acquire(Context context)
    {
        if(wakeLock==null)
        {
            PowerManager powerManager=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock=powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"Alarm");
            if(!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
    }

    public static void release()
    {
        if(wakeLock!=null)
        {
            if(wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
}
