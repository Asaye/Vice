
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import android.net.Uri;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import java.lang.Exception;

public class FilePath extends AppCompatActivity {

    private static final String TAG = "FILE_OPEN_ERROR";
    private static final int PICK_REQUEST = 10050;
    public static Callback mCallback;
    public static String mSettingKey;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (mCallback != null && requestCode == PICK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = intent.getData();

                if (uri == null) {
                    mCallback.send(TAG, false);
                } else {
                    if (uri.getScheme().equalsIgnoreCase("file")) {
                        mCallback.send(mSettingKey, uri.toString());
                        return;
                    }
                    try {
                        String path = Util.getPath(uri, getApplicationContext());
                        mCallback.send(mSettingKey, path);
                    } catch (Exception e) {
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mCallback.send(TAG, false);
            }
            mCallback = null;
            this.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSettingKey.equalsIgnoreCase("OUTPUT_FOLDER") ||
            mSettingKey.equalsIgnoreCase("DESTINATION")) {
            openDialog(Intent.ACTION_OPEN_DOCUMENT_TREE);
        } else {
            openDialog(Intent.ACTION_GET_CONTENT);
        }
    }

    private void openDialog(String action) {
          Intent intent = new Intent(action);
          if (action == Intent.ACTION_GET_CONTENT) intent.setType("*/*");
          intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          startActivityForResult(intent, PICK_REQUEST);
    }
}
