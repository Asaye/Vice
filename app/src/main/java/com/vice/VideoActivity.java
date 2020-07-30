
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.view.Surface;
import android.app.Activity;
import android.os.HandlerThread;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;
import android.graphics.SurfaceTexture;
import android.widget.Toast;
import android.os.Environment;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat; 
import java.io.File;

public class VideoActivity extends CameraActivity  {

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private MediaRecorder.OnInfoListener mRecorderListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int response, int extra) {
            if ( response == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                 response == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED ) {
                stopRecordingVideo();
            }
        }
    };
   
    protected void setUpCameraOutputs(int width, int height) {
        try {
          String cameraId = getCameraId();
          CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
          StreamConfigurationMap map = characteristics
                  .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
          mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
          if (map == null) {
              throw new RuntimeException("Cannot get available preview/video sizes");
          }
          mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
          mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

          int orientation = getResources().getConfiguration().orientation;
          if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
              mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
          } else {
              mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
          }
          configureTransform(width, height);
          mMediaRecorder = new MediaRecorder();
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
              mCameraManager.openCamera(cameraId, mStateCallback, null);
          }
      } catch(Exception ex) {
      }
    }

    void start() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }

        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();
            
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);
            
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);
            final Activity activity = this;

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mSession = cameraCaptureSession;
                    updatePreview();
                    	runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
			                    	mMediaRecorder.start();
	                        }
	                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
        }
    }
    
    private void setUpMediaRecorder() throws IOException {
         
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        String outputFormat = mRequestData.optString("OUTPUT_FORMAT"), ext=".mp4";
        if (outputFormat != null && outputFormat.equalsIgnoreCase("3gpp")) {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            ext = ".3gpp";
        } else if (outputFormat != null && outputFormat.equalsIgnoreCase("webm")) {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.WEBM);
            ext = ".webm";
        } else {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        }

        String path = mRequestData.optString("OUTPUT_FOLDER");
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        if (path == null) {
           File file = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
           path = file.getPath();
        }
        
        mMediaRecorder.setOutputFile(path + "/" + timeStamp + ext);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); 
			  mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

        String maxDuration = mRequestData.optString("MAX_DURATION");
        String maxFileSize = mRequestData.optString("MAX_FILE_SIZE");
        double maxD = 0, maxF = 0;

        if (maxDuration != null && maxDuration.length() > 0) {
            maxD = Double.parseDouble(maxDuration);
        }
        if (maxFileSize != null && maxFileSize.length() > 0) {
            maxF = Double.parseDouble(maxFileSize);
        }

        if (maxF == 0 && maxD == 0) {
            maxD = 5;
        }  
            
        if (maxD > 0) {
            mMediaRecorder.setMaxDuration((int) (maxD*60*1000));
        }

        if (maxF > 0) {
            mMediaRecorder.setMaxFileSize((int) (maxF*1024*1024));
        }

        mMediaRecorder.setOnInfoListener(mRecorderListener);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    } 
    
    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    protected void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);
            final Activity activity = this;

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
        }
    }

	  void stopRecordingVideo() {
        if (mMediaRecorder != null) {
        	mMediaRecorder.stop();
        	mMediaRecorder.reset();
        }        
        exit();       
    }

    private void closePreviewSession() {
        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
    }

	private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
        }
    }	
}
