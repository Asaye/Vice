
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import com.vice.R;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;

import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsView {

    private Context mContext;
    private ViewFactory mViewFactory;
    private int WRAP = LayoutParams.WRAP_CONTENT;
    private int MATCH = LayoutParams.MATCH_PARENT;

    public SettingsView(Context context) {
        mContext = context;
        mViewFactory = new ViewFactory(context);
    }

    public LinearLayout getSettings(String activity, JSONObject json, Object obj) {
        try {
            JSONObject value;
            if (json == null) {
                value = new JSONObject();
            } else {
                value = new JSONObject(json.toString());
            }

            if (activity.equalsIgnoreCase("CALL_NUMBER")) {
                return getCallSettings(activity, value, obj);
            } else if (activity.equalsIgnoreCase("SEND_SMS")) {
                return getSmsSettings(activity, value, obj);
            } else if (activity.equalsIgnoreCase("RECORD_AUDIO")) {
                return getAudioSettings(activity, value, obj);
            } else if (activity.equalsIgnoreCase("RECORD_VIDEO")) {
                return getVideoSettings(activity, value, obj);
            } else if (activity.equalsIgnoreCase("TAKE_PICTURES")) {
                return getPictureSettings(activity, value, obj);
            } else if (activity.equalsIgnoreCase("FILE_OPEN")) {
                return getFileOpenSettings(activity, value, obj);
            } else if (activity.equalsIgnoreCase("FILE_DOWNLOAD")) {
                return getFileDownloadSettings(activity, value, obj);
            } else if (activity.equalsIgnoreCase("FILE_UPLOAD")) {
                return getFileUploadSettings(activity, value, obj);
            }
        } catch (Exception e) {
        }

        return null;
    }

    private LinearLayout getEditButtons(final Callback callback, final String label, final LinearLayout layout) {
        LinearLayout container = new LinearLayout(mContext);
        LayoutParams containerParams = new LayoutParams(WRAP, WRAP);
        containerParams.gravity = Gravity.RIGHT;
        containerParams.bottomMargin = 15;
        container.setLayoutParams(containerParams);

            ImageView undo = mViewFactory.createImageView(R.drawable.ic_undo, 0, 0, R.color.modal_body);
            container.addView(undo);
            undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.send(label, "undo");
                }
            });

            ImageView save = mViewFactory.createImageView(R.drawable.ic_save, 0, 0, R.color.modal_body);
            container.addView(save);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.send(label, "save");
                }
            });

        return container;
    }

    private LinearLayout getContainer() {
        LinearLayout container = new LinearLayout(mContext);
        LayoutParams containerParams = new LayoutParams(MATCH, WRAP);
        containerParams.gravity = Gravity.CENTER;
        containerParams.leftMargin = 80;
        containerParams.rightMargin = 50;
        containerParams.topMargin = 10;
        containerParams.bottomMargin = 10;
        container.setLayoutParams(containerParams);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setVisibility(View.GONE);
        container.setPadding(50, 20, 50, 20);
        container.setBackground(mViewFactory.getBorder(10, 10, R.color.activity_detail));

        return container;
    }

    private LinearLayout getEditText(Callback callback, String label, String hint, String value, int lines) {
        LinearLayout layout = new LinearLayout(mContext);
        LayoutParams params = new LayoutParams(MATCH, WRAP);
        layout.setLayoutParams(params);

            EditText editText = createEditText(callback, label, hint, value, lines);
            layout.addView(editText);

            return layout;
    }

    public LinearLayout getEditTextWithButton(Callback callback, String label, int resourceId, final String hint, String value) {

        LinearLayout layout = new LinearLayout(mContext);
        LayoutParams containerParams = new LayoutParams(MATCH, WRAP);
        layout.setLayoutParams(containerParams);

        final EditText editText = createEditText(callback, label, hint, value,1);
        editText.setBackground(mViewFactory.getBorder(8, 0, R.color.input_border));
        layout.addView(editText);

        final Callback btnCallback = new Callback() {
            @Override
            public void send(String key, Object data) {
                try {
                    if (!(boolean) data) {
                        editText.setText((String) data);
                    }
                } catch (Exception e) {
                    editText.setText((String) data);
                }
            }
        };

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FilePath.mSettingKey = hint;
                FilePath.mCallback = btnCallback;
                Intent intent = new Intent(mContext, FilePath.class);
                mContext.startActivity(intent);
            }
        };

        ImageView imageView = mViewFactory.createImageView(resourceId, 0, 8, R.color.colorPrimary);
        LayoutParams imageParams = new LayoutParams(WRAP, 60);
        imageView.setLayoutParams(imageParams);
        imageView.setClickable(true);
        imageView.setPadding(13, 5, 13, 5);
        imageView.setOnClickListener(listener);
        layout.addView(imageView);

        return layout;
    }

    private EditText createEditText(final Callback callback, final String label, final String hint, String value, int lines) {
        TextWatcher watcher = new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                try {
                    callback.send(label, new String[] { hint, c.toString()});
                } catch (Exception e) { }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) { }
            public void afterTextChanged(Editable c) { }
        };
        EditText editText = new EditText(mContext);
        LayoutParams textParams = new LayoutParams(0, 60);
        textParams.weight = 1;
        textParams.bottomMargin = 20;
        if (lines > 1) {
            editText.setLines(lines);
        }

        if (value != null) {
            editText.setText(value);
        }
        editText.addTextChangedListener(watcher);
        editText.setPadding(30, 5, 30, 5);
        editText.setBackgroundResource(R.drawable.activity_btn);
        editText.setEms(10);
        editText.setHint(Constants.SETTING_HINTS.get(hint));
        editText.setTextSize(16);
        editText.setTextColor(ContextCompat.getColor(mContext, R.color.modal_text));
        editText.setBackground(mViewFactory.getBorder(8, 8, R.color.activity_detail));
        editText.setLayoutParams(textParams);
        editText.setHintTextColor(ContextCompat.getColor(mContext, R.color.text_blur));
        mViewFactory.styleCursor(editText, false);
        mViewFactory.addOnFocusChangeListener(editText);

        return editText;
    }

    private ArrayAdapter<ViewFactory.SpinnerView> getAdapter(ArrayList<ViewFactory.SpinnerView> list) {
        int c1 = R.color.light_blue;
        int c2 = R.color.modal_text;
        int c3 = R.color.activity_text;

        return mViewFactory.getAdapter(list, c1, c2, c3);
    }

    private LinearLayout getCallSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String num = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);
        if (json != null) {
            num = json.optString(param[0]);
        }
        LinearLayout container = getContainer();

            container.addView(getEditButtons(callback, label, container));
            container.addView(getEditText(callback, label, param[0], num, 1));

        return container;
    }

    private LinearLayout getSmsSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String num = "", msg = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);
        if (json != null) {
            num = json.optString(param[0]);
            msg = json.optString(param[1]);
        }
        LinearLayout container = getContainer();

            container.addView(getEditButtons(callback, label, container));
            container.addView(getEditText(callback, label,param[0], num, 1));
            container.addView(getEditText(callback, label,param[1], msg,3));

        return container;
    }

    private LinearLayout getAudioSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String output = "", size = "", duration = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);
        if (json != null) {
            output = json.optString(param[0]);
            size = json.optString(param[1]);
            duration = json.optString(param[2]);
        }

        LinearLayout container = getContainer();

        container.addView(getEditButtons(callback, label, container));
        container.addView(getEditTextWithButton(callback, label, R.drawable.ic_folder, param[0], output));
        container.addView(getEditText(callback, label,param[1], size, 1));
        container.addView(getEditText(callback, label,param[2], duration,1));
        ArrayAdapter<ViewFactory.SpinnerView> adapter = getAdapter(Constants.OUTPUT_FORMAT);
        Spinner spinner = mViewFactory
                .getSpinner(param[3],  label, adapter, callback, 60);
        LinearLayout layout = new LinearLayout(mContext);
        LayoutParams spinnerParams = new LayoutParams(MATCH, WRAP);
        spinnerParams.bottomMargin = 20;
        layout.setLayoutParams(spinnerParams);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(spinner);
        container.addView(layout);

        return container;
    }

    private LinearLayout getVideoSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String output = "", size = "", duration = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);
        if (json != null) {
            output = json.optString(param[0]);
            size = json.optString(param[1]);
            duration = json.optString(param[2]);
        }

        LinearLayout container = getContainer();

        container.addView(getEditButtons(callback, label, container));
        container.addView(getEditTextWithButton(callback, label, R.drawable.ic_folder, param[0], output));
        container.addView(getEditText(callback, label,param[1], size,1));
        container.addView(getEditText(callback, label,param[2], duration, 1));

        ArrayAdapter<ViewFactory.SpinnerView> cameraAdapter = getAdapter(Constants.CAMERA_TYPE);
        Spinner cameraType = mViewFactory
                .getSpinner(param[3], label, cameraAdapter, callback, 60);

        LinearLayout cameraLayout = new LinearLayout(mContext);
        LayoutParams spinnerParams = new LayoutParams(MATCH, WRAP);
        spinnerParams.bottomMargin = 20;
        cameraLayout.setLayoutParams(spinnerParams);
        cameraLayout.setOrientation(LinearLayout.HORIZONTAL);
        cameraLayout.addView(cameraType);
        container.addView(cameraLayout);

        ArrayAdapter<ViewFactory.SpinnerView> outputAdapter = getAdapter(Constants.OUTPUT_FORMAT);
        Spinner outputFormat = mViewFactory
                .getSpinner(param[4], label, outputAdapter, callback, 60);

        LinearLayout outputLayout = new LinearLayout(mContext);
        outputLayout.setLayoutParams(spinnerParams);
        outputLayout.setOrientation(LinearLayout.HORIZONTAL);
        outputLayout.addView(outputFormat);
        container.addView(outputLayout);

        return container;
    }

    private LinearLayout getPictureSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String output = "", width = "", height = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);
        if (json != null) {
            output = json.optString(param[0]);
            width = json.optString(param[2]);
            height = json.optString(param[3]);
        }

        LinearLayout container = getContainer();

        container.addView(getEditButtons(callback, label, container));
        container.addView(getEditTextWithButton(callback, label, R.drawable.ic_folder, param[0], output));

        ArrayAdapter<ViewFactory.SpinnerView> cameraAdapter = getAdapter(Constants.CAMERA_TYPE);
        Spinner cameraType = mViewFactory
                .getSpinner(param[1], label, cameraAdapter, callback, 60);

        LinearLayout cameraLayout = new LinearLayout(mContext);
        LayoutParams spinnerParams = new LayoutParams(MATCH, WRAP);
        spinnerParams.bottomMargin = 20;
        cameraLayout.setLayoutParams(spinnerParams);
        cameraLayout.setOrientation(LinearLayout.HORIZONTAL);
        cameraLayout.addView(cameraType);
        container.addView(cameraLayout);

        container.addView(getEditText(callback, label,"WIDTH", width, 1));
        container.addView(getEditText(callback, label,"HEIGHT", height, 1));

        return container;
    }

    private LinearLayout getFileOpenSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String source = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);
        if (json != null) {
            source = json.optString(param[0]);
        }

        LinearLayout container = getContainer();

        container.addView(getEditButtons(callback, label, container));
        container.addView(getEditTextWithButton(callback, label, R.drawable.ic_folder, param[0], source));

        return container;
    }

    private LinearLayout getFileDownloadSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String source = "", destination = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);

        if (json != null) {
            source = json.optString(param[0]);
            destination = json.optString(param[1]);
        }

        LinearLayout container = getContainer();

        container.addView(getEditButtons(callback, label, container));
        container.addView(getEditText(callback, label,param[0], source, 3));
        container.addView(getEditTextWithButton(callback, label, R.drawable.ic_folder, param[1], destination));

        return container;
    }

    private LinearLayout getFileUploadSettings(String label, JSONObject json, Object obj) {
        Callback callback = (Callback) obj;
        String source = "", destination = "";
        String[] param = Constants.TASK_SETTING_PARAMETERS.get(label);
        if (json != null) {
            source = json.optString(param[0]);
            destination = json.optString(param[1]);
        }

        LinearLayout container = getContainer();

        container.addView(getEditButtons(callback, label, container));
        container.addView(getEditTextWithButton(callback, label, R.drawable.ic_folder, param[0], source));
        container.addView(getEditText(callback, label, param[1], destination, 3));

        return container;
    }
}
