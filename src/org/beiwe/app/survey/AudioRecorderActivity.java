package org.beiwe.app.survey;

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;


public class AudioRecorderActivity extends AudioRecorderCommon {    
    @Override
	protected String getFileExtension() { return ".mp4"; }
        
    /*#########################################################
    ################## Activity Overrides ##################### 
    #########################################################*/

    @Override
	public void onDestroy() {
		super.onDestroy();
		if (isFinishing()) { // If the activity is being finished()...
	        if (mRecorder != null) { stopRecording(); }
		}
	}
    
    /*#########################################################
    ################# Recording and Playing ################### 
    #########################################################*/
    
    /** Start recording from the device's microphone, uses MediaRecorder,
     * output file is the unencryptedTempAudioFilePath */
    @Override
    protected void startRecording() {
    	super.startRecording();
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
    @Override
    public void stopRecording() {
    	super.stopRecording();
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        // Encrypt the audio file as soon as recording is finished
        new EncryptAudioFileTask().execute();
    }
}