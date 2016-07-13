package com.example.codyhammond.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by codyhammond on 6/8/16.
 */

interface AlarmUpdate
{
    void updateAlarmLabel(String label);
    String getAlarmLabel();
    String getAlarmRingtoneTitle();
    void updateSoundLabel(String label);
    String getTime();
    void updateRepeatDays(String label, Set<Alarm.Day>days);
    Set<Alarm.Day> getDays();
}

public class Alarm_Picker extends Fragment implements AlarmUpdate {

    private TimePicker timePicker;
    private Toolbar toolbar;
    private MainActivity hostActivity;
    private LinearLayout repeat_section,sound_section,label_section,delete_section,snooze_section;
    private SwitchCompat switchSnooze;
    private Button cancel,save;
    private TextView repeat_text,label_text,sound_text;
    private Database AlarmDatabase;
    private static DataSetChanged DataChangeCallBack;
    private Map<String,String> ringtones=new HashMap<>();
    private Alarm newAlarm;
    private StringBuilder timeBuilder=new StringBuilder();

    @Override
    public void updateAlarmLabel(String label)
    {
        label_text.setText(label);
        newAlarm.setLabel(label);
    }

    @Override
    public String getTime()
    {
        return newAlarm.getTime();
    }

    @Override
    public String getAlarmRingtoneTitle()
    {
        return newAlarm.getAlarmSoundTitle();
    }

    @Override
    public String getAlarmLabel()
    {
        return newAlarm.getLabel();
    }

    @Override
    public Set<Alarm.Day> getDays()
    {
        return newAlarm.getDays();
    }

    @Override
    public void updateSoundLabel(String label)
    {
        sound_text.setText(label);
        newAlarm.setAlarmSoundTitle(label);
        newAlarm.setAlarmSoundPath(ringtones.get(label));
    }


    @Override
    public void updateRepeatDays(String label, Set<Alarm.Day> days)
    {
        adjustRepeatSectionTextSize(label);
        newAlarm.setDays(days);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        hostActivity=(MainActivity)context;
        AlarmDatabase=Database.getInstance(getContext());
        ringtones=AlarmDatabase.getRingtones();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.alarm_picker_layout,parent,false);
        timePicker=(TimePicker)view.findViewById(R.id.timePicker);
        toolbar=(Toolbar)view.findViewById(R.id.toolbar_picker_fragment);
        cancel=(Button) view.findViewById(R.id.cancel);
        save=(Button)view.findViewById(R.id.save);
        repeat_text=(TextView)view.findViewById(R.id.repeat_text);
        label_text=(TextView)view.findViewById(R.id.label_setting);
        sound_text=(TextView)view.findViewById(R.id.sound_text);
        sound_text.setText(ringtones.keySet().toArray(new String[ringtones.size()])[0]);
        repeat_section=(LinearLayout)view.findViewById(R.id.repeat_section);
        sound_section=(LinearLayout)view.findViewById(R.id.sound_section);
        label_section=(LinearLayout)view.findViewById(R.id.label_section);
        delete_section=(LinearLayout)view.findViewById(R.id.delete_section);
        snooze_section=(LinearLayout)view.findViewById(R.id.snooze_section);
        switchSnooze=(SwitchCompat) view.findViewById(R.id.switchSnooze);
        DataChangeCallBack=(DataSetChanged)getTargetFragment();

        if(getArguments()!=null) {
            int pos=getArguments().getInt(AlarmList.sADD_DELETE);
            newAlarm = AlarmDatabase.getAlarm(pos);
            adjustRepeatSectionTextSize(newAlarm.getDaysToString());
            sound_text.setText(newAlarm.getAlarmSoundTitle());
            label_text.setText(newAlarm.getLabel());
        }
        else {
            newAlarm = new Alarm();
            delete_section.setVisibility(View.GONE);

        }


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                newAlarm.setAlarmTime(getpickerTime());
                if(getArguments()!=null)
                {
                    AlarmDatabase.updateAlarm(newAlarm);
                    newAlarm.scheduleAlarm(getContext());
                }
                else {
                    newAlarm.setAlarmSoundTitle(sound_text.getText().toString());
                    newAlarm.setAlarmSoundPath(ringtones.get(sound_text.getText().toString()));
                    newAlarm.setAlarmOnOff(true);
                    AlarmDatabase.saveAlarm(newAlarm);
                    newAlarm.scheduleAlarm(getContext());
                }

                DataChangeCallBack.notifyDataSetChanged();
                Toast.makeText(getContext(), newAlarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        repeat_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DaysDialog daysDialog=new DaysDialog();
                daysDialog.setTargetFragment(Alarm_Picker.this,0);
                daysDialog.show(getActivity().getSupportFragmentManager(),null);
            }
        });

        sound_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int style=R.style.DialogSlideAnimation;
              RingToneDialog ringToneDialog=new RingToneDialog();
                ringToneDialog.setTargetFragment(Alarm_Picker.this,0);
                ringToneDialog.show(getActivity().getSupportFragmentManager(),null);
               // getActivity().getSupportFragmentManager().beginTransaction().add(R.id.activity_main,ringToneDialog).commit();

            }
        });

        label_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LabelDialog labelDialog=new LabelDialog();
                labelDialog.setTargetFragment(Alarm_Picker.this,0);
                //getActivity().getSupportFragmentManager().beginTransaction().add(R.id.activity_main,labelDialog).addToBackStack(null).commit();
                FragmentManager manager=getActivity().getSupportFragmentManager();
                labelDialog.show(getActivity().getSupportFragmentManager(),null);
                //labelDialog.
            }
        });

        snooze_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSnooze.performClick();
            }
        });
        switchSnooze.setSwitchMinWidth(200);

        switchSnooze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    Log.i("Interpretation","ON");
                }
                else
                {
                    Log.i("Interpretation","OFF");
                }

                newAlarm.setSetSnoozeOnorOff(isChecked);
            }
        });

        delete_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteAlarm();
                DataChangeCallBack.notifyDataSetChanged();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        hostActivity.setSupportActionBar(toolbar);
        hostActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        return view;
    }

    public void adjustRepeatSectionTextSize(String label)
    {
        repeat_text.setText(label);
        if(newAlarm.getDays().size() == 6)
        {
            repeat_text.setTextSize(TypedValue.COMPLEX_UNIT_SP,20f);
        }
        else
        {
            repeat_text.setTextSize(TypedValue.COMPLEX_UNIT_SP,25f);
        }
    }
    public void deleteAlarm()
    {
        AlarmManager alarmManager=(AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(getContext(),AlertReceiver.class);
        intent.putExtra(Alarm.ALARM_KEY,newAlarm);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(getContext(),0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);

        AlarmDatabase.deleteAlarm(newAlarm);
    }
 /*   public void UpdateAlarm()
    {
        newAlarm.setAlarmTime(getTime());
        newAlarm.setAlarm_sound(sound_text.toString());
        newAlarm.setLabel(label_text.getText().toString());
        newAlarm.setDays();
    } */

    private String getpickerTime()
    {
        timeBuilder.append(String.valueOf(timePicker.getCurrentHour()));
        timeBuilder.append(":");
        String minute=timePicker.getCurrentMinute().toString();
        if(minute.length()==1)
        {
            timeBuilder.append("0").append(minute);
        }
        else
        {
            timeBuilder.append(minute);
        }

        return timeBuilder.toString();
    }


    public static Alarm_Picker newInstance(int pos)
    {
        Bundle bundle=new Bundle();
        bundle.putInt(AlarmList.sADD_DELETE,pos);
        Alarm_Picker fragment=new Alarm_Picker();
        fragment.setArguments(bundle);

        return fragment;
    }
}