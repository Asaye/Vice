
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.content.ContextCompat;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.vice.R;
import com.vice.TaskActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ViewFactory {

    private Context mContext;
    private int HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    private int WRAP = LayoutParams.WRAP_CONTENT;
    private int MATCH = LayoutParams.MATCH_PARENT;

    public ViewFactory(Context context) {
        mContext = context;
    }

    public LinearLayout getHomePage(final Activity activity) {
        LinearLayout mainContainer = new LinearLayout(mContext);
        LayoutParams homeParams = new LayoutParams(MATCH, MATCH);
        mainContainer.setLayoutParams(homeParams);
        mainContainer.setOrientation(LinearLayout.VERTICAL);

            LinearLayout homeContainer = new LinearLayout(mContext);
            homeContainer.setLayoutParams(homeParams);
            homeContainer.setOrientation(LinearLayout.VERTICAL);
            homeContainer.setPadding(0, 0, 0, 100);
            mainContainer.addView(homeContainer);

                LinearLayout logoContainer = new LinearLayout(mContext);
                LayoutParams logoContainerParams = new LayoutParams(MATCH, (int) (HEIGHT/1.75));
                logoContainer.setLayoutParams(logoContainerParams);
                logoContainer.setGravity(Gravity.CENTER);
                homeContainer.addView(logoContainer);

                    ImageView logo = new ImageView(mContext);
                    LayoutParams logoParams = new LayoutParams(500, 500);
                    logo.setLayoutParams(logoParams);
                    logo.setImageResource(R.drawable.logo);
                    logoContainer.addView(logo);

                TextView message = new TextView(mContext);
                LayoutParams messageParams = new LayoutParams(MATCH, 0);
                messageParams.weight = 1;
                message.setLayoutParams(messageParams);
                message.setText(R.string.home_message);
                message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                message.setTextColor(ContextCompat.getColor(mContext, R.color.text));
                message.setTextSize(20);
                message.setPadding(100, 0, 100, 0);
                homeContainer.addView(message);

                ImageView addBtn = new ImageView(mContext);
                LayoutParams addParams = new LayoutParams(120, 120);
                addParams.gravity = Gravity.RIGHT;
                addParams.rightMargin = 50;
                addBtn.setLayoutParams(addParams);
                addBtn.setImageResource(R.drawable.ic_add);
                addBtn.setPadding(25, 25, 25, 25);
                addBtn.setBackground(getBackground(60, 60, R.color.button));
                addBtn.setClickable(true);
                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, TaskActivity.class);
                        mContext.startActivity(intent);
                    }
                });
                homeContainer.addView(addBtn);

        return mainContainer;
    }

    public LinearLayout getEditText(TextWatcher watcher, String hint) {
        LinearLayout layout = createEditLayout();

            EditText editText = createEditText(watcher, hint);
            editText.setBackground(getBorder(8, 8, R.color.input_border));
            layout.addView(editText);

        return layout;
    }

    public LinearLayout getEditTextWithButton(TextWatcher watcher, View.OnClickListener listener,
                                              int resourceId, String hint, int ht) {
        LinearLayout layout = createEditLayout();
        EditText editText = createEditText(watcher, hint, ht);
        editText.setBackground(getBorder(8, 0, R.color.input_border));
        layout.addView(editText);

        ImageView imageView = createImageView(resourceId, 0, 8, R.color.colorPrimary, ht);
        imageView.setClickable(true);
        imageView.setOnClickListener(listener);
        layout.addView(imageView);

        return layout;
    }

    public LinearLayout getFixedTextWithButton(View.OnClickListener listener,
                                              int resourceId, String text, int ht) {
        LinearLayout layout = createEditLayout();
        TextView textView = createTextView(text, R.color.input_text, ht);
        textView.setBackground(getBorder(8, 0, R.color.input_border));
        layout.addView(textView);

        ImageView imageView = createImageView(resourceId, 0, 8, R.color.colorPrimary, ht);
        imageView.setClickable(true);
        imageView.setOnClickListener(listener);
        layout.addView(imageView);

        return layout;
    }

    public TextView createTextView(String text, int colorId) {
        TextView textView = new TextView(mContext);
        LayoutParams textParams = new LayoutParams(0, WRAP);
        textParams.weight = 1;
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(mContext, colorId));
        textView.setPadding(30, 10, 30, 10);
        textView.setLayoutParams(textParams);

        return textView;
    }

    public TextView createTextView(String text, int colorId, int ht) {
        TextView textView = new TextView(mContext);
        LayoutParams textParams = new LayoutParams(0, ht);
        textParams.weight = 1;
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(mContext, colorId));
        textView.setPadding(30, 10, 30, 10);
        textView.setLayoutParams(textParams);

        return textView;
    }

    public EditText createEditText(TextWatcher watcher, String hint) {
        EditText editText = new EditText(mContext);
        LayoutParams textParams = new LayoutParams(0, WRAP);
        textParams.weight = 1;
        editText.addTextChangedListener(watcher);
        editText.setPadding(30, 10, 30, 10);
        editText.setBackgroundResource(R.drawable.activity_btn);
        editText.setEms(10);
        editText.setHint(hint);
        editText.setTextColor(ContextCompat.getColor(mContext, R.color.input_text));
        editText.setBackground(getBorder(8, 8, R.color.input_border));
        editText.setLayoutParams(textParams);
        styleCursor(editText, true);
        addOnFocusChangeListener(editText);

        return editText;
    }

    public void styleCursor(EditText editText, boolean isDark) {
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            if (isDark) {
                f.set(editText, R.drawable.edittext_cursor);
            } else {
                f.set(editText, R.drawable.edittext_cursor_lt);
            }
        } catch (Exception e) {
        }
    }

    public void addOnFocusChangeListener(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm =
                        (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    public EditText createEditText(TextWatcher watcher, String hint, int ht) {
        EditText editText = new EditText(mContext);
        LayoutParams textParams = new LayoutParams(0, ht);
        textParams.weight = 1;
        editText.addTextChangedListener(watcher);
        editText.setPadding(30, 10, 30, 10);
        editText.setBackgroundResource(R.drawable.activity_btn);
        editText.setEms(10);
        editText.setHint(hint);
        editText.setTextColor(ContextCompat.getColor(mContext, R.color.input_text));
        editText.setBackground(getBorder(8, 8, R.color.input_border));
        editText.setLayoutParams(textParams);
        styleCursor(editText, true);
        addOnFocusChangeListener(editText);

        return editText;
    }

    public ImageView createImageView(int resourceId, int r1, int r2, int color) {
        ImageView imageView = new ImageView(mContext);
        LayoutParams imageParams = new LayoutParams(WRAP, WRAP);
        imageView.setPadding(13, 13, 13, 13);
        imageView.setBackground(this.getBackground(r1, r2, color));
        imageView.setImageResource(resourceId);
        imageView.setLayoutParams(imageParams);

        return imageView;
    }

    public ImageView createImageView(int resourceId, int r1, int r2, int color, int ht) {
        ImageView imageView = new ImageView(mContext);
        LayoutParams imageParams = new LayoutParams(WRAP, ht);
        imageView.setPadding(13, 13, 13, 13);
        imageView.setBackground(this.getBackground(r1, r2, color));
        imageView.setImageResource(resourceId);
        imageView.setLayoutParams(imageParams);

        return imageView;
    }

    private LinearLayout createEditLayout() {
        LinearLayout layout = new LinearLayout(mContext);
        LayoutParams containerParams = new LayoutParams(MATCH, WRAP);
        containerParams.topMargin = 30;
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.activity_detail));
        layout.setLayoutParams(containerParams);

        return layout;
    }

    public LinearLayout getDropDown(final String label, ArrayList<SpinnerView> list,
                                    final Callback callback, int bColor, int tColor, int sColor) {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.activity_detail));
        layout.setPadding(50, 10, 50, 10);

            TextView textView = new TextView(mContext);
            LayoutParams labelParams = new LayoutParams(0, WRAP);
            labelParams.weight = 0.4f;
            textView.setGravity(Gravity.RIGHT);
            textView.setPadding(0, 0, 20, 0);
            textView.setLayoutParams(labelParams);
            textView.setText(label);
            textView.setTextSize(16);
            layout.addView(textView);

            ArrayAdapter<SpinnerView> adapter = new SpinnerAdapter(list, bColor, tColor, sColor);
            Spinner spinner = getSpinner(label, adapter, callback, 75);
            layout.addView(spinner);

        return layout;
    }

    public LinearLayout getDropDown(ArrayList<SpinnerView> list, final Callback callback) {
        int bColor = R.color.light_blue;
        int tColor = R.color.text;

        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(10, 0, 10, 0);

        ArrayAdapter<SpinnerView> adapter = new SpinnerAdapter(list, bColor, tColor, tColor);
        Spinner spinner = getSpinner(adapter, callback, 75);
        layout.addView(spinner);

        return layout;
    }

    public ArrayAdapter<SpinnerView> getAdapter(ArrayList<ViewFactory.SpinnerView> list, int bColor,
                                                int tColor, int sColor) {
        return new SpinnerAdapter(list, bColor, tColor, sColor);
    }

    public Spinner getSpinner(final String label, ArrayAdapter<SpinnerView> adapter, final Callback callback, int ht) {
        Spinner spinner = new Spinner(mContext);
        LayoutParams dropdownParams = new LayoutParams(0, ht);
        dropdownParams.weight = 0.6f;
        spinner.setPadding(0, 0, 0, 0);
        spinner.setAdapter(adapter);
        spinner.setLayoutParams(dropdownParams);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView selected = (TextView) view;
                callback.send(label, selected.getText());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return spinner;
    }

    public Spinner getSpinner(ArrayAdapter<SpinnerView> adapter, final Callback callback, int ht) {
        Spinner spinner = new Spinner(mContext);
        LayoutParams dropdownParams = new LayoutParams(MATCH, ht);
        spinner.setPadding(0, 0, 0, 0);
        spinner.setAdapter(adapter);
        spinner.setLayoutParams(dropdownParams);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                TextView selected = (TextView) view;
//                callback.send(label, selected.getText());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return spinner;
    }

    public Spinner getSpinner(final String label, final String title, ArrayAdapter<SpinnerView> adapter,
                              final Callback callback, int ht) {
        Spinner spinner = new Spinner(mContext);
        LayoutParams dropdownParams = new LayoutParams(0, ht);
        dropdownParams.weight = 0.6f;
        spinner.setPadding(0, 0, 0, 0);
        spinner.setAdapter(adapter);
        spinner.setLayoutParams(dropdownParams);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView selected = (TextView) view;
                callback.send(title, new String[] {label, selected.getText().toString() });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return spinner;
    }

    public Drawable getBorder(int r1, int r2, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadii(new float[]{r1, r1, r2, r2, r2, r2, r1, r1});
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(1, ContextCompat.getColor(mContext, color));

        return drawable;
    }

    public Drawable getBackground(int r1, int r2, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadii(new float[]{r1, r1, r2, r2, r2, r2, r1, r1});
        drawable.setColor(ContextCompat.getColor(mContext, color));

        return drawable;
    }

    public Drawable getBackground(float[] radii, int bColor, int sColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        if (radii != null) {
            drawable.setCornerRadii(radii);
        }
        drawable.setColor(ContextCompat.getColor(mContext, bColor));
        drawable.setStroke(1, ContextCompat.getColor(mContext, sColor));

        return drawable;
    }

    public static class SpinnerView {
        private String value;
        public SpinnerView(String value) {
            this.value = value;
        }
        public String getValue() {
            return this.value;
        }
    }

    public class SpinnerAdapter extends ArrayAdapter<SpinnerView> {

        private int mBackgroundColor, mTextColor, mSelectedTextColor;
        private String mSelected = "";
        private ArrayList<SpinnerView> mSpinnerTexts;
        private int mIndex = -1;

        public SpinnerAdapter(ArrayList<SpinnerView> spinnerTexts, int bColor, int tColor, int sColor) {
            super(mContext, android.R.layout.simple_list_item_1, spinnerTexts);
            mSpinnerTexts = spinnerTexts;
            mBackgroundColor = bColor;
            mTextColor = tColor;
            mSelectedTextColor = sColor;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView != null) {
                TextView textView = (TextView) convertView;
                if (textView.getText().length() == 0 ||  mSelected != mSpinnerTexts.get(0).getValue()) {
                   textView.setText(mSelected);
                }
            }
            if (parent != null) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setColor(ContextCompat.getColor(mContext, mBackgroundColor));
                parent.setBackground(drawable);
            }
            return getTextView(convertView, position, true);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (parent != null) {
                parent.setBackgroundResource(R.drawable.spinner_background);
            }
            return getTextView(convertView, position, false);
        }

        private TextView getTextView(View convertView, int position, boolean isDropDown) {
            if (convertView == null) {
                TextView textView = null;
                SpinnerView spinnerText = getItem(position);
                String text = spinnerText.getValue();

                textView = new TextView(mContext);
                textView.setText(text);

                if (!isDropDown) {
                    textView.setTextColor(ContextCompat.getColor(mContext, mSelectedTextColor));
                    textView.setPadding(30, 0, 0, 0);
                    mSelected = textView.getText().toString();
                    mIndex = position;
                } else {
                    textView.setTextColor(ContextCompat.getColor(mContext, mTextColor));
                    textView.setPadding(30, 10, 0, 10);
                }

                return textView;
            } else {
                return (TextView) convertView;
            }
        }
    }
}
