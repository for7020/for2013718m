package org.my.masonry;

import org.masonry.R;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class MasonryActivity extends Activity {
	private LayoutInflater layoutInflater;
	private ImageView iv_drag;
	private View fromView;
	private int fromViewId;
	private int toViewId;
	private int fromViewIndex;
	private int dragPosition;
	private int dropPosition;
	private int startX;
	private int startY;
	private boolean isUp;
	
	private Animation AtoB, BtoA, DelDone;
	private int mLastX, xtox;
	boolean isCountXY = false;
	private int mLastY, ytoy;
	private Masonry masonry;

	private WindowManager windowManager;
	private WindowManager.LayoutParams windowParams;
	private static final int[] ITEM_IDS = { 
			R.layout.small_item1, R.layout.small_item2,  R.layout.large_item,R.layout.small_item3,
			R.layout.small_item4, R.layout.large_item,R.layout.long_item,R.layout.small_item5, R.layout.small_item6,
			R.layout.small_item7};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.masonry);
		layoutInflater = getLayoutInflater();
	 masonry = (Masonry) findViewById(R.id.masonry);
		for (int i = 0; i < ITEM_IDS.length; i++) {
			View view = layoutInflater.inflate(ITEM_IDS[i], masonry, false);
			view.setTag(i);
			view.setId(ITEM_IDS[i]);
			view.setOnTouchListener(new MyOnTouchListener());
			masonry.addView(view);
		}
	}
	private class MyOnTouchListener implements OnTouchListener{
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			int x = (int) event.getX();
			int y = (int) event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (iv_drag != null) {
					if (!isCountXY) {
						xtox = x - mLastX;
						ytoy = y - mLastY;
						isCountXY = true;
					}
					onDrag(x, y);
				}
				break;
			case MotionEvent.ACTION_UP:
				isUp =true;
				System.out.println("MotionEvent.ACTION_UP");
				if (iv_drag != null) {
					stopDrag();
					x=(int) event.getRawX();
					y=(int) event.getRawY();
					onDrop(x,y);	
				}
				break;
			case MotionEvent.ACTION_DOWN:
				System.out.println("MotionEvent.ACTION_DOWN");
				isUp=false;
				Configure.isMove = true;
				int x1 = (int) event.getX();
				int y1 = (int) event.getY();
				mLastX = x1;
				mLastY = y1;
				fromViewId=v.getId();
				fromViewIndex=(Integer) v.getTag();
				dragPosition = dropPosition = fromViewIndex;
				fromView = v;
				int[] location = new int[2];
				v.getLocationOnScreen(location);
				startX = location[0];
				startY = location[1];
				fromView.destroyDrawingCache();
				fromView.setDrawingCacheEnabled(true);
				fromView.setDrawingCacheBackgroundColor(0xff6DB7ED);
				Bitmap bm = Bitmap.createBitmap(fromView
						.getDrawingCache());
				startDrag(bm, 0,0);
				break;
			}
			return true;
		}

	}
	
	/**
	 * @param x
	 * @param y
	 */
	private void getToViewId(int x, int y) {
		for (int i = 0; i < masonry.getChildCount(); i++) {
			View toView=masonry.findViewWithTag(i);
			int[] location = new int[2];
			toView.getLocationOnScreen(location);
			int left = location[0];
			int top = location[1];
			int right=left+toView.getWidth();
			int bottom=top+toView.getHeight();
			
			//
			System.out.println(left+","+top+"-----"+right+","+bottom+"-----"+x+","+y);
			if (x-left>0&&y-top>0&&x-right<0&&y-bottom<0) {
				if (fromViewIndex==i) {
					return;
				}
				System.out.println(toView.getId());
				toViewId=toView.getId();
				View fromView = masonry.findViewWithTag(fromViewIndex);
				masonry.removeViewInLayout(fromView);
				masonry.removeViewInLayout(toView);
				
				//
				toView= layoutInflater.inflate(toViewId, masonry, false);
				fromView= layoutInflater.inflate(fromViewId, masonry, false);
				fromView.setTag(i);
				toView.setTag(fromViewIndex);
				fromView.setId(fromViewId);
				toView.setId(toViewId);
				
				if (i<fromViewIndex) {
					masonry.addView(fromView, i);
					masonry.addView(toView, fromViewIndex);
				}else {
					masonry.addView(toView, fromViewIndex);
					masonry.addView(fromView, i);
				}
				toView.setOnTouchListener(new MyOnTouchListener());
				fromView.setOnTouchListener(new MyOnTouchListener());
			}
		}
		
	}

	private void startDrag(final Bitmap bm, final int x, final int y) {

		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);// "window"
		Animation disappear = AnimationUtils.loadAnimation(this, R.anim.out);
		disappear.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				fromView.setVisibility(8);
				System.out.println("onAnimationEnd");
				stopDrag();
				windowParams = new WindowManager.LayoutParams();
				windowParams.gravity = Gravity.TOP | Gravity.LEFT;
//				windowParams.x = fromView.getLeft();
//				windowParams.y = fromView.getTop();
				windowParams.x = startX;
				windowParams.y = startY;

				windowParams.alpha = 0.8f;
				windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
				windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;

				iv_drag = new ImageView(MasonryActivity.this);
				iv_drag.setImageBitmap(bm);
				windowManager.addView(iv_drag, windowParams);
				iv_drag.startAnimation(AnimationUtils.loadAnimation(
						MasonryActivity.this, R.anim.del_done));
				System.out.println("onAnimationEnd--over");
				if (isUp) {
				stopDrag();
				fromView.setVisibility(View.VISIBLE);
				}
			}
		});
		fromView.startAnimation(disappear);
	}

	private void onDrag(int x, int y) {
		if (iv_drag != null) {
			windowParams.alpha = 0.8f;
			windowParams.x = (x - mLastX - xtox) +startX;
			windowParams.y = (y - mLastY - ytoy) + startY;
			windowManager.updateViewLayout(iv_drag, windowParams);
		}
	}

	private void onDrop(final int x, final int y) {
		System.out.println("onDrop----");
//		fromView.setDrawingCacheBackgroundColor(0);
		Configure.isMove = false;
		if (dragPosition % 2 == 0) {
			AtoB = getDownAnimation((dropPosition % 2 == dragPosition % 2) ? 0
					: 1, (dropPosition / 2 - dragPosition / 2));
		} else {
			AtoB = getDownAnimation((dropPosition % 2 == dragPosition % 2) ? 0
					: -1, (dropPosition / 2 - dragPosition / 2));
		}
		fromView.startAnimation(AtoB);
		AtoB.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				System.out.println("getToViewId(x,y)");
				fromView.setVisibility(View.VISIBLE);
				getToViewId(x,y);
			}
		});
	}

	private void stopDrag() {
		System.out.println("stopDrag----");
		if (iv_drag != null) {
			windowManager.removeView(iv_drag);
			iv_drag = null;
		}
	}

	public Animation getDelAnimation() {
		AnimationSet set = new AnimationSet(true);
		RotateAnimation rotate = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setFillAfter(true);
		rotate.setDuration(550);
		AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
		alpha.setFillAfter(true);
		alpha.setDuration(550);
		set.addAnimation(alpha);
		set.addAnimation(rotate);
		return set;
	}

	public Animation getDownAnimation(float x, float y) {
		AnimationSet set = new AnimationSet(true);
		TranslateAnimation go = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, x, Animation.RELATIVE_TO_SELF, x,
				Animation.RELATIVE_TO_SELF, y, Animation.RELATIVE_TO_SELF, y);
		go.setFillAfter(true);
		go.setDuration(550);

		AlphaAnimation alpha = new AlphaAnimation(0.1f, 1.0f);
		alpha.setFillAfter(true);
		alpha.setDuration(550);

		ScaleAnimation scale = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f);
		scale.setFillAfter(true);
		scale.setDuration(550);

		set.addAnimation(go);
		set.addAnimation(alpha);
		set.addAnimation(scale);
		return set;
	}

	public Animation getMyAnimation(float x, float y) {
		TranslateAnimation go = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, x,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, y);
		go.setFillAfter(true);
		go.setDuration(550);
		return go;
	}

}