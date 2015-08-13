package org.beiwe.app.ui.user;

import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundServiceActivity;

import android.os.Bundle;

/**The about page!
 * @author Everyone! */
public class AboutActivityLoggedOut extends RunningBackgroundServiceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
}
