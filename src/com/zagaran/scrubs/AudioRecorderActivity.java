package com.zagaran.scrubs;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Audio Recorder
 * 
 * Provides a GUI to record audio clips and save them as files.
 * Heavily based on code from:
 * http://developer.android.com/guide/topics/media/audio-capture.html
 * 
 * @author Josh Zagorsky, May 2014
 */
public class AudioRecorderActivity extends Activity {
    private static final String LOG_TAG = "AudioRecorderActivity";
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
    
    private boolean currentlyRecording = false;
    private boolean currentlyPlaying = false;

    private PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;

    public void buttonRecordPressed(View view) {
    	if (!currentlyRecording) {
			startRecording();
		} else {
			stopRecording();
		}
    }
    
    public void buttonPlayPressed(View view) {
    	if (!currentlyPlaying) {
			startPlaying();
		} else {
			stopPlaying();
		}    	
    }
    
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
    	currentlyPlaying = true;
    	Button playButton = (Button) findViewById(R.id.buttonPlay);
    	playButton.setText("Stop Playing");

    	mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
    	currentlyPlaying = false;
    	Button playButton = (Button) findViewById(R.id.buttonPlay);
    	playButton.setText("Start Playing");

    	mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
    	currentlyRecording = true;
    	Button recordingButton = (Button) findViewById(R.id.buttonRecord);
    	recordingButton.setText("Stop Recording");
    	
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
    	currentlyRecording = false;
    	Button recordingButton = (Button) findViewById(R.id.buttonRecord);
    	recordingButton.setText("Start Recording");

    	mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    public class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    public AudioRecorderActivity() {
	    mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

		setContentView(R.layout.activity_audio_recorder);
        
		/*Button oldRecordButton = (Button) findViewById(R.id.buttonRecord);
        Button oldPlayButton = (Button) findViewById(R.id.buttonPlay);
        
        replaceButton(oldRecordButton, new RecordButton(getApplicationContext()));
        replaceButton(oldPlayButton, new PlayButton(getApplicationContext()));*/
		
        /*LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        setContentView(ll);*/
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    
    
    private void replaceButton(Button oldButton, Button newButton) {
    	ViewGroup parent = (ViewGroup) oldButton.getParent();
    	int index = parent.indexOfChild(oldButton);
    	parent.removeView(oldButton);
    	parent.addView(newButton, index);
    }
    
}
