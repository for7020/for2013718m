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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MasonryActivity extends Activity {
	private LayoutInflater layoutInflater;
	private ImageView iv_drag; // 拖动的影像
	
	private View fromView;
	private int fromViewId;
	private int fromViewIndex;
	
	private int toViewIndex; 
	private int toViewId;
	
	private int startX;  //view的左上角X坐标
	private int startY;		//view的左上角Y坐标

	private int mLastX;	  //点击屏幕的X坐标
	private int mLastY;		//点击屏幕的Y坐标
	
	private Masonry masonry;
	
	private boolean isUp;
	// 悬浮窗口
	private WindowManager windowManager;
	
	private MyOnTouchListener onTouchListener;

	private WindowManager.LayoutParams windowParams;
	
	private static final int[] ITEM_IDS = { R.layout.small_item1,
			R.layout.small_item2, R.layout.large_item, R.layout.small_item3,
			R.layout.small_item4, R.layout.large_item, R.layout.long_item,
			R.layout.small_item5, R.layout.small_item6, R.layout.small_item7 };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.masonry);
		layoutInflater = getLayoutInflater();
		masonry = (Masonry) findViewById(R.id.masonry);
		onTouchListener=new MyOnTouchListener();
		for (int i = 0; i < ITEM_IDS.length; i++) {
			View view = layoutInflater.inflate(ITEM_IDS[i], masonry, false);
			view.setTag(i);
			view.setId(ITEM_IDS[i]);
			view.setOnTouchListener(onTouchListener);
			masonry.addView(view);
		}
	}

	/**
	 * 触摸事件处理
	 */
	private class MyOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			int x = (int) event.getX();
			int y = (int) event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isUp=false;
				System.out.println("MotionEvent.ACTION_DOWN");
				Configure.isMove = true;
				//获取点击的view的信息
				fromViewId = v.getId();
				fromViewIndex = (Integer) v.getTag();
				fromView = v;
				//保存点击的x、y坐标
				mLastX = x;
				mLastY = y;
				//获取点击的view左上角坐标
				int[] location = new int[2];
				v.getLocationOnScreen(location);
				startX = location[0];
				startY = location[1];
				// 通过cache机制保存为bitmap
				fromView.setDrawingCacheEnabled(true); // 把cache开启
				Bitmap bm = Bitmap.createBitmap(fromView.getDrawingCache());
				
				startDrag(bm);
				
				break;
			case MotionEvent.ACTION_MOVE:
				if (iv_drag != null) {
					onDrag(x, y);
				}
				break;
			case MotionEvent.ACTION_UP:
				isUp=true;
				System.out.println("MotionEvent.ACTION_UP");
				if (iv_drag != null) {
					x = (int) event.getRawX();
					y = (int) event.getRawY();
					stopDrag();
					onDrop(x, y);
				}
				break;

			}

			// mLastX=x;mLastY=y;
			return true;
		}

	}

	/**
	 * 获得目标视图的ID
	 * 
	 * @param x
	 * @param y
	 */
	private void getToViewId(int x, int y) {
		for (int i = 0; i < masonry.getChildCount(); i++) {
			//获取子view的坐标范围
			View toView = masonry.findViewWithTag(i);
			int[] location = new int[2];
			toView.getLocationOnScreen(location);
			int left = location[0];
			int top = location[1];
			int right = left + toView.getWidth();
			int bottom = top + toView.getHeight();
			System.out.println(left + "," + top + "-----" + right + ","
					+ bottom + "-----" + x + "," + y);
			//获取xy坐标是否在子view的坐标范围
			if (x - left > 0 && y - top > 0 && x - right < 0 && y - bottom < 0) {
				System.out.println(toView.getId());
				//获取view的信息
				toViewId = toView.getId();
				toViewIndex=i;
				//相同的view不交换
				if (toViewIndex==fromViewIndex) {
					return;
				}
				changerView(fromView,toView);
				return;
			}
		}

	}
	
	/**
	 * 交换view的位置
	 * @param fromView
	 * @param toView
	 */
	private void changerView(View fromView, View toView) {
		//移除要交换位置的view
		masonry.removeViewInLayout(fromView);
		masonry.removeViewInLayout(toView);
		toView = layoutInflater.inflate(toViewId, masonry, false);
		fromView = layoutInflater.inflate(fromViewId, masonry, false);
		//重新设置view的信息
		fromView.setTag(toViewIndex);
		toView.setTag(fromViewIndex);
		fromView.setId(fromViewId);
		toView.setId(toViewId);
		//按位置添加view
		if (toViewIndex < fromViewIndex) {
			masonry.addView(fromView, toViewIndex);
			masonry.addView(toView, fromViewIndex);
		} else {
			masonry.addView(toView, fromViewIndex);
			masonry.addView(fromView, toViewIndex);
		}
		//重新设置view的监听
		toView.setOnTouchListener(onTouchListener);
		fromView.setOnTouchListener(onTouchListener);
	}

	/**
	 * 准备拖动，初始化拖动项的图像
	 * 
	 * @param bm
	 * @param x
	 * @param y
	 */
	private void startDrag(final Bitmap bm) {
		// 得到WindoeManager对象
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);// "window"
		fromView.setVisibility(View.INVISIBLE);
		stopDrag();
		windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.TOP | Gravity.LEFT;
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
		if (isUp) {
			stopDrag();
			fromView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 拖动执行
	 * 
	 * @param x
	 * @param y
	 */
	private void onDrag(int x, int y) {
		if (iv_drag != null) {
			windowParams.alpha = 0.8f;
			windowParams.x = (x - mLastX) + startX;
			windowParams.y = (y - mLastY) + startY;
			windowManager.updateViewLayout(iv_drag, windowParams);
		}
	}

	/**
	 * 拖动放下的时候
	 * 
	 * @param x
	 * @param y
	 */
	private void onDrop(int x, int y) {
		System.out.println("onDrop----");
		Configure.isMove = false;
		fromView.setVisibility(View.VISIBLE);
		getToViewId(x, y);

	}

	/**
	 * 停止拖动，删除影像
	 */
	private void stopDrag() {
		if (iv_drag != null) {
			windowManager.removeView(iv_drag);
			iv_drag = null;
		}
	}

}