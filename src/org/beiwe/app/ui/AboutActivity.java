package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;

import android.os.Bundle;

public class AboutActivity extends RunningBackgroundProcessActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
}
