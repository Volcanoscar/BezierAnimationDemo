package com.example.demo;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.example.demo.animation.BezierView;

/**
 * 
 * @author cainli
 *
 */
public class MainActivity extends Activity {
	
	BezierView bView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bView = (BezierView) findViewById(R.id.bezierView);
		Drawable drawable = getResources().getDrawable(R.drawable.test);
		if(drawable!=null && drawable instanceof BitmapDrawable){
			bView.setImageBitmap(((BitmapDrawable)drawable).getBitmap());
		}
	}
	
	@Override
	public void onBackPressed() {
		if(bView!=null){
			bView.reset();
		}
	}
}
