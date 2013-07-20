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
	private ImageView iv_drag; // �϶���Ӱ��
	
	private View fromView;
	private int fromViewId;
	private int fromViewIndex;
	
	private int toViewIndex; 
	private int toViewId;
	
	private int startX;  //view�����Ͻ�X����
	private int startY;		//view�����Ͻ�Y����

	private int mLastX;	  //�����Ļ��X����
	private int mLastY;		//�����Ļ��Y����
	
	private Masonry masonry;
	
	private boolean isUp;
	// ��������
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
	 * �����¼�����
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
				//��ȡ�����view����Ϣ
				fromViewId = v.getId();
				fromViewIndex = (Integer) v.getTag();
				fromView = v;
				//��������x��y����
				mLastX = x;
				mLastY = y;
				//��ȡ�����view���Ͻ�����
				int[] location = new int[2];
				v.getLocationOnScreen(location);
				startX = location[0];
				startY = location[1];
				// ͨ��cache���Ʊ���Ϊbitmap
				fromView.setDrawingCacheEnabled(true); // ��cache����
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
	 * ���Ŀ����ͼ��ID
	 * 
	 * @param x
	 * @param y
	 */
	private void getToViewId(int x, int y) {
		for (int i = 0; i < masonry.getChildCount(); i++) {
			//��ȡ��view�����귶Χ
			View toView = masonry.findViewWithTag(i);
			int[] location = new int[2];
			toView.getLocationOnScreen(location);
			int left = location[0];
			int top = location[1];
			int right = left + toView.getWidth();
			int bottom = top + toView.getHeight();
			System.out.println(left + "," + top + "-----" + right + ","
					+ bottom + "-----" + x + "," + y);
			//��ȡxy�����Ƿ�����view�����귶Χ
			if (x - left > 0 && y - top > 0 && x - right < 0 && y - bottom < 0) {
				System.out.println(toView.getId());
				//��ȡview����Ϣ
				toViewId = toView.getId();
				toViewIndex=i;
				//��ͬ��view������
				if (toViewIndex==fromViewIndex) {
					return;
				}
				changerView(fromView,toView);
				return;
			}
		}

	}
	
	/**
	 * ����view��λ��
	 * @param fromView
	 * @param toView
	 */
	private void changerView(View fromView, View toView) {
		//�Ƴ�Ҫ����λ�õ�view
		masonry.removeViewInLayout(fromView);
		masonry.removeViewInLayout(toView);
		toView = layoutInflater.inflate(toViewId, masonry, false);
		fromView = layoutInflater.inflate(fromViewId, masonry, false);
		//��������view����Ϣ
		fromView.setTag(toViewIndex);
		toView.setTag(fromViewIndex);
		fromView.setId(fromViewId);
		toView.setId(toViewId);
		//��λ�����view
		if (toViewIndex < fromViewIndex) {
			masonry.addView(fromView, toViewIndex);
			masonry.addView(toView, fromViewIndex);
		} else {
			masonry.addView(toView, fromViewIndex);
			masonry.addView(fromView, toViewIndex);
		}
		//��������view�ļ���
		toView.setOnTouchListener(onTouchListener);
		fromView.setOnTouchListener(onTouchListener);
	}

	/**
	 * ׼���϶�����ʼ���϶����ͼ��
	 * 
	 * @param bm
	 * @param x
	 * @param y
	 */
	private void startDrag(final Bitmap bm) {
		// �õ�WindoeManager����
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
	 * �϶�ִ��
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
	 * �϶����µ�ʱ��
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
	 * ֹͣ�϶���ɾ��Ӱ��
	 */
	private void stopDrag() {
		if (iv_drag != null) {
			windowManager.removeView(iv_drag);
			iv_drag = null;
		}
	}

}