package com.juhezi.slipperylayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.content.ContentValues.TAG;

/**
 * Created by Juhezi on 2017/3/26.
 */

public class SlipperyLayout extends RelativeLayout {

    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    private @interface State {
    }

    public static final int LEFT = 1;
    public static final int RIGHT = 1 << 1;
    public static final int TOP = 1 << 2;
    public static final int BOTTOM = 1 << 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LEFT, RIGHT, TOP, BOTTOM})
    private @interface SlideGravity {
    }

    private static final int MIN_FLING_VELOCITY = 800;  //dips per second

    private boolean isLock = false;
    private boolean isMenuViewVisible = false;

    private static final int DEFAULT_SLIDE_GRAVITY = LEFT;

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
        loadDataFromAttrs(context, attrs);
        initView(context);
        placeView(context);
        mCallback = new ViewDragCallback(mSlideGravity);
        mDrager = ViewDragHelper.create(this, mCallback);
        mCallback.setDrager(mDrager);
    }

    public void setSlideGravity(@SlideGravity int slideGravity) {
        this.mSlideGravity = slideGravity;
        if (mCallback != null) {
            mCallback.setGravity(mSlideGravity);
        }
    }

    private void loadDataFromAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlipperyLayout);
        isLock = typedArray.getBoolean(R.styleable.SlipperyLayout_lock, false);
        int tempSlideGravity = typedArray.getInt(R.styleable.SlipperyLayout_slideGravity, DEFAULT_SLIDE_GRAVITY);
        /*switch (tempSlideGravity) {
            case 1:
                mSlideGravity = LEFT;
                break;
            case 2:
                mSlideGravity = RIGHT;
                break;
            case 4:
                mSlideGravity = TOP;
                break;
            case 8:
                mSlideGravity = BOTTOM;
                break;
        }*/
//        mContentViewLayoutRes = typedArray(R.styleable.SlipperyLayout_content, R.layout.content);
        typedArray.recycle();
    }

    private void placeView(Context context) {
        if (mMenuView == null || mContentView == null) {
            mContentView = new View(context);
            mMenuView = new View(context);
        }
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
        // TODO: 2017/3/27
    }

    public void closeMenuView() {
        // TODO: 2017/3/27
    }

    public boolean isLock() {
        return isLock;
    }

    public boolean isMenuViewVisible() {
        return isMenuViewVisible;
    }
/*
    public int getContentViewLayoutRes() {
        return mContentViewLayoutRes;
    }

    public void setContentViewLayoutRes(int contentViewLayoutRes) {
        this.mContentViewLayoutRes = contentViewLayoutRes;
    }

    public View getContentView() {
        return mContentView;
    }

    public void setContentView(View contentView) {
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
    }*/

    private int menuLeft;
    private int menuTop;
    private int menuRight;
    private int menuBottom;

    private int contentLeft;
    private int contentTop;
    private int contentRight;
    private int contentBottom;

    private int mMaxSlideDistanceX;
    private int mMaxSlideDistanceY;

    private int lastX = 0;
    private int lastY = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMaxSlideDistanceX = mMenuView.getMeasuredWidth();
        mMaxSlideDistanceY = mMenuView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        contentLeft = l;
        contentTop = t;
        contentRight = r;
        contentBottom = b;
        for (int i = getChildCount() - 1; i > 1; i--) {
            removeViewAt(i);
        }
        switch (mSlideGravity) {
            case LEFT:
                menuTop = contentTop;
                menuLeft = contentRight;
                menuRight = contentRight + mMenuView.getMeasuredWidth();
                menuBottom = contentBottom;
                break;
            case RIGHT:
                menuTop = contentTop;
                menuLeft = contentLeft - mMenuView.getMeasuredWidth();
                menuRight = contentLeft;
                menuBottom = contentBottom;
                break;
            case TOP:
                menuLeft = contentLeft;
                menuTop = contentBottom;
                menuRight = contentRight;
                menuBottom = contentBottom + mMenuView.getMeasuredHeight();
                break;
            case BOTTOM:
                menuLeft = contentLeft;
                menuTop = contentTop - mMenuView.getMeasuredHeight();
                menuRight = contentRight;
                menuBottom = contentTop;
                break;
        }
        mContentView.layout(contentLeft, contentTop, contentRight, contentBottom);
        mMenuView.layout(menuLeft, menuTop, menuRight, menuBottom);
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
                return RIGHT;
            } else {
                return LEFT;
            }
        } else {    //top or bottom
            if (dy > 0) {
                return BOTTOM;
            } else {
                return TOP;
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
                int moveGravity = getSlideGravityByMotionEvent(ev);
                //There need rethink.
                if (((moveGravity | mSlideGravity) & (RIGHT | LEFT)) != 0 &&
                        ((moveGravity | mSlideGravity) & (TOP | BOTTOM)) != 0
                        && mState != STATE_DRAGGING) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Invokde indirectly in {@link ViewCompat#postInvalidateOnAnimation(View)}
     */
    @Override
    public void computeScroll() {
        if (mDrager.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        private int mGravity; //滑动的方向
        private ViewDragHelper mDrager;

        public void setGravity(int gravity) {
            this.mGravity = gravity;
        }

        private int currentTransferX;
        private int currentTransferY;

        ViewDragCallback(int mGravity) {
            this.mGravity = mGravity;
        }

        public void setDrager(ViewDragHelper drager) {
            this.mDrager = drager;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return isCaptured(child) && !isLock();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {

            if (((RIGHT | LEFT) & mGravity) == 0) {
                return 0;
            }
            return mMaxSlideDistanceX;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            if (((TOP | BOTTOM) & mGravity) == 0) {
                return 0;
            }
            return mMaxSlideDistanceY;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (isLock) return 0;
            if (((RIGHT | LEFT) & mGravity) == 0) {
                return 0;
            }
            mState = STATE_DRAGGING;
            int arg1 = mGravity == LEFT ? -1 : 1;
            if (child == mContentView) {
                return getValueWithLimit(arg1 * mMaxSlideDistanceX, 0, left);
            }
            int arg2 = mGravity == LEFT ? 1 : 0;
            if (child == mMenuView) {
                return getValueWithLimit(arg2 * getMeasuredWidth() - mMaxSlideDistanceX, arg2 * getMeasuredWidth(), left);
            }
            return 0;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (isLock) return 0;
            if (((TOP | BOTTOM) & mGravity) == 0) {
                return 0;
            }
            mState = STATE_DRAGGING;
            int arg1 = mGravity == TOP ? -1 : 1;
            if (child == mContentView) {
                return getValueWithLimit(arg1 * mMaxSlideDistanceY, 0, top);
            }
            int arg2 = mGravity == TOP ? 1 : 0;
            if (child == mMenuView) {
                return getValueWithLimit(arg2 * getMeasuredHeight() - mMaxSlideDistanceY, arg2 * getMeasuredHeight(), top);
            }
            return 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mContentView) {
                currentTransferX = left - contentLeft;
                currentTransferY = top - contentTop;
                mMenuView.offsetLeftAndRight(dx);
                mMenuView.offsetTopAndBottom(dy);
            }
            if (changedView == mMenuView) {
                currentTransferX = left - menuLeft;
                currentTransferY = top - menuTop;
                mContentView.offsetLeftAndRight(dx);
                mContentView.offsetTopAndBottom(dy);
            }
            if (currentTransferX == 0 && currentTransferY == 0) {
                mMenuView.setVisibility(View.GONE);
                isMenuViewVisible = false;
                mState = STATE_IDLE;
            } else {
                mMenuView.setVisibility(View.VISIBLE);
                isMenuViewVisible = true;
                mState = STATE_IDLE;
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            mState = STATE_SETTLING;
            boolean isSlideToLeft;
            boolean isSlideToTop;
            int destX = contentLeft;
            int destY = contentTop;
            switch (mGravity) {
                case LEFT:
                    if (currentTransferX >= -mMaxSlideDistanceX / 2) {
                        isSlideToLeft = false;
                    } else {
                        isSlideToLeft = true;
                    }
                    if (xvel > MIN_FLING_VELOCITY) {
                        isSlideToLeft = false;
                    }
                    if (xvel < -MIN_FLING_VELOCITY) {
                        isSlideToLeft = true;
                    }
                    destX = isSlideToLeft ? contentLeft - mMaxSlideDistanceX : contentLeft;
                    break;
                case RIGHT:
                    if (currentTransferX >= mMaxSlideDistanceX / 2) {
                        isSlideToLeft = false;
                    } else {
                        isSlideToLeft = true;
                    }
                    if (xvel > MIN_FLING_VELOCITY) {
                        isSlideToLeft = false;
                    }
                    if (xvel < -MIN_FLING_VELOCITY) {
                        isSlideToLeft = true;
                    }
                    destX = isSlideToLeft ? contentLeft : contentLeft + mMaxSlideDistanceX;
                    break;
                case TOP:
                    if (currentTransferY >= -mMaxSlideDistanceY / 2) {
                        isSlideToTop = false;
                    } else {
                        isSlideToTop = true;
                    }
                    if (yvel > MIN_FLING_VELOCITY) {
                        isSlideToTop = false;
                    }
                    if (yvel < -MIN_FLING_VELOCITY) {
                        isSlideToTop = true;
                    }
                    destY = isSlideToTop ? contentTop - mMaxSlideDistanceY : contentTop;
                    break;
                case BOTTOM:
                    if (currentTransferY >= mMaxSlideDistanceY / 2) {
                        isSlideToTop = false;
                    } else {
                        isSlideToTop = true;
                    }
                    if (yvel > MIN_FLING_VELOCITY) {
                        isSlideToTop = false;
                    }
                    if (yvel < -MIN_FLING_VELOCITY) {
                        isSlideToTop = true;
                    }
                    destY = isSlideToTop ? contentTop : contentTop + mMaxSlideDistanceY;
                    break;
            }
            mDrager.smoothSlideViewTo(mContentView, destX, destY);
            ViewCompat.postInvalidateOnAnimation(SlipperyLayout.this);
        }
    }

    private int getValueWithLimit(int limit1, int limit2, int value) {
        if (limit1 > limit2) {
            return Math.min(Math.max(limit2, value), limit1);
        } else {
            return Math.min(Math.max(limit1, value), limit2);
        }
    }

    private boolean isCaptured(View child) {
        return child == mMenuView || child == mContentView;
    }

}
