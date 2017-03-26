package com.juhezi.slipperylayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Juhezi on 2017/3/26.
 */

public class SlipperyLayout extends RelativeLayout {

    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    private @interface State {
    }

    @IntDef({Gravity.LEFT, Gravity.RIGHT, Gravity.TOP, Gravity.BOTTOM})

    private @interface SlideGravity {
    }

    private static final int MIN_FLING_VELOCITY = 600;  //dips per second

    private boolean isLock = false;
    private boolean isMenuViewVisible = false;

    private static final int DEFAULT_SLIDE_GRAVITY = Gravity.LEFT;

    @State
    private int mState = STATE_IDLE;

    @SlideGravity
    private int mSlideGravity = DEFAULT_SLIDE_GRAVITY;

    @LayoutRes
    private int mContentViewLayoutRes = -1;

    @LayoutRes
    private int mMenuViewLayoutRes = -1;

    private View mMenuView = null;
    private View mContentView = null;

    private ViewDragHelper mDrager;
    private ViewDragCallback mCallback;

    public SlipperyLayout(Context context) {
        this(context, null);
    }

    public SlipperyLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlipperyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SlipperyLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        loadDataFromAttrs();
        initView(context);
        placeView();
        mCallback = new ViewDragCallback(mSlideGravity);
        mDrager = ViewDragHelper.create(this, mCallback);
        mCallback.setDrager(mDrager);
    }

    private void loadDataFromAttrs() {

    }

    private void placeView() {
        if (mMenuView == null || mContentView == null)
            throw new NullPointerException("The menuView and contentView can not be null!");
        addView(mContentView, 0);
        addView(mMenuView, 1);
    }

    private void initView(Context context) {
        if (mMenuViewLayoutRes != -1)
            mMenuView = LayoutInflater.from(context).inflate(mMenuViewLayoutRes, this, false);
        if (mContentViewLayoutRes != -1)
            mContentView = LayoutInflater.from(context).inflate(mContentViewLayoutRes, this, false);
    }

    public void openMenuView() {

    }

    public void closeMenuView() {

    }

    public boolean isLock() {
        return isLock;
    }

    public boolean isMenuViewVisible() {
        return isMenuViewVisible;
    }

    public int getContentViewLayoutRes() {
        return mContentViewLayoutRes;
    }

    public void setContentViewLayoutRes(int contentViewLayoutRes) {
        this.mContentViewLayoutRes = contentViewLayoutRes;
    }

    public View getContentView() {
        return mContentView;
    }

    public void setmContentView(View contentView) {
        this.mContentView = contentView;
    }

    public int getMenuViewLayoutRes() {
        return mMenuViewLayoutRes;
    }

    public void setMenuViewLayoutRes(int menuViewLayoutRes) {
        this.mMenuViewLayoutRes = menuViewLayoutRes;
    }

    public View getMenuView() {
        return mMenuView;
    }

    public void setMenuView(View menuView) {
        this.mMenuView = menuView;
    }

    private int menuLeft;
    private int menuTop;
    private int menuRight;
    private int menuBottom;

    private int mMaxSlideDistacneX;
    private int mMaxSlideDistanceY;

    private int lastX = 0;
    private int lastY = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMaxSlideDistacneX = mMenuView.getMeasuredWidth();
        mMaxSlideDistanceY = mMenuView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = getChildCount() - 1; i > 1; i--) {
            removeViewAt(i);
        }
        switch (mSlideGravity) {
            case Gravity.LEFT:
                menuTop = t;
                menuLeft = r;
                menuRight = r + mMenuView.getMeasuredWidth();
                menuBottom = b;
                break;
            case Gravity.RIGHT:
                menuTop = t;
                menuLeft = l - mMenuView.getMeasuredWidth();
                menuRight = l;
                menuBottom = b;
                break;
            case Gravity.TOP:
                menuLeft = l;
                menuTop = b;
                menuRight = r;
                menuBottom = b + mMenuView.getMeasuredHeight();
                break;
            case Gravity.BOTTOM:
                menuLeft = l;
                menuTop = t - mMenuView.getMeasuredHeight();
                menuRight = r;
                menuBottom = t;
                break;
        }
        mContentView.layout(l, t, r, b);
        mContentView.layout(menuLeft, menuTop, menuRight, menuBottom);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_CANCEL ||
                ev.getAction() == MotionEvent.ACTION_UP) {
            mDrager.cancel();
            return false;
        }
        return mDrager.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDrager.processTouchEvent(event);
        return true;
    }

    @SlipperyLayout.SlideGravity
    private int getSlideGravityByMotionEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        int dx = x - lastX;
        int dy = y - lastY;
        lastX = x;
        lastY = y;
        if (Math.abs(dx) > Math.abs(dy)) {   //left or right
            if (dx > 0) {
                return Gravity.RIGHT;
            } else {
                return Gravity.LEFT;
            }
        } else {    //top or bottom
            if (dy > 0) {
                return Gravity.BOTTOM;
            } else {
                return Gravity.TOP;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:

                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        private final int mGravity; //滑动的方向
        private ViewDragHelper mDrager;

        ViewDragCallback(int mGravity) {
            this.mGravity = mGravity;
        }

        public void setDrager(ViewDragHelper drager) {
            this.mDrager = drager;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return false;
        }
    }

}
