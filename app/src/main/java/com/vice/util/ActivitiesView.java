
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vice.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import java.util.Set;

public class ActivitiesView {

    private Activity mActivity;
    private int WRAP = LinearLayout.LayoutParams.WRAP_CONTENT;
    private int MATCH = LinearLayout.LayoutParams.MATCH_PARENT;
    private JSONObject mList, mTemp = new JSONObject();
    private ScrollView mScrollView;
    private SettingsView mSettingsView;
    private ViewFactory mViewFactory;
    private Callback mCallback = new Callback() {
        @Override
        public void send(String k, Object data) {
            if (data instanceof String) {
                if (((String) data).equalsIgnoreCase("save")) {
                    updateList();
                } else if (((String) data).equalsIgnoreCase("undo")) {
                    Map<String, String> tasks = Constants.TASK_LIST;
                    Set<String> keys = tasks.keySet();
                    int index = 0;
                    for (String key: keys) {
                        if (key.equalsIgnoreCase(k)) {
                            break;
                        }
                        index++;
                    }

                    if (index != -1 && index != keys.size()) {
                        LinearLayout bodyContainer = (LinearLayout) mScrollView.getChildAt(0);
                        LinearLayout rowContainer = (LinearLayout) bodyContainer.getChildAt(index);
                        LinearLayout settings = (LinearLayout) rowContainer.getChildAt(1);
                        String[] param = Constants.TASK_SETTING_PARAMETERS.get(k);
                        JSONObject json = mList.optJSONObject(k);

                        for (int i = 1; i < settings.getChildCount(); i++) {
                            LinearLayout container = (LinearLayout) settings.getChildAt(i);
                            Object child = container.getChildAt(0);
                            if (child instanceof TextView) {
                                ((TextView) child).setText(json.optString(param[i - 1]));
                            }
                        }
                    }
                }
            } else if (data instanceof Boolean) {
                if (!(boolean) data) {
                    updateList(k, data);
                }
            } else {
                updateList(k, data);
            }
        }
    };

    public ActivitiesView(Activity activity, JSONObject json) {
        mActivity = activity;
        mSettingsView = new SettingsView(activity);
        mViewFactory = new ViewFactory(activity);

        if (json == null) {
            mList = new JSONObject();
        } else {
            mList = json;
        }
    }

    public void getView(final Callback callback) {
        Map<String, String> tasks = Constants.TASK_LIST;
        Set<String> keys = tasks.keySet();

        final Dialog dialog = new Dialog(mActivity,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (callback != null) {
                    callback.send(null, null);
                }
            }
        });

        LinearLayout container = new LinearLayout(mActivity);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(MATCH, MATCH);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(containerParams);

            LinearLayout titleLayout = new LinearLayout(mActivity);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(MATCH,125);
            titleLayout.setLayoutParams(titleParams);
            titleLayout.setGravity(Gravity.CENTER_VERTICAL);
            titleLayout.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.card));
            container.addView(titleLayout);

                TextView title = new TextView(mActivity);
                title.setText("Activities");
                title.setTextSize(24);
                title.setPadding(50, 10, 50, 10);
                title.setTextColor(ContextCompat.getColor(mActivity, R.color.text));
                titleLayout.addView(title);

            mScrollView = new ScrollView(mActivity);
            LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(MATCH,0);
            scrollParams.weight = 1;
            mScrollView.setLayoutParams(scrollParams);
            container.addView(mScrollView);

                LinearLayout bodyLayout = new LinearLayout(mActivity);
                LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(MATCH, MATCH);
                bodyLayout.setLayoutParams(bodyParams);
                bodyLayout.setOrientation(LinearLayout.VERTICAL);
                bodyLayout.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.modal_body));

                for (final String key: keys) {
                    final LinearLayout rowContainer = new LinearLayout(mActivity);
                    rowContainer.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout taskRow = new LinearLayout(mActivity);
                    taskRow.setPadding(20, 0, 20, 0);
                    rowContainer.addView(taskRow);
                        int checkbox = R.drawable.ic_checkbox;
                        if (mList.opt(key) != null) {
                            checkbox = R.drawable.ic_checked;
                        }

                        ImageView imageView =
                                mViewFactory.createImageView(checkbox, 0, 0, R.color.modal_body);
                        imageView.setClickable(true);
                        taskRow.addView(imageView);

                        TextView textView = new TextView(mActivity);
                        textView.setText(tasks.get(key));
                        textView.setTextSize(16);
                        textView.setPadding(0, 10, 10, 10);
                        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.modal_text));
                        taskRow.addView(textView);

                        if (Constants.TASKS_WITH_SETTINGS.indexOf(key) != -1) {
                            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, WRAP);
                            textParams.weight = 1;
                            textView.setLayoutParams(textParams);
                            int arrow = R.drawable.ic_chevron_up;
                            JSONObject json = mList.optJSONObject(key);
                            if (json != null) {
                                arrow = R.drawable.ic_chevron_down;
                            }

                            final ImageView dropArrow = mViewFactory.createImageView(arrow, 0, 0, R.color.modal_body);
                            dropArrow.setClickable(true);

                            if (mList == null || mList.optJSONObject(key) == null) {
                                dropArrow.setVisibility(View.GONE);
                            }
                            taskRow.addView(dropArrow);

                            LinearLayout settings = mSettingsView.getSettings(key, json, mCallback);
                            addDropArrowListener(dropArrow, settings);
                            addCheckboxListener(imageView, dropArrow, settings, key);
                            if (settings != null) {
                                rowContainer.addView(settings);
                            }
                        } else {
                            addCheckboxListener(imageView, null, null, key);
                        }

                        bodyLayout.addView(rowContainer);
                    }

                mScrollView.addView(bodyLayout);

        addFooter(container, dialog, titleParams);
        dialog.setContentView(container);
        dialog.show();
    }

    private void addDropArrowListener(ImageView dropArrow, final LinearLayout settings) {
        dropArrow.setOnClickListener(new View.OnClickListener() {
            private boolean mIsExpanded = true;
            @Override
            public void onClick(View v) {
                ImageView arrow = (ImageView) v;
                if (mIsExpanded) {
                    arrow.setImageResource(R.drawable.ic_chevron_down);
                    updateList();
                    settings.setVisibility(View.GONE);
                } else {
                    arrow.setImageResource(R.drawable.ic_chevron_up);
                    settings.setVisibility(View.VISIBLE);
                }
                mIsExpanded = !mIsExpanded;
            }
        });
    }

    private void addCheckboxListener(ImageView imageView, final ImageView dropArrow,
                                     final LinearLayout settings, final String key) {
        imageView.setOnClickListener(new View.OnClickListener() {
            private boolean isChecked = mList.opt(key) != null;
            @Override
            public void onClick(View v) {
                ImageView view = (ImageView) v;
                if (isChecked) {
                    view.setImageResource(R.drawable.ic_checkbox);
                    if (dropArrow != null) {
                        dropArrow.setVisibility(View.GONE);
                        dropArrow.setImageResource(R.drawable.ic_chevron_down);
                        settings.setVisibility(View.GONE);
                    }
                    mList.remove(key);
                } else {
                    view.setImageResource(R.drawable.ic_checked);
                    if (dropArrow != null) {
                        dropArrow.setVisibility(View.VISIBLE);
                        dropArrow.setImageResource(R.drawable.ic_chevron_up);
                        settings.setVisibility(View.VISIBLE);
                    }
                    try {
                        mList.put(key, "");
                    } catch(Exception e) {
                    }
                }
                isChecked = !isChecked;
            }
        });
    }

    private void addFooter(LinearLayout container, final Dialog dialog, LinearLayout.LayoutParams titleParams) {
        LinearLayout footerLayout = new LinearLayout(mActivity);
        footerLayout.setLayoutParams(titleParams);
        footerLayout.setGravity(Gravity.RIGHT);
        footerLayout.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.card));
        container.addView(footerLayout);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(200,80);
        buttonParams.gravity = Gravity.CENTER;
        buttonParams.rightMargin = 20;
        buttonParams.gravity = Gravity.CENTER_VERTICAL;
        String[] btnTexts = new String[] {"Save", "Cancel"};
        for (String text: btnTexts) {
            Button btn = new Button(mActivity);
            btn.setLayoutParams(buttonParams);
            btn.setText(text);
            btn.setTextColor(ContextCompat.getColor(mActivity, R.color.text));
            btn.setBackground(mViewFactory.getBackground(10, 10 , R.color.modal_body));
            btn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((Button)v).getText().toString().equalsIgnoreCase("Save")) {
                        updateList();
                    }
                    dialog.cancel();
                }
            });
            footerLayout.addView(btn);
        }
    }

    private void updateList() {
        JSONArray names = mTemp.names();
        if (names == null || names.length() == 0) return;

        for(int i = 0; i < names.length(); i++) {
            String tempKey = names.optString(i);
            JSONObject temp = mTemp.optJSONObject(tempKey);
            if (temp != null) {
                try {
                    mList.put(tempKey, new JSONObject(temp.toString()));
                    mTemp.remove(tempKey);
                } catch (Exception e) {
                }
            }
        }
    }

    private void updateList(String key, Object data) {
        try {
            if (data instanceof String) {
                if (((String) data).equalsIgnoreCase("undo")) {
                    mTemp.put(key, mList.optString((String) data));
                } else {
                    updateList();
                }
            } else {
                JSONObject temp = mTemp.optJSONObject(key);

                String[] keyValue = (String[]) data;
                if (temp == null) {
                    JSONObject pValue = mList.optJSONObject(key);
                    if (pValue != null) {
                        temp = new JSONObject(pValue.toString());
                    } else {
                        temp = new JSONObject();
                    }
                }

                temp.put(keyValue[0], keyValue[1]);
                mTemp.put(key, temp);
            }
        } catch(Exception e) {
        }
    }
}
