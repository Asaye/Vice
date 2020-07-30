
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.vice.util.Callback;
import com.vice.util.Util;

public class WorksScheduler  {

    private Context mContext;
    private static Callback mCallback;

    public WorksScheduler(Context context) {
        mContext = context;
    }

    public void schedule(String data) {
        try {
            scheduleWork(mContext, new JSONObject(data), "save", null);
        } catch (Exception ex) {
        }                
    }


    public void update(String data, Callback callback) {
        try {
            mCallback = callback;
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            JSONObject json = new JSONObject(data);
            long id = json.optLong("_id");     
            JSONArray array = dbHelper.getData("requestId", "id = " + id, null);

            if (array == null) {
                mCallback.send("Error", "No data could be retrieved from the database.");
                return;
            }

            String reqId = array.getString(0);

            if (reqId == null) {
                callback.send("Error", "No scheduled work with the given id can be found.");
                return;
            }

            String frequency = json.optString("Frequency");

            if (frequency != null && frequency.length() > 0) {
                cancelPeriodic(dbHelper, "id=" + id);
            }

            UUID requestId = UUID.fromString(reqId);
            WorkManager.getInstance().cancelWorkById(requestId); 
            scheduleWork(mContext, json, "update", null);
        } catch(Exception ex) {
            callback.send("Error", ex.getMessage());
        }   
    }

    public void cancel(long[] ids, Callback callback) {
        mCallback = callback;
        try {
            int len = ids.length;
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            String criteria;
            int counter = 0;
            for (int i = 0; i < len; i++) {
                criteria = "id = " + ids[i];
                JSONArray array = dbHelper.getData("requestId", criteria, null);
                if (array != null) {
                    String reqId = array.getString(0);
                    UUID requestId = UUID.fromString(reqId);
                    WorkManager.getInstance().cancelWorkById(requestId);
                    cancelPeriodic(dbHelper, criteria);
                    dbHelper.setName("Tasks");
                    dbHelper.deleteData(criteria);
                }
                counter++;
            }

            if (counter == len) {
                mCallback.send("Error", true);
            } else {
                mCallback.send("Error", "Error");
            }
        } catch (Exception ex) {
            callback.send("Error", ex.getMessage());
        }
    }


    public void cancel(String criteria, Callback callback) {
        mCallback = callback;

        try {
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            JSONArray array = dbHelper.getData("requestId", criteria, null);
            if (array == null) {
                callback.send("Error", "No data with the given id could not be retrieved.");
            }
            String reqId = array.getString(0);
            UUID requestId = UUID.fromString(reqId);
            WorkManager.getInstance().cancelWorkById(requestId);  
            cancelPeriodic(dbHelper, criteria);
            dbHelper.setName("Tasks");
            String res = dbHelper.deleteData(criteria);
            if (res == null) {
                mCallback.send("Success", null);
            } else {
                mCallback.send("Error", res);
            }
        } catch (Exception ex) {
            callback.send("Error", ex.getMessage());
        }
    }


    public void cancelAll(String tag) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            dbHelper.deleteTable();
            dbHelper.setName("Periodic");
            dbHelper.deleteTable();
            //WorkManager.getInstance().cancelAllWorkByTag(tag);
            WorkManager.getInstance().cancelAllWork();
        } catch (Exception ex) {
            throw new Error(ex.getMessage());
        }
    }  

    public void reset(String name) {
        WorkManager.getInstance().cancelAllWork();
        mContext.deleteDatabase(name);
    }

    private void cancelPeriodic(DatabaseHelper dbHelper, String criteria) {
        WorkManager wManager = WorkManager.getInstance();
                
        dbHelper.setName("Periodic");
        JSONArray resArray = dbHelper.getData("requestId", criteria, null);
        if (resArray != null) {
            String res;
            UUID uuid;
            for (int i = 0; i < resArray.length(); i++) {
                res = resArray.optString(i);
                uuid = UUID.fromString(res);
                wManager.cancelWorkById(uuid); 
            }
        }  
    }

    static void scheduleWork(Context context, JSONObject json, String command, String periodic) {
        try {

            long id = json.optLong("_id");
            String uuid = null;
            DatabaseHelper dbHelper = new DatabaseHelper(context);
                           
            long delay = json.optLong("_delay");
            String _stage = json.optString("_stage");  
            if (_stage.equalsIgnoreCase("alert")) {
                String alert = json.optString("Prenotification");
                if (alert != null && alert.length() > 0) {          
                    long time = Util.getMillis(alert);
                    delay -= time;
                }
            }
            WorkRequest.Builder builder = new OneTimeWorkRequest.Builder(WorksExecutor.class);
            ((OneTimeWorkRequest.Builder) builder).setInitialDelay(delay, TimeUnit.MILLISECONDS);
            //builder.addTag(id)
            WorkRequest request = builder.build(); 
            uuid = request.getId().toString();
            WorkManager.getInstance().enqueue(request);
            String save_res = null;
            int update_res = -1;

            if (command.equalsIgnoreCase("save")) {             
                save_res = dbHelper.save(id, json.toString(), uuid);
            } else {                
                update_res = dbHelper.update(id, json.toString(), uuid);
            } 

            if (periodic != null) {               
                dbHelper.setName("Periodic");
                json.put("_stage", "periodic");
                
                String frequency = json.optString("Frequency");                
                long interval = Util.getMillis(frequency);
                builder = 
                    new PeriodicWorkRequest.Builder(WorksExecutor.class, interval, TimeUnit.MILLISECONDS, 60000, TimeUnit.MILLISECONDS);
                if (periodic.equalsIgnoreCase("ALERT")) {                    
                    json.put("Tasks", "{ SET_ALARM: {} }");
                } else if (periodic.equalsIgnoreCase("REVERSE")) {
                    JSONObject object = new JSONObject(json.optString("Tasks"));  
                    JSONObject reverses = Util.getReverses(object.keys());
                    json.put("Tasks", reverses.toString());
                }
                //builder.addTag(id)
                request = builder.build();
                uuid = request.getId().toString();
                WorkManager.getInstance().enqueue(request);

                String res = dbHelper.save(id, json.toString(), uuid);
                if (res == null) {
                    mCallback.send("Success", null);
                } else {
                    mCallback.send("Error", res);
                }
                return;
            }
            if (update_res > 0 || save_res == null) {
                mCallback.send("Success", null);
            } else {
                mCallback.send("Error", save_res);
            }
        } catch (Exception ex) {
            mCallback.send("Error", ex.getMessage());
        }
    }  
}
 