package com.zagaran.scrubs;

import android.app.Activity;
import android.os.Bundle;

//audio recording android devs:
// http://developer.android.com/guide/topics/media/audio-capture.html

public class AudioRecorderActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_recorder);
	}
}
