package com.example.demo.animation;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


/**
 * 
 * @author cainli
 *
 */
public class BezierView extends View{
	private Paint paintQ,redPaint,picPaint;
	int mWidth,mHeight;
	int WIDTH = 50,HEIGHT = 50;
	long startTime = 0;
	long stopTime = 0;
	long stopDuringTime = 0;
	
	long duration = 3*1000;
	long overTime = 0L;
	private int state = 0;
	private ArrayList<PicObject> objs = new ArrayList<BezierView.PicObject>();
	private Bitmap mBitmap;
	private Rect mDstRect = new Rect();
	public Interpolator mInterpolator = new LinearInterpolator();
	private float t;
	float touchX=-1,touchY=-1;
	private Random random;
	private boolean isTest = false;
	private PicObject testPicObj;
	public BezierView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        picPaint = new Paint();
        picPaint.setAntiAlias(true);
        paintQ = new Paint();    
        paintQ.setAntiAlias(true);    
        paintQ.setStyle(Style.STROKE);    
        paintQ.setStrokeWidth(5);    
        paintQ.setColor(Color.BLACK);
        redPaint = new Paint(paintQ);
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Style.FILL_AND_STROKE);
        random = new Random();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			if(state == 0){
				if(objs != null && objs.size() > 0){
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						touchX = event.getX();
						touchY = event.getY();
					}
				}
			}else if(state == 1){
				stop();
			}else if(state == 2){
				start();
			}
		}else if(event.getAction() == MotionEvent.ACTION_UP){
			if(state == 0){
				if(objs != null && objs.size() > 0){
					if(touchX != -1 && touchY != -1){
						start();
						touchX = touchY = -1;
					}
				}
			}
		}
		return true;
	}

	
	public BezierView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public BezierView(Context context) {
		this(context,null);
	}
	
	
	public void reset(){
		t = 0;
		state = 0;
		startTime = 0;
		stopDuringTime = 0;
		for(PicObject obj : objs){
			obj.reset();
		}
		invalidate();
	}
	
	public void cancle(){
		reset();
	}
	
	private void start() {
		if(state == 2){
			stopDuringTime += SystemClock.uptimeMillis() - stopTime;	
		}else{
			stopDuringTime = 0;
		}
		state = 1;
		invalidate();
	}
	
	public void stop(){
		if(state == 1){
			stopTime = SystemClock.uptimeMillis();
		}
		state = 2;
		invalidate();
	}
	
	public static interface Callback{
		public void onEnd();
		public void onStart();
	}
	
	public Callback callback;

	public void setCallback(Callback callback){
		this.callback = callback;
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		mWidth = getWidth();
		mHeight = getHeight();
		float scaleX = mWidth/(mBitmap.getWidth()*1.0f);
		float scaleY = mHeight/(mBitmap.getHeight()*1.0f);
		float scale = Math.min(scaleX, scaleY);
		
		Matrix matrix = new Matrix(); 
	    matrix.postScale(scale,scale); //长和宽放大缩小的比例
	    mBitmap = Bitmap.createBitmap(mBitmap,0,0,mBitmap.getWidth(),mBitmap.getHeight(),matrix,true);
	    
		int mLocX = (getWidth()-mBitmap.getWidth())/2;
		int mLocY = (getHeight()-mBitmap.getHeight())/2;
		mDstRect = new Rect(mLocX, mLocY,getWidth()-mLocX , getHeight()-mLocY);
		PicObject picObject;
		int xCount = (int) mBitmap.getWidth() / WIDTH;
		int yCount = (int) mBitmap.getHeight() / HEIGHT;
		for (int i = 0; i < xCount; i++) {
			for (int j = 0; j < yCount; j++) {
				picObject = new PicObject(mDstRect.left+i * WIDTH, 
										  mDstRect.top+j * HEIGHT,
										  WIDTH, 
										  HEIGHT,
										  Bitmap.createBitmap(mBitmap, i * WIDTH, j * HEIGHT,WIDTH, HEIGHT));
				objs.add(picObject);
			}
		}
		
//		if(mBitmap != null && !mBitmap.isRecycled() ){
//			mBitmap.recycle();
//		}
		
		if(isTest){
			int nextInt = random.nextInt(objs.size()-1);
			nextInt = nextInt > 0 ? nextInt:0 ;
			testPicObj = objs.get(nextInt);
		}
		
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if(state == 1){
			Log.v("BezierView", "startTime: "+startTime+",current:"+SystemClock.uptimeMillis());
			if(callback!=null){
				callback.onStart();
			}
			if(objs.isEmpty()){
				return;
			}
			if(startTime == 0){
				startTime = SystemClock.uptimeMillis();
			}
			overTime = SystemClock.uptimeMillis()-startTime-stopDuringTime;
			if(mInterpolator!=null){
				t = mInterpolator.getInterpolation(overTime / (duration * 1f));
			}
			if(overTime >= duration){
				reset();
				if(callback!=null){
					callback.onEnd();
				}
			}else{
				if(isTest){
					testPicObj.draw(canvas);
					drawPath(canvas,testPicObj);
				}else{
					for(PicObject obj : objs){
						obj.draw(canvas);
					}
				}
				invalidate();
			}
			Log.v("BezierView", ""+overTime);
		}else{
			picPaint.setAlpha(255);
			for(PicObject obj : objs){
				obj.draw(canvas);
			}
		}
	}


	private class PicObject {
		public float x,y,w,h;
		public float orgX,orgY;
		public Bitmap bitmap;
		public int alpha;
		public int rotate = 0;
		public int ROTATE = 1;
		public Point sPoint,cPoint,ePoint;
		public Path path;
//		public RectF rectf;
		public PicObject(float x, float y, float w, float h, Bitmap bitmap) {
			super();
			this.orgX = this.x = x;
			this.orgY = this.y = y;
			this.w = w;
			this.h = h;
//			this.rectf = new RectF(x, y, x+w,y+h);
			this.bitmap = bitmap;
			initPoint();
		}

		public void reset() {
			this.x = this.orgX;
			this.y = this.orgY;
		}

		public void draw(Canvas canvas) {
			init();
			picPaint.setAlpha(alpha);
			canvas.save();
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
					Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			canvas.rotate(rotate, x + w / 2, y + h / 2);
			canvas.drawBitmap(bitmap, x, y, picPaint);
			canvas.restore();
		}
		public void initPoint() {
			sPoint = new Point((int)x, (int)y);
			int tempX,tempY;
			if(sPoint.x >= touchX){
				tempX  = getRandom((int) sPoint.x, (int) (sPoint.x+mWidth/2));
			}else{
				tempX = getRandom((int)(sPoint.x-mWidth/2),(int) sPoint.x);
			}
			if(sPoint.y >= touchY){
				tempY  = getRandom((int) sPoint.y, (int)(sPoint.y+mHeight/2));
			}else{
				tempY  = getRandom((int)(sPoint.y-mHeight/2), (int) mHeight);
			}
			ePoint = new Point(tempX,tempY);
			int maxX = Math.max(sPoint.x , ePoint.x);
		    int minX = Math.min(sPoint.x , ePoint.x);
		    int maxY ,minY;
		    if(sPoint.y >= touchY){
		    	maxY = (int) sPoint.y;
		    	minY = (int) (sPoint.y-mHeight/2);
		    }else{
		    	maxY = mHeight;
		    	minY = (int) sPoint.y;
		    }
		    cPoint = new Point(getRandom(minX, maxX),getRandom(minY, maxY));
		    ROTATE = getRandom(0, 360*2);
		    
		    path = new Path();
			path.reset();
			path.moveTo(sPoint.x, sPoint.y);
			path.quadTo(cPoint.x, cPoint.y, ePoint.x, ePoint.y);
		}
	
		public void init(){
			   float oneMinusT = 1 - t;
			   x = oneMinusT*oneMinusT*sPoint.x+2*oneMinusT*t*cPoint.x+t*t*ePoint.x;
	           y = oneMinusT*oneMinusT*sPoint.y+2*oneMinusT*t*cPoint.y+t*t*ePoint.y;
//	           alpha = (int) (255*oneMinusT);
	           alpha = 255;
	           if(sPoint.x <= touchX){
	        	   rotate = -(int) (ROTATE*t);
	           }else{
	        	   rotate = (int) (ROTATE*t);
	           }
		}
		
		public int getRandom(int min,int max){
			if(max > 0){
				return random.nextInt(max)%(max-min+1) + min;
			}else{
				return max;
			}
		}
	}


//    private void drawImageWithGrid(Bitmap image){  
//        Canvas canvas = new Canvas(mBitmap);  
//        float w = mBitmap.getWidth();  
//        float h = mBitmap.getHeight();  
//        int xCount = (int)w/WIDTH;  
//        int yCount = (int)h/HEIGHT;  
//        Paint paint = new Paint();  
//        canvas.drawBitmap(image, 0, 0, paint);  
//        paint.setStyle(Paint.Style.STROKE);  
//        paint.setStrokeWidth(1);  
//        paint.setColor(0x8000FF00);  
//        for(int i = 0; i < xCount;i++){  
//            for(int j = 0; j < yCount; j++){  
//                canvas.drawRect(i*WIDTH, j*HEIGHT,i*WIDTH+WIDTH,j*HEIGHT+HEIGHT, paint);  
//                  
//            }  
//        }  
//    }  
    
    private void drawPath(Canvas canvas,PicObject obj) {
		paintQ.setColor(Color.BLACK);
		canvas.drawPath(obj.path, paintQ);
		canvas.drawPoint(obj.cPoint.x, obj.cPoint.y, redPaint);
		canvas.drawPoint(obj.sPoint.x, obj.sPoint.y, redPaint);
		canvas.drawPoint(obj.ePoint.x, obj.ePoint.y, redPaint);
//		paintQ.setColor(Color.BLUE);
//		Path path2 = new Path();
//		path2.reset();
//		path2.moveTo(sPoint.x, sPoint.y);
//		path2.lineTo(cPoint.x, cPoint.y);
//		canvas.drawPath(path2, paintQ);
//		path2.reset();
//		path2.moveTo(cPoint.x, cPoint.y);
//		path2.lineTo(ePoint.x, ePoint.y);
//		canvas.drawPath(path2, paintQ);
	}
	
}
