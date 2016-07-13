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
    private Calendar calendar;
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
        calendar=Calendar.getInstance();
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
        return alarmTime;
    }

    public String getLabel()
    {
        return Label;
    }

    public void setAlarmOnOff(boolean flag)
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

    public void setDays(Set<Day>days)
    {
        this.days=days;
    }

    public Set<Day> getDays()
    {
        return days;
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
        calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,min);
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
       /* if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DAY_OF_MONTH, 1); */
        if(getDays().size() > 0) {

            while (!getDays().contains(Day.values()[calendar.get(Calendar.DAY_OF_WEEK) - 1])) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        Log.i("add day",calendar.getTime().toString());
        return calendar.getTimeInMillis();
    }

    public void scheduleAlarm(Context context)
    {
        Intent intent=new Intent(context,AlertReceiver.class);
        intent.putExtra(ALARM_KEY,this);
        PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getTimeInMilliseconds(), pi);
    }


    public String getTimeUntilNextAlarmMessage(){
        long timeDifference = calendar.getTimeInMillis() - System.currentTimeMillis();
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

    public void scheduleAlarmSnooze(Context context)
    {
        long current=System.currentTimeMillis();

        Intent intent=new Intent(context,AlertReceiver.class);
        intent.putExtra(ALARM_KEY,this);
        PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,current+SNOOZE_TIME,pi);
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

    public void removeFromSchedule(Context context)
    {
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        //alarmManager.cancel();
    }
}
