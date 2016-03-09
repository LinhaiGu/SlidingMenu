package com.example.slidingmenuproject.view;

import java.util.concurrent.Executors;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

/**
 * 
 * Created by glh on 2016/3/9.
 * 
 */
public class SlidingMenuView extends LinearLayout implements
		View.OnTouchListener {

	private Context mContext;

	/**
	 * 侧拉一半不到时的状态
	 */
	public static final int STATUS_FAIL = 0;

	/**
	 * 拉出一半多时松手状态
	 */
	public static final int STATUS_SUCESS = 1;

	/**
	 * 侧边栏隐藏状态
	 */
	public static final int STATUS_UNSTART = 2;

	/**
	 * 侧边栏完全拉出
	 */
	public static final int STATUS_OK = 3;

	/**
	 * 当前状态
	 */
	private int currentStatus = STATUS_UNSTART;

	/**
	 * 侧边栏
	 */
	private View mSlidingChildView;

	/**
	 * 在被判定为滚动之前用户手指可以移动的最大值
	 */
	private int touchSlop;

	/**
	 * 用于控制onLayout中的初始化只需加载一次
	 */
	private boolean once;

	/**
	 * 侧边栏的宽度
	 */
	private int mSlidingMenuWidth;

	/**
	 * 侧边栏的属性
	 */
	private MarginLayoutParams mSlidingMarginParams;

	/**
	 * 侧边拖动的黏性比率
	 */
	private static final float STICK_RATIO = .65f;

	/**
	 * 手指按下时屏幕Y坐标
	 */
	private float preDownY;

	/**
	 * 手指按下时屏幕X坐标
	 */
	private float preDownX;

	private boolean isUp = false;

	public SlidingMenuView(Context context) {
		this(context, null);
	}

	public SlidingMenuView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingMenuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		setOrientation(HORIZONTAL);
		touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
		setOnTouchListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !once) {
			mSlidingChildView = getChildAt(0);
			mSlidingMenuWidth = -mSlidingChildView.getWidth();
			mSlidingMarginParams = (MarginLayoutParams) mSlidingChildView
					.getLayoutParams();
			mSlidingMarginParams.leftMargin = mSlidingMenuWidth;
			once = true;
			currentStatus = STATUS_UNSTART;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isUp) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			preDownY = event.getRawY();
			preDownX = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			isUp = false;
			float currY = event.getRawY();
			float currX = event.getRawX();
			float distanceY = currY - preDownY;
			float distanceX = currX - preDownX;
			/**
			 * 如果是水平滑动小于等于垂直滑动，说明不需要滑动侧边栏
			 */
			if (Math.abs(distanceX) <= Math.abs(distanceY)) {
				return false;
			}

			/**
			 * 如果滑动距离小于最小滑动值，不需要滑动
			 */
			if (Math.abs(distanceX) < touchSlop) {
				return false;
			}

			float offsetX = distanceX * STICK_RATIO;

			if (mSlidingMarginParams.leftMargin > (mSlidingMenuWidth / 2)
					&& mSlidingMarginParams.leftMargin < 0) {
				/**
				 * 当侧边栏一半多时
				 */
				currentStatus = STATUS_SUCESS;
			} else if (mSlidingMarginParams.leftMargin < (mSlidingMenuWidth / 2)) {
				/**
				 * 侧边栏拉出一半不到时的状态；
				 */
				currentStatus = STATUS_FAIL;
			}

			if (mSlidingMarginParams.leftMargin > mSlidingMenuWidth
					&& offsetX < 0) {
				/**
				 * 侧边显示，往回滑动
				 */
				setSlidingMenuLeftMarign((int) (0 - Math.abs(offsetX)));
			} else if (mSlidingMarginParams.leftMargin == mSlidingMenuWidth
					&& offsetX < 0) {
				/**
				 * 侧边栏完全隐藏，并向左滑动，保持不变
				 */
				currentStatus = STATUS_UNSTART;
			} else if (mSlidingMarginParams.leftMargin == 0 && offsetX > 0) {
				/**
				 * 侧边栏完全显示，距离左边为0，并继续向右滑动时侧边栏保持不动
				 */
				currentStatus = STATUS_OK;
			} else {
				setSlidingMenuLeftMarign((int) (offsetX + mSlidingMenuWidth));
			}

			break;
		case MotionEvent.ACTION_UP:
			isUp = true;
			if (currentStatus == STATUS_FAIL) {
				/**
				 * 拉出一半不到时松手
				 */
				slidingMenuFail();
			}
			if (currentStatus == STATUS_SUCESS) {
				/**
				 * 拉出一半多时松手
				 */
				slidingMenuSucess();
			}

			if (currentStatus == STATUS_OK || currentStatus == STATUS_UNSTART) {
				/**
				 * 侧边栏完全显示或完全隐藏
				 */
				isUp = false;
			}

			break;
		default:
			break;
		}

		return true;
	}

	/**
	 * 设置侧边栏的位置
	 * 
	 * @param offset
	 */
	private void setSlidingMenuLeftMarign(int offset) {
		mSlidingMarginParams.leftMargin = offset;
		mSlidingChildView.setLayoutParams(mSlidingMarginParams);
	}

	/**
	 * 滑动一半不到时松手，侧边栏弹回原位隐藏
	 */
	private void slidingMenuFail() {
		// 从滑动时侧边栏距离坐边距离到mSlidingMenuWidth之间进行平滑回滚
		ValueAnimator leftAnimator = ValueAnimator.ofInt(
				mSlidingMarginParams.leftMargin, mSlidingMenuWidth);
		leftAnimator.setDuration(500);
		leftAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		leftAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						int marginValue = Integer.parseInt(animation
								.getAnimatedValue().toString());
						setSlidingMenuLeftMarign(marginValue);
					}
				});
		leftAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				currentStatus = STATUS_UNSTART;
				isUp = false;
			}
		});
		leftAnimator.start();
	}

	/**
	 * 滑动超过一半时松手，弹出侧边栏
	 */
	private void slidingMenuSucess() {
		ValueAnimator leftAnimator = ValueAnimator.ofInt(
				mSlidingMarginParams.leftMargin, 0);
		leftAnimator.setDuration(500);
		leftAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		leftAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						int marginValue = Integer.parseInt(animation
								.getAnimatedValue().toString());
						setSlidingMenuLeftMarign(marginValue);
					}
				});
		leftAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				currentStatus = STATUS_UNSTART;
				isUp = false;
			}
		});
		leftAnimator.start();
	}

}
