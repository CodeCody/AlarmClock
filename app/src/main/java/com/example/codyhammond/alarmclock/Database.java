package com.example.codyhammond.alarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by codyhammond on 6/15/16.
 */
public class Database extends SQLiteOpenHelper {

    public static final String ALARM_TABLE="alarm_table";
    public static final String RINGTONE_TABLE="ringtone_table";
    public static final String ALARM_DATABASE="alarm_database";

    private static final int DB_VERSION=1;
    private final String ALARM_ID="alarm_id";
    private final String ALARM_TIME="alarm_time";
    private final String ALARM_DAYS="alarm_days";
    private final String ALARM_TONE="alarm_tone";
  //  private final String ALARM_VIBRATE="alarm_vibrate";
    private final String ALARM_NAME="alarm_name";
    private final String ALARM_ACTIVE="alarm_active";
    private final String ALARM_SNOOZE="alarm_snooze";
    private final String RINGTONE_ID="ringtone_id";
    private final String RINGTONE_NAME="ringtone_name";
    private final String RINGTONE_PATH="ringtone_path";
    private  SQLiteDatabase myDatabase=null;
    private static Database AlarmDatabase=null;
    private Context context;
    private List<Alarm>alarmList=new LinkedList<>();
    Map<String,String>ringtones=new HashMap<>();

    public static synchronized Database getInstance(Context context)
    {
      if(AlarmDatabase==null)
      {
        AlarmDatabase=new Database(context);
      }
        return AlarmDatabase;
    }
    private Database(Context context)
    {
        super(context,ALARM_DATABASE,null,DB_VERSION);
        this.context=context;
        if(checkifDBExists())
            return;

        myDatabase=getWritableDatabase();
        saveRingTones();
    }

    private boolean checkifDBExists()
    {
        File file=context.getDatabasePath(ALARM_DATABASE);
       if(myDatabase==null) {
           try {
               myDatabase = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
               Log.i("Confirmed", "DB exists");
               return true;
           } catch (Exception database) {
               Log.e("SQLException", "No database");
               return false;
           }
       }
        return true;
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL("CREATE TABLE IF NOT EXISTS "+ALARM_TABLE +
                " ( " + ALARM_ID + " INTEGER primary key autoincrement,"
                + ALARM_ACTIVE + " INTEGER NOT NULL,"
                + ALARM_SNOOZE + " INTEGER NOT NULL,"
                + ALARM_TIME + " TEXT NOT NULL,"
                + ALARM_DAYS + " BLOB,"
                + ALARM_TONE + " TEXT NOT NULL,"
                + ALARM_NAME + " TEXT NOT NULL)");

        database.execSQL("CREATE TABLE IF NOT EXISTS "+RINGTONE_TABLE +
         " ( " + RINGTONE_ID + " INTEGER primary key autoincrement," +
                 RINGTONE_NAME + " TEXT NOT NULL," +
                 RINGTONE_PATH + " TEXT NOT NULL)");
    }

    public Alarm getAlarm(int position)
    {
        return alarmList.get(position);
    }

    public List<Alarm> getAlarms()
    {
        String [] columns= new String[]{ALARM_ID,ALARM_NAME,ALARM_TIME,ALARM_TONE,ALARM_ACTIVE,ALARM_DAYS,ALARM_SNOOZE};
        Cursor cursor=myDatabase.query(ALARM_TABLE,columns,null,null,null,null,null);
        Alarm [] alarms=new Alarm [cursor.getCount()];

        Set<Alarm.Day>days=new TreeSet<>(new Comparator<Alarm.Day>() {
            @Override
            public int compare(Alarm.Day lhs, Alarm.Day rhs) {
                return lhs.ordinal()-rhs.ordinal();
            }
        });

        ringtones=getInitRingTones();

        if(cursor.moveToFirst())
        {
            int i=0;
            while(!cursor.isAfterLast())
            {
                alarms[i]=new Alarm();
                alarms[i].setAlarmID(cursor.getColumnIndex(ALARM_ID));
                alarms[i].setLabel(cursor.getString(cursor.getColumnIndex(ALARM_NAME)));
                alarms[i].setAlarmSoundTitle(cursor.getString(cursor.getColumnIndex(ALARM_TONE)));
                alarms[i].setAlarmSoundPath(ringtones.get(alarms[i].getAlarmSoundTitle()));
                alarms[i].setAlarmTime(cursor.getString(cursor.getColumnIndex(ALARM_TIME)));
    //            for(int j=1; j < cursor.getColumnCount(); j++)
      //          Log.i("Column #",String.valueOf(cursor.getColumnName(j)));

                for(String name : cursor.getColumnNames()) {
                    Log.i("Column Names", name);
                }
                Log.i("Column for active",String.valueOf(cursor.getColumnIndex(ALARM_SNOOZE)));
                if(cursor.getInt(cursor.getColumnIndex(ALARM_ACTIVE))==1)
                    alarms[i].setAlarmOnOff(true);
                else
                    alarms[i].setAlarmOnOff(false);



                if( cursor.getInt(cursor.getColumnIndex(ALARM_SNOOZE))==1)
                {
                    alarms[i].setSetSnoozeOnorOff(true);
                }
                else
                {
                    alarms[i].setSetSnoozeOnorOff(false);
                }


                byte[] repeatDaysBytes = cursor.getBlob(5);
                if(repeatDaysBytes==null)
                {
                    cursor.moveToNext();
                    i++;
                    continue;
                }
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
                try {

                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    Alarm.Day[] repeatDays;
                    Object object = objectInputStream.readObject();
                    if(object instanceof Alarm.Day[]){
                        repeatDays = (Alarm.Day[]) object;

                        days.addAll(Arrays.asList(repeatDays));
                        alarms[i].setDays(days);
                    }
                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                i++;
                cursor.moveToNext();
            }
        }
        cursor.close();
        alarmList.addAll(Arrays.asList(alarms));
        return alarmList;
    }

    public Map<String,String> getRingtones()
    {
        return ringtones;
    }

    public Map<String,String> getInitRingTones()
    {

        Cursor cursor=myDatabase.rawQuery("SELECT "+RINGTONE_NAME+","+RINGTONE_PATH+" FROM "+RINGTONE_TABLE,null);

        if(cursor.moveToFirst())
        {
            while(!cursor.isAfterLast())
            {
                String name=cursor.getString(cursor.getColumnIndex(RINGTONE_NAME));
                String path=cursor.getString(cursor.getColumnIndex(RINGTONE_PATH));
                ringtones.put(name,path);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return ringtones;
    }

    private void saveRingTones()
    {
        RingtoneManager ToneManager=new RingtoneManager(context);
        ToneManager.setType(RingtoneManager.TYPE_ALARM);
        Cursor tonecursor=ToneManager.getCursor();
        ContentValues values=new ContentValues();
        if(tonecursor.moveToFirst()) {
            while (!tonecursor.isAfterLast()) {
                values.put(RINGTONE_NAME, ToneManager.getRingtone(tonecursor.getPosition()).getTitle(context));
                values.put(RINGTONE_PATH, ToneManager.getRingtoneUri(tonecursor.getPosition()).toString());
                myDatabase.insert(RINGTONE_TABLE,null,values);
                tonecursor.moveToNext();
            }
        }
        Log.i("CURSOR COUNT",String.valueOf(tonecursor.getCount()));
        tonecursor.close();
    }

    public void saveAlarm(Alarm alarm)
    {
        alarmList.add(alarm);
        saveAlarmtoDatabase(alarm);

    }

    public void updateAlarm(Alarm alarm)
    {
        ContentValues cv=new ContentValues();
        cv.put(ALARM_NAME,alarm.getLabel());
        cv.put(ALARM_TIME,alarm.getTime());
        cv.put(ALARM_TONE,alarm.getAlarmSoundTitle());
        cv.put(ALARM_ACTIVE,alarm.isAlarmOn() ? 1 : 0);
        cv.put(ALARM_SNOOZE,alarm.isSnoozeOn() ? 1 : 0);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            if(alarm.getDays().size() > 0) {
                oos.writeObject(alarm.getDays());
                byte[] buff = bos.toByteArray();

                cv.put(ALARM_DAYS, buff);
            }

        } catch (Exception e){
            Log.e("saveAlarmDB",e.getMessage());
        }

        myDatabase.update(ALARM_TABLE,cv,"alarm_id=?",new String[]{Integer.toString(alarm.getAlarmID())});

    }

    public void Close()
    {
        close();

    }

    private void saveAlarmtoDatabase(Alarm alarm)
    {
        ContentValues cv=new ContentValues();
        cv.put(ALARM_NAME,alarm.getLabel());
        cv.put(ALARM_TIME,alarm.getTime());
        cv.put(ALARM_TONE,alarm.getAlarmSoundTitle());
        cv.put(ALARM_ACTIVE,alarm.isAlarmOn() ? 1 : 0);
        cv.put(ALARM_SNOOZE,alarm.isSnoozeOn() ? 1 : 0);

        try {
            if(alarm.getDays().size() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = null;
                oos = new ObjectOutputStream(bos);
                oos.writeObject(alarm.getDays());
                byte[] buff = bos.toByteArray();

                cv.put(ALARM_DAYS, buff);
            }

        } catch (Exception e){
            Log.e("saveAlarmDB",e.getMessage());
        }

        myDatabase.insert(ALARM_TABLE,null,cv);
    }

    public void deleteAlarm(Alarm alarm)
    {
        myDatabase.delete(ALARM_TABLE,"alarm_id=?",new String[]{String.valueOf(alarm.getAlarmID())});
        int index=alarmList.indexOf(alarm);
        alarmList.remove(index);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion)
    {

    }
}


