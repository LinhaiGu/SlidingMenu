# SlidingMenu
简易的侧边栏控件
前言

目前市面上包含侧边栏的APP比较多，自己在使用第三方控件时，就在想何不自己也做个属于自己的侧边栏控件呢，这也是写这篇文章的原因，不单单能用别人，还能写出自己侧边栏。看过一起玩转下拉刷新控件的同学们，再看这篇文章就比较容易理解，原理与下拉刷新控件一样。

下面就是今天所要完成的效果：

侧边栏滑动效果

侧边栏的原理

包含侧边栏的整体布局是这样的：

侧边栏布局

左边是我们的SlidingMenu，右边就是我们的显示的主界面，我们称为content。SlidingMenu的宽度我们暂且是slidingWidth。

那我们写的侧边栏控件比较简单，通过将SlidingMenu整体往左移动slidingWidth距离，这样我们的整体布局就显示完毕。如何将SlidingMenu从左边拖动出来呢，这里面我们通过监听整个控件的touch事件，通过滑动到手指离开屏幕时所滑动距离进行显示，SlidingMenu有个MarginLayoutParams属性，通过设置它的leftMargin可以达到SlidingMenu的进出。

代码展示

创建我们SlidingMenuView继承LinearLayout并实现OnTouchListener接口：

public class SlidingMenuView extends LinearLayout implements
        View.OnTouchListener {
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
}

进行相应的初始化，设置我们SlidingMenuView为水平布局，并获取我们滑动的最小距离，最后给我们SlidingMenuView设置touch事件。

在效果图可以看出，刚进入界面时，侧边栏是不显示的，这时需要我们去隐藏侧边栏，这个时候需要去重写onLayout方法来确定侧边栏的位置。

我们知道View的工作流程主要包括measure、layout、draw这三大流程，也就是测量、布局和绘制，measure用于确定View的测量宽和高，layout确定View的最终确定宽和高以及位置，而draw将View绘制到屏幕，在这里我们只实现onLayout方法，用于确定我们的侧边栏的位置。
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

onLayout方法中，我们获取我们的侧边栏mSlidingChildView ,并且设置侧边栏距离左边的距离，这里面的距离给的是负的侧边栏宽度的距离，这样刚好将侧边栏隐藏。

整个View的touch事件，也是比较简单，我们这里一步一步看，先看点击的时候：

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
        }

        return true;
    }

当手指点击屏幕时，只要获取点击时的坐标位置，上面的isUp是用于是否拦截touch事件，默认是不拦截。

当手指点击屏幕到滑动时的，注意几点：

什么时候是侧滑，也就是手指在屏幕上不是水平的滑动，这个时候需要判断是上下滑动，还是左右滑动？
滑动多远才算滑动？
侧边栏被滑出一半多时，松手怎么处理？
侧边栏被滑动一半都不到时，松手又该怎么处理？
侧边栏被全部显示时，再往右滑动，slidingmenu需不需要继续往右移动？
侧边栏被隐藏时，手指往左滑动，slidingmenu需不需要继续往左移动？如果往左移动会不会导致我们的主界面也跟着往左了，这不是我们想要看到效果。
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

由此，我们根据前面的几个问题，创建出上面的代码。这里给出侧边栏的几种状态：

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

在滑动的过程中侧边栏被滑出一半多，如何判断侧边栏被滑出一半多呢？在这里打个比方，我们的侧边栏宽度为80，一开始是被隐藏的，也就是距离左边屏幕-80，侧边栏被慢慢的滑出【-80,0】之间，因此当滑出的距离在【-40,0】之间说明侧边栏被滑出一半多，如以下代码：

if (mSlidingMarginParams.leftMargin > (mSlidingMenuWidth / 2)
                    && mSlidingMarginParams.leftMargin < 0) {
                /**
                 * 当侧边栏一半多时
                 */
                currentStatus = STATUS_SUCESS;
}

相反，如果滑出的距离在【-80，-40】之间就松开了手指，说明侧边栏一半都没有显示：

else if (mSlidingMarginParams.leftMargin < (mSlidingMenuWidth / 2)) {
                /**
                 * 侧边栏拉出一半不到时的状态；
                 */
                currentStatus = STATUS_FAIL;
}

侧边被全部显示后，通过触摸屏幕向左滑动，将侧边栏进行隐藏：

if (mSlidingMarginParams.leftMargin > mSlidingMenuWidth
                    && offsetX < 0) {
                /**
                 * 侧边显示，往回滑动
                 */
                setSlidingMenuLeftMarign((int) (0 - Math.abs(offsetX)));
} 

当侧边被全部显示时，距离左边屏幕就为0，向左滑动时，滑动距离为负值，因此将滑动距离变为正数，这时向左滑动的范围【0，-slidingmenu的宽度】。这时一直往左滑动，直到SlidingMenu被完全隐藏，这时侧边栏不应该继续移动：

else if (mSlidingMarginParams.leftMargin == mSlidingMenuWidth
                    && offsetX < 0) {
                /**
                 * 侧边栏完全隐藏，并向左滑动，保持不变
                 */
                currentStatus = STATUS_UNSTART;
} 

如果侧边栏全部显示，这时继续向右滑动，侧边栏应该保持不变：

else if (mSlidingMarginParams.leftMargin == 0 && offsetX > 0) {
        /**
        * 侧边栏完全显示，距离左边为0，并继续向右滑动时侧边栏保持不动
        */
        currentStatus = STATUS_OK;
    } else {
        setSlidingMenuLeftMarign((int) (offsetX + mSlidingMenuWidth));
    }

setSlidingMenuLeftMarign方法比较简单，不停的给SlidingtMeun的leftMargin赋值，用以改变自身的位置。

到这里滑动时侧边栏移动就已经介绍完，最后的就是手指离开屏幕后的操作了，之前我们在滑动时定义了几种状态值，这时在手指离开屏幕后就能排上用处：

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

侧边栏显示一半多松手，这时应该将侧边栏平滑的显示出来，我们看slidingMenuSucess方法：

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

这里重点关照ValueAnimator.ofInt(mSlidingMarginParams.leftMargin, 0)这句话，当侧边栏被移出一半多时松手到侧边栏全部显示，这期间的轨迹应该是这样的【>-40,0】，这里面的大于-40就是上面的leftMargin，通过这个轨迹一直给侧边栏的leftMargin赋值，直到距离屏幕左边为0为止。

那侧边栏被移出一半不到松手，侧边重新进入隐藏状态，这里面的逻辑应该难不倒你们了吧：

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

最后给出完整的代码，整个项目在下方github地址下载：

package com.example.slidingmenuproject.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
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


<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >

    <com.example.slidingmenuproject.view.SlidingMenuView
        android:id="@+id/sliding_menu_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <include layout="@layout/sliding_menu_layout" />

        <include layout="@layout/content_layout" />

    </com.example.slidingmenuproject.view.SlidingMenuView>

</RelativeLayout>



