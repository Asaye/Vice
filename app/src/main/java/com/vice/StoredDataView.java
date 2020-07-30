
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vice.util.ActivitiesView;
import com.vice.util.Callback;
import com.vice.util.Constants;
import com.vice.util.CustomDatePicker;
import com.vice.util.CustomTimePicker;
import com.vice.util.Util;
import com.vice.util.ViewFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class StoredDataView {

    private Context mContext;
    private JSONArray mResponse;
    private JSONObject mTempData;
    private WorksScheduler mScheduler;
    private ViewFactory mViewFactory;
    private boolean mIsPending = false;
    private int ID = View.generateViewId();
    private int SCROLL_ID = View.generateViewId();
    private int WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    private int WRAP = LayoutParams.WRAP_CONTENT;
    private int MATCH = LayoutParams.MATCH_PARENT;
    private ArrayList<Integer> VIEW_IDS;
    private Map<Integer, Boolean> EDITTING_VIEWS;
    private LinearLayout mainContainer;

    public StoredDataView(Context context, boolean isPending) {
        mContext = context;
        mIsPending = isPending;
        mViewFactory = new ViewFactory(context);
    }

    public LinearLayout getStoredDataView() {
        HomeView.ID = ID;
        mainContainer = new LinearLayout(mContext);
        LayoutParams mainParams = new LayoutParams(MATCH, HEIGHT);
        mainParams.topMargin = -50;
        mainContainer.setLayoutParams(mainParams);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        mainContainer.setId(ID);

        LinearLayout handleContainer = new LinearLayout(mContext);
        LayoutParams handleContainerParams = new LayoutParams(100,100);
        handleContainer.setLayoutParams(handleContainerParams);
        handleContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.addView(handleContainer);

        ImageView handleView = new ImageView(mContext);
        handleView.setBackground(mViewFactory.getBackground(50, 50, R.color.background2));
        handleView.setImageResource(R.drawable.ic_drag_handle);
        handleContainer.addView(handleView);

        ScrollView scroll = new ScrollView(mContext);
        LayoutParams scrollParams = null;
        scroll.setVisibility(View.GONE);
        HomeView.SCROLL_ID = SCROLL_ID;
        scroll.setId(SCROLL_ID);
        mainContainer.addView(scroll);

        LinearLayout dataContainer = new LinearLayout(mContext);
        LayoutParams containerParams = new LayoutParams(MATCH, WRAP);
        dataContainer.setOrientation(LinearLayout.VERTICAL);
        dataContainer.setLayoutParams(containerParams);
        dataContainer.setBackgroundColor(ContextCompat.getColor(mContext, R.color.background_data));
        getStoredData(dataContainer);
        updateLayoutSize(dataContainer);
        scrollParams = new LayoutParams(MATCH, WRAP);
        scroll.setLayoutParams(scrollParams);
        scroll.setPadding(0, 0, 0, 100);
        scroll.addView(dataContainer);

        return mainContainer;
    }

    private void updateLayoutSize(LinearLayout dataContainer) {
        dataContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int height = Math.min(dataContainer.getMeasuredHeight(), HEIGHT);
        HomeView.LIMIT = height;
    }

    private void addListSummary(final LinearLayout layout, int num) {
        LinearLayout container = new LinearLayout(mContext);
        LayoutParams containerParams = new LayoutParams(MATCH, 70);
        containerParams.rightMargin = 15;
        containerParams.topMargin = 15;
        containerParams.bottomMargin = 25;
        containerParams.leftMargin = 15;
        container.setGravity(Gravity.CENTER);
        container.setLayoutParams(containerParams);
        container.setPadding(20, 10, 20, 10);

            TextView textView = new TextView(mContext);
            textView.setText("There " + (num > 1? "are " : "is ") + num +
                             (mIsPending ? " pending": " completed") + " activit" + (num > 1?"ies.": "y."));
            textView.setTextSize(16);
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.background2));

            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            container.addView(textView);

            ImageView imageView = mViewFactory.createImageView(R.drawable.ic_delete, 25, 25, R.color.colorAccent);
            imageView.setPadding(5, 5, 5, 5);
            LayoutParams imageParams = new LayoutParams(50, 50);
            imageParams.gravity = Gravity.CENTER;
            imageParams.leftMargin = 25;
            imageView.setLayoutParams(imageParams);
            imageView.setClickable(true);
            container.addView(imageView);

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mResponse == null || mResponse.length() == 0) {
                        return true;
                    }

                    int len = mResponse.length();
                    WorksScheduler scheduler = new WorksScheduler(mContext);
                    Callback callback = new Callback() {
                        @Override
                        public void send(String key, Object data) {
                            if (!(data instanceof String)) {
                                layout.removeAllViews();
                                ((ScrollView) layout.getParent()).setVisibility(View.GONE);
                                getStoredData(layout);
                                updateLayoutSize(layout);
                            }
                        }
                    };

                    long[] ids = new long[len];
                    for (int i = 0; i < len; i++) {
                        ids[i] = mResponse.optJSONObject(i).optLong("_id");
                    }
                    scheduler.cancel(ids, callback);
                    return true;
                }
            });

            layout.addView(container);
    }

    public void getStoredData(LinearLayout layout) {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        JSONArray temp = dbHelper.getData("data", "", null);
        mResponse = new JSONArray();

        if (temp != null) {
            Date now = new Date();
            DateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy kk:mm", Locale.ENGLISH);

            for (int i = 0; i < temp.length(); i++) {
                try {
                    JSONObject temp2 = new JSONObject(temp.optString(i));
                    String dateString = temp2.optString("Date");
                    String timeString = temp2.optString("Time");
                    Date date = format.parse( dateString + " " + timeString);

                    if (mIsPending) {
                        if (date.getTime() >= now.getTime()) {
                            mResponse.put(temp2);
                        }
                    } else {
                        if (date.getTime() < now.getTime()) {
                            mResponse.put(temp2);
                        }
                    }
                } catch (Exception e) {
                   continue;
                }
            }
        }

        if (mResponse == null || mResponse.length() == 0) {
            TextView textView = new TextView(mContext);

            textView.setTextSize(18);
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.text_blur));
            if (mIsPending) {
                textView.setText(R.string.no_data);
                textView.setPadding(0, 150, 0, 150);
            } else{
                textView.setText(R.string.no_data_archive);
                LayoutParams tParams = new LayoutParams(MATCH, MATCH);
                layout.setLayoutParams(tParams);
                layout.setGravity(Gravity.CENTER);
            }
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(textView);
        } else {
            try {
                JSONObject json;
                VIEW_IDS = new ArrayList<>();
                layout.setPadding(20, 20, 20, 20);
                int len = mResponse.length();
                addListSummary(layout, len);

                for (int i = 0; i < len; i++) {
                    json = new JSONObject(mResponse.optJSONObject(i).toString());

                    final int index = i, viewId = View.generateViewId();
                    VIEW_IDS.add(viewId);

                    final LinearLayout row = new LinearLayout(mContext);
                    LayoutParams rowParams = new LayoutParams(WIDTH + 115, WRAP);
                    row.setLayoutParams(rowParams);
                    layout.addView(row);

                    LinearLayout data = new LinearLayout(mContext);
                    LayoutParams dataParams = new LayoutParams(WIDTH - 60, WRAP);
                    dataParams.leftMargin = 10;
                    dataParams.topMargin = 10;
                    dataParams.rightMargin = 10;
                    dataParams.bottomMargin = 10;
                    data.setOrientation(LinearLayout.VERTICAL);
                    data.setLayoutParams(dataParams);
                    data.setPadding(30, 20, 20, 20);
                    data.setBackgroundResource(R.drawable.card_background);
                    data.setId(viewId);
                    row.addView(data);

                    addDataRow(data, json, index,false);
                    ImageView arrow = new ImageView(mContext);
                    LayoutParams iconsParams = new LayoutParams(WRAP, WRAP);
                    iconsParams.gravity = Gravity.CENTER_VERTICAL;
                    iconsParams.leftMargin = -50;
                    arrow.setBackgroundResource(R.drawable.ic_chevron_left);
                    arrow.setLayoutParams(iconsParams);
                    arrow.setClickable(true);
                    row.addView(arrow);

                    arrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            float translationX = row.getTranslationX();
                            if (translationX == -75) {
                                removeClickListener(row);
                                if (mTempData != null) {
                                    mTempData.remove("" + index);
                                }
                                row.animate().translationX(0).setDuration(0).start();
                                v.setBackgroundResource(R.drawable.ic_chevron_left);
                            } else {
                                if (mTempData == null) {
                                    mTempData = new JSONObject();
                                }
                                if (mScheduler == null) {
                                    mScheduler = new WorksScheduler(mContext);
                                }
                                addClickListeners(row, index);
                                row.animate().translationX(-75).setDuration(0).start();
                                v.setBackgroundResource(R.drawable.ic_chevron_right);
                            }
                        }
                    });

                    addSideButtons(row);
                }
            } catch (Exception e) {

            }
        }
    }

    private void addName(LinearLayout dataView, String value) {
        LinearLayout nameRow = new LinearLayout(mContext);
        nameRow.setPadding(10, 10, 10, 10);
        dataView.addView(nameRow);

        LinearLayout nameLayout = new LinearLayout(mContext);
        LayoutParams nameParams = new LayoutParams(WRAP, 60);
        nameLayout.setLayoutParams(nameParams);
        nameRow.addView(nameLayout);
        TextView nameView = mViewFactory.createTextView(value == null ? "" : value, R.color.modal_text);
        nameView.setBackground(mViewFactory.getBackground(30, 30, R.color.button));
        nameView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        nameView.setTextSize(18);
        nameView.setPadding(30, 0, 30, 10);
        nameLayout.addView(nameView);
    }

    private void addDataKey(LinearLayout dataRow, String key) {
        TextView keyText = new TextView(mContext);
        LayoutParams keyParams = new LayoutParams(0, WRAP);
        keyParams.weight = 0.4f;
        keyText.setLayoutParams(keyParams);
        keyText.setText(key + ":");
        keyText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        keyText.setTextColor(ContextCompat.getColor(mContext, R.color.modal_text));
        keyText.setTextSize(16);
        keyText.setTypeface(Typeface.DEFAULT_BOLD);
        dataRow.addView(keyText);
    }

    private void addDataValue(LinearLayout dataRow, String value, boolean isEditting) {
        TextView valueText = getDataValue(value, isEditting);
        dataRow.addView(valueText);
    }

    private TextView getDataValue(String value, boolean isEditting) {
        TextView valueText;
        if (isEditting) {
            valueText = new EditText(mContext);
            valueText.setBackground(null);
            mViewFactory.styleCursor((EditText) valueText, false);
            mViewFactory.addOnFocusChangeListener((EditText) valueText);
        } else {
            valueText = new TextView(mContext);
        }
        LayoutParams valueParams = new LayoutParams(0, WRAP);
        valueParams.weight = 0.6f;
        valueText.setLayoutParams(valueParams);
        valueText.setTextSize(16);
        valueText.setTextColor(ContextCompat.getColor(mContext, R.color.text));
        valueText.setPadding(10, 0, 30, 0);
        valueText.setText(value);

        return valueText;
    }

    private void addEdittingDataValue(LinearLayout dataRow, String key, String value, final int index) {
        switch (key) {
            case "Date": {
                final TextView valueText = getDataValue(value, true);
                dataRow.addView(valueText);

                final String val = value;

                valueText.setOnTouchListener(new View.OnTouchListener() {
                    private Dialog dialog;
                    private Callback callback = new Callback() {
                        @Override
                        public void send(String key, Object data) {
                            try {
                                mTempData.optJSONObject("" + index).put("Date", data);
                                valueText.setText((String) data);
                            } catch(Exception e) {
                            }
                        }
                    };
                    @Override
                    public boolean onTouch(View v, MotionEvent e) {
                        if (dialog == null) {
                            dialog = new Dialog(mContext);
                            LinearLayout dateLayout = new LinearLayout(mContext);
                            new CustomDatePicker(mContext, callback, val).getDatePicker(dateLayout);
                            LinearLayout dLayout = new LinearLayout(mContext);
                            dLayout.setPadding(5, 0, 5, 0);
                            dLayout.addView(dateLayout);
                            Window window = dialog.getWindow();
                            WindowManager.LayoutParams wlp = window.getAttributes();
                            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            wlp.gravity = Gravity.FILL_HORIZONTAL;
                            window.setAttributes(wlp);
                            dialog.setContentView(dLayout);
                            dialog.setCancelable(true);
                        }
                        dialog.show();
                        return true;
                    }
                });

                break;
            }
            case "Time": {
                Callback callback = new Callback() {
                    @Override
                    public void send(String key, Object data) {
                        try {
                            mTempData.optJSONObject("" + index).put("Time", (String) data);
                        } catch(Exception e) {}
                    }
                };

                LinearLayout tLayout = new CustomTimePicker(mContext, callback, value).getTimeLayout(true);
                LayoutParams tParams = new LayoutParams(0, WRAP);
                tLayout.setBackground(mViewFactory.getBorder(10, 10, R.color.activity_detail));
                tParams.leftMargin = 10;
                tParams.rightMargin = 10;
                tParams.weight = 0.65f;
                tLayout.setLayoutParams(tParams);
                dataRow.addView(tLayout);
                break;
            }
            case "Tasks": {
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JSONObject tasks = mTempData.optJSONObject("" + index);
                        JSONObject activities = null;
                        if (tasks != null) {
                            activities = tasks.optJSONObject("Tasks");
                        }
                        ActivitiesView aView =
                                new ActivitiesView((Activity) mContext, activities);
                        aView.getView(null);
                    }
                };
                addEdittingBtn(dataRow, listener, R.drawable.ic_add);
                break;
            }
            case "Duration": {
                LinearLayout durLayout = mViewFactory.getDropDown(Constants.DURATION, new Callback() {
                    @Override
                    public void send(String key, Object data) {
                        try {
                            mTempData.optJSONObject("" + index).put(key, data);
                        } catch (Exception e) {
                        }
                    }
                });
                ((Spinner) durLayout.getChildAt(0)).setSelection(Constants.DURATION_LIST.indexOf(value));
                LayoutParams durParams = new LayoutParams(0, WRAP);
                durParams.weight = 0.65f;
                durLayout.setLayoutParams(durParams);
                dataRow.addView(durLayout);
                break;
            }
            case "Prenotification": {
                LinearLayout notifLayout = mViewFactory.getDropDown(Constants.PRENOTIFICATION, new Callback() {
                    @Override
                    public void send(String key, Object data) {
                        try {
                            mTempData.optJSONObject("" + index).put(key, data);
                        } catch (Exception e) {
                        }
                    }
                });
                ((Spinner) notifLayout.getChildAt(0)).setSelection(Constants.PRENOTIFICATION_LIST.indexOf(value));
                LayoutParams notifParams = new LayoutParams(0, WRAP);
                notifParams.weight = 0.65f;
                notifLayout.setLayoutParams(notifParams);
                dataRow.addView(notifLayout);
                break;
            }
            case "Frequency": {
                LinearLayout freqLayout = mViewFactory.getDropDown(Constants.FREQUENCY, new Callback() {
                    @Override
                    public void send(String key, Object data) {
                        try {
                            mTempData.optJSONObject("" + index).put(key, data);
                        } catch (Exception e) {
                        }
                    }
                });
                ((Spinner) freqLayout.getChildAt(0)).setSelection(Constants.FREQUENCY_LIST.indexOf(value));
                LayoutParams freqParams = new LayoutParams(0, WRAP);
                freqParams.weight = 0.65f;
                freqLayout.setLayoutParams(freqParams);
                dataRow.addView(freqLayout);
                break;
            }
            case "Place": {
                addDataValue(dataRow, value, true);
                break;
            }
            case "Description": {
                addDataValue(dataRow, value, true);
                break;
            }
        }
    }

    private void updateEdittingDataValue(LinearLayout dataRow, String key, String value) {
        if (key.equalsIgnoreCase("Date") || key.equalsIgnoreCase("Description")) {
            LinearLayout container = (LinearLayout) dataRow.getChildAt(1);
            TextView valueText = (TextView) container.getChildAt(1);
            valueText.setText(value);
        } else if (key.equalsIgnoreCase("Duration") ||
                key.equalsIgnoreCase("Prenotification") ||
                key.equalsIgnoreCase("Frequency")) {
            LinearLayout container = (LinearLayout) dataRow.getChildAt(1);
            LinearLayout durLayout = (LinearLayout) container.getChildAt(1);
            ((Spinner) durLayout.getChildAt(0)).setSelection(Constants.DURATION_LIST.indexOf(value));
        }
    }

    private void addEdittingBtn(LinearLayout dataRow, View.OnClickListener listener, int resourceId) {
        ImageView imageView = mViewFactory.createImageView(resourceId, 5, 5, R.color.colorPrimaryDark);
        imageView.setClickable(true);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setOnClickListener(listener);
        LinearLayout valueLayout = new LinearLayout(mContext);
        LayoutParams valueParams = new LayoutParams(0, WRAP);
        valueParams.weight = 0.65f;
        valueParams.leftMargin = 10;
        valueLayout.setLayoutParams(valueParams);
        valueLayout.addView(imageView);
        dataRow.addView(valueLayout);
    }

//    private void updateDataRow(LinearLayout dataView, JSONObject json) {
//        String value;
//        for (String key : Constants.ACTIVITY_KEYS) {
//            value = json.optString(key);
//            updateEdittingDataValue(dataView, key, value);
//        }
//    }

    private void addDataRow(LinearLayout dataView, JSONObject json, final int index, boolean isEditting) {
        try {
            if (json == null) {
                json = mResponse.optJSONObject(index);
            }
            addName(dataView, json.optString("Name"));
            String value;

            for (String key : Constants.ACTIVITY_KEYS) {

                value = json.optString(key);
                if (isEditting) {
                    LinearLayout dataRow = new LinearLayout(mContext);
                    dataRow.setOrientation(LinearLayout.HORIZONTAL);
                    LayoutParams dParams = new LayoutParams(MATCH, WRAP);
                    dataRow.setLayoutParams(dParams);
                    dataRow.setPadding(10, 10, 10, 10);
                    dataView.addView(dataRow);
                    addDataKey(dataRow, key);
                    addEdittingDataValue(dataRow, key, value, index);
                } else {
                    if (value == null || value.length() == 0) {
                        continue;
                    }

                    LinearLayout dataRow = new LinearLayout(mContext);
                    dataRow.setPadding(10, 10, 10, 10);
                    dataView.addView(dataRow);
                    addDataKey(dataRow, key);

                    if (key.equalsIgnoreCase("Tasks")) {
                        JSONObject tasksJson = new JSONObject(value);
                        Iterator<String> iterator = tasksJson.keys();
                        LinearLayout gridLayout = new LinearLayout(mContext);
                        LayoutParams valueParams = new LayoutParams(0, WRAP);
                        valueParams.weight = 0.65f;
                        gridLayout.setOrientation(LinearLayout.VERTICAL);
                        gridLayout.setLayoutParams(valueParams);
                        gridLayout.setPadding(10, 0, 0, 0);

                        while (iterator.hasNext()) {
                            String taskText = Constants.TASK_LIST.get(iterator.next());
                            TextView taskTextView = new TextView(mContext);
                            taskTextView.setText(taskText);
                            taskTextView.setTextSize(16);
                            taskTextView.setTextColor(ContextCompat.getColor(mContext, R.color.text));
                            gridLayout.addView(taskTextView);
                        }
                        dataRow.addView(gridLayout);
                    } else {
                        addDataValue(dataRow, value, isEditting);
                    }
                }
            }
        } catch(Exception e) {
        }
    }

    private void addClickListeners(final LinearLayout layout, final int index) {
        int n = layout.getChildCount();
        LinearLayout container = (LinearLayout) layout.getChildAt(n - 1);
        final boolean isEditting = false;

        ImageView editView = (ImageView) ((LinearLayout) container.getChildAt(0)).getChildAt(0);
        editView.setClickable(true);
        editView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (EDITTING_VIEWS == null) {
                    EDITTING_VIEWS = new HashMap<>();
                    EDITTING_VIEWS.put(index, false);
                }
                boolean isEditting = EDITTING_VIEWS.get(index);
                try {
                    final LinearLayout parent = ((Activity) mContext).findViewById(VIEW_IDS.get(index));
                    parent.removeAllViews();
                    JSONObject json = new JSONObject(mResponse.getJSONObject(index).toString());
                    if (!isEditting) {
                        mTempData.put("" + index, json);
                    } else {
                        mTempData.remove("" + index);
                    }
                    addDataRow(parent, json, index, !isEditting);
                    EDITTING_VIEWS.put(index, !isEditting);
                    updateLayoutSize((LinearLayout) parent.getParent().getParent());
                    if (mainContainer != null) {
                        LayoutParams layout = (LayoutParams) mainContainer.getLayoutParams();
                        layout.topMargin = -Math.min(HEIGHT - 100, HomeView.LIMIT + 100);
                    }
                } catch (Exception ex) {
                }
            }
        });

        ImageView undoView = (ImageView) ((LinearLayout) container.getChildAt(1)).getChildAt(0);
        undoView.setClickable(true);
        undoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EDITTING_VIEWS == null || !EDITTING_VIEWS.get(index)) {
                   return;
                }
                try {
                    LinearLayout parent = ((Activity) mContext).findViewById(VIEW_IDS.get(index));
                    parent.removeAllViews();
                    JSONObject json = new JSONObject(mResponse.getJSONObject(index).toString());
                    mTempData.put("" + index, json);
                    addDataRow(parent, json, index, true);
                    LinearLayout gParent = (LinearLayout) parent.getParent().getParent();
                    updateLayoutSize(gParent);
                    if (mainContainer != null) {
                        LayoutParams layout = (LayoutParams) mainContainer.getLayoutParams();
                        layout.topMargin = -Math.min(HEIGHT - 100, HomeView.LIMIT + 100);
                    }
                } catch(Exception e) {
                }
            }
        });

        ImageView saveView = (ImageView) ((LinearLayout) container.getChildAt(2)).getChildAt(0);
        saveView.setClickable(true);
        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EDITTING_VIEWS == null || !EDITTING_VIEWS.get(index)) {
                    return;
                }
                try {
                    final LinearLayout parent = ((Activity) mContext).findViewById(VIEW_IDS.get(index));
                    final JSONObject json = mTempData.optJSONObject("" + index);
                    String dateString = json.optString("Date");
                    String timeString = json.optString("Time");
                    json.put("_delay", Util.getDelay(dateString, timeString));
                    mScheduler.update(json.toString(), new Callback() {
                        @Override
                        public void send(String key, Object data) {
                            if (key.equalsIgnoreCase("Error")) {
                                Toast.makeText(mContext, "Error occured.", Toast.LENGTH_SHORT);
                            } else {
                                parent.removeAllViews();
                                try {
                                    mResponse.put(index, json);
                                } catch (Exception e) {
                                }
                                addDataRow(parent, json, index, false);
                                EDITTING_VIEWS.put(index, false);
                                updateLayoutSize((LinearLayout) parent.getParent().getParent());
                                if (mainContainer != null) {
                                    LayoutParams layout = (LayoutParams) mainContainer.getLayoutParams();
                                    layout.topMargin = -Math.min(HEIGHT - 100, HomeView.LIMIT + 100);
                                }
                            }
                        }
                    });
                } catch(Exception e) {
                }
            }
        });

        ImageView deleteView = (ImageView) ((LinearLayout) container.getChildAt(3)).getChildAt(0);
        deleteView.setClickable(true);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject json = new JSONObject(mResponse.getJSONObject(index).toString());
                    mScheduler.cancel("id=" + json.optString("_id"), new Callback() {
                        @Override
                        public void send(String key, Object data) {
                            if (key.equalsIgnoreCase("Error")) {
                                Toast.makeText(mContext, "Error occured.", Toast.LENGTH_SHORT);
                            } else {
                                LinearLayout dataContainer = ((Activity) mContext).findViewById(VIEW_IDS.get(index));
                                LinearLayout parent = (LinearLayout) dataContainer.getParent();
                                LinearLayout gParent = (LinearLayout) parent.getParent();
                                gParent.removeAllViews();
                                getStoredData(gParent);
                                updateLayoutSize(gParent);
                                if (mainContainer != null) {
                                    LayoutParams layout = (LayoutParams) mainContainer.getLayoutParams();
                                    layout.topMargin = -Math.min(HEIGHT - 100, HomeView.LIMIT + 100);
                                }
                            }
                        }
                    });
                } catch(Exception e) {
                }
            }
        });
    }

    private void removeClickListener(final LinearLayout layout) {
        int n = layout.getChildCount();
        LinearLayout container = (LinearLayout) layout.getChildAt(n - 1);

        for (int i = 0; i < 4; i++) {
            ImageView imageView = (ImageView) ((LinearLayout) container.getChildAt(i)).getChildAt(0);
            imageView.setOnClickListener(null);
        }
    }

    private void addSideButtons(LinearLayout layout) {
        layout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout container = new LinearLayout(mContext);
        container.setLayoutParams(new LayoutParams(80, MATCH));
        container.setPadding(10, 10, 0, 10);
        container.setOrientation(LinearLayout.VERTICAL);
        layout.addView(container);

        LayoutParams params = new LayoutParams(MATCH,0);
        params.weight = 1;
        Drawable background1 = mViewFactory.getBackground(null, R.color.card, R.color.modal_text);
        Drawable background2 = mViewFactory.getBackground(null, R.color.danger, R.color.modal_text);

        LinearLayout editContainer = new LinearLayout(mContext);
        editContainer.setLayoutParams(params);
        editContainer.setGravity(Gravity.CENTER);
        editContainer.setBackground(background1);
        container.addView(editContainer);

        ImageView edit = new ImageView(mContext);
        edit.setPadding(5, 5, 5, 5);
        edit.setImageResource(R.drawable.ic_edit);
        editContainer.addView(edit);

        LinearLayout undoContainer = new LinearLayout(mContext);
        undoContainer.setLayoutParams(params);
        undoContainer.setGravity(Gravity.CENTER);
        undoContainer.setBackground(background1);
        container.addView(undoContainer);

        ImageView undo = new ImageView(mContext);
        undo.setPadding(5, 5, 5, 5);
        undo.setImageResource(R.drawable.ic_undo);
        undoContainer.addView(undo);

        LinearLayout saveContainer = new LinearLayout(mContext);
        saveContainer.setLayoutParams(params);
        saveContainer.setGravity(Gravity.CENTER);
        saveContainer.setBackground(background1);
        container.addView(saveContainer);

        ImageView save = new ImageView(mContext);
        save.setPadding(5, 5, 5, 5);
        save.setImageResource(R.drawable.ic_save);
        saveContainer.addView(save);


        LinearLayout deleteContainer = new LinearLayout(mContext);
        deleteContainer.setLayoutParams(params);
        deleteContainer.setGravity(Gravity.CENTER);
        deleteContainer.setBackground(background2);
        container.addView(deleteContainer);

        ImageView delete = new ImageView(mContext);
        delete.setImageResource(R.drawable.ic_delete);
        deleteContainer.addView(delete);
    }
}
