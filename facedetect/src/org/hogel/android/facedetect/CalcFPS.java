package org.hogel.android.facedetect;

public class CalcFPS {
	private long prev = 0;
	public CalcFPS() {
		prev = System.currentTimeMillis();
	}
	public double calc() {
		long current = System.currentTimeMillis();
		double fps = 1000.0 / (current - prev);
		prev = current;
		return fps;
	}
}
