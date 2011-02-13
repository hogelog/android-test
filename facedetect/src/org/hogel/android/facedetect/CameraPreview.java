package org.hogel.android.facedetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
    private SurfaceHolder holder;
	private Camera camera;
	private int[] rgbBuffer;
	private YUV420toRGB8888 yuv2rgb;
	private Size size;
	private FaceDetector detector;
	private final FaceDetector.Face[] face = new FaceDetector.Face[1];
	private final PointF point = new PointF();
	private final Paint redPaint = new Paint();
	private final CalcFPS fps = new CalcFPS();
	
	public CameraPreview(Context context) {
		super(context);

        holder = getHolder();
        holder.addCallback(this);
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
		camera.setPreviewCallback(this);
		camera.startPreview();
		size = camera.getParameters().getPreviewSize();
        yuv2rgb = new YUV420toRGB8888(size.width, size.height, 2, 3, 3);
		rgbBuffer = new int[size.width * size.height];
		detector = new FaceDetector(size.width, size.height, 1);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
    	camera.setPreviewCallback(null);
    	camera.stopPreview();
    	camera.release();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		camera.setPreviewCallback(null);

		final Canvas canvas = holder.lockCanvas();

		final int w = size.width, h = size.height;
		yuv2rgb.getFast(data, rgbBuffer);

		canvas.drawBitmap(rgbBuffer, 0, w, 0, 0, w, h, false, null);
		final Bitmap bmp = Bitmap.createBitmap(rgbBuffer, 0, w, w, h, Bitmap.Config.RGB_565);
		if (detector.findFaces(bmp, face) == 1) {
			Face f = face[0];
			f.getMidPoint(point);
			float x = point.x, y = point.y;
			float fw = f.eyesDistance() * (float)0.7, fh = f.eyesDistance() * (float)1.2;
			canvas.drawRect(x-fw, y-fh, x+fw, y+fh, redPaint);
		}
		holder.unlockCanvasAndPost(canvas);

		Log.i("FaceDetector", "FPS: "+fps.calc());

		camera.setPreviewCallback(this);
	}
}
