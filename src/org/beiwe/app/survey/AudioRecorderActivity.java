package org.beiwe.app.survey;

import java.io.IOException;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.AppNotifications;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    
    private Button playButton;
    private Button recordingButton;
    
    private TextView surveyMessage;
    
    private final Handler recordingTimeoutHandler = new Handler();
    // Number of milliseconds before the recording stops automatically:
    private final int maxRecordingTimeLength = 5 * 60 * 1000;

    
    /*/////////////////////////////////////////////////*/
    /*///////////////Overrides go here/////////////////*/ 
    /*/////////////////////////////////////////////////*/
    
    /**
     * On create, the activity presents the message to the user, and only a record button.
     * After recording, the app will present the user with the play button.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio_recorder);        

	    fileDirectory = getApplicationContext().getFilesDir().getAbsolutePath();
        
    	playButton = (Button) findViewById(R.id.recording_activity_playback_button);
    	recordingButton = (Button) findViewById(R.id.recording_activity_recording_button);
        
    	// Each time the screen is flipped, the app checks if it's time to show the play button
    	checkPlayButtonVisibility(mFileName);
    	
    	surveyMessage = (TextView) findViewById(R.id.record_activity_textview);
    	surveyMessage.setText("Please record what is on your mind. How are you feeling today?");
    }

    /**
     * Makes sure nothing is recording
     */
	@Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
        	stopRecording();
        }

        if (mPlayer != null) {
        	stopPlaying();
        }
        
        // Make mFileName null, so that the play button will turn invisible
    	mFileName = null;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		mFileName = null;
		checkPlayButtonVisibility(mFileName);
	}

    /*/////////////////////////////////////////////////*/
    /*/////////////Button functionalities//////////////*/ 
    /*/////////////////////////////////////////////////*/
	
    /**
     * Checks if mFileName is null. If it is, then the play button will be invisible. Otherwise,
     * the button will be visible. 
     * 
     * @param mFileName
     */
    private void checkPlayButtonVisibility(String fileName) {
    	if (fileName == null) {
    		playButton.setVisibility(Button.INVISIBLE);
    	} else {
    		playButton.setVisibility(Button.VISIBLE) ;
    	}
	}
    
    
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
    
    /*/////////////////////////////////////////////////*/
    /*/////Recording and playing functionalities///////*/ 
    /*/////////////////////////////////////////////////*/

    // Start playing back the recording
    private void startPlaying() {
    	currentlyPlaying = true;
    	
    	// Toggles button
    	playButton.setText("Press to stop");
    	playButton.setBackgroundResource(R.drawable.ic_scrubs_stop_recording);
    	
    	// Recording sequence
    	mPlayer = new MediaPlayer();
    	try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					stopPlaying();
				}
			});
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    
    // Stop playing back the recording, and reset the button to "play"
    private void stopPlaying() {
    	currentlyPlaying = false;
    	
    	// Toggles button
    	playButton.setText("Press to play");
    	playButton.setBackgroundResource(R.drawable.ic_scrubs_record_play);

    	mPlayer.release();
        mPlayer = null;
    }

    /**
     * Creates a new file, and names mFileName to that name. The name consists of the time the recording takes place.
     * Returns the final file name.
     * 
     * @return fileName
     */
    private String getAudioFileName() {
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		String fileName = fileDirectory + "/" + TextFileManager.getUserId() + "_voiceRecording" + "_" + timecode + ".mp4";
		
		mFileName = fileName;
		return fileName;
    }
    
    
    // Start recording from the device's microphone
    private void startRecording() {
    	currentlyRecording = true;
    	
		AppNotifications.dismissNotificatoin(getApplicationContext(), AppNotifications.recordingCode);

    	// Toggles button
    	recordingButton.setText("Press to stop");
    	recordingButton.setBackgroundResource(R.drawable.ic_scrubs_stop_recording);
    	
    	// Note: AudioEncoder.AAC requires the phone to have api 10+.
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

        startTimerForRecordingTimeout();
        
        mRecorder.start();
    }

    
    // Stop recording, and reset the button to "record"
    private void stopRecording() {
    	checkPlayButtonVisibility(mFileName);
    	currentlyRecording = false;
    	recordingButton.setText("Press to record");
    	recordingButton.setBackgroundResource(R.drawable.ic_scrubs_recording_button);

    	cancelTimerForRecordingTimeout();
    	
    	mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
    
    
    /*/////////////////////////////////////////////////*/
    /*//////// Recording timeout functionality ////////*/ 
    /*/////////////////////////////////////////////////*/
    
    
    /**
     * Automatically stop recording if the recording runs longer than n seconds
     */
    private void startTimerForRecordingTimeout() {
    	recordingTimeoutHandler.postDelayed(new Runnable() {    		
			@Override
			public void run() {
				showTimeoutToast();
				stopRecording();
			}
		}, maxRecordingTimeLength);
    }
    
    
    /**
     * Show a Toast with message "the recording timed out after n minutes"
     */
    private void showTimeoutToast() {
    	Resources resources = getApplicationContext().getResources();
    	String msg = (String) resources.getText(R.string.timeout_msg_1st_half);
    	msg += ((float) maxRecordingTimeLength / 60 / 1000);
    	msg += resources.getText(R.string.timeout_msg_2nd_half);
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    
    
    /**
     * Cancel the stop-recording timer (this should be called when 
     * stopRecording() has already been called somewhere else, so that we don't
     * call stopRecording twice
     */
    private void cancelTimerForRecordingTimeout() {
    	recordingTimeoutHandler.removeCallbacksAndMessages(null);
    }
    
    
    /*/////////////////////////////////////////////////*/
    /*////////Hotline - consider making static/////////*/ 
    /*/////////////////////////////////////////////////*/
    /**
     * Places a call to the hotline.
     * 
     * Note: Consider making this a static function.
     */
    public void callHotline(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNum = (String) getApplicationContext().getResources().getText(R.string.hotline_phone_number);
	    callIntent.setData(Uri.parse("tel:" + phoneNum));
	    startActivity(callIntent);
	}
    
}