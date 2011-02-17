package org.hogel.android.facedetect;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;

public class FaceDetectActivity extends Activity {
	private static final String PREF_CAMERA_W = "CAM_W";
	private static final String PREF_CAMERA_H = "CAM_H";

	Camera camera;
	CameraLayout preview;
	SharedPreferences prefs;
	List<Camera.Size> supportedSizes;

	private void setCameraSize(int width, int height) {
		final Camera.Parameters params = camera.getParameters();
		params.setPreviewSize(width, height);
		camera.setParameters(params);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
        camera = Camera.open();

        final int cam_w = prefs.getInt(PREF_CAMERA_W, 0);
        final int cam_h = prefs.getInt(PREF_CAMERA_H, 0);
        if (cam_w != 0 && cam_h != 0) {
        	setCameraSize(cam_w, cam_h);
        }

        preview = new CameraLayout(this, camera);
		setContentView(preview);
	}

	@Override
	protected void onPause() {
		preview.stopPreview();
		camera.release();
		camera = null;
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		supportedSizes = camera.getParameters().getSupportedPreviewSizes();
		final SubMenu sub = menu.addSubMenu(Menu.NONE, R.id.camera_sizes, Menu.NONE, "Camera Size");
        final int cam_w = prefs.getInt(PREF_CAMERA_W, 0);
        final int cam_h = prefs.getInt(PREF_CAMERA_H, 0);
		for (int i = 0, len = supportedSizes.size(); i < len; ++i) {
			Camera.Size size = supportedSizes.get(i);
			String s = size.width + "x" + size.height;
			MenuItem item = sub.add(R.id.camera_size_group, R.id.camera_size_item, i, s);
			item.setCheckable(true);
			if (size.width == cam_w && size.height == cam_h) {
				item.setChecked(true);
			}
		}
		sub.setGroupCheckable(R.id.camera_size_group, true, true);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		preview.stopPreview();
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		preview.startPreview();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.camera_size_item) {
			item.setChecked(true);

			final int order = item.getOrder();
			final Camera.Size size = supportedSizes.get(order);
			final Editor edit = prefs.edit();
			edit.putInt(PREF_CAMERA_W, size.width);
			edit.putInt(PREF_CAMERA_H, size.height);
			edit.commit();

			setCameraSize(size.width, size.height);
			preview = new CameraLayout(this, camera);
			setContentView(preview);
		}
		return true;
	}
}
