package fr.steren.camerasensors;

import java.io.PrintWriter;
import android.hardware.SensorEvent;


public class SensorRecord {
	
	private static final int SIZE = 256;
	
	private int		mAccuracyArray[] 	= new int[SIZE];
	private long	mTimeStamps[] 		= new long[SIZE];
	private float	mXValues[] = new float[SIZE];
	private float	mYValues[] = new float[SIZE];
	private float	mZValues[] = new float[SIZE];
	
	private int 	mIterator;

	public SensorRecord() {
		mIterator = 0;
	}
	
	public boolean add(SensorEvent event) {
		if(mIterator < SIZE) {
			mAccuracyArray[mIterator] = event.accuracy;
			mTimeStamps[mIterator] = event.timestamp;
			mXValues[mIterator] = event.values[0];
			mYValues[mIterator] = event.values[1];
			mZValues[mIterator] = event.values[2];
			mIterator++;
			return true;
		} else {
			return false;
		}
	}

	public void writeRecord(PrintWriter pw) {
		for(int i=0; i<SIZE; i++) {
			//pw.println(mAccuracyArray[i]);
			pw.print(mTimeStamps[i]);
			pw.print(" ");
			pw.print(mXValues[i]);
			pw.print(" ");
			pw.print(mYValues[i]);
			pw.print(" ");
			pw.print(mZValues[i]);
			pw.println();
		}
	}

}
