package org.beiwe.app.survey;

import java.io.IOException;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.AppNotifications;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    	// TODO: Get a survey message (from server??) and display it to the user instead of the default message
    	surveyMessage.setText("Lorem ipsum dolor sit amet, et lobortis intellegat mel, " +
    			"est utinam graeci in. Ei quo appetere moderatius, " +
    			"in dolorem inimicus assentior has, " +
    			"te sed summo explicari. Dolores appareat eu mel, ne meliore");
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
    
    // TODO: add arbitrary recording length limiter, like 2 hours
    // probably make this a timer that calls stopRecording() after a while
    // TODO: add UI text saying "the recording can't be longer than X"
    
    // When the user presses the "record" button, toggle (start/stop) recording
    public void buttonRecordPressed(View view) {
    	if (!currentlyRecording) {
    		AppNotifications.dismissNotificatoin(getApplicationContext(), AppNotifications.recordingCode);
    		startRecording();
    	}
    	else {
    		stopRecording();
    		checkPlayButtonVisibility(mFileName);
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

        mRecorder.start();
    }

    
    // Stop recording, and reset the button to "record"
    private void stopRecording() {
    	currentlyRecording = false;
    	recordingButton.setText("Press to record");
    	recordingButton.setBackgroundResource(R.drawable.ic_scrubs_recording_button);

    	mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
    
    
    /*/////////////////////////////////////////////////*/
    /*////////Hotline - consider making static/////////*/ 
    /*/////////////////////////////////////////////////*/
    /**
     * Places a call to the hotline.
     * 
     * Note: Consider making this a static function.
     * @param v
     */
    public void callHotline(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
	    callIntent.setData(Uri.parse("tel:123456789"));
	    startActivity(callIntent);
	}
    
}