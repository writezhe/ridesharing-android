package org.beiwe.app.survey;

import java.io.IOException;

import org.beiwe.app.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Audio Recorder
 * 
 * Provides a GUI to record audio clips and save them as files.
 * Heavily based on code from:
 * http://developer.android.com/guide/topics/media/audio-capture.html
 * 
 * filename
 * type of data: "voice recording" or "accereometer
 * start timestamp
 * stop timestamp
 * user id #
 * 
 * @author Josh Zagorsky, May 2014
 */
public class AudioRecorderActivity extends Activity {
    private static final String LOG_TAG = "AudioRecorderActivity";
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    
    private boolean currentlyRecording = false;
    private boolean currentlyPlaying = false;

    
    public AudioRecorderActivity() {
	    mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		setContentView(R.layout.activity_audio_recorder);        
    }

    
    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
        	stopRecording();
        }

        if (mPlayer != null) {
        	stopPlaying();
        }
    }

    
    // TODO: add arbitrary recording length limiter, like 2 hours
    // probably make this a timer that calls stopRecording() after a while
    // TODO: add UI text saying "the recording can't be longer than X"
    
    // When the user presses the "record" button, toggle (start/stop) recording
    public void buttonRecordPressed(View view) {
    	if (!currentlyRecording) {
    		startRecording();
    	}
    	else {
    		stopRecording();
    	}
    }

    
    // When the user presses the "play" button, toggle (start/stop) playback
    public void buttonPlayPressed(View view) {
    	if (!currentlyPlaying) {
			startPlaying();
		}
    	else {
			stopPlaying();
		}    	
    }
    

    // Start playing back the recording
    private void startPlaying() {
    	currentlyPlaying = true;
    	Button playButton = (Button) findViewById(R.id.buttonPlay);
    	playButton.setText("Stop Playing");

    	mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    
    // Stop playing back the recording, and reset the button to "play"
    private void stopPlaying() {
    	currentlyPlaying = false;
    	Button playButton = (Button) findViewById(R.id.buttonPlay);
    	playButton.setText("Start Playing");

    	mPlayer.release();
        mPlayer = null;
    }

    
    // Start recording from the device's microphone
    private void startRecording() {
    	currentlyRecording = true;
    	Button recordingButton = (Button) findViewById(R.id.buttonRecord);
    	recordingButton.setText("Stop Recording");
    	
    	//note: AudioEncoder.AAC requires the phone to have api 10+.
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(64000);
        
        try {
            mRecorder.prepare();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    
    // Stop recording, and reset the button to "record"
    private void stopRecording() {
    	currentlyRecording = false;
    	Button recordingButton = (Button) findViewById(R.id.buttonRecord);
    	recordingButton.setText("Start Recording");

    	mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
    
}
