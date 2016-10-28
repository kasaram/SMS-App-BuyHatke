package com.buyhutkesmsapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.buyhutkesmsapp.models.SMS;
import com.smsapplication.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Vishwajeet
 * @since 27 October 2016
 */
public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.SMSViewHolder> {
    private ArrayList<SMS> smsList;

    public class SMSViewHolder extends RecyclerView.ViewHolder {
        public TextView textView1,textView2,textView3;


        public SMSViewHolder(RelativeLayout v) {
            super(v);
            textView1= (TextView) v.findViewById(R.id.address);
            textView2= (TextView) v.findViewById(R.id.date);
            textView3= (TextView) v.findViewById(R.id.body);
        }
    }
    public SMSAdapter(ArrayList<SMS> data) {
        smsList = data;
        notifyDataSetChanged();
    }

    @Override
    public SMSAdapter.SMSViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_list_item, parent, false);
        SMSViewHolder vh = new SMSViewHolder((RelativeLayout)view);
        return vh;
    }
    @Override
    public void onBindViewHolder(SMSViewHolder holder, int position) {
        SMS sms = smsList.get(position);
        holder.textView1.setText(sms.address);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy   hh:mm");

        long milliSeconds= Long.parseLong(sms.date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        holder.textView2.setText(formatter.format(calendar.getTime()));
        holder.textView3.setText(sms.body);

    }
    @Override
    public int getItemCount() {
        return smsList.size();
    }

}