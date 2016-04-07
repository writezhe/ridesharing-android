package org.beiwe.app.survey;

import java.io.IOException;

import org.beiwe.app.R;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.storage.AudioFileManager;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.user.MainMenuActivity;
import org.beiwe.app.ui.utils.SurveyNotifications;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



/**Audio Recorder Common 
 * This is the superclass which the audio survey types extend. 
 * Provides a GUI to record audio clips and save them as files.
 * Heavily based on code from:
 * http://developer.android.com/guide/topics/media/audio-capture.html
 * 
 * @author Eli Jones, Josh Zagorsky */
public class AudioRecorderCommon extends SessionActivity {

	@Override public Boolean isAudioRecorderActivity() { return true; }
	
	protected static final String LOG_TAG = "AudioRecorderActivity";
	protected static boolean displayPlaybackButton = false;

    protected String unencryptedTempAudioFilePath;
    protected Boolean finishedEncrypting = true; // a lock on deleting the temp file
        
    protected boolean currentlyRecording = false;
    protected boolean currentlyPlaying = false;
    
    protected Button playButton;
    protected Button recordingButton;
    
    protected MediaRecorder mRecorder = null;
    protected MediaPlayer mediaPlayer = null;
    
    protected final Handler recordingTimeoutHandler = new Handler();
    protected String surveyId;
	
    public static final String unencryptedTempAudioFileName = "unencryptedTempAudioFile";
    protected String getFileExtension() { throw new NullPointerException("BAD CODE."); } //You must override this to get a different file extension
    
    /**On create, the activity presents the message to the user, and only a record button.
     * After recording, the app will present the user with the play button. */
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_audio_recorder );
		
        surveyId = getIntent().getStringExtra("surveyId");
    	
    	// grab the layout element objects that we will add questions to:
		TextView textbox = (TextView) findViewById(R.id.record_activity_textview );
		textbox.setText( getPromptText(surveyId, getApplicationContext() ) );
        // Handle file path issues with this variable
        
        unencryptedTempAudioFilePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + unencryptedTempAudioFileName;
        
    	playButton = (Button) findViewById(R.id.play_button);
    	recordingButton = (Button) findViewById(R.id.recording_button);
        
    	Button callClinicianButton = (Button) findViewById(R.id.record_activity_call_clinician);
    	callClinicianButton.setText(PersistentData.getCallClinicianButtonText());
    	
    	// Each time the screen is flipped, the app checks if it's time to show the play button
    	setPlayButtonVisibility();
	}
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isFinishing()) { // If the activity is being finished()
			if (mediaPlayer != null) { stopPlaying(); }
			displayPlaybackButton = false;
			/* Delete the temporary, unencrypted audio file so that nobody can play it back after
	         * the user leaves this screen */
			if (finishedEncrypting) { AudioFileManager.delete(unencryptedTempAudioFileName); }
		}
	// case !isfinishing: The activity is probably just getting restarted because the screen rotated
	}
	
	private static String getPromptText(String surveyId, Context appContext) {
		try {
			JSONObject contentArray = new JSONArray(PersistentData.getSurveyContent(surveyId)).getJSONObject(0);
			return contentArray.getString("prompt");
		} catch (JSONException e) {
			Log.e("Audio Survey", "audio survey received either no or invalid prompt text.");
			e.printStackTrace();
			//TODO: Low Priority. Eli/Josh.  update the default prompt string to be... not a question?
			return appContext.getString(R.string.record_activity_default_message);
		}
    }
        
    /** While encrypting the audio file we block out user interaction.*/
    protected class EncryptAudioFileTask extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected void onPreExecute() { recordingButton.setClickable(false); }
		@Override
		protected Void doInBackground(Void... params) {
			AudioFileManager.encryptAudioFile(unencryptedTempAudioFilePath, getFileExtension(), getApplicationContext() );
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
    #################### Recording Timeout #################### 
    #########################################################*/
    
    /** Automatically stop recording if the recording runs longer than n seconds. */
    protected void startRecordingTimeout() {
    	recordingTimeoutHandler.postDelayed(new Runnable() {    		
			@Override
			public void run() {
				showTimeoutToast();
				stopRecording();
			}
		}, PersistentData.getVoiceRecordingMaxTimeLengthMilliseconds());
    }
    
    /** Show a Toast with message "the recording timed out after n minutes" */
    protected void showTimeoutToast() {
    	Resources resources = getApplicationContext().getResources();
    	String msg = (String) resources.getText(R.string.timeout_msg_1st_half);
    	msg += ((float) PersistentData.getVoiceRecordingMaxTimeLengthMilliseconds() / 60 / 1000);
    	msg += resources.getText(R.string.timeout_msg_2nd_half);
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    
    /**Cancel the stop-recording timer (this should be called when 
     * stopRecording() has already been called somewhere else, so that we don't
     * call stopRecording twice. */
    protected void cancelRecordingTimeout() { recordingTimeoutHandler.removeCallbacksAndMessages(null); }
    
    /*#########################################################
    ################# Button functionalities ################## 
    #########################################################*/
	
    /** Checks if mFileName is null. If it is, then the play button will be invisible. Otherwise,
     * the button will be visible. 
     *@param fullFileName */
    protected void setPlayButtonVisibility() {
    	if (!displayPlaybackButton) { playButton.setVisibility(Button.INVISIBLE); }
    	else { playButton.setVisibility(Button.VISIBLE) ; }
	}
    
    /** When the user presses the "record" button toggle (start/stop) recording. */
    public void buttonRecordPressed(View view) {
    	if (!currentlyRecording) { startRecording(); }
    	else { stopRecording(); }
    }
    
    //FIXME: ensure you override and run super
    protected void startRecording() {
    	currentlyRecording = true;
    	finishedEncrypting = false;
    	// Toggles button
    	recordingButton.setText( getApplicationContext().getString(R.string.record_button_stop_text) );
    	recordingButton.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.stop_recording_button, 0, 0 );
    }
    //FIXME: ensure you override and run super
    public void stopRecording(){
    	displayPlaybackButton = true;
    	setPlayButtonVisibility();
    	currentlyRecording = false;
    	recordingButton.setText(getApplicationContext().getString(R.string.record_button_text));
    	recordingButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_button, 0, 0);
    	cancelRecordingTimeout();
    }
    
    /** When the user presses the "play" button, toggle (start/stop) playback. */
    public void buttonPlayPressed(View view) {
    	if (!currentlyPlaying) { startPlaying(); }
    	else { stopPlaying(); }  	
    }
    
    //FIXME: Ensure you override
    /** Stops playing back the recording, and reset the button to "play" */
    protected void stopPlaying() {
    	currentlyPlaying = false;
    	// Toggles button
    	playButton.setText(getApplicationContext().getString(R.string.play_button_text));
    	playButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play_button, 0, 0);
    	mediaPlayer.stop();
    	mediaPlayer.reset();
    	mediaPlayer.release();
    	mediaPlayer = null;
    }
    
    //FIXME: Ensure you override, need subclass's 
    /** Starts playing back the recording */
    protected void startPlaying() {
    	currentlyPlaying = true;
    	// Toggles button
    	playButton.setText(getApplicationContext().getString(R.string.play_button_stop_text));
    	playButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_button, 0, 0);
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
    
    
    /** When the user presses "Done", just kill this activity and take them
     * back to the last one; the audio file should already be saved, so we
     * don't need to do anything other than kill the activity.  */
    //TODO: confirm the above
    public void buttonDonePressed(View v) {
    	PersistentData.setSurveyNotificationState(surveyId, false);
		SurveyNotifications.dismissNotification( getApplicationContext(), surveyId );
    	startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
    	finish();
    }
}