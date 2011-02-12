package org.hogel.android.cameratest;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private Camera camera;
	
	public CameraPreview(Context context) {
		super(context);

        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
        try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			camera.release();
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
    	camera.stopPreview();
    	camera.release();
	}
}