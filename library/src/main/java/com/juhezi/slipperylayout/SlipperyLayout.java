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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Juhezi on 2017/3/26.
 */


public class SlipperyLayout extends RelativeLayout {

    public static final String TAG = "SlipperyLayout";

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

    public static final int[] GRAVITY_ARRAY = {LEFT, RIGHT, TOP, BOTTOM};

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

    private int contentDestX;
    private int contentDestY;

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
        loadDataFromAttrs(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
        placeView();
        mCallback = new ViewDragCallback(mSlideGravity);
        mDrager = ViewDragHelper.create(this, mCallback);
        mCallback.setDrager(mDrager);
    }

    private void loadDataFromAttrs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlipperyLayout, defStyleAttr, defStyleRes);
        isLock = typedArray.getBoolean(R.styleable.SlipperyLayout_lock, false);
        mSlideGravity = GRAVITY_ARRAY[typedArray.getInt(R.styleable.SlipperyLayout_slideGravity, 0)];
        mContentViewLayoutRes = typedArray.getResourceId(R.styleable.SlipperyLayout_content, 0);
        mMenuViewLayoutRes = typedArray.getResourceId(R.styleable.SlipperyLayout_menu, 0);
        typedArray.recycle();
    }

    private void placeView() {
        if (mMenuView == null || mContentView == null) {
            throw new NullPointerException("The content and menu can not be null!");
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
        if (isMenuViewVisible || isLock()) return;
        mDrager.smoothSlideViewTo(mContentView, contentDestX, contentDestY);
        ViewCompat.postInvalidateOnAnimation(SlipperyLayout.this);
        isMenuViewVisible = true;
    }

    public void closeMenuView() {
        if (!isMenuViewVisible || isLock()) return;
        mDrager.smoothSlideViewTo(mContentView, contentLeft, contentTop);
        ViewCompat.postInvalidateOnAnimation(SlipperyLayout.this);
        isMenuViewVisible = false;
    }

    public void setSlideGravity(@SlideGravity int slideGravity) {
        this.mSlideGravity = slideGravity;
        if (mCallback != null) {
            mCallback.setGravity(mSlideGravity);
        }
    }

    public boolean isLock() {
        return isLock;
    }

    public boolean isMenuViewVisible() {
        return isMenuViewVisible;
    }

    public View getMenuView() {
        return mMenuView;
    }

    public View getContentView() {
        return mContentView;
    }

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
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        contentLeft = getPaddingLeft();
        contentTop = getPaddingTop();
        contentRight = getMeasuredWidth() - getPaddingRight();
        contentBottom = getMeasuredHeight() - getPaddingBottom();
        for (int i = getChildCount() - 1; i > 1; i--) {
            removeViewAt(i);
        }
        switch (mSlideGravity) {
            case LEFT:
                menuTop = contentTop;
                menuLeft = contentRight;
                menuRight = contentRight + mMenuView.getMeasuredWidth();
                menuBottom = contentBottom;
                contentDestX = contentLeft - mMaxSlideDistanceX;
                contentDestY = contentTop;
                break;
            case RIGHT:
                menuTop = contentTop;
                menuLeft = contentLeft - mMenuView.getMeasuredWidth();
                menuRight = contentLeft;
                menuBottom = contentBottom;
                contentDestX = contentLeft + mMaxSlideDistanceX;
                contentDestY = contentTop;
                break;
            case TOP:
                menuLeft = contentLeft;
                menuTop = contentBottom;
                menuRight = contentRight;
                menuBottom = contentBottom + mMenuView.getMeasuredHeight();
                contentDestX = contentLeft;
                contentDestY = contentTop - mMaxSlideDistanceY;
                break;
            case BOTTOM:
                menuLeft = contentLeft;
                menuTop = contentTop - mMenuView.getMeasuredHeight();
                menuRight = contentRight;
                menuBottom = contentTop;
                contentDestX = contentLeft;
                contentDestY = contentTop + mMaxSlideDistanceY;
                break;
        }
        /**
         * 注意，这里的 left、top、right、bottom 都是相对于父布局的。
         */
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

    private final static int OBJECT_NONE = 0;
    private final static int OBJECT_PARENT = 1;
    private final static int OBJECT_SELF = 2;

    @IntDef({OBJECT_NONE, OBJECT_PARENT, OBJECT_SELF})
    private @interface ScrollObject {
    }

    @ScrollObject
    private int scrollObject = OBJECT_NONE;
    private boolean firstMove;

    /**
     * 一旦开始拦截了，那么这个事件序列会一直拦截
     * 一旦不拦截，那么就一直不拦截
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstMove = true;
                scrollObject = OBJECT_NONE;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveGravity = getSlideGravityByMotionEvent(ev);
                if (((moveGravity | mSlideGravity) & (RIGHT | LEFT)) != 0 &&
                        ((moveGravity | mSlideGravity) & (TOP | BOTTOM)) != 0
                        && mState == STATE_IDLE) {    //不拦截
                    if (firstMove) { //第一次移动
                        getParent().requestDisallowInterceptTouchEvent(false);
                        scrollObject = OBJECT_PARENT;
                        firstMove = false;
                    } else {    //之后的移动，根据滑动对象判断是否进行拦截
                        if (scrollObject == OBJECT_PARENT) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                } else {
                    if (firstMove) {
                        scrollObject = OBJECT_SELF;
                        getParent().requestDisallowInterceptTouchEvent(false);
                        firstMove = false;
                    } else {
                        if (scrollObject == OBJECT_SELF) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
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
            mState = STATE_DRAGGING;
            if (isLock) return 0;
            if (((RIGHT | LEFT) & mGravity) == 0) {
                return 0;
            }
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
            mState = STATE_DRAGGING;
            if (isLock) return 0;
            if (((TOP | BOTTOM) & mGravity) == 0) {
                return 0;
            }
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
            }
            if (contentLeft + currentTransferX == contentDestX
                    && contentTop + currentTransferY == contentDestY) {
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
