package org.beiwe.app.survey;

import java.io.IOException;

import org.beiwe.app.R;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.user.MainMenuActivity;
import org.beiwe.app.ui.utils.SurveyNotifications;
import org.beiwe.app.storage.AudioFileManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
	
/**Audio Recorder
 * 
 * Provides a GUI to record audio clips and save them as files.
 * Heavily based on code from:
 * http://developer.android.com/guide/topics/media/audio-capture.html
 * 
 * @author Josh Zagorsky, Eli Jones */

public class AudioRecorderActivity extends SessionActivity {
	protected static final String LOG_TAG = "AudioRecorderActivity";
	protected static boolean displayPlaybackButton = false;

    public static final String unencryptedTempAudioFileName = "unencryptedTempAudioFile.mp4";
    protected String unencryptedTempAudioFilePath;
    protected Boolean finishedEncrypting = true; // Effectively a lock on deleting the temp file
    
    protected MediaRecorder mRecorder = null;
    protected MediaPlayer mediaPlayer = null;
    
    protected boolean currentlyRecording = false;
    protected boolean currentlyPlaying = false;
    
    protected Button playButton;
    protected Button recordingButton;
    
    protected final Handler recordingTimeoutHandler = new Handler();
    protected String surveyId;
    
    @Override public Boolean isAudioRecorderActivity() { return true; }
    
    /*#########################################################
    ################## Activity Overrides ##################### 
    #########################################################*/
    
    /**On create, the activity presents the message to the user, and only a record button.
     * After recording, the app will present the user with the play button. */
    @Override
    public void onCreate(Bundle bundle) {
    	super.onCreate(bundle);
        setContentView(R.layout.activity_audio_recorder);

        surveyId = getIntent().getStringExtra("surveyId");
    	
    	// grab the layout element objects that we will add questions to:
		TextView textbox = (TextView) findViewById(R.id.record_activity_textview );
		textbox.setText( getPromptText(surveyId) );
        
        String fileDirectory = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
        unencryptedTempAudioFilePath = fileDirectory + unencryptedTempAudioFileName;
        
    	playButton = (Button) findViewById(R.id.play_button);
    	recordingButton = (Button) findViewById(R.id.recording_button);
        
    	Button callClinicianButton = (Button) findViewById(R.id.record_activity_call_clinician);
    	callClinicianButton.setText(PersistentData.getCallClinicianButtonText());
    	
    	// Each time the screen is flipped, the app checks if it's time to show the play button
    	setPlayButtonVisibility();
    }
    
    private String getPromptText(String surveyId) {
		try {
			JSONObject contentArray = new JSONArray(PersistentData.getSurveyContent(surveyId)).getJSONObject(0);
			return contentArray.getString("prompt");
		} catch (JSONException e) {
			Log.e("Audio Survey", "audio survey received either no or invalid prompt text.");
			e.printStackTrace();
			//TODO: Low Priority. Eli/Josh.  update the default prompt string to be... not a question?
			return getApplicationContext().getString(R.string.record_activity_default_message);
		}
    }
    

    @Override
	public void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			// If the activity is being finished()
	        if (mRecorder != null) { stopRecording(); }
	        if (mediaPlayer != null) { stopPlaying(); }
	        displayPlaybackButton = false;
	        /* Delete the temporary, unencrypted audio file so that nobody can play it back after
	         * the user leaves this screen */
	        if (finishedEncrypting) { TextFileManager.delete(unencryptedTempAudioFileName); }
		}
		// case: The activity is probably just getting restarted because the screen rotated
	}

    /** While encrypting the audio file we block out user interaction.*/
    private class EncryptAudioFileTask extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected void onPreExecute() { recordingButton.setClickable(false); }
		@Override
		protected Void doInBackground(Void... params) {
			AudioFileManager.encryptAudioFile(unencryptedTempAudioFilePath, getApplicationContext() );
			return null;
		}
		@Override
		protected void onPostExecute(Void arg) {
			finishedEncrypting = true;  // Can now delete audio file
			// If isFinishing(), the other call to delete the temp file won't get triggered, so do it here
			if (isFinishing()) { TextFileManager.delete(unencryptedTempAudioFileName); }
			recordingButton.setClickable(true);
		}
		//TODO: make this pop the same toast as a regular survey.
    }
    

    /*#########################################################
    ################# Button functionalities ################## 
    #########################################################*/
	
    /** Checks if mFileName is null. If it is, then the play button will be invisible. Otherwise,
     * the button will be visible. 
     *@param fullFileName */
    private void setPlayButtonVisibility() {
    	if (!displayPlaybackButton) { playButton.setVisibility(Button.INVISIBLE); }
    	else { playButton.setVisibility(Button.VISIBLE) ; }
	}
    
    
    /** When the user presses the "record" button toggle (start/stop) recording. */
    public void buttonRecordPressed(View view) {
    	if (!currentlyRecording) { startRecording(); }
    	else { stopRecording(); }
    }
    
    /** When the user presses the "play" button, toggle (start/stop) playback. */
    public void buttonPlayPressed(View view) {
    	if (!currentlyPlaying) { startPlaying(); }
    	else { stopPlaying(); }    	
    }
    
    /*#########################################################
    ################# Recording and Playing ################### 
    #########################################################*/

    /** Starts playing back the recording */
    private void startPlaying() {
    	currentlyPlaying = true;
    	
    	// Toggles button
    	playButton.setText(getApplicationContext().getString(R.string.play_button_stop_text));
    	playButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_button, 0, 0);
    	
    	// Recording sequence
    	mediaPlayer = new MediaPlayer();
    	try {
    		// Play the temporary unencrypted file, because you can't read the encrypted file
            mediaPlayer.setDataSource(unencryptedTempAudioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mediaPlayer) { stopPlaying(); }
			} );
        }
        catch (IOException e) { Log.e(LOG_TAG, "prepare() failed"); }
    }
    
    /** Stops playing back the recording, and reset the button to "play" */
    private void stopPlaying() {
    	currentlyPlaying = false;
    	// Toggles button
    	playButton.setText(getApplicationContext().getString(R.string.play_button_text));
    	playButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play_button, 0, 0);
    	mediaPlayer.stop();
    	mediaPlayer.reset();
    	mediaPlayer.release();
        mediaPlayer = null;
    }    
    
    /** Start recording from the device's microphone */
    private void startRecording() {
    	currentlyRecording = true;
    	finishedEncrypting = false;
    	// Toggles button
    	recordingButton.setText( getApplicationContext().getString(R.string.record_button_stop_text) );
    	recordingButton.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.stop_recording_button, 0, 0 );
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile( unencryptedTempAudioFilePath );
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(64000);
        
        try { mRecorder.prepare(); }
        catch (IOException e) { Log.e(LOG_TAG, "prepare() failed"); }

        startRecordingTimeout();
        mRecorder.start();
    }
    
    /** Stop recording, and reset the button to "record" */
    private void stopRecording() {
    	displayPlaybackButton = true;
    	setPlayButtonVisibility();
    	currentlyRecording = false;
    	recordingButton.setText(getApplicationContext().getString(R.string.record_button_text));
    	recordingButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_button, 0, 0);

    	cancelRecordingTimeout();
    	
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;

        // Encrypt the audio file as soon as recording is finished
        new EncryptAudioFileTask().execute();
    }
    
    
    /*#########################################################
    #################### Recording Timeout #################### 
    #########################################################*/
    
    /** Automatically stop recording if the recording runs longer than n seconds. */
    private void startRecordingTimeout() {
    	recordingTimeoutHandler.postDelayed(new Runnable() {    		
			@Override
			public void run() {
				showTimeoutToast();
				stopRecording();
			}
		}, PersistentData.getVoiceRecordingMaxTimeLengthMilliseconds());
    }
    
    
    /** Show a Toast with message "the recording timed out after n minutes" */
    private void showTimeoutToast() {
    	Resources resources = getApplicationContext().getResources();
    	String msg = (String) resources.getText(R.string.timeout_msg_1st_half);
    	msg += ((float) PersistentData.getVoiceRecordingMaxTimeLengthMilliseconds() / 60 / 1000);
    	msg += resources.getText(R.string.timeout_msg_2nd_half);
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    
    
    /**Cancel the stop-recording timer (this should be called when 
     * stopRecording() has already been called somewhere else, so that we don't
     * call stopRecording twice. */
    private void cancelRecordingTimeout() { recordingTimeoutHandler.removeCallbacksAndMessages(null); }
    
    /** When the user presses "Done", just kill this activity and take them
     * back to the last one; the audio file should already be saved, so we
     * don't need to do anything other than kill the activity.  */
    public void buttonDonePressed(View v) {
    	PersistentData.setSurveyNotificationState(surveyId, false);
		SurveyNotifications.dismissNotification( getApplicationContext(), surveyId );
    	startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
    	finish();
    }
}