
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Handler;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;
import android.util.Size;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;

import org.json.JSONObject;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.lang.InterruptedException;

public class CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener  {

	protected static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
   
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

	protected AutoFitTextureView mTextureView = null;	
	protected CameraDevice mCameraDevice;
	protected MediaRecorder mMediaRecorder = null;
	protected CameraManager mCameraManager = null;
	protected CameraCaptureSession mSession = null;
	protected CaptureRequest.Builder mPreviewBuilder = null;
	protected Size mPreviewSize, mVideoSize;
	protected Integer mSensorOrientation;
	protected JSONObject mRequestData;	
	protected HandlerThread mBackgroundThread;
	protected Handler mBackgroundHandler;
	protected Semaphore mCameraOpenCloseLock = new Semaphore(1);

	protected CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }
    };


	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

    	try {
            String data = getIntent().getStringExtra("recorderData");
    		mRequestData = new JSONObject(data);
            setContentView(com.vice.R.layout.video);
    	} catch(Exception ex) {
    	}
    }

    @Override
    public void onStart() {
	    super.onStart();
        mTextureView = findViewById(com.vice.R.id.texture);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                start();
            }
        }, 3000);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(this);
        }
    }

	@Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                          int width, int height) {
        openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                            int width, int height) {
        configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }    

    protected void openCamera(int width, int height) {
      try {           
        if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
          throw new RuntimeException("Time out waiting to lock camera opening.");
        }
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        setUpCameraOutputs(width, height);   
      } catch (Exception e) {
          finish();
      }    
    }

    protected String getCameraId() throws CameraAccessException {
    	String cameraId = mCameraManager.getCameraIdList()[0];    	
    	String type = mRequestData.optString("CAMERA_TYPE");
    	String cameraType = type == null ? "BACK" : type;    	

    	CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);				
			Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
			
			if (facing != null) {
				if (facing == CameraCharacteristics.LENS_FACING_FRONT && cameraType.equalsIgnoreCase("BACK") || 
					facing == CameraCharacteristics.LENS_FACING_BACK && cameraType.equalsIgnoreCase("FRONT")) {
					cameraId = mCameraManager.getCameraIdList()[1];
				} 
			}
			return cameraId;
    }

    void start() { }
    
    protected void startPreview() { }
    
    protected void setUpCameraOutputs(int width, int height) { }
    
    protected static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }    

    protected void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    void exit() {
        if (mCameraOpenCloseLock != null) {
            mCameraOpenCloseLock.release();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                closeCamera();
                stopBackgroundThread();
                finish();
            }
        }, 3000);
    }

    private void closePreviewSession() {
        if (mSession != null) {
            try {
                mSession.close();
                mSession = null;
            } catch(Exception e) {
            }
        }
    }	

	private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

	private void stopBackgroundThread() {
		if (null != mBackgroundThread) {
			mBackgroundThread.quitSafely();
            mBackgroundThread = null;
            mBackgroundHandler = null;
		}
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}
