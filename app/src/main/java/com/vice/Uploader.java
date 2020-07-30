
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import java.lang.Exception;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.File;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.lang.Runnable;

public class Uploader implements Runnable {

	private static final String TAG = "FILE_UPLOAD_ERROR";

	private String mDestination;
	private String mFilePath;

	public Uploader(String src, String dest) {
		mFilePath = src;
		mDestination = dest;
	}

	@Override
	public void run() {
		
        HttpURLConnection conn = null;
        DataOutputStream output = null;  
        FileInputStream input = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        try {
        	URL url = new URL(mDestination);
        	//URL url = new URL("http://192.168.42.225:8080/FileDownloader/rest/transfer/upload");
			File file = new File(mFilePath);
			input = new FileInputStream(file);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true); 
			conn.setDoOutput(true); 
			conn.setUseCaches(false); 
			conn.setChunkedStreamingMode(0);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("file", file.getName());

			output = new DataOutputStream(conn.getOutputStream());
			output.writeBytes(twoHyphens + boundary + lineEnd);
			output.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
			                         + file.getName() + "\"" + lineEnd);
			output.writeBytes(lineEnd);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = input.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}           

			output.writeBytes(lineEnd);
			output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			output.flush();
			conn.getResponseMessage();
			conn.getResponseCode();
		} catch (Exception ex) {
		} finally {
			if (conn != null) conn.disconnect();
			try {
				if (input != null) input.close();		
				if (output != null) output.close();
			} catch (Exception e) {
			}
		}
	}
}