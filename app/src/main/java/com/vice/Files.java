
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import com.vice.util.Util;

import java.lang.Exception;
import java.io.File;

public class Files  {

    private Context mContext;  
    private String mPackageName;
    
    public Files(Context context) {
        mContext = context;
        mPackageName = mContext.getPackageName() + ".provider";
    }
 
    void open(String url) {
        try {
            File file = new File(url);
            Uri uri = FileProvider.getUriForFile(mContext, mPackageName, file);

            if (uri.getScheme().equalsIgnoreCase("file")) {
                openFile(uri);
            } else {
                openProvidedFile(uri);
            }
        } catch(Exception ex) {
        }
    }  
    

 
    void upload(String src, String dest) {       
       try {
        File file = new File(src);
        Uri uri = Uri.fromFile(file);  

        if (uri != null) {
            String path = file.getPath();

            if (!file.isFile() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    
                    Uri fpUri = FileProvider.getUriForFile(mContext, mPackageName, file);
                    mContext.grantUriPermission(mPackageName, fpUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    path = Util.getPath(fpUri, mContext);
                    file = new File(path);
                } catch (Exception ex) {
                }
            }

            HandlerThread thread = new HandlerThread("FileUpload");
            thread.start();
            Handler handler = new Handler(thread.getLooper());     
            handler.post(new Uploader(path, dest));
        } 
        } catch(Exception e) {
       }   
    }

    private void openProvidedFile(Uri uri) {
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String mimeType = null;
        mContext.grantUriPermission(mPackageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |  Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        mimeType = mContext.getContentResolver().getType(uri);

        intent.setDataAndType(uri, mimeType);
        
        if (intent.resolveActivity(pm) != null) {
            mContext.startActivity(intent);
        }
    }

    private void openFile(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
 
        String url = uri.toString();
                
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            intent.setDataAndType(uri, "application/msword");
        } else if(url.toString().contains(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(url.toString().contains(".zip") || url.toString().contains(".rar")) {
            intent.setDataAndType(uri, "application/x-wav");
        } else if(url.toString().contains(".rtf")) {
            intent.setDataAndType(uri, "application/rtf");
        } else if(url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.toString().contains(".gif")) {
            intent.setDataAndType(uri, "image/gif");
        } else if(url.toString().contains(".jpg") || url.toString().contains(".jpeg") || 
                  url.toString().contains(".png")) {
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.toString().contains(".txt")) {
            intent.setDataAndType(uri, "text/plain");
        } else if(url.toString().contains(".3gp") || url.toString().contains(".mpg") || 
            url.toString().contains(".mpeg") || url.toString().contains(".mpe") || 
            url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        PackageManager pm = mContext.getPackageManager();

        if (intent.resolveActivity(pm) != null) {
            mContext.startActivity(intent);
        }
    }
}
