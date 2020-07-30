
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.vice.util.ActivitiesView;
import com.vice.util.Callback;
import com.vice.util.Constants;
import com.vice.util.CustomDatePicker;
import com.vice.util.CustomTimePicker;
import com.vice.util.PermissionsActivity;
import com.vice.util.Util;
import com.vice.util.ViewFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity {

    private int MATCH = LayoutParams.MATCH_PARENT;
    private int WRAP = LayoutParams.WRAP_CONTENT;
    private int ADDED_LiST_VIEW = View.generateViewId();
    private int mActivitiesId = View.generateViewId();
    private boolean mIsNotificationPermitted = true;
    private boolean mIsPermissionGranted = true;

    private ViewFactory mViewFactory;
    private Context mContext;
    private JSONObject mTasks = new JSONObject();
    private JSONObject mActivities = new JSONObject();
    private HashMap<String, View.OnClickListener> LISTENERS = new HashMap<String, View.OnClickListener>();

    private Callback mCallback = new Callback() {
        @Override
        public void send(String key, Object data) {
            try {
                mTasks.put(key, (String) data);
            } catch (Exception ex) {}
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        mContext = this;
        mViewFactory = new ViewFactory(mContext);

        SwitchCompat switchHandle = findViewById(R.id.archive_switch);
        Button nameBtn = findViewById(R.id.name);
        Button dateBtn = findViewById(R.id.date);
        Button timeBtn = findViewById(R.id.time);
        Button placeBtn = findViewById(R.id.place);
        Button activityBtn = findViewById(R.id.activities_list);
        Button descriptionBtn = findViewById(R.id.description);
        ImageView saveBtn = findViewById(R.id.save_task);

        switchHandle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            LinearLayout mContainer = findViewById(R.id.activities_container);
            LinearLayout mDataContainer = findViewById(R.id.data_container);

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mContainer.setVisibility(View.GONE);
                    mDataContainer.setVisibility(View.VISIBLE);
                    if (mDataContainer.getChildCount() == 0) {
                        LinearLayout dataContainer = new LinearLayout(mContext);
                        LayoutParams containerParams = new LayoutParams(MATCH, WRAP);
                        dataContainer.setOrientation(LinearLayout.VERTICAL);
                        dataContainer.setLayoutParams(containerParams);

                        new StoredDataView(mContext, false).getStoredData(dataContainer);
                        if (dataContainer.getChildCount() > 1) {
                            ScrollView scroll = new ScrollView(mContext);
                            LayoutParams scrollParams = new LayoutParams(MATCH, WRAP);
                            scroll.setBackgroundColor(ContextCompat.getColor(mContext, R.color.activity_detail));
                            scroll.setLayoutParams(scrollParams);
                            scroll.addView(dataContainer);
                            mDataContainer.addView(scroll);
                        } else {
                            mDataContainer.addView(dataContainer);
                        }
                    }
                } else {
                    mContainer.setVisibility(View.VISIBLE);
                    mDataContainer.setVisibility(View.GONE);
                }
            }
        });

        nameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout nameLayout = findViewById(R.id.name_layout);
                updateLayout(nameLayout, "Name", 0, 0);
            }
        });

        dateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout dateLayout = findViewById(R.id.date_layout);
                LinearLayout container = findViewById(R.id.activities_container);
                int n_children = dateLayout.getChildCount();
                if (n_children == 1) {
                    container.getChildAt(0).setVisibility(View.GONE);
                    String date = mTasks.optString("Date");
                    CustomDatePicker datePicker = new CustomDatePicker(mContext, mCallback, date);
                    LinearLayout layout = new LinearLayout(mContext);
                    datePicker.getDatePicker(layout);
                    dateLayout.addView(layout);
                } else if (n_children == 2) {
                    container.getChildAt(0).setVisibility(View.VISIBLE);
                    dateLayout.removeViewAt(1);
                }
            }
        });

        timeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout timeLayout = findViewById(R.id.time_layout);
                LinearLayout mainContainer = findViewById(R.id.activities_container);

                LinearLayout container = new LinearLayout(mContext);
                LayoutParams params = new LayoutParams(MATCH, WRAP);
                params.leftMargin = -10;
                params.rightMargin = -10;
                container.setLayoutParams(params);
                container.setOrientation(LinearLayout.VERTICAL);
                int n_children = timeLayout.getChildCount();
                if (n_children == 1) {
                    for (int i = 0; i < 2; i++) {
                        mainContainer.getChildAt(i).setVisibility(View.GONE);
                    }
                    String time = mTasks.optString("Time");
                    CustomTimePicker timePicker = new CustomTimePicker(mContext, mCallback, time);
                    LinearLayout layout = timePicker.getTimePicker();


                    container.addView(layout);

                    LinearLayout durLayout = getTimeDropDown("Duration:", Constants.DURATION);
                    String duration = mTasks.optString("Duration");

                    if (duration != null) {
                        Spinner spinner = (Spinner) durLayout.getChildAt(1);
                        spinner.setSelection(Constants.DURATION_LIST.indexOf(duration));
                    }
                    container.addView(durLayout);

                    LinearLayout notifLayout = getTimeDropDown("Prenotification:", Constants.PRENOTIFICATION);
                    String prenotification = mTasks.optString("Prenotification");

                    if (prenotification != null) {
                        Spinner spinner = (Spinner) notifLayout.getChildAt(1);
                        spinner.setSelection(Constants.PRENOTIFICATION_LIST.indexOf(prenotification));
                    }
                    container.addView(notifLayout);

                    LinearLayout freqLayout = getTimeDropDown("Frequency:",Constants.FREQUENCY);
                    String frequency = mTasks.optString("Frequency");

                    if (frequency != null) {
                        Spinner spinner = (Spinner) freqLayout.getChildAt(1);
                        spinner.setSelection(Constants.FREQUENCY_LIST.indexOf(frequency));
                    }
                    freqLayout.setPadding(50, 10, 50, 40);
                    container.addView(freqLayout);

                    timeLayout.addView(container);
                } else if (n_children == 2) {
                    for (int i = 0; i < 2; i++) {
                        mainContainer.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                    timeLayout.removeViewAt(1);
                }
            }
        });

        placeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout placeLayout = findViewById(R.id.place_layout);
                updateLayout(placeLayout, "Place", R.drawable.ic_location, 3);
            }
        });

        activityBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout activityLayout = findViewById(R.id.activity_layout);
                if (LISTENERS.get("Activities") == null) {
                    addActivityListListener();
                }
                updateLayout(activityLayout, "Activities", R.drawable.ic_add, 4);
            }
        });

        descriptionBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout descriptionLayout = findViewById(R.id.description_layout);
                updateLayout(descriptionLayout, "Description", -1, 5);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = null;
                JSONArray array = new JSONArray();
                Iterator<String> iterator = mActivities.keys();

                finalizeTaskList();

                while(iterator.hasNext()) {
                    key = iterator.next();

                    if (key.equalsIgnoreCase("VOLUME_SILENT") ||
                            key.equalsIgnoreCase("VOLUME_NORMAL") ||
                            key.equalsIgnoreCase("VOLUME_VIBRATE")) {
                        mIsNotificationPermitted = false;
                        changeNotificationPolicy();
                    } else {
                        array.put(key);
                    }
                    if (array.length() > 0) {
                        mIsPermissionGranted = false;
                        Intent intent = new Intent(mContext, PermissionsActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putString("type", "activity");
                        bundle.putSerializable("activities", array.toString());

                        Callback callback = new Callback(){
                            @Override
                            public void send(String key, Object data) {
                                if ((boolean) data) {
                                    mIsPermissionGranted = true;
                                    if (mIsNotificationPermitted) {
                                        new WorksScheduler(mContext).schedule(mTasks.toString());
                                        finish();
                                    }
                                }
                            }
                        };
                        PermissionsActivity.mCallback = callback;
                        intent.putExtra("data", bundle);

                        try {
                            startActivity(intent);
                        } catch(Exception e) {
                        }

                    } else {
                        new WorksScheduler(mContext).schedule(mTasks.toString());
                        finish();
                    }
                }
            }
        });
    }

    private void finalizeTaskList() {
        try {

            Date now = new Date();
            String dateString = mTasks.optString("Date");
            String timeString = mTasks.optString("Time");

            if (dateString == null || dateString.length() == 0) {
                DateFormat dFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH);
                dateString = dFormat.format(now);
                mTasks.put("Date", dateString);
            }

            if (timeString == null || timeString.length() == 0) {
                Calendar calendar = Calendar.getInstance();
                timeString = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                mTasks.put("Time", timeString);
            }

            mTasks.put("_id", now.getTime());
            mTasks.put("_delay", Util.getDelay(dateString, timeString));

            if (mTasks.optString("Prenotification").length() > 0) {
                mTasks.put("_stage", "alert");
            } else {
                mTasks.put("_stage", "ontime");
            }
            mTasks.put("Tasks", mActivities);
        } catch(Exception e) {
        }
    }

    public void changeNotificationPolicy() {
        Intent intent = new Intent(mContext, PermissionsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", "notification");
        Callback callback = new Callback(){
            @Override
            public void send(String key, Object data) {
                if ((boolean) data) {
                    mIsNotificationPermitted = true;
                    if (mIsPermissionGranted) {
                        new WorksScheduler(mContext).schedule(mTasks.toString());
                        finish();
                    }
                }
            }
        };
        PermissionsActivity.mCallback = callback;
        intent.putExtra("data", bundle);
        startActivity(intent);
    }

    private void addActivityListListener() {
        final Activity activity = this;
        LISTENERS.put("Activities", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ActivitiesView(activity, mActivities).getView(new Callback() {
                    @Override
                    public void send(String key, Object data) {
                        LinearLayout addedActivityLayout = findViewById(ADDED_LiST_VIEW);
                        if (addedActivityLayout != null) {
                            final LinearLayout container = findViewById(mActivitiesId);
                            ((LinearLayout)container.getParent()).removeView(addedActivityLayout);
                        }
                        updateActivitiesMessage(null);
                    }
                });
            }
        });
    }

    private LinearLayout getTimeDropDown(String label, ArrayList<ViewFactory.SpinnerView> list) {
        int c1 = R.color.light_blue;
        int c2 = R.color.text;
        int c3 = R.color.input_text;

        return mViewFactory.getDropDown(label, list, mCallback, c1, c2, c3);
    }

    private void updateLayout(LinearLayout parent, String title, int resourceId, int index) {
        LinearLayout container = findViewById(R.id.activities_container);

        int n_children = parent.getChildCount();
        if (n_children == 1) {
            for (int i = 0; i < index; i++) {
                container.getChildAt(i).setVisibility(View.GONE);
            }
            View.OnClickListener listener = LISTENERS.get(title);
            TextWatcher watcher = new CustomTextWatcher(title);

            if (listener == null) {
                LinearLayout layout = mViewFactory.getEditText(watcher, title);
                EditText editText = (EditText)layout.getChildAt(0);
                String text = mTasks.optString(title);
                if (text != null) {
                    editText.setText(text);

                }
                if (title.equalsIgnoreCase("Description")){
                    editText.setLines(5);
                    editText.setGravity(Gravity.TOP);
                }
                layout.setPadding(85, 30, 85, 30);
                parent.addView(layout);
            } else {
                if (title.equalsIgnoreCase("Activities")) {
                    Iterator<String> iter = mActivities.keys();
                    LinearLayout addedActivityLayout = new LinearLayout(this);
                    addedActivityLayout.setBackground(mViewFactory.getBackground(10, 10, R.color.colorPrimary));
                    addedActivityLayout.setOrientation(LinearLayout.VERTICAL);

                    String activityKey = null;
                    while (iter.hasNext()) {
                        activityKey = iter.next();
                        addedActivityLayout.addView(mViewFactory.createTextView(activityKey, R.color.modal_text));
                    }

                    final LinearLayout layout = mViewFactory.getFixedTextWithButton(listener, resourceId, "", 70);
                    layout.setId(mActivitiesId);
                    updateActivitiesMessage(layout);
                    layout.setPadding(85, 30, 85, 30);
                    parent.addView(layout);
                } else {
                    LinearLayout layout = mViewFactory.getEditTextWithButton(watcher, listener, resourceId, title, 70);
                    layout.setPadding(85, 30, 85, 30);
                    parent.addView(layout);
                }
            }

        } else if (n_children == 2) {
            for (int i = 0; i < index; i++) {
                container.getChildAt(i).setVisibility(View.VISIBLE);
            }
            parent.removeViewAt(1);
        }
    }

    private void updateActivitiesMessage(LinearLayout layout) {
        Iterator<String> iter = mActivities.keys();
        int counter = 0;

        while (iter.hasNext()) {
            iter.next();
            counter++;
        }

        String msg = "There " + (counter == 1 ? "is " : "are ") +
                (counter > 0 ? counter : "no") + " added " +
                (counter == 1 ? "activity." : "activities.");
        if (layout != null) {
            TextView textView = (TextView) layout.getChildAt(0);
            textView.setText(msg);
        } else {
            final LinearLayout container = findViewById(mActivitiesId);
            TextView textView = (TextView) container.getChildAt(0);
            textView.setText(msg);
            textView.setOnClickListener(new View.OnClickListener() {



                @Override
                public void onClick(View v) {
                    LinearLayout addedActivityLayout = findViewById(ADDED_LiST_VIEW);
                    if (addedActivityLayout == null) {
                        Context context = getApplicationContext();
                        addedActivityLayout = new LinearLayout(context);
                        LayoutParams params = new LayoutParams(MATCH, WRAP);
                        params.leftMargin = 102;
                        params.rightMargin = 138;
                        params.topMargin = -28;
                        addedActivityLayout.setBackground(mViewFactory.getBackground(10, 10, R.color.colorPrimary));
                        addedActivityLayout.setOrientation(LinearLayout.VERTICAL);
                        addedActivityLayout.setPadding(30, 15, 30, 15);
                        addedActivityLayout.setLayoutParams(params);
                        addedActivityLayout.setId(ADDED_LiST_VIEW);

                        String activityKey = null;
                        Iterator<String> iter = mActivities.keys();
                        while (iter.hasNext()) {
                            activityKey = iter.next();
                            TextView tv = new TextView(context);
                            tv.setText(Constants.TASK_LIST.get(activityKey));
                            tv.setTextColor(ContextCompat.getColor(context, R.color.modal_text));
                            addedActivityLayout.addView(tv);
                        }
                        ((LinearLayout)container.getParent()).addView(addedActivityLayout);
                    } else {
                        ((LinearLayout)container.getParent()).removeView(addedActivityLayout);
                    }
                }
            });
        }
    }

    public class CustomTextWatcher implements TextWatcher {
        String title = "";
        public CustomTextWatcher(String title) {
            this.title = title;
        }
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            try {
                mTasks.put(this.title, c.toString());
            } catch (Exception e) { }
        }

        public void beforeTextChanged(CharSequence c, int start, int count, int after) { }
        public void afterTextChanged(Editable c) { }
    };
}
