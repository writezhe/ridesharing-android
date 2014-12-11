package org.beiwe.app.ui;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.beiwe.app.R;
import org.beiwe.app.Timer;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.storage.EncryptionEngine;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**Audio Recorder
 * 
 * Provides a GUI to record audio clips and save them as files.
 * Heavily based on code from:
 * http://developer.android.com/guide/topics/media/audio-capture.html
 * 
 * @author Josh Zagorsky, Eli Jones */
public class AudioRecorderActivity extends SessionActivity {
    private static final String LOG_TAG = "AudioRecorderActivity";
    
    private static String fileDirectory = null;
    private static String fullFileName = null;
    private static String fileName = null;
    private static boolean displayPlayback = false;
    
    private MediaRecorder mRecorder = null;
    private MediaPlayer mediaPlayer = null;
    
    private boolean currentlyRecording = false;
    private boolean currentlyPlaying = false;
    
    private Button playButton;
    private Button recordingButton;
    
    private final Handler recordingTimeoutHandler = new Handler();
    
    /*/////////////////////////////////////////////////*/
    /*///////////////Overrides go here/////////////////*/ 
    /*/////////////////////////////////////////////////*/
    
    /**On create, the activity presents the message to the user, and only a record button.
     * After recording, the app will present the user with the play button. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio_recorder);        

	    fileDirectory = getApplicationContext().getFilesDir().getAbsolutePath();
        
    	playButton = (Button) findViewById(R.id.play_button);
    	recordingButton = (Button) findViewById(R.id.recording_button);
        
    	// Each time the screen is flipped, the app checks if it's time to show the play button
    	setPlayButtonVisibility();
    	
    	/* Improvement idea: make the Audio Recording prompt a string that can
    	 * be edited on the server, and then propagates via automatic downloads
    	 * to all phones running the app, just like the survey questions. */
    	//TextView surveyMessage = (TextView) findViewById(R.id.record_activity_textview);
    	//surveyMessage.setText("Please record a statement about how you are feeling today.");
    }

    /** Makes sure nothing is recording */
	@Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) { stopRecording(); }
        if (mediaPlayer != null) { stopPlaying(); }
        // Make mFileName null, so that the play button will turn invisible
        displayPlayback = false;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		displayPlayback = false;
		setPlayButtonVisibility();
	}

	
    /*/////////////////////////////////////////////////
    ///////////////Button functionalities////////////// 
    /////////////////////////////////////////////////*/
	
    /** Checks if mFileName is null. If it is, then the play button will be invisible. Otherwise,
     * the button will be visible. 
     *@param fullFileName */
    private void setPlayButtonVisibility() {
    	if (!displayPlayback) { playButton.setVisibility(Button.INVISIBLE); }
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
    
    /*/////////////////////////////////////////////////
    ///////Recording and playing functionalities/////// 
    /////////////////////////////////////////////////*/

    /** Starts playing back the recording */
    private void startPlaying() {
    	currentlyPlaying = true;
    	
    	// Toggles button
    	playButton.setText(getApplicationContext().getString(R.string.play_button_stop_text));
    	playButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_button, 0, 0);
    	
    	// Recording sequence
    	mediaPlayer = new MediaPlayer();
    	try {
            mediaPlayer.setDataSource(fullFileName);
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

    
    /**Generates new file name variables. The name consists of the time the recording takes place. */
    private void setAudioFileName() {
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		fileName = LoginManager.getPatientID() + "_voiceRecording" + "_" + timecode + ".mp4";
		fullFileName = fileDirectory + "/" + fileName;
    }
    
    
    /** Start recording from the device's microphone */
    private void startRecording() {
    	currentlyRecording = true;
		AppNotifications.dismissNotificatoin( getApplicationContext(), AppNotifications.recordingCode );

    	// Toggles button
    	recordingButton.setText( getApplicationContext().getString(R.string.record_button_stop_text) );
    	recordingButton.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.stop_recording_button, 0, 0 );
    	
    	setAudioFileName();
    	
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile( fullFileName );
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(64000);
        
        try { mRecorder.prepare(); }
        catch (IOException e) { Log.e(LOG_TAG, "prepare() failed"); }

        startRecordingTimeout();
        mRecorder.start();
    }

    
    // Stop recording, and reset the button to "record"
    private void stopRecording() {
    	displayPlayback = true;
    	setPlayButtonVisibility();
    	currentlyRecording = false;
    	recordingButton.setText(getApplicationContext().getString(R.string.record_button_text));
    	recordingButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_button, 0, 0);

    	cancelRecordingTimeout();
    	
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
    }
    
    
    /*/////////////////////////////////////////////////*/
    /*//////// Recording timeout functionality ////////*/ 
    /*/////////////////////////////////////////////////*/
    
    
    /** Automatically stop recording if the recording runs longer than n seconds. */
    private void startRecordingTimeout() {
    	recordingTimeoutHandler.postDelayed(new Runnable() {    		
			@Override
			public void run() {
				showTimeoutToast();
				stopRecording();
			}
		}, Timer.VOICE_RECORDING_MAX_TIME_LENGTH);
    }
    
    
    /** Show a Toast with message "the recording timed out after n minutes" */
    private void showTimeoutToast() {
    	Resources resources = getApplicationContext().getResources();
    	String msg = (String) resources.getText(R.string.timeout_msg_1st_half);
    	msg += ((float) Timer.VOICE_RECORDING_MAX_TIME_LENGTH / 60 / 1000);
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
    	//TODO: Eli/Josh.  pop up a spinner here.
    	
    	if (fullFileName == null) {
    		Log.w("audiorecorder", "did not record.");
    		finish();
    		return;
    	}
    	
		writePlaintext( EncryptionEngine.encryptAES( readInAudioFile() ) );		
    	finish();
    }
    
    
    /** Writes string data to a the audio file. */
	public synchronized void writePlaintext(String data){
		FileOutputStream outStream;
		
		try {  //write the output, we want mode private because we want to overwrite the existing data
			outStream = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
			outStream.write( ( data ).getBytes() );
			outStream.write( "\n".getBytes() );
			outStream.flush();
			outStream.close(); }
		catch (Exception e) {
			Log.i("audiorecorder", "Write error: " + fullFileName);
			e.printStackTrace(); }
	}
    
	
	/** Reads a byte array of the current audio file contents
	 * @return byte array of file contents. */
	public synchronized byte[] readInAudioFile() {
		DataInputStream dataInputStream;
		byte[] data = null;
		try {  //Read the (data) input stream, into a bytearray.  Catch exceptions.
			File file = new File(fullFileName);
			dataInputStream = new DataInputStream( new FileInputStream( file ) );	
			data = new byte[ (int) file.length() ];
			try{ dataInputStream.readFully(data); }
			catch (IOException e) { Log.i("DataFileManager", "error reading " + fullFileName);
				e.printStackTrace(); }
			dataInputStream.close(); }
		catch (FileNotFoundException e) {
			Log.i("audiorecorder", "file " + fullFileName + " does not exist");
			e.printStackTrace(); }
		catch (IOException e) {
			Log.i("audiorecorder", "could not close " + fullFileName);
			e.printStackTrace(); }
		return data;
	}
}