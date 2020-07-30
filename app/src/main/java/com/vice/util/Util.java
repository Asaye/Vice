
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONObject;
import org.json.JSONException;

public class Util {

	public static long getDelay(String dateString, String timeString) {
		try {
			Date now = new Date();
			DateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy kk:mm", Locale.ENGLISH);
			Date date = format.parse(dateString + " " + timeString);
			return date.getTime() - now.getTime();
		} catch (Exception e) {
		}

		return 0;
	}
	
	public static long getMillis(String str) {
		double time = 0;
		String unit = null;

		if (str.equalsIgnoreCase("Hourly")) {
			time = 1;
		  unit = "hours";
		} else if (str.equalsIgnoreCase("Daily")){
			time = 1;
		  unit = "days";
		} else if (str.equalsIgnoreCase("Weekly")) {
			time = 1;
		  unit = "weeks";
		} else {
			String[] array = str.split(" ");
			time = Double.parseDouble(array[0]);
		  unit = array[1];
		}

		if ("seconds".indexOf(unit.toLowerCase()) != -1) {
			return (long) time*1000;
		} else if ("minutes".indexOf(unit.toLowerCase()) != -1) {
			return (long) time*1000*60;
		} else if ("hours".indexOf(unit.toLowerCase()) != -1) {
			return (long) time*1000*60*60;
		} else if ("days".indexOf(unit.toLowerCase()) != -1) {
			return (long) time*1000*60*60*24;
		} else if ("weeks".indexOf(unit.toLowerCase()) != -1) {
			return (long) time*1000*60*60*24*7;
		}
		return 0;
	}

	public static JSONObject getReverses(Iterator<String> keys) {
		JSONObject object = new JSONObject();
		String key = null;

		try {
			while (keys.hasNext()) {
				key = keys.next();		

				switch (key) {
					case "VOLUME_SILENT": {					
						object.put("VOLUME_NORMAL", "");								
						break;
					}
					case "VOLUME_NORMAL": {					
						object.put("VOLUME_SILENT", "");										
						break;
					}
					case "VOLUME_VIBRATE": {					
						object.put("VOLUME_NORMAL", "");	 									
						break;
					}
					case "BLUETOOTH_ON": {				
					  object.put("BLUETOOTH_OFF", "");	
						break;
					}
					case "BLUETOOTH_OFF": {		
					 	object.put("BLUETOOTH_ON", "");
						break;
					}
					case "WIFI_ON": {		
					  object.put("WIFI_OFF", "");
						break;
					}
					case "WIFI_OFF": {	
					  object.put("WIFI_ON", "");
						break;
					}
				}
			}
			
		} catch (JSONException ex) {
		}

		return object;
	}

	public static String getPath(Uri uri, Context context) throws Exception {
		String path = URLDecoder.decode(uri.getPath(), "UTF8");

		if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
			final String[] split = path.split(":");
			String segment = uri.getLastPathSegment();
			if (segment.indexOf("primary") != -1) {
				path = Environment.getExternalStorageDirectory() + "/" + split[1];
			} else {
				path = "/storage/" + segment.split(":")[0] + "/" + split[1];
			}
		} else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
			final String id = DocumentsContract.getDocumentId(uri);
			String fileType = MediaStore.Files.FileColumns.DATA;
			uri = ContentUris.withAppendedId(
					Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
			path = getFilePath(uri, null, null, context, fileType);
		} else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
			final String docId = DocumentsContract.getDocumentId(uri);
			final String[] split = docId.split(":");
			final String type = split[0];
			String fileType = MediaStore.Files.FileColumns.DATA;
			if ("image".equals(type)) {
				uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				fileType = MediaStore.Images.Media.DATA;
			} else if ("video".equals(type)) {
				uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				fileType = MediaStore.Video.Media.DATA;
			} else if ("audio".equals(type)) {
				uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				fileType = MediaStore.Audio.Media.DATA;
			}
			path = getFilePath(uri, "_id=?", new String[]{split[1]}, context, fileType);
		}

		return path;
	}

	private static String getFilePath(Uri uri, String selection, String[] selectionArgs, Context context, String type) {
		String[] projection = { type };
		Cursor cursor = null;

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			int column_index = cursor.getColumnIndexOrThrow(type);
			if (cursor.moveToFirst()) {
				return cursor.getString(column_index);
			}
		} catch (Exception e) {
		}
		return null;
	}
}