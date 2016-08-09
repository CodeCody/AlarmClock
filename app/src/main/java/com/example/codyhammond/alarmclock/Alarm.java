package com.example.codyhammond.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by codyhammond on 6/15/16.
 */
public class Alarm implements Parcelable
{
    private String alarmTime;
    public final static String ALARM_KEY="alarm";
    public final static int SNOOZE_TIME=300000;
    private StringBuilder standardTime;
    private String Label="Alarm";
    private Calendar calendar=Calendar.getInstance();
    private boolean isAlarmOn=false;
    private Set<Day>days=new TreeSet<>(new Comparator<Alarm.Day>() {
        @Override
        public int compare(Day lhs, Day rhs) {
            return lhs.ordinal()-rhs.ordinal();
        }
    });
    private String ringtone_title;
    private int alarmID=0;
    private String ringtone_path;
    private boolean snooze_flag=false;


    public Alarm()
    {

    }

    public void setAlarmID(int id)
    {
        alarmID=id;
    }

    public int getAlarmID()
    {
        return alarmID;
    }

    public String getTime()
    {
        String hour=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String min=String.valueOf(calendar.get(Calendar.MINUTE));
        return alarmTime=hour+":"+min;
    }

    public String getLabel()
    {
        return Label;
    }

    public void toggleAlarmOnOff(boolean flag)
    {
        isAlarmOn=flag;
    }

    public void setSetSnoozeOnorOff(boolean snooze_flag)
    {
        this.snooze_flag=snooze_flag;
    }

    public boolean isSnoozeOn()
    {
        return snooze_flag;
    }

    public boolean isAlarmOn()
    {
        return isAlarmOn;
    }

    public void setLabel(String label)
    {
        Label=label;
    }

    public void setDays(Day[] days)
    {
        this.days.addAll(Arrays.asList(days));

    }

    public Set<Day> getDays()
    {
        return days;
    }

    public Day[] getSerializableDayArray()
    {
        return days.toArray(new Day[days.size()]);
    }

    public String getDaysToString()
    {
        StringBuilder dayBuilder=new StringBuilder();
        Iterator<Day>day=days.iterator();
        if(days.size()==0)
            return "Never";
        else if(days.size()==7)
        {
            return "Everyday";
        }
        else
        {
            dayBuilder.append(day.next().toShortString());
            while(day.hasNext())
            {
                dayBuilder.append(" ").append(day.next().toShortString());
            }
        }
        return dayBuilder.toString();
    }

    public void setAlarmTime(String alarmTime)
    {
        this.alarmTime=alarmTime;
        setStandardTime(alarmTime);
    }

    public String getStandardTime()
    {
        return standardTime.toString();
    }


    private void setStandardTime(String time)
    {
        standardTime=new StringBuilder();
        String []splitTime = time.split(":");

        int hour=Integer.parseInt(splitTime[0]);
        int min=Integer.parseInt(splitTime[1]);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,min);
        calendar.set(Calendar.SECOND,0);
        String format;

        if (hour == 0) {
            hour += 12;
            format = "AM";
        }
        else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        standardTime.append(hour).append(" : ");


       // calendar.add(Calendar.AM_PM,format.equals("AM") ? Calendar.AM : Calendar.PM);
        Log.i("Date",calendar.getTime().toString());

        if(String.valueOf(min).length()==1)
        {
            standardTime.append("0").append(min);
        }
        else
        {
            standardTime.append(min);
        }

       standardTime.append(" ").append(format);
        Log.i("Actual Time",standardTime.toString());

    }

    public void setAlarmSoundTitle(String ringtone)
    {
       ringtone_title=ringtone;
    }

    public String getAlarmSoundTitle()
    {
        return ringtone_title;
    }

    public void setAlarmSoundPath(String path)
    {
        ringtone_path=path;
    }

    public String getAlarmSoundPath()
    {
        return ringtone_path;
    }


    public enum Day
    {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY;

        @Override
        public String toString()
        {
            switch (this.ordinal())
            {
                case 0:
                    return "Sunday";
                case 1:
                    return "Monday";
                case 2:
                    return "Tuesday";
                case 3:
                    return "Wednesday";
                case 4:
                    return "Thursday";
                case 5:
                    return "Friday";
                case 6:
                    return "Saturday";
            }
            return null;
        }

        public String toShortString()
        {
            switch (this.ordinal())
            {
                case 0:
                    return "Sun";
                case 1:
                    return "Mon";
                case 2:
                    return "Tue";
                case 3:
                    return "Wed";
                case 4:
                    return "Thu";
                case 5:
                    return "Fri";
                case 6:
                    return "Sat";
            }

            return null;
        }
    }


    public long getTimeInMilliseconds() {
        Log.i("before",calendar.getTime().toString());
        Log.i("before",String.valueOf(calendar.getTimeInMillis()));
        Log.i("current millitime",String.valueOf(System.currentTimeMillis()));

        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        if(getDays().size() > 0) {

            while (!getDays().contains(Day.values()[calendar.get(Calendar.DAY_OF_WEEK) - 1])) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        Log.i("after",calendar.getTime().toString());
        Log.i("after",String.valueOf(calendar.getTimeInMillis()));
        Log.i("current millitime",String.valueOf(calendar.getTimeInMillis()-System.currentTimeMillis()));

        return calendar.getTimeInMillis();
    }

    public void scheduleAlarm(Context context)
    {
        Intent intent=new Intent(context,AlertReceiver.class);
        intent.putExtra(ALARM_KEY,this);
        PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getTimeInMilliseconds(), pi);
      //  Toast.makeText(context,getTimeUntilNextAlarmMessage(),Toast.LENGTH_SHORT).show();
    }


    public String getTimeUntilNextAlarmMessage(){
        long timeDifference = getTimeInMilliseconds() - System.currentTimeMillis();
        long days = timeDifference / (1000 * 60 * 60 * 24);
        long hours = timeDifference / (1000 * 60 * 60) - (days * 24);
        long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
        long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
        String alert = "Alarm will sound in ";
        if (days > 0) {
            alert += String.format(
                    "%d days, %d hours, %d minutes and %d seconds", days,
                    hours, minutes, seconds);
        } else {
            if (hours > 0) {
                alert += String.format("%d hours, %d minutes and %d seconds",
                        hours, minutes, seconds);
            } else {
                if (minutes > 0) {
                    alert += String.format("%d minutes, %d seconds", minutes,
                            seconds);
                } else {
                    alert += String.format("%d seconds", seconds);
                }
            }
        }
       return alert;
    }

    public void scheduleAlarmSnooze(Context context,Database database)
    {
        long current=System.currentTimeMillis();

        Intent intent=new Intent(context,AlarmScheduleService.class);
        intent.putExtra(ALARM_KEY,this);
        calendar.add(Calendar.MINUTE,1);
        database.updateAlarm(this);
        context.startService(intent);


    }

    @Override
    public void writeToParcel(Parcel parcel,int flags)
    {
        parcel.writeInt(isAlarmOn ? 1 : 0);
        parcel.writeInt(isSnoozeOn() ? 1 : 0);
        parcel.writeInt(alarmID);

        parcel.writeString(alarmTime);
        parcel.writeString(standardTime.toString());
        parcel.writeString(Label);
        parcel.writeString(ringtone_path);
        parcel.writeString(ringtone_title);


        try
        {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            ObjectOutputStream oos=new ObjectOutputStream(out);
            oos.writeObject(days.toArray(new Day[days.size()]));
            byte[]byteArray=out.toByteArray();

            parcel.writeInt(byteArray.length);


            parcel.writeByteArray(byteArray);
        }
        catch (IOException io)
        {
            Log.e("writeToParcel",io.getMessage());
        }
    }

    public static final Parcelable.Creator<Alarm>CREATOR=new ClassLoaderCreator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel source, ClassLoader loader) {
            return new Alarm(source);
        }

        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[0];
        }
    };

    private Alarm(Parcel in)
    {
        if(in.readInt()==1)
            isAlarmOn=true;
        else
            isAlarmOn=false;

        if(in.readInt()==1)
            snooze_flag=true;
        else
            snooze_flag=false;


        alarmID=in.readInt();
        alarmTime=in.readString();
        standardTime=new StringBuilder(in.readString());
        Label=in.readString();
        Log.i("Alarm(Parcel in)",alarmTime);

        ringtone_path=in.readString();
        ringtone_title=in.readString();

        try
        {
            byte[] bytes=new byte[in.readInt()];
            in.readByteArray(bytes);
            ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
            ObjectInputStream ois=new ObjectInputStream(bais);
            Object object=ois.readObject();
            if(object instanceof Day[]);
            {
                days.addAll(Arrays.asList((Day[])object));
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            Log.e("readParcel",e.getMessage());
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

/*    public void removeFromSchedule(Context context)
    {
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent();
        intent.putExtra(ALARM_KEY,this);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    } */
}
