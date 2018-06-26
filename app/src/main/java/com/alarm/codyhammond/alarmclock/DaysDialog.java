package com.alarm.codyhammond.alarmclock;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by codyhammond on 6/21/16.
 */
public class DaysDialog extends DialogFragment
{
    private ListView week_list;
    private Alarm.Day [] days= Alarm.Day.values();
    private DayAdapter adapter;
    private Set<Alarm.Day> selectedDays=new TreeSet<>(new Comparator<Alarm.Day>() {
        @Override
        public int compare(Alarm.Day lhs, Alarm.Day rhs) {
            return lhs.ordinal() - rhs.ordinal();
        }
    });

    private AlarmUpdate alarmUpdate;
    private Button confirmDays;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        adapter=new DayAdapter(getContext(),R.layout.days_dialog_items,days);
        alarmUpdate=(AlarmUpdate)getTargetFragment();
        selectedDays=alarmUpdate.getDays();
    }


    @Override
    public int getTheme()
    {
        return R.style.DialogSlideAnimation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.days_dialog_layout,viewGroup,false);
        week_list=(ListView)view.findViewById(R.id.weeklist);
        confirmDays=(Button)view.findViewById(R.id.confirm_days);
        confirmDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        week_list.setAdapter(adapter);
        getDialog().setTitle("Choose Days");
        getDialog().getWindow().setLayout(200,400);
     //   week_list.setItemsCanFocus(true);

        return view;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        StringBuilder builder=new StringBuilder();

        if(selectedDays.size()==7)
        {
            alarmUpdate.updateRepeatDays("Everyday",selectedDays);
            return;
        }
        else if(selectedDays.size()==0)
        {
            alarmUpdate.updateRepeatDays("Never",selectedDays);
            return;
        }



        Iterator<Alarm.Day>iterator=selectedDays.iterator();
        builder.append(iterator.next().toShortString());

        while(iterator.hasNext())
        {
            builder.append(" ").append(iterator.next().toShortString());
        }
        alarmUpdate.updateRepeatDays(builder.toString(),selectedDays);
    }

    class DayAdapter extends ArrayAdapter<Alarm.Day>
    {
        private CheckBox checkBox;
        private List<Alarm.Day>checkedDays=new LinkedList<>();

         DayAdapter(Context context, int id, Alarm.Day[] array)
        {
            super(context,id,array);
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent)
        {
            view=getActivity().getLayoutInflater().inflate(R.layout.days_dialog_items,parent,false);
            checkBox=(CheckBox)view.findViewById(R.id.day_box);
            checkBox.setText(days[position].toString());
            if(selectedDays.contains(days[position]))
            {
                checkBox.setChecked(true);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        selectedDays.add(days[position]);
                    }
                    else
                    {
                        selectedDays.remove((days[position]));
                    }
                }
            });

            return view;
        }

        public List<Alarm.Day> getCheckedValues()
        {
            return checkedDays;
        }

        @Override
        public int getCount()
        {
            return days.length;
        }
    }
}
