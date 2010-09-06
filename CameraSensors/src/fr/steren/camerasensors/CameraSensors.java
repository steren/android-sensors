package fr.steren.camerasensors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.widget.Toast;

public class CameraSensors extends Activity implements SensorEventListener, View.OnClickListener {

	private SensorRecord mOrientationRecord = new SensorRecord();
	private SensorRecord mAccelerationRecord = new SensorRecord();
	
	private SensorManager mSensorManager;
	
	/** is recording ? */
	private boolean mRecording;
	/** has been saved ?*/
	private boolean mSaved;
	/** Uris of the savedfiles **/
	private Uri mFileUri;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mRecording = false;
        mSaved = false;
        
        //get the sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        //set click listeners to buttons
        View b;
        b = (View) findViewById(R.id.button_record);
        b.setOnClickListener(this);
        b = (View) findViewById(R.id.button_pause);
        b.setOnClickListener(this);
        b = (View) findViewById(R.id.button_stop);
        b.setOnClickListener(this);
        b = (View) findViewById(R.id.button_save);
        b.setOnClickListener(this);
        b = (View) findViewById(R.id.button_send);
        b.setOnClickListener(this);
        
        /*Button but;
        but = (Button) findViewById(R.id.button_record);
        but.isEnabled();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register the sensors
        mSensorManager.registerListener( (SensorEventListener) this,
        		mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        		SensorManager.SENSOR_DELAY_FASTEST );
        
        mSensorManager.registerListener( (SensorEventListener) this,
        		mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        		SensorManager.SENSOR_DELAY_FASTEST );
    }
    
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener((SensorEventListener) this);
        super.onStop();
    }
    
    
	public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_record:
        	mRecording = true;
        	break;
        case R.id.button_pause:
        	mRecording = false;
        	break;
        case R.id.button_stop:
        	mRecording = false;
        	break;
        case R.id.button_save:
        	mRecording = false;
        	saveOnDisk();
        	break;
        case R.id.button_send:
        	mRecording = false;
        	if(!mSaved) {
        		saveOnDisk();
        	}
        	send();
        	break;
        }
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		if(mRecording) {
			if(event.sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)) {
				mOrientationRecord.add(event);
			}else if(event.sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) {
				mAccelerationRecord.add(event);
			}			
		}
	}
	
	private void saveOnDisk() {
    	
    	//Check if SDCard
    	if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
	   	    //First create the directory if it doesn't exist
	    	File directory = new File(Environment.getExternalStorageDirectory(), "CameraSensors");
	    	if(!directory.exists())
	    	{  	
	    		directory.mkdir();
	    	}  

	    	PrintWriter pw = null;
			try {
				String fileName = computeFileName();
				File file1 = new File(directory, fileName + ".txt");
		        file1.createNewFile();
				pw = new PrintWriter(new BufferedWriter(new FileWriter(file1)));

				pw.println("Orientation");
				mOrientationRecord.writeRecord(pw);
				pw.println("Acceleration");
				mAccelerationRecord.writeRecord(pw);
				
				pw.close();
				mFileUri = Uri.fromFile(file1);

				mSaved = true;
				Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show(); 

			} catch (FileNotFoundException e1) {
				Toast.makeText(this, "Error : Can't create the file",Toast.LENGTH_LONG).show(); 
				e1.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(this, "Error : Can't write in file",Toast.LENGTH_LONG).show(); 
				e.printStackTrace();
			} finally {
				if (pw != null) {
					pw.close();
				}
			}
    	}else {
			Toast.makeText(this, "Error : No SD-Card found",Toast.LENGTH_LONG).show(); 
    	}
    }
    
	private String computeFileName() {
	        DateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
	        Date date = new Date();
	        return "CameraSensors_"+ dateFormat.format(date);
	}
     
    private void send() {
    	Intent email = new Intent(Intent.ACTION_SEND);
    	email.putExtra(Intent.EXTRA_STREAM, mFileUri );
    	email.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject) ); 
    	email.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.email_body) ); 
    	email.setType("text/*"); 
    	startActivity(email);  	
    }
}