package com.alarm.codyhammond.alarmclock;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codyhammond on 6/16/16.
 */
public class RingToneDialog extends DialogFragment
{
    private Map<String,String> ringtones=new HashMap<>();
    private RecyclerView ringtone_list;
    private Database AlarmDatabase;
    private RingToneAdapter adapter=new RingToneAdapter();
    private AlarmUpdate alarmUpdate;
    private MediaPlayer mediaPlayer;
    private Button confirmButton;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AlarmDatabase=Database.getInstance(getContext().getApplicationContext());
        ringtones=AlarmDatabase.getRingtones();
        String [] keys=new String[ringtones.size()];
        alarmUpdate=(AlarmUpdate)getTargetFragment();
        adapter.setRingToneList(Arrays.asList(ringtones.keySet().toArray(keys)));
        mediaPlayer=new MediaPlayer();
        adapter.setCheckedTone(adapter.ringtone_names.indexOf(alarmUpdate.getAlarmRingtoneTitle()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,Bundle savedInstanceState)
    {
        getDialog().setTitle("Choose Ring Tone");
        View view=inflater.inflate(R.layout.ringtone_dialog_layout,viewGroup,false);
        confirmButton=(Button)view.findViewById(R.id.ringtone_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ringtone_list=(RecyclerView)view.findViewById(R.id.ringtone_list);
        ringtone_list.setAdapter(adapter);
        ringtone_list.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        return view;
    }

    @Override
    public int getTheme()
    {
        return R.style.DialogSlideAnimation;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        alarmUpdate.updateSoundLabel(adapter.getSelectedSound());
    }

    private class RingToneHolder extends RecyclerView.ViewHolder
    {
        public RadioButton radioButton;

        public RingToneHolder(View view)
        {
            super(view);
            radioButton=(RadioButton) view.findViewById(R.id.radio);
        }

        public void bindRingTone(String name)
        {
            radioButton.setText(name);
        }
    }

    private class RingToneAdapter extends RecyclerView.Adapter<RingToneHolder> implements View.OnClickListener,MediaPlayer.OnPreparedListener
    {
        private  RadioButton lastradioButton=null;
        private List<String>ringtone_names;
        private int checkedPosition=0;

        CountDownTimer countDownTimer=new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mediaPlayer.start();
            }

            @Override
            public void onFinish() {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
            }
        };


        public String getSelectedSound()
        {
            if(lastradioButton==null)
                return ringtone_names.get(0);
            else if(ringtones.size()==0)
                return " ";


            return lastradioButton.getText().toString();
        }

        public void setCheckedTone(int pos)
        {
            if(pos <= 0)
            {
            checkedPosition=0;
            }
            else {
                checkedPosition = pos;
            }
        }

        public void setRingToneList(List<String>names)
        {
            ringtone_names=names;
        }

        @Override
        public RingToneHolder onCreateViewHolder(ViewGroup parent,int position)
        {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.ringtone_items_layout,parent,false);

            return new RingToneHolder(view);
        }


        @Override
        public void onClick(View view)
        {
            Log.i("Click","Clicked");
        }

        @Override
        public void onBindViewHolder(final RingToneHolder holder, final int position)
        {
            if(holder.radioButton.isChecked())
            {
                holder.radioButton.setChecked(false);
            }
            holder.bindRingTone(ringtone_names.get(position));
            if(ringtone_names.get(checkedPosition).equals(ringtone_names.get(position)))
            {
                holder.radioButton.setChecked(true);
                lastradioButton=holder.radioButton;
            }

            holder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    lastradioButton.setChecked(false);
                    holder.radioButton.setChecked(true);
                    checkedPosition=position;
                    playTone(holder.radioButton.getText().toString());
                    lastradioButton=holder.radioButton;
                }
            });
        }

        public void onPrepared(MediaPlayer mediaPlayer)
        {
            countDownTimer.start();
        }

        public void playTone(String name)
        {

         //   String former=lastradioButton.getText().toString();

                if (mediaPlayer.isPlaying() && lastradioButton.getText().toString().equals(name))
                    return;

            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setOnPreparedListener(this);


            try
            {
                mediaPlayer.setDataSource(getContext(), Uri.parse(ringtones.get(name)));
                mediaPlayer.setLooping(true);
                mediaPlayer.prepareAsync();
            }
            catch (IOException io)
            {
                Log.e("player.setDataSource()",io.getMessage());
            }

        }
        @Override
        public int getItemCount()
        {
            return ringtone_names.size();
        }
    }


}
