
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import org.json.JSONArray;

public class PermissionsActivity extends AppCompatActivity {

    private int mLength;
    public static Callback mCallback;

    private static int PERMISSIONS_REQUEST_CODE = 10020;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == mLength) {
            mCallback.send(null, true);
        } else {
            mCallback.send(null, false);
        }
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getBundleExtra("data");
        String type = bundle.getString("type");
        if (type.equalsIgnoreCase("location")) {
            requestLocationPermission();
        } else if (type.equalsIgnoreCase("activity")){
            String activities = bundle.getString("activities");
            requestActivityPermission(activities);
        } else if (type.equalsIgnoreCase("notification")){
            changeNotificationPolicy();
        }
    }

    public void requestLocationPermission() {
        String[] required = (String[]) Constants.PERMISSIONS.get("LOCATION");
        int status = ContextCompat.checkSelfPermission(this, required[0]);
        if (status == PackageManager.PERMISSION_GRANTED) {
            mCallback.send(null, true);
            finish();
        } else {
            mLength = 1;
            ActivityCompat.requestPermissions(this, required, PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestActivityPermission(String s) {
        try {
            JSONArray array = new JSONArray(s);
            JSONArray required = new JSONArray();

            for (int i = 0; i < array.length(); i++) {
                String key = array.optString(i);
                Object permission = Constants.PERMISSIONS.get(key);
                final Context context = getApplicationContext();
                int status = -1;

                if (permission instanceof String) {
                    status = ContextCompat.checkSelfPermission(context, (String) permission);
                    if (status != PackageManager.PERMISSION_GRANTED) {
                        required.put(permission);
                    }
                } else if (permission instanceof String[]) {
                    for (String p : (String[]) permission) {
                        status = ContextCompat.checkSelfPermission(context, p);
                        if (status != PackageManager.PERMISSION_GRANTED) {
                            required.put(p);
                        }
                    }
                }
            }
            int len = required.length();
            if (len > 0) {
                String[] permissions = new String[len];
                for (int j = 0; j < len; j++) {
                    permissions[j] = required.getString(j);
                }
                mLength = len;
                ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
            } else {
                mCallback.send(null, true);
                finish();
            }
        } catch (Exception e) {
        }
    }

    public void changeNotificationPolicy() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mCallback.send(null, true);
                finish();
                return;
            }
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
            } else {
                mCallback.send(null, true);
                finish();
            }
        } catch ( Exception e) {
        }
    }
}
