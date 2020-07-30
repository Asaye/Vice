
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import com.vice.util.Util;

import android.R;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;

import android.net.Uri;
import android.net.wifi.WifiManager;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.bluetooth.BluetoothAdapter;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.text.SimpleDateFormat;

public class WorksExecutor extends Worker {

    private MediaRecorder mMediaRecorder = null;
    private JSONObject mRequestData = null;    

    private MediaRecorder.OnInfoListener mRecorderListener = new MediaRecorder.OnInfoListener() { 
        @Override
        public void onInfo(MediaRecorder mr, int response, int extra) {
            if (response == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                response == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED ) {
                closeRecorder();
            }
        }
    }; 

    public WorksExecutor(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    } 

    @Override
    public Result doWork() {

        try {        
            Context context = getApplicationContext(); 
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            String requestId = getId().toString();           
            String criteria = "requestId = '" + requestId + "'";
            JSONArray array = dbHelper.getData("data", criteria, null);

            if (array == null) {
                dbHelper.setName("Periodic");
                array = dbHelper.getData("data", criteria, null);
            }

            if (array == null) return Result.failure();
            
            String data = array.getString(0);
            String key = null, stage = null;
            JSONObject options = null;
            Iterator<String> keys = null;
            JSONObject json = new JSONObject(data);
            JSONObject object = new JSONObject(json.optString("Tasks"));            
            
            if (object == null) return Result.failure();

            keys = object.keys();                  
            stage = json.optString("_stage");

            String periodic = null;
            String frequency = json.optString("Frequency");
            
            if (stage.equalsIgnoreCase("alert")) {             
                long delay = Util.getMillis(json.optString("Prenotification"));

                if (frequency != null && frequency.length() > 0) {
                    periodic = "ALERT";
                }

                json.put("_stage", "ontime");
                json.put("_delay", delay);
                notify(json);
                WorksScheduler.scheduleWork(context, json, "update", periodic);

                return Result.success();
            } else if (stage.equalsIgnoreCase("ontime")) {
                String duration = json.optString("Duration");                

                if (frequency != null && frequency.length() > 0) {
                    periodic = "ONTIME";
                }
                
                if ((duration != null && duration.length() > 0) || periodic != null) {
                    json.put("_stage", "reverse");
                    if (duration != null && duration.length() > 0) {
                        long delay = Util.getMillis(duration);                        
                        json.put("_delay", delay);  
                    }                  
                    WorksScheduler.scheduleWork(context, json, "update", periodic);
                }
            } else if (stage.equalsIgnoreCase("reverse")) {
                keys = Util.getReverses(keys).keys();

                if (frequency != null && frequency.length() > 0) {
                    periodic = "REVERSE";
                }

                if (periodic != null) {                                        
                    WorksScheduler.scheduleWork(context, json, "update", periodic);
                }
            }
        
            while (keys.hasNext()) {
                key = keys.next();
                options = object.optJSONObject(key);
                switch (key) {
                    case "SET_ALARM": {
                        notify(json);      
                        break;      
                    }
                    case "CALL_NUMBER": {                   
                        String number = options.optString("NUMBER");
                        Intent intent = new Intent(Intent.ACTION_CALL); 
                        intent.setData(Uri.parse("tel:" + number));
                        context.startActivity(intent);
                        break;
                    }
                    case "SEND_SMS": {
                        String number = options.optString("NUMBER");
                        String msg = options.optString("MESSAGE");
                        SmsManager smsManager = SmsManager.getDefault();
                        ArrayList<String> message = smsManager.divideMessage(msg); 
                        smsManager.sendMultipartTextMessage(number, null, message, null, null);                 
                        break;
                    }
                    case "VOLUME_SILENT": {                 
                        AudioManager audioManager = (AudioManager) 
                                     context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);                                    
                        break;
                    }
                    case "VOLUME_NORMAL": {                 
                        AudioManager audioManager = (AudioManager) 
                                     context.getSystemService(Context.AUDIO_SERVICE);                   
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);                                    
                        break;
                    }
                    case "VOLUME_VIBRATE": {                    
                        AudioManager audioManager = (AudioManager) 
                                     context.getSystemService(Context.AUDIO_SERVICE);                   
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);                                   
                        break;
                    }
                    case "BLUETOOTH_ON": {              
                        BluetoothAdapter.getDefaultAdapter().enable();
                        break;
                    }
                    case "BLUETOOTH_OFF": {     
                        BluetoothAdapter.getDefaultAdapter().disable();   
                        break;
                    }
                    case "WIFI_ON": {       
                        ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                                            .setWifiEnabled(true);  
                        break;
                    }
                    case "WIFI_OFF": {  
                        ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                                            .setWifiEnabled(false);     
                        break;
                    }
                    case "FILE_OPEN": { 
                        new Files(context).open(options.optString("SOURCE"));
                        break;
                    }
                    case "FILE_DOWNLOAD": { 
                        Uri uri = Uri.parse(options.optString("SOURCE"));
                        String destination = options.optString("DESTINATION");
                        if (destination.indexOf("file://") == -1) {
                             destination = "file://" + destination;
                        }           
                        
                        Request request = new Request(uri);
                        Query q = new Query();
                        DownloadManager downloadmanager = (DownloadManager) 
                                        context.getSystemService(Context.DOWNLOAD_SERVICE);     
                        request.setDescription("Downloading");
                        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        q.setFilterById(downloadmanager.enqueue(request));
                        Cursor c = downloadmanager.query(q);

                        if (c.moveToFirst()) {
                            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {          
                                int index = c.getColumnIndex(DownloadManager.COLUMN_TITLE);                  
                                String title = c.getString(index);                                
                                request.setDestinationUri(Uri.parse(destination  + "/" + title));                             
                            }
                        }
                        break;
                    }
                    case "FILE_UPLOAD": {   
                        String src = options.optString("SOURCE");
                        String dest = options.optString("DESTINATION");
                        new Files(context).upload(src, dest);
                        break;
                    }
                    case "RECORD_AUDIO": {
                        mRequestData = options;
                        startRecording();   
                        break;
                    }
                    case "TAKE_PICTURES": {
                        Intent intent = new Intent(context, ImageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("recorderData", options.toString());
                        context.startActivity(intent);
                        break;
                    }
                    case "RECORD_VIDEO": {
                        Intent intent = new Intent(context, VideoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("recorderData", options.toString());
                        context.startActivity(intent);
                        break;
                    }   
                }
            }

            return Result.success();    
        } catch(Exception ex) {
            return Result.failure(); 
        } 
    }

    void notify(JSONObject json) {
        int id = (int) (Math.random() * 100000);
        Context context = getApplicationContext();
        NotificationManager nm = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Builder builder = new Builder(context, json.optString("Id"))
        .setSmallIcon(R.mipmap.sym_def_app_icon)
        .setVibrate(new long[]{100, 250})
        .setContentTitle(json.optString("Name"))
        .setContentText(json.optString("Description"))
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .setPriority(Notification.PRIORITY_MAX);      
        
        nm.notify(id, builder.build());     
    }

    void startRecording() {
        
        try {               
            String maxDuration, maxFileSize, path, temp = "mp4";    
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());       
            int maxF = 0, maxD = 0, outputFormat = MediaRecorder.OutputFormat.MPEG_4;
            
            mMediaRecorder = new MediaRecorder();               

            temp = mRequestData.optString("OUTPUT_FORMAT");
            path = mRequestData.optString("OUTPUT_FOLDER");
            
            if (path == null) {
                File file = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                path = file.getPath();
            }
            
            if (temp != null && temp.toLowerCase() == "3gpp") {
                outputFormat = MediaRecorder.OutputFormat.THREE_GPP;
            } else if (temp != null && temp.toLowerCase() == "webm") {
                outputFormat = MediaRecorder.OutputFormat.WEBM;
            } 
          
            maxDuration = mRequestData.optString("MAX_DURATION", "0");
            maxFileSize = mRequestData.optString("MAX_FILE_SIZE", "0"); 
            maxD = (int) Double.parseDouble(maxDuration);
            maxF = (int) Double.parseDouble(maxFileSize);       
            
            if (maxF == 0 && maxD == 0) {
                maxD = 5;
            }  

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setOutputFormat(outputFormat);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);            
            mMediaRecorder.setOutputFile(path + "/" + timeStamp + "." + temp.toLowerCase());     
        
            if (maxD > 0) {
                mMediaRecorder.setMaxDuration(maxD*60*1000);
            }

            if (maxF > 0) {
                mMediaRecorder.setMaxFileSize(maxF*1024*1024);
            }

            mMediaRecorder.setOnInfoListener(mRecorderListener); 
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception ex) {        
             closeRecorder();
        }
    }

    private void closeRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }            
    }
}  