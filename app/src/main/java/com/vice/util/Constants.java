
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import android.Manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Constants {

    List<String> MONTHS = Arrays.asList(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    );

    Map<String, String> TASK_LIST = new LinkedHashMap<String, String>() {
        {
            put("SET_ALARM", "Set alarm");
            put("VOLUME_SILENT", "Mute");
            put("VOLUME_NORMAL", "Normalize volume");
            put("VOLUME_VIBRATE", "Vibrate");
            put("CALL_NUMBER", "Call a number");
            put("SEND_SMS", "Send SMS");
            put("TAKE_PICTURES", "Take a picture");
            put("RECORD_VIDEO", "Record a video");
            put("RECORD_AUDIO", "Record an audio");
            put("FILE_OPEN", "Open a file");
            put("FILE_DOWNLOAD", "Download a file");
            put("FILE_UPLOAD", "Upload a file");
            put("BLUETOOTH_ON", "Turn bluetooth on");
            put("BLUETOOTH_OFF", "Turn bluetooth off");
            put("WIFI_ON", "Turn wifi on");
            put("WIFI_OFF", "Turn wifi off");
        }
    };

    String[] ACTIVITY_KEYS = new String[] {
        "Date",
        "Time",
        "Tasks",
        "Duration",
        "Prenotification",
        "Frequency",
        "Place",
        "Description"
    };

    Map<String, String> SETTING_HINTS = new LinkedHashMap<String, String>() {
        {
            put("OUTPUT_FOLDER", "Output folder");
            put("OUTPUT_FORMAT", "Output format");
            put("MAX_FILE_SIZE", "Max. file size (MB)");
            put("MAX_DURATION", "Max. duration (min.)");
            put("DESTINATION", "Destination");
            put("SOURCE", "Source");
            put("CAMERA_TYPE", "Camera type");
            put("NUMBER", "Number");
            put("MESSAGE", "Message");
            put("WIDTH", "Width (px.)");
            put("HEIGHT", "Height (px.)");
        }
    };

    List<String> TASKS_WITH_SETTINGS = Arrays.asList(
        "CALL_NUMBER",
        "SEND_SMS",
        "TAKE_PICTURES",
        "RECORD_VIDEO",
        "RECORD_AUDIO",
        "FILE_OPEN",
        "FILE_UPLOAD",
        "FILE_DOWNLOAD"
    );

    Map<String, String[]> TASK_SETTING_PARAMETERS = new LinkedHashMap<String, String[]>() {
        {
            put("CALL_NUMBER", new String[] { "NUMBER"});
            put("SEND_SMS", new String[] { "NUMBER", "MESSAGE"});
            put("TAKE_PICTURES", new String[] { "OUTPUT_FOLDER", "CAMERA_TYPE", "WIDTH", "HEIGHT" });
            put("RECORD_VIDEO", new String[] { "OUTPUT_FOLDER", "MAX_FILE_SIZE", "MAX_DURATION", "CAMERA_TYPE", "OUTPUT_FORMAT" });
            put("RECORD_AUDIO", new String[] { "OUTPUT_FOLDER", "MAX_FILE_SIZE", "MAX_DURATION", "OUTPUT_FORMAT" });
            put("FILE_OPEN", new String[] { "SOURCE" });
            put("FILE_UPLOAD", new String[] { "SOURCE", "DESTINATION" });
            put("FILE_DOWNLOAD", new String[] { "SOURCE", "DESTINATION" });
        }
    };

    List<String> REQUIRES_DIRECTORY = Arrays.asList(
            "TAKE_PICTURES",
            "RECORD_VIDEO",
            "RECORD_AUDIO",
            "FILE_DOWNLOAD"
    );

    List<String> REQUIRES_SOURCE = Arrays.asList("FILE_OPEN");
    List<String> REQUIRES_DESTINATION = Arrays.asList("FILE_UPLOAD");

    Map<String, Object> PERMISSIONS = new HashMap<String, Object>() {{
        put("SET_ALARM", null);
        put("FILE_OPEN", Manifest.permission.WRITE_EXTERNAL_STORAGE);
        put("PHONE_SWITCH_OFF", Manifest.permission.BLUETOOTH);
        put("CALL_NUMBER", Manifest.permission.CALL_PHONE);
        put("SEND_SMS", Manifest.permission.SEND_SMS);
        put("FILE_DOWNLOAD", Manifest.permission.WRITE_EXTERNAL_STORAGE);
        put("FILE_UPLOAD", Manifest.permission.WRITE_EXTERNAL_STORAGE);
        put("BLUETOOTH_ON", Manifest.permission.BLUETOOTH);
        put("BLUETOOTH_OFF", Manifest.permission.BLUETOOTH);
        put("WIFI_ON", Manifest.permission.CHANGE_WIFI_STATE);
        put("WIFI_OFF", Manifest.permission.CHANGE_WIFI_STATE);
        put("LOCATION", new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION
        });
        put("TAKE_PICTURES", new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        });
        put("RECORD_AUDIO", new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        });
        put("RECORD_VIDEO", new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        });
    }};

    List<String> DURATION_LIST = Arrays.asList("", "30 min", "1 hour", "2 hours", "3 hours", "4 hours", "1 day");
    ArrayList<ViewFactory.SpinnerView> DURATION =
            new ArrayList<ViewFactory.SpinnerView>(){{
                for (String item: DURATION_LIST) add(new ViewFactory.SpinnerView(item));
            }};

    List<String> PRENOTIFICATION_LIST = Arrays.asList("", "15 min", "30 min", "1 hour", "2 hours", "6 hours", "1 day");
    ArrayList<ViewFactory.SpinnerView> PRENOTIFICATION =
            new ArrayList<ViewFactory.SpinnerView>(){{
                for (String item: PRENOTIFICATION_LIST) add(new ViewFactory.SpinnerView(item));
            }};

    List<String> FREQUENCY_LIST = Arrays.asList("", "Hourly", "Daily", "Weekly");
    ArrayList<ViewFactory.SpinnerView> FREQUENCY =
            new ArrayList<ViewFactory.SpinnerView>(){{
                for (String item: FREQUENCY_LIST) add(new ViewFactory.SpinnerView(item));
            }};

    ArrayList<ViewFactory.SpinnerView> OUTPUT_FORMAT =
            new ArrayList<ViewFactory.SpinnerView>(){{
                add(new ViewFactory.SpinnerView("Output format"));
                add(new ViewFactory.SpinnerView("mp4"));
                add(new ViewFactory.SpinnerView("3gpp"));
                add(new ViewFactory.SpinnerView("webm"));
            }};

    ArrayList<ViewFactory.SpinnerView> CAMERA_TYPE =
            new ArrayList<ViewFactory.SpinnerView>(){{
                add(new ViewFactory.SpinnerView("Camera type"));
                add(new ViewFactory.SpinnerView("back"));
                add(new ViewFactory.SpinnerView("front"));
            }};


}

