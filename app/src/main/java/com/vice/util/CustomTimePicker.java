
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.vice.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomTimePicker {

    private Context mContext;
    private Callback mCallback;
    private ViewFactory mViewFactory;
    private String mEditting;
    private Calendar mCalendar = Calendar.getInstance();
    private int WRAP = LayoutParams.WRAP_CONTENT;
    private int MATCH = LayoutParams.MATCH_PARENT;

    public CustomTimePicker(Context context, Callback callback, String timeString) {
        mContext = context;
        mCallback = callback;
        mViewFactory = new ViewFactory(mContext);
        DateFormat format = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
        try {
            Date date = format.parse(timeString);
            mCalendar.setTime(date);
        } catch(Exception e) { }
    }

    public LinearLayout getTimePicker() {

        LinearLayout container = new LinearLayout(mContext);
        LayoutParams containerParams = new LayoutParams(MATCH, WRAP);
        container.setLayoutParams(containerParams);
        containerParams.topMargin = 30;
        container.setPadding(50, 40, 50, 15);
        container.setBackgroundColor(ContextCompat.getColor(mContext, R.color.activity_detail));

        LinearLayout labelLayout = new LinearLayout(mContext);
        LayoutParams labelParams = new LayoutParams(0, WRAP);
        labelParams.weight = 0.4f;
        labelParams.gravity = Gravity.CENTER_VERTICAL;
        labelLayout.setLayoutParams(labelParams);
        labelLayout.setGravity(Gravity.RIGHT);
        labelLayout.setPadding(0, 0, 20, 0);
        container.addView(labelLayout);

        TextView label = new TextView(mContext);
        label.setText("Time:");
        label.setTextSize(16);
        labelLayout.addView(label);

        container.addView(getTimeLayout(false));

        return container;
    }
    
    public LinearLayout getTimeLayout(boolean isFullWidth) {
        LinearLayout timeContainer = new LinearLayout(mContext);
        timeContainer.setPadding(10, 5, 5, 5);

        if (!isFullWidth) {
            LayoutParams timeContainerParams;timeContainerParams = new LayoutParams(0, WRAP);
            timeContainerParams.weight = 0.6f;
            timeContainer.setLayoutParams(timeContainerParams);
            timeContainer.setBackground(mViewFactory.getBorder(10, 10, R.color.card));
        }

        LinearLayout timeLayout = new LinearLayout(mContext);
        LayoutParams timeLayoutParams = new LayoutParams(WRAP, WRAP);
        timeLayout.setLayoutParams(timeLayoutParams);
        timeLayout.setGravity(Gravity.CENTER_VERTICAL);
        timeContainer.addView(timeLayout);

        final TextView hoursText = new TextView(mContext);
        LayoutParams hoursParams = new LayoutParams(WRAP, 75);
        hoursParams.gravity = Gravity.CENTER;
        hoursText.setLayoutParams(hoursParams);
        int hours = mCalendar.get(Calendar.HOUR_OF_DAY);
        String prefix = hours < 10 ? "0" : "";
        hoursText.setText(prefix + hours);
        hoursText.setTextSize(26);
        hoursText.setPadding(10, 0, 10, 0);
        hoursText.setTextColor(ContextCompat.getColor(mContext, R.color.text));
        hoursText.setBackground(mViewFactory.getBackground(10, 10, R.color.background2));
        timeLayout.addView(hoursText);

        TextView colonText = new TextView(mContext);
        LayoutParams colonParams = new LayoutParams(25,100);
        colonParams.gravity = Gravity.CENTER;
        colonText.setText(":");
        colonText.setTextSize(26);
        colonText.setLayoutParams(colonParams);
        colonText.setGravity(Gravity.CENTER_VERTICAL);
        colonText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        colonText.setTextColor(ContextCompat.getColor(mContext, R.color.background2));
        timeLayout.addView(colonText);

        final TextView minutesText = new TextView(mContext);
        minutesText.setLayoutParams(hoursParams);
        int minutes = mCalendar.get(Calendar.MINUTE);
        prefix = minutes < 10 ? "0" : "";
        minutesText.setText(prefix + minutes);
        minutesText.setTextSize(26);
        minutesText.setPadding(10, 0, 10, 0);
        minutesText.setTextColor(ContextCompat.getColor(mContext, R.color.text));
        minutesText.setBackground(mViewFactory.getBackground(10, 10, R.color.background2));
        timeLayout.addView(minutesText);

        hoursText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditting == null || !mEditting.equalsIgnoreCase("hours")) {
                    mEditting = "hours";
                    hoursText.setBackground(mViewFactory.getBackground(10, 10, R.color.button));
                    minutesText.setBackground(mViewFactory.getBackground(10, 10, R.color.background2));
                } else {
                    mEditting = null;
                    hoursText.setBackground(mViewFactory.getBackground(10, 10, R.color.background2));
                }
            }
        });

        minutesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditting == null || !mEditting.equalsIgnoreCase("minutes")) {
                    mEditting = "minutes";
                    minutesText.setBackground(mViewFactory.getBackground(10, 10, R.color.button));
                    hoursText.setBackground(mViewFactory.getBackground(10, 10, R.color.background2));
                } else {
                    mEditting = null;
                    minutesText.setBackground(mViewFactory.getBackground(10, 10, R.color.background2));
                }
            }
        });

        TextView spacer = new TextView(mContext);
        LayoutParams spacerParams = new LayoutParams(0,75);
        spacer.setText("");
        spacer.setLayoutParams(spacerParams);
        spacerParams.weight = 1;
        timeContainer.addView(spacer);

        ImageView plusView = mViewFactory.createImageView(R.drawable.ic_add, 10, 10, R.color.button);
        LayoutParams imageParams = new LayoutParams(WRAP,75);
        imageParams.gravity = Gravity.CENTER;
        imageParams.rightMargin = 10;
        plusView.setLayoutParams(imageParams);
        plusView.setPadding(5, 0, 5, 0);
        plusView.setClickable(true);
        plusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean editted = false;
                if (mEditting != null && mEditting.equalsIgnoreCase("hours")) {
                    mCalendar.add(Calendar.HOUR_OF_DAY, 1);
                    editted = true;
                } else if (mEditting != null && mEditting.equalsIgnoreCase("minutes")) {
                    mCalendar.add(Calendar.MINUTE, 1);
                    editted = true;
                }
                if (editted) {
                    int hours = mCalendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = mCalendar.get(Calendar.MINUTE);
                    String prefix_h = hours < 10 ? "0" : "";
                    String prefix_m = minutes < 10 ? "0" : "";
                    hoursText.setText(prefix_h + hours);
                    minutesText.setText(prefix_m + minutes);
                    mCallback.send("Time", prefix_h + hours + ":" + prefix_m + minutes);
                }
            }
        });
        timeContainer.addView(plusView);

        ImageView minusView = mViewFactory.createImageView(R.drawable.ic_minus, 10, 10, R.color.button);
        minusView.setPadding(5, 0, 5, 0);
        minusView.setLayoutParams(imageParams);
        minusView.setClickable(true);
        minusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean editted = false;
                if (mEditting != null && mEditting.equalsIgnoreCase("hours")) {
                    mCalendar.add(Calendar.HOUR_OF_DAY, -1);
                    editted = true;
                } else if (mEditting != null && mEditting.equalsIgnoreCase("minutes")) {
                    mCalendar.add(Calendar.MINUTE, -1);
                    editted = true;
                }
                if (editted) {
                    int hours = mCalendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = mCalendar.get(Calendar.MINUTE);
                    String prefix_h = hours < 10 ? "0" : "";
                    String prefix_m = minutes < 10 ? "0" : "";
                    hoursText.setText(prefix_h + hours);
                    minutesText.setText(prefix_m + minutes);
                    mCallback.send("Time", prefix_h + hours + ":" + prefix_m + minutes);
                }
            }
        });
        timeContainer.addView(minusView);

        return timeContainer;
    }
}
