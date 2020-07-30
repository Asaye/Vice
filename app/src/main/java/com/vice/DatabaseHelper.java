
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {

    private String mTable = "Tasks";

    public DatabaseHelper(Context context) {
        super(context, "Vice", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String constraint = this.mTable.equals("Tasks") ? "PRIMARY KEY" : "FOREIGN KEY";
        String sql = "CREATE TABLE IF NOT EXISTS " + this.mTable +
                     " (id INTEGER " + constraint + ", requestId TEXT UNIQUE, data TEXT);";  
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {        
        onCreate(db);
    } 

    public void setName(String table) {
        this.mTable = table;
    }

    public String save(long id, String data, String requestId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues(3);
            String sql = "CREATE TABLE IF NOT EXISTS " + this.mTable;

            if (this.mTable.equals("Tasks")) {
                sql = sql + " (id INTEGER PRIMARY KEY, requestId TEXT UNIQUE, data TEXT);";
            } else {
                sql = sql + " (id INTEGER NOT NULL, requestId TEXT UNIQUE, data TEXT, FOREIGN KEY (id) " +
                        "REFERENCES Tasks (id) ON DELETE CASCADE);";
            }

            values.put("id", id);
            values.put("data", data);
            values.put("requestId", requestId);

            db.execSQL(sql);
            db.insert(this.mTable, null, values);
            db.close();
        } catch (Exception e) {
            return e.getMessage();
        }

        return null;
    } 

    public int update(long id, String data, String requestId) {
        SQLiteDatabase db = this.getWritableDatabase();        
        ContentValues values = new ContentValues(); 
        values.put("data", data);
        if (requestId != null) {
            values.put("requestId", requestId);
        }       

        int result = db.update(this.mTable, values, "id = " + id, null);
        db.close();

        return result;
    }

    public String addColumn(String name, String column, String val) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL(
                "ALTER TABLE " + name + 
                " ADD COLUMN " + column + 
                " TEXT DEFAULT " + val
            );
        } catch (Exception ex) {
            return ex.getMessage();
        }
        db.close();

        return null;
    } 

    public String deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL("DROP TABLE IF EXISTS " + this.mTable);
        } catch (Exception ex) {            
            return ex.getMessage();
        }
        db.close();

        return null;
    }  

    public String deleteData(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int status = db.delete(this.mTable, "id = " + id, null);

        if (status == -1) {
            return "Error occured.";
        }
        db.close();
        return null;
    }  

    public String deleteData(String criteria) {
        SQLiteDatabase db = this.getWritableDatabase();
        int status = db.delete(this.mTable, criteria, null);

        if (status == -1) {
            return "Error occured.";
        }
        db.close();
        return null;
    }

    public String deleteData(String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        int status = db.delete(this.mTable, null, whereArgs);

        if (status == -1) {
            return "Error occured.";
        }
        db.close();
        return null;
    }

    public JSONArray getData(String cols, String criteria, String order) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (db == null) return null;

        Cursor res = null;
        try {
            if (criteria != null && criteria.length() == 0) {
                criteria = null;
            }
            res =  db.query(this.mTable, cols.split(","), criteria, null, null, null, order);
        } catch(Exception ex) {
            String msg = ex.getMessage();
            if (msg.indexOf("no such table") != -1) {
                return null;
            }
        }  


        JSONArray array = null;
        if (res != null && res.moveToFirst()) {
            array = new JSONArray();
            String[] columns = res.getColumnNames();
            String val = "";
            do {
                for (String col : columns) {
                   val = res.getString(res.getColumnIndex(col));
                   array.put(val);
                }
            } while (res.moveToNext());
            res.close();
        }         
        db.close();

        return array;
    }

    public JSONArray getData(String cols, String[] selectionArgs, String order) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (db == null) return null;

        Cursor res = null;
        try {
            res =  db.query(this.mTable, cols.split(","), null, selectionArgs, null, null, order);
        } catch(Exception ex) {
            String msg = ex.getMessage();
            if (msg.indexOf("no such table") != -1) {
                return null;
            }
        }


        JSONArray array = null;
        if (res != null && res.moveToFirst()) {
            array = new JSONArray();
            String[] columns = res.getColumnNames();
            String val = "";
            do {
                JSONObject map = new JSONObject();
                for (String col : columns) {
                    val = res.getString(res.getColumnIndex(col));
                    array.put(val);
                }
            } while (res.moveToNext());
            res.close();
        }
        db.close();

        return array;
    }
}
