package org.my.masonry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.masonry.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class Masonry extends ViewGroup {

	public static final int DURATION = 500;

	private boolean init;

	private int columnWidth; // 一列宽度加间距

	private int gutter; // 间距

	private int cols; // 列数

	private boolean animated;

	private int duration;

	private List<Integer> colYs; // 保存各列高度的集合

	private List<Style> styleQueue;

	private ExecutorService animator = Executors.newSingleThreadExecutor();

	public Masonry(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.Masonry);
		gutter = a.getDimensionPixelSize(R.styleable.Masonry_gutter, 0);
		columnWidth = a.getDimensionPixelSize(R.styleable.Masonry_columnWidth,
				0);
		animated = a.getBoolean(R.styleable.Masonry_animated, false);
		duration = a.getInt(R.styleable.Masonry_duration, DURATION);
		a.recycle();
	}

	public int getColumnWidth() {
		return columnWidth;
	}

	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	public int getGutter() {
		return gutter;
	}

	public void setGutter(int gutter) {
		this.gutter = gutter;
	}

	public boolean isAnimated() {
		return animated;
	}

	public void setAnimated(boolean animated) {
		this.animated = animated;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		List<View> bricks = new ArrayList<View>();
		for (int i = 0; i < getChildCount(); i++) {
			bricks.add(getChildAt(i));
		}
		// 一行总宽度
		int containerWidth = getMeasuredWidth();
		if (columnWidth == 0) {
			columnWidth = (bricks.isEmpty() ? containerWidth : bricks.get(0)
					.getMeasuredWidth()) + gutter;
		}
		// 根据columnWidth计算最多列数
		cols = Math.max(
				Double.valueOf(
						Math.floor((double) (containerWidth + gutter)
								/ columnWidth)).intValue(), 1);

		// 保存各列高度的集合
		colYs = new ArrayList<Integer>();
		for (int i = 0; i < cols; i++) {
			colYs.add(0);
		}
		styleQueue = new ArrayList<Style>();
		for (View brick : bricks) {
			// 计算子view所占列数
			int colSpan = Math.min(
					Double.valueOf(
							Math.ceil((double) brick.getMeasuredWidth()
									/ columnWidth)).intValue(), cols);

			// 子view只占一列
			if (colSpan == 1) {
				placeBrick(brick, colYs, styleQueue);
			} else {
				//计算列数
				int groupCount = cols - colSpan + 1;   
				//重新设置各列高度
				List<Integer> groupY = new ArrayList<Integer>();
				for (int i = 0; i < groupCount; i++) {
					groupY.add(Collections.max(colYs.subList(i, i + colSpan)));
				}
				placeBrick(brick, groupY, styleQueue);
			}
		}
		int heightSpec;
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			heightSpec = MeasureSpec.makeMeasureSpec(Collections.max(colYs),
					MeasureSpec.EXACTLY);
		} else {
			heightSpec = heightMeasureSpec;
		}
		super.onMeasure(widthMeasureSpec, heightSpec);
	}

	private void placeBrick(View brick, List<Integer> y, List<Style> styleQueue) {
		// 取得列中高度最小的列的高度
		int minimumY = Collections.min(y);
		int shortCol = 0;
		// 取得列中高度最小的列
		int len = y.size();
		for (int i = 0; i < len; i++) {
			if (y.get(i) == minimumY) {
				shortCol = i;
				break;
			}
		}

		Position position = new Position();
		position.top = minimumY; // 列中高度最小的列的高度为view的左上角Y坐标
		position.left = columnWidth * shortCol; // columnWidth列数倍为view的左上角X坐标
		Style style = new Style();
		style.brick = brick;
		style.position = position;
		styleQueue.add(style);
		// 计算当前列的高度
		int height = minimumY + gutter + brick.getMeasuredHeight();
		int span = cols + 1 - len;
		// 保存当前列的高度
		for (int i = 0; i < span; i++) {
			colYs.set(shortCol + i, height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (init && animated) {
			animator.submit(new Runnable() {
				@Override
				public void run() {
					final CountDownLatch done = new CountDownLatch(styleQueue
							.size());
					for (Style s : styleQueue) {
						final Position position = s.position;
						final View brick = s.brick;
						post(new Runnable() {
							@Override
							public void run() {
								int currentLeft = brick.getLeft();
								int currentTop = brick.getTop();
								int left = position.left;
								int top = position.top;
								brick.layout(left, top,
										left + brick.getMeasuredWidth(), top
												+ brick.getMeasuredHeight());
								TranslateAnimation anim = new TranslateAnimation(
										currentLeft - left, 0,
										currentTop - top, 0);
								anim.setDuration(duration);
								anim.setAnimationListener(new Animation.AnimationListener() {
									@Override
									public void onAnimationStart(
											Animation animation) {
									}

									@Override
									public void onAnimationRepeat(
											Animation animation) {
									}

									@Override
									public void onAnimationEnd(
											Animation animation) {
										done.countDown();
									}
								});
								brick.startAnimation(anim);
							}
						});
					}
					try {
						done.await();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			});
		} else {
			init = true;
			for (Style s : styleQueue) {
				Position position = s.position;
				View brick = s.brick;
				int left = position.left;
				int top = position.top;
				brick.layout(left, top, left + brick.getMeasuredWidth(), top
						+ brick.getMeasuredHeight());
			}
		}
	}

	private static class Style {
		View brick;
		Position position;
	}

	private static class Position {
		int top;
		int left;
	}

}
