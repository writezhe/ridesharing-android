package org.beiwe.app.survey;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Based on http://stackoverflow.com/a/19008611
 * @author admin
 *
 */
public class SeekBarEditableThumb extends SeekBar {

	public SeekBarEditableThumb(Context context) {
		super(context);
	}
	
	public SeekBarEditableThumb(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarEditableThumb(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	private Drawable compatThumb;
	
	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
		compatThumb = thumb;
	}
	
	public Drawable getSeekBarThumb() {
		return compatThumb;
	}
}
