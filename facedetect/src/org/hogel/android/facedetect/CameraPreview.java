package org.hogel.android.facedetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.FaceDetector;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
    private final SurfaceHolder holder;
	private final Bitmap bitmap;
	private final int[] rgbBuffer;
	private final byte[] yuvBuffer;
	private final YUV420toRGB8888 yuv2rgb;
	private final FaceDetector detector;
	private final Camera camera;
	private final FaceDetector.Face[] face = new FaceDetector.Face[1];
	private final PointF point = new PointF();
	private final Paint redPaint = new Paint();
	private final CalcFPS fps = new CalcFPS();

	private final Camera.Size size;

	public CameraPreview(Context context, Camera camera) {
		super(context);

        holder = getHolder();
        holder.addCallback(this);

        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);

        this.camera = camera;

		final Camera.Parameters params = camera.getParameters();
		size = params.getPreviewSize();

        final int w = size.width, h = size.height;
        Log.i("FaceDetect", w+"x"+h);

		detector = new FaceDetector(w, h, 1);
		rgbBuffer = new int[w * h];
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

        yuv2rgb = new YUV420toRGB8888(w, h, 2, 3, 3);

        final int yuvPerBits = ImageFormat.getBitsPerPixel(params.getPreviewFormat());
		yuvBuffer = new byte[w * h * yuvPerBits / 8];
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
    	//stopPreview();

    	Toast.makeText(getContext(), String.format("FPS Average: %.1f", fps.average), Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		stopPreview();

		final Canvas canvas = holder.lockCanvas();

		final int w = size.width, h = size.height;
		yuv2rgb.getFast(data, rgbBuffer);

		canvas.drawBitmap(rgbBuffer, 0, w, 0, 0, w, h, false, null);
		bitmap.setPixels(rgbBuffer, 0, w, 0, 0, w, h);
		if (detector.findFaces(bitmap, face) == 1) {
			FaceDetector.Face f = face[0];
			f.getMidPoint(point);
			float x = point.x, y = point.y;
			float fw = f.eyesDistance() * (float)0.7, fh = f.eyesDistance() * (float)1.2;
			canvas.drawRect(x-fw, y-fh, x+fw, y+fh, redPaint);
		}
		holder.unlockCanvasAndPost(canvas);

		fps.calc();
		Log.i("FaceDetect", String.format("FPS: %.1f, ave: %.1f", fps.current, fps.average));

		startPreview();
	}

	public void startPreview() {
		camera.addCallbackBuffer(yuvBuffer);
		camera.setPreviewCallbackWithBuffer(this);
		camera.startPreview();
	}

	public void stopPreview() {
		camera.setPreviewCallbackWithBuffer(null);
		camera.stopPreview();
	}
}
