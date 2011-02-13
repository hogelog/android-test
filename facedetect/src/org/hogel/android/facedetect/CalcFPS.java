package org.hogel.android.facedetect;

public class CalcFPS {
	private long frames = 0;
	private long start = 0;
	private long prev = 0;

	public double current = 0;
	public double average = 0;

	public CalcFPS() {
		prev = start = System.currentTimeMillis();
	}

	public void calc() {
		long now = System.currentTimeMillis();
		++frames;
		current = 1000.0 / (now - prev);
		average = frames * 1000.0 / (now - start);
		prev = now;
	}
}
