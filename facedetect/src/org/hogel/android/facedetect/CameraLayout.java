package org.hogel.android.facedetect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
//import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.FaceDetector;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraLayout extends FrameLayout implements PreviewCallback {
	private final Camera camera;
	private final YUV420toRGB8888 yuv2rgb;
	private final int[] rgbBuffer;
	private final byte[] yuvBuffer;
	private final Paint redPaint = new Paint();
	private final FaceDetector.Face[] face = new FaceDetector.Face[1];
	private final FaceDetector detector;

	private final PointF point = new PointF();
	private final CalcFPS fps = new CalcFPS();
	private final Bitmap bitmap;

	private final Camera.Size previewSize;
	private final CameraView cameraPreview;
	private final GLView paintView;
	private final float[] rect;

	public CameraLayout(Context context, Camera camera) {
		super(context);
		this.camera = camera;

		redPaint.setColor(Color.RED);
		redPaint.setStyle(Paint.Style.STROKE);

		final Camera.Parameters params = camera.getParameters();

		previewSize = params.getPreviewSize();

		final int w = previewSize.width, h = previewSize.height;
		Log.i("FaceDetect", w+"x"+h);

		detector = new FaceDetector(w, h, 1);
		rgbBuffer = new int[w * h];
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

		yuv2rgb = new YUV420toRGB8888(w, h, 2, 3, 3);

		final int yuvPerBits = ImageFormat.getBitsPerPixel(params.getPreviewFormat());
		yuvBuffer = new byte[w * h * yuvPerBits / 8];

		rect = new float[8];

		paintView = new GLView(context);
		
		cameraPreview = new CameraView(context, camera);

		addView(paintView);
		addView(cameraPreview);

		Log.d("HOGE preview", previewSize.width+"x"+previewSize.height);
	}

	public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
		private final SurfaceHolder holder;
		private final Camera camera;

		public CameraView(Context context, Camera camera) {
			super(context);

			holder = getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			this.camera = camera;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
        	try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			startPreview();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
    		//stopPreview();
			Toast.makeText(getContext(), String.format("FPS Average: %.1f", fps.average), Toast.LENGTH_SHORT).show();
		}
	}
	public class GLView extends GLSurfaceView implements GLSurfaceView.Renderer {
		protected FloatBuffer makeFloatBuffer(int length) {
			ByteBuffer bb = ByteBuffer.allocateDirect(length*4);
			bb.order(ByteOrder.nativeOrder());
			FloatBuffer fb = bb.asFloatBuffer();
			return fb;
		}

	    private final FloatBuffer buffer;
	    private final int size = 4;
	    private boolean hasFace = false;

	    public GLView(Context context) {
			super(context);
			buffer = makeFloatBuffer(size*2);
	        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			getHolder().setFormat(PixelFormat.TRANSPARENT);
			setRenderer(this);
		}

		public void face(float[] points) {
			hasFace = true;
			buffer.clear();
			buffer.put(points);
			buffer.position(0);
		}

		public void noface() {
			hasFace = false;
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			gl.glClearColor(0f, 0f, 0f, 0f);
	        gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);

	        if (hasFace) {
	        	gl.glColor4f(1f, 0f, 0f, 1.0f);
	        	gl.glVertexPointer(2, GL10.GL_FLOAT, 0, buffer);
	        	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        	gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, size);
	        }
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			//gl.glViewport(0, 0, width, height);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	        // ディザを無効化
	        //gl.glDisable(GL10.GL_DITHER);
	        // カラーとテクスチャ座標の補間精度を、最も効率的なものに指定
	        //gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

	        // バッファ初期化時のカラー情報をセット
	        //gl.glClearColor(0,0,0,1);
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		yuv2rgb.getFast(data, rgbBuffer);

		final int w = previewSize.width, h = previewSize.height;
		bitmap.setPixels(rgbBuffer, 0, w, 0, 0, w, h);
		if (detector.findFaces(bitmap, face) == 1) {
			FaceDetector.Face f = face[0];
			f.getMidPoint(point);
			float x = 2f * point.x / previewSize.width - 1f;
			float y = -2f * point.y / previewSize.height + 1f;
			float fw = 1.7f * f.eyesDistance() / previewSize.width;
			float fh = 2.2f * f.eyesDistance() / previewSize.height;
			rect[0] = x - fw; rect[1] = y - fh;
			rect[2] = x + fw; rect[3] = y - fh;
			rect[4] = x - fw; rect[5] = y + fh;
			rect[6] = x + fw; rect[7] = y + fh;
			paintView.face(rect);
		} else {
			paintView.noface();
		}

		fps.calc();
		//Log.i("FaceDetect", String.format("FPS: %.1f, ave: %.1f", fps.current, fps.average));

		camera.addCallbackBuffer(yuvBuffer);
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