
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import com.vice.R;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomDatePicker {
    private int date = 0;
    private int month = 0;
    private int year = 0;
    private Calendar mCalendar;
    private Context mContext;
    private Callback mCallback;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");

    private static List<String> WEEK_DAYS = Arrays.asList("S", "M", "T", "W", "T", "F", "S");

    public CustomDatePicker(Context context, Callback callback, String dateString) {
        mContext = context;
        mCalendar = Calendar.getInstance();
        mCallback = callback;
        DateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH);
        try {
            Date date = format.parse(dateString);
            mCalendar.setTime(date);
        } catch(Exception e) { }
    }

    private ArrayAdapter getAdapter() {
        ArrayList<DateView> list = new ArrayList<DateView>();
        int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = mCalendar.get(Calendar.DATE);
        int nDays = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int start = dayOfWeek - dayOfMonth % 7;

        if (start < 0) {
            start = 7 + start;
        }

        for (int i = 0; i < 7; i++) {
            list.add(new DateView(WEEK_DAYS.get(i)));
        }

        for (int i = 0; i < start; i++) {
            list.add(new DateView(""));
        }

        for (int i = 1; i <= nDays; i++) {
            list.add(new DateView("" + i));
        }

        return new DateAdapter(list);
    }

    public void getDatePicker(final LinearLayout container) {
        ViewFactory factory = new ViewFactory(mContext);
        LayoutParams containerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        containerParams.topMargin = 30;
        containerParams.gravity = Gravity.CENTER;
        container.setLayoutParams(containerParams);
        container.setPadding(50, 30, 50, 30);
        container.setBackgroundColor(ContextCompat.getColor(mContext, R.color.activity_detail));
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout header = new LinearLayout(mContext);
        container.addView(header);

            ImageView leftDouble = factory.createImageView(R.drawable.ic_double_left, 0, 0, R.color.card);
            leftDouble.setClickable(true);
            leftDouble.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = mCalendar.get(Calendar.YEAR);
                    mCalendar.set(Calendar.YEAR, year - 1);
                    container.removeAllViews();
                    getDatePicker(container);
                }
            });
            header.addView(leftDouble);

            ImageView leftSingle = factory.createImageView(R.drawable.ic_chevron_left, 0, 0, R.color.card);            header.addView(leftSingle);
            leftSingle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int month = mCalendar.get(Calendar.MONTH);
                    mCalendar.set(Calendar.MONTH, month - 1);
                    container.removeAllViews();
                    getDatePicker(container);
                }
            });

            int month = mCalendar.get(Calendar.MONTH);
            int year = mCalendar.get(Calendar.YEAR);
            mCallback.send("Date", mDateFormat.format(mCalendar.getTime()));
            String dateHeader = Constants.MONTHS.get(month) + " " + year;
            TextView textView = factory.createTextView(dateHeader, R.color.card);
            LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
            textParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            textView.requestLayout();
            textView.setTextSize(17);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.text));
            textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card));
            header.addView(textView);

            ImageView rightSingle = factory.createImageView(R.drawable.ic_chevron_right, 0, 0, R.color.card);
            rightSingle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int month = mCalendar.get(Calendar.MONTH);
                    mCalendar.set(Calendar.MONTH, month + 1);
                    container.removeAllViews();
                    getDatePicker(container);
                }
            });
            header.addView(rightSingle);

            ImageView rightDouble = factory.createImageView(R.drawable.ic_double_right, 0, 0, R.color.card);
            rightDouble.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = mCalendar.get(Calendar.YEAR);
                    mCalendar.set(Calendar.YEAR, year + 1);
                    container.removeAllViews();
                    getDatePicker(container);
                }
            });
            header.addView(rightDouble);

        LinearLayout grid = new LinearLayout(mContext);
        container.addView(grid);

            GridView gridView = new GridView(mContext);
            LayoutParams gridParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            gridView.setLayoutParams(gridParams);
            gridView.setNumColumns(7);
            gridView.setAdapter(getAdapter());
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String text = ((TextView) view).getText().toString();
                    try {
                        int date = Integer.parseInt(text);
                        mCalendar.set(Calendar.DATE, date);
                        container.removeAllViews();
                        getDatePicker(container);
                    } catch(Exception ex) {}
                }
            });
            grid.addView(gridView);
    }

    public class DateView {
        private String value;
        public DateView(String value) {
            this.value = value;
        }
        public String getValue() {
            return this.value;
        }
    }

    public class DateAdapter extends ArrayAdapter<DateView> {

        public DateAdapter(ArrayList<DateView> dateTexts) {
            super(mContext, android.R.layout.simple_list_item_1, dateTexts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = null;
            String text = null;

            if (convertView == null) {
                DateView dateText = getItem(position);
                textView = new TextView(mContext);
                text = dateText.getValue();
                textView.setText(text);
                textView.setPadding(0, 10, 0, 10);

                if (text != null && mCalendar != null &&
                        text.equalsIgnoreCase("" + mCalendar.get(Calendar.DATE))) {
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.OVAL);
                    drawable.setColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    textView.setTextColor(ContextCompat.getColor(mContext, R.color.text));
                    textView.setBackground(drawable);
                }

                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            } else {
                textView = (TextView) convertView;
            }

            return textView;
        }
    }
}
