package org.hogel.android.recordandplay;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class RecordAndPlay extends Activity {
	private final MediaRecorder recorder = new MediaRecorder();
	private final MediaPlayer player = new MediaPlayer();
	private boolean onRecord = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final String path = "/sdcard/recordandplay.3gp";

		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(path);
		try {
			recorder.prepare();
			player.setDataSource(path);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void clickRecord(View v) {
		if (!onRecord) {
			recorder.start(); // Recording is now started
			Toast.makeText(this, "Start Recording", Toast.LENGTH_SHORT).show();
			onRecord = true;
		}
	}

	public void clickStop(View v) {
		recorder.stop();
		recorder.reset(); // You can reuse the object by going back to
		Toast.makeText(this, "Stop Recording", Toast.LENGTH_SHORT).show();
		onRecord = false;
	}

	public void clickPlay(View v) {
		if (!onRecord) {
			try {
				player.prepare();
				player.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}