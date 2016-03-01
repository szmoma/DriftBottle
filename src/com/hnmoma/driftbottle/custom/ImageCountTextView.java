package com.hnmoma.driftbottle.custom;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;

public class ImageCountTextView extends TextView {

	public ImageCountTextView(Context context) {
		super(context);
	}

	public ImageCountTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ImageCountTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		if (!TextUtils.isEmpty(text) && text.toString().contains("/")) {
			String[] s = text.toString().split("/");
			SpannableString sp = new SpannableString(text);
			sp.setSpan(new AbsoluteSizeSpan((int) (this.getTextSize() + 25)), 0, s[0].length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			super.setText(sp, type);
		} else {
			super.setText(text, type);
		}
	}
}
