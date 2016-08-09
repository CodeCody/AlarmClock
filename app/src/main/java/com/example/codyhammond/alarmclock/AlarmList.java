package com.example.codyhammond.alarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by codyhammond on 6/8/16.
 */

 interface DataSetChanged
{
    void notifyDataSetChanged();
    boolean getEditFlag();
}

public class AlarmList extends Fragment implements DataSetChanged {

    private Toolbar toolbar;
    private TextView Edit;
    private ImageView add_alarm;
    private MainActivity hostActivity;
    private ListView alarmList;
    private List<Alarm>alarmArray=new LinkedList<>();
    private Database database;
    private Integer color;
    private boolean isEditModeEnabled=false;
    private List<ViewGroup>viewGroupList=new LinkedList<>();
    private AlarmAdapter alarmAdapter;

    @Override
    public void notifyDataSetChanged()
    {
        if(alarmList==null)
            return;

        ((AlarmAdapter)alarmList.getAdapter()).notifyDataSetChanged();
        alarmAdapter.clearViewGroupList();
        alarmAdapter.animateViewGroupList();
        listVisibilityChange();
    }

    @Override
    public boolean getEditFlag()
    {
        return isEditModeEnabled;
    }

    public final static String sADD_DELETE="option";

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        hostActivity=(MainActivity)context;
        database=Database.getInstance(context);
        database.setListViewDataSetChangedListener(this);
        alarmArray=(LinkedList)database.getAlarms();
        color=new TextView(getContext()).getCurrentTextColor();
    }

    public static AlarmList newInstanceWithAlarm(Alarm alarm)
    {
        Bundle bundle=new Bundle();
        bundle.putParcelable(Alarm.ALARM_KEY,alarm);

        AlarmList alarmListFragment=new AlarmList();
        alarmListFragment.setArguments(bundle);

        return alarmListFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        alarmAdapter=new AlarmAdapter(getContext(),R.layout.alarm_list_items,alarmArray);
       // alarmAdapter.registerDataSetObserver(datasetObserver);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i("OnDestroy","OnDestroy Called");
     //   alarmAdapter.unregisterDataSetObserver(datasetObserver);
        database.close();
        database=null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.alarm_list_layout,parent,false);
        final View emptyView=inflater.inflate(R.layout.no_alarm_layout,parent,false);
        alarmList=(ListView)view.findViewById(R.id.alarm_list);
        toolbar=(Toolbar)view.findViewById(R.id.toolbar);
        Edit=(Button)view.findViewById(R.id.edit);
        add_alarm=(ImageView)view.findViewById(R.id.add_alarm);
        toolbar.setTitle("Alarm");
        toolbar.setNavigationContentDescription(R.string.edit);
        hostActivity.setSupportActionBar(toolbar);
        hostActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);


        alarmList.setAdapter(new AlarmAdapter(getContext(),R.layout.alarm_list_items,alarmArray));
        listVisibilityChange();

        alarmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout alarm_section=(LinearLayout)view.findViewById(R.id.alarm_section);
                ImageButton turnAlarmOnOff=(ImageButton)view.findViewById(R.id.alarm_toggle);
                TextView time=(TextView)view.findViewById(R.id.alarm_time);
                TextView alarmLabel=(TextView)view.findViewById(R.id.alarm_label);
                Alarm alarm=alarmArray.get(position);

                if(!isEditModeEnabled || id==MainActivity.DEACTIVATE_ALARM) {
                    if (alarm.isAlarmOn()) {
                        turnAlarmOnOff.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_off_black_24dp));
                        alarm.toggleAlarmOnOff(false);
                        alarm_section.setBackgroundColor(getResources().getColor(R.color.mid_gray));
                        time.setTextColor(color);
                        alarmLabel.setTextColor(color);
                        database.updateAlarm(alarm);

                        if(id==MainActivity.DEACTIVATE_ALARM)
                            alarmList.invalidateViews();

                    } else {
                        turnAlarmOnOff.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_on_black_24dp));
                        alarm.toggleAlarmOnOff(true);
                        alarm_section.setBackgroundColor(Color.WHITE);
                        time.setTextColor(Color.BLACK);
                        alarmLabel.setTextColor(Color.BLACK);
                        database.updateAlarm(alarm);
                    }

                    AlarmScheduleService.updateAlarmSchedule(getContext());

                }
                else
                {
                    Alarm_Picker alarm_picker=Alarm_Picker.newInstance(position);
                    alarm_picker.setTargetFragment(AlarmList.this,0);
                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.activity_main,alarm_picker).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                }

            }
        });

        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isEditModeEnabled) {
                    isEditModeEnabled = true;
                    Edit.setText(R.string.done);
                     alarmAdapter.animateViewGroupList();
                }
                else
                {
                    isEditModeEnabled=false;
                    Edit.setText(R.string.edit);
                    alarmAdapter.animateViewGroupList();
                }
            }
        });

        add_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alarm_Picker alarm_picker=new Alarm_Picker();
                alarm_picker.setTargetFragment(AlarmList.this,0);
                hostActivity.getSupportFragmentManager().beginTransaction().add(R.id.activity_main,alarm_picker).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();

            }
        });

        return view;
    }


    public void listVisibilityChange()
    {
        if(alarmAdapter.getCount()==0)
        {
            alarmList.setVisibility(View.GONE);
        }
        else
        {
            alarmList.setVisibility(View.VISIBLE);
        }
    }

    public void showDeactivatedAlarmView(int id)
    {
        int pos=0;
        for(Alarm alarm : alarmArray)
        {
            if(alarm.getAlarmID()==id)
            {
                break;
            }
            pos++;
        }
        alarmList.performItemClick(alarmList.getAdapter().getView(pos,null,null),pos,MainActivity.DEACTIVATE_ALARM);
    }

    class AlarmAdapter extends ArrayAdapter<Alarm>
    {
        private TextView time,alarmLabel;
        private ImageButton turnAlarmOnOff;
        private LinearLayout alarm_section;

        //private List<LinkedList>viewList=new LinkedList<>();


        public AlarmAdapter(Context context, int id,List<Alarm>arrayList)
        {
            super(context,id,arrayList);

        }

        @Override
        public View getView(int pos,View view,ViewGroup viewGroup)
        {
            view=getActivity().getLayoutInflater().inflate(R.layout.alarm_list_items,viewGroup,false);
            alarm_section=(LinearLayout)view.findViewById(R.id.alarm_section);
            ViewGroup viewGroup1=(ViewGroup)view.findViewById(R.id.transition_container);
            if(!viewGroupList.contains(viewGroup1)) {
                viewGroupList.add(viewGroup1);
            }

            final Alarm alarm=alarmArray.get(pos);
            time=(TextView)view.findViewById(R.id.alarm_time);

            ImageView arrow=(ImageView)view.findViewById(R.id.edit_hint);
            alarmLabel=(TextView)view.findViewById(R.id.alarm_label);
            turnAlarmOnOff=(ImageButton)view.findViewById(R.id.alarm_toggle);
            alarmLabel.setText(alarm.getLabel());
            String repeatDays=alarm.getDaysToString();
            if(!repeatDays.equals("Never") && !repeatDays.equals("Everyday"))
            {
                alarmLabel.append(",");
                alarmLabel.append(repeatDays);
            }
            else if(repeatDays.equals("Everyday"))
            {
                alarmLabel.append(",");
                alarmLabel.append(repeatDays);
            }
           // repeat_days.append(alarm.getDays().size() > 0 ? alarm.getDaysToString() : null);

            if(isEditModeEnabled)
            {
                arrow.setVisibility(View.VISIBLE);
            }
            else
            {
                arrow.setVisibility(View.GONE);
            }

            time.setText(alarm.getStandardTime());
            initializeAlarmStatuses(alarm);
            return view;
        }

        public void initializeAlarmStatuses(Alarm alarm)
        {
            if(!alarm.isAlarmOn())
            {
                turnAlarmOnOff.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_off_black_24dp));
                alarm_section.setBackgroundColor(getResources().getColor(R.color.mid_gray));
                time.setTextColor(color);
                //repeat_days.setTextColor(color);
                alarmLabel.setTextColor(color);
            }
            else
            {
                turnAlarmOnOff.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_on_black_24dp));
                alarm_section.setBackgroundColor(Color.WHITE);
                time.setTextColor(Color.BLACK);
                // repeat_days.setTextColor(Color.BLACK);
                alarmLabel.setTextColor(Color.BLACK);
            }
        }
        public void clearViewGroupList()
        {
            viewGroupList.clear();
        }

        @SuppressWarnings("NewApi")
        public void animateViewGroupList()
        {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                alarmList.invalidateViews();
                return;
            }

            for( ViewGroup viewGroup : viewGroupList)
            {
                TransitionManager.beginDelayedTransition(viewGroup);
                ((ImageView)viewGroup.findViewById(R.id.edit_hint)).setVisibility(isEditModeEnabled ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public int getCount()
        {
            return alarmArray.size();
        }

    }
}
