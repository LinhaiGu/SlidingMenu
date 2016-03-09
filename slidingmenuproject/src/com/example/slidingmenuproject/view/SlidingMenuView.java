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
	 * ����һ�벻��ʱ��״̬
	 */
	public static final int STATUS_FAIL = 0;

	/**
	 * ����һ���ʱ����״̬
	 */
	public static final int STATUS_SUCESS = 1;

	/**
	 * ���������״̬
	 */
	public static final int STATUS_UNSTART = 2;

	/**
	 * �������ȫ����
	 */
	public static final int STATUS_OK = 3;

	/**
	 * ��ǰ״̬
	 */
	private int currentStatus = STATUS_UNSTART;

	/**
	 * �����
	 */
	private View mSlidingChildView;

	/**
	 * �ڱ��ж�Ϊ����֮ǰ�û���ָ�����ƶ������ֵ
	 */
	private int touchSlop;

	/**
	 * ���ڿ���onLayout�еĳ�ʼ��ֻ�����һ��
	 */
	private boolean once;

	/**
	 * ������Ŀ��
	 */
	private int mSlidingMenuWidth;

	/**
	 * �����������
	 */
	private MarginLayoutParams mSlidingMarginParams;

	/**
	 * ����϶�����Ա���
	 */
	private static final float STICK_RATIO = .65f;

	/**
	 * ��ָ����ʱ��ĻY����
	 */
	private float preDownY;

	/**
	 * ��ָ����ʱ��ĻX����
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
	 * ��ʼ��
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
			 * �����ˮƽ����С�ڵ��ڴ�ֱ������˵������Ҫ���������
			 */
			if (Math.abs(distanceX) <= Math.abs(distanceY)) {
				return false;
			}

			/**
			 * �����������С����С����ֵ������Ҫ����
			 */
			if (Math.abs(distanceX) < touchSlop) {
				return false;
			}

			float offsetX = distanceX * STICK_RATIO;

			if (mSlidingMarginParams.leftMargin > (mSlidingMenuWidth / 2)
					&& mSlidingMarginParams.leftMargin < 0) {
				/**
				 * �������һ���ʱ
				 */
				currentStatus = STATUS_SUCESS;
			} else if (mSlidingMarginParams.leftMargin < (mSlidingMenuWidth / 2)) {
				/**
				 * ���������һ�벻��ʱ��״̬��
				 */
				currentStatus = STATUS_FAIL;
			}

			if (mSlidingMarginParams.leftMargin > mSlidingMenuWidth
					&& offsetX < 0) {
				/**
				 * �����ʾ�����ػ���
				 */
				setSlidingMenuLeftMarign((int) (0 - Math.abs(offsetX)));
			} else if (mSlidingMarginParams.leftMargin == mSlidingMenuWidth
					&& offsetX < 0) {
				/**
				 * �������ȫ���أ������󻬶������ֲ���
				 */
				currentStatus = STATUS_UNSTART;
			} else if (mSlidingMarginParams.leftMargin == 0 && offsetX > 0) {
				/**
				 * �������ȫ��ʾ���������Ϊ0�����������һ���ʱ��������ֲ���
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
				 * ����һ�벻��ʱ����
				 */
				slidingMenuFail();
			}
			if (currentStatus == STATUS_SUCESS) {
				/**
				 * ����һ���ʱ����
				 */
				slidingMenuSucess();
			}

			if (currentStatus == STATUS_OK || currentStatus == STATUS_UNSTART) {
				/**
				 * �������ȫ��ʾ����ȫ����
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
	 * ���ò������λ��
	 * 
	 * @param offset
	 */
	private void setSlidingMenuLeftMarign(int offset) {
		mSlidingMarginParams.leftMargin = offset;
		mSlidingChildView.setLayoutParams(mSlidingMarginParams);
	}

	/**
	 * ����һ�벻��ʱ���֣����������ԭλ����
	 */
	private void slidingMenuFail() {
		// �ӻ���ʱ������������߾��뵽mSlidingMenuWidth֮�����ƽ���ع�
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
	 * ��������һ��ʱ���֣����������
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
