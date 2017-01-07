package com.example.libtest.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class SmartTechProgressBar extends ProgressBar {
	 private String text = "";
	    private Paint textPaint;
	    private final static int pontsize = 26;
	    public SmartTechProgressBar(Context context) {
	        super(context);
//	        text = "0/100";
	        textPaint = new Paint();
	        textPaint.setColor(Color.WHITE);
	        textPaint.setTextSize(pontsize);
	    }

	    public SmartTechProgressBar(Context context, AttributeSet attrs) {
	        super(context, attrs);
//	        text = "0/100";
	        textPaint = new Paint();
	        textPaint.setColor(Color.WHITE);
	        textPaint.setTextSize(pontsize);
	    }

	    public SmartTechProgressBar(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
//	        text = "0/100";
	        textPaint = new Paint();
	        textPaint.setColor(Color.WHITE);
	        textPaint.setTextSize(pontsize);
	    }

	    @Override
	    protected synchronized void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        Rect bounds = new Rect();
	        textPaint.getTextBounds(text, 0, text.length(), bounds);
	        int x = getWidth() / 2 - bounds.centerX();
	        int y = getHeight() / 2 - bounds.centerY();
	        canvas.drawText(text, x, y, textPaint);
	    }

	    public synchronized void setText(String text) {
	        this.text = text;
	        drawableStateChanged();
	    }

	    public void setTextColor(int color) {
	        textPaint.setColor(color);
	        drawableStateChanged();
	    }
}
