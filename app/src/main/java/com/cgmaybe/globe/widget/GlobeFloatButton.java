package com.cgmaybe.globe.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.cgmaybe.globe.R;

/**
 * Created by moubiao on 2016/12/13.
 * window上面的悬浮按钮
 */

public class GlobeFloatButton extends View {
    private static final String TAG = "moubiao";

    private int mFloatWidth;
    private int mFloatHeight;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWmParams;
    private float mTouchStartX;
    private float mTouchStartY;
    private int mScreenWidth;
    private int mScreenHeight;
    private long lastClickTime;

    private int mTouchSlop;
    private boolean mSlide = false;

    public GlobeFloatButton(Context context) {
        this(context, null);
    }

    public GlobeFloatButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlobeFloatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setClickable(true);
        createView(context);

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GlobeFloatButtonStyle, defStyleAttr, 0);
        mFloatWidth = typedArray.getDimensionPixelSize(R.styleable.GlobeFloatButtonStyle_float_width, 50);
        mFloatHeight = typedArray.getDimensionPixelSize(R.styleable.GlobeFloatButtonStyle_float_height, 50);
        typedArray.recycle();

        mTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
    }

    private void createView(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWmParams = new WindowManager.LayoutParams();
        // 设置window type 下面变量2002是在屏幕区域显示，2003则可以显示在状态栏之上
        // wmParams.type = LayoutParams.TYPE_PHONE;
//        mWmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//        mWmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;//等于API19或API19以下需要指定窗口参数type值为TYPE_TOAST才可以作为悬浮控件显示出来
        } else {
            mWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;//API19以上侧只需指定为TYPE_PHONE即可
        }
        // 设置图片格式，效果为背景透明
        mWmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        // wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        // 设置可以显示在状态栏上
        mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        // 设置悬浮窗口长宽数据
        mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mWmParams.gravity = Gravity.START | Gravity.TOP;
        mWmParams.x = dp2px(6f);
        mWmParams.y = dp2px(60f);
        mWindowManager.addView(this, mWmParams);
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mFloatWidth, mFloatHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        long intervalTime = System.currentTimeMillis() - lastClickTime;
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (intervalTime > 500) {
                    float mMoveStartX = event.getX();
                    float mMoveStartY = event.getY();
                    if (!mSlide && Math.abs(mTouchStartX - mMoveStartX) > mTouchSlop && Math.abs(mTouchStartY - mMoveStartY) > mTouchSlop) {
                        mSlide = true;
                    }
                    if (mSlide) {
                        mWmParams.x = (int) (x - mTouchStartX);
                        mWmParams.y = (int) (y - mTouchStartY);
                        mWindowManager.updateViewLayout(this, mWmParams);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastClickTime = System.currentTimeMillis();
                if (mWmParams.x >= mScreenWidth / 2) {
                    mWmParams.x = mScreenWidth - dp2px(6f);
                } else if (mWmParams.x < mScreenWidth / 2) {
                    mWmParams.x = dp2px(6f);
                }
                if (mWmParams.y >= mScreenHeight - dp2px(60f)) {
                    mWmParams.y = mScreenHeight - dp2px(60f);
                } else if (mWmParams.y < dp2px(60f)) {
                    mWmParams.y = dp2px(60f);
                }
                mWindowManager.updateViewLayout(this, mWmParams);
                mTouchStartX = mTouchStartY = 0;
                mSlide = false;
                break;
        }

        return true;
    }

    public int getFloatWidth() {
        return mFloatWidth;
    }

    public void setFloatWidth(int floatWidth) {
        mFloatWidth = floatWidth;
    }

    public int getFloatHeight() {
        return mFloatHeight;
    }

    public void setFloatHeight(int floatHeight) {
        mFloatHeight = floatHeight;
    }

    private int dp2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
