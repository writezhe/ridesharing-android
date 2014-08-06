package org.beiwe.app.survey;

import java.io.IOException;

import org.beiwe.app.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

/**
 * Audio Recorder
 * 
 * Provides a GUI to record audio clips and save them as files.
 * Heavily based on code from:
 * http://developer.android.com/guide/topics/media/audio-capture.html
 * 
 * filename
 * type of data: "voice recording" or "acceleometer"
 * start timestamp
 * stop timestamp
 * user id #
 * 
 * @author Josh Zagorsky, May 2014
 */
public class AudioRecorderActivity extends Activity {
    private static final String LOG_TAG = "AudioRecorderActivity";
    private static String fileDirectory = null;
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    
    private boolean currentlyRecording = false;
    private boolean currentlyPlaying = false;
    
    private ImageButton playButton;
    private ImageButton recordingButton;

        
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		setContentView(R.layout.activity_audio_recorder);        

	    fileDirectory = getApplicationContext().getFilesDir().getAbsolutePath();
        mFileName += "/audiorecordtest.mp4";
        Log.i("AudioRecorderActivity", "Filepath = " + mFileName);
        
    	playButton = (ImageButton) findViewById(R.id.recording_activity_playback_button);
    	recordingButton = (ImageButton) findViewById(R.id.recording_activity_recording_button);
        
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
    	playButton.setImageResource(R.drawable.ic_scrubs_stop_recording);

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
    	playButton.setImageResource(R.drawable.ic_scrubs_record_play);

    	mPlayer.release();
        mPlayer = null;
    }

    
    private String getAudioFileName() {
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		String fileName = fileDirectory + "/audioSample" + "-" + timecode + ".mp4";
		
		mFileName = fileName;
		return fileName;
    }
    
    
    // Start recording from the device's microphone
    private void startRecording() {
    	currentlyRecording = true;
    	recordingButton.setImageResource(R.drawable.ic_scrubs_stop_recording);
    	
    	//note: AudioEncoder.AAC requires the phone to have api 10+.
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(getAudioFileName());
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
    	recordingButton.setImageResource(R.drawable.ic_scrubs_record);

    	mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
    
}
