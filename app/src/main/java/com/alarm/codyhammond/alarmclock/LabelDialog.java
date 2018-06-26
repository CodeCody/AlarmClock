package com.alarm.codyhammond.alarmclock;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * Created by codyhammond on 6/20/16.
 */
public class LabelDialog extends DialogFragment
{
    private EditText label;
    private Button confirm;
    private boolean keyboardShown=false;

    private AlarmUpdate alarmLabelUpdate;

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState)
    {
        alarmLabelUpdate=(AlarmUpdate)getTargetFragment();
        View view=inflater.inflate(R.layout.label_dialog_layout,viewGroup,false);
        label=(EditText)view.findViewById(R.id.edit_label);
        confirm=(Button)view.findViewById(R.id.confirm_label);
        getDialog().setTitle("Create Label");
        String alarm_name=alarmLabelUpdate.getAlarmLabel();


        final InputMethodManager imm=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm!=null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alarmLabelUpdate.updateAlarmLabel(label.getText().toString());
                if(!keyboardShown) {
                    if(imm!=null)
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                dismiss();
            }
        });

        label.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    if(label.isFocused())
                    {
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                        keyboardShown=true;
                    }
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(!Pattern.matches("[A-Za-z]",label.getText().toString())) {
            alarmLabelUpdate.updateAlarmLabel("Alarm");
        }
    }

    @Override
    public int getTheme()
    {
        return R.style.DialogSlideAnimation;
    }
}
