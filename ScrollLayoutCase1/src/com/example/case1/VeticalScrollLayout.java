package com.example.case1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/***
 * 垂直方向滑动子view容器
 */
public class VeticalScrollLayout extends ViewGroup {

	public static boolean startTouch = true;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private static int mCurScreen;
	
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	
	private int mTouchState = TOUCH_STATE_REST;
	private static final int SNAP_VELOCITY = 600;
	private int mTouchSlop;
	
	private float mLastMotionX;
	private float mLastMotionY;
	
	private OnScrollToScreenListener onScrollToScreen = null;
	
	public VeticalScrollLayout(Context context) {
		super(context);
	}
	
	public VeticalScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	public VeticalScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childBottom = 0;
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				final int childHeight = childView.getMeasuredHeight();
				childView.layout(0, childBottom,childWidth,
						childBottom+childHeight);
				childBottom += childHeight;
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"ScrollLayout only can run at EXACTLY mode!");
		}
		final int widthMode = MeasureSpec.getMode(heightMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"ScrollLayout only can run at EXACTLY mode!");
		}
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(0,mCurScreen * height);
		doScrollAction(mCurScreen);
	}

	public void snapToDestination() {
		final int screenHeight = getHeight();
		final int destScreen = (getScrollY() + screenHeight / 2) / screenHeight;
		snapToScreen(destScreen);
	}

	public void snapToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollY() != (whichScreen * getHeight())) {
			final int delta = whichScreen * getHeight() - getScrollY();
			mScroller.startScroll(0, getScrollY(), 0, delta,
					Math.abs(delta) * 2);
			mCurScreen = whichScreen;
			doScrollAction(mCurScreen);
			invalidate();
		}
	}

	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurScreen = whichScreen;
		scrollTo(whichScreen * getHeight(), 0);
		doScrollAction(whichScreen);
	}
	
	public int getCurScreen() {
		return mCurScreen;
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		final int action = event.getAction();
		final float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionY = y;
			break;
			
		case MotionEvent.ACTION_MOVE:
			int deltaY = (int) (mLastMotionY - y);
			mLastMotionY = y;
			if (!(mCurScreen == 0 && deltaY < 0 || mCurScreen == getChildCount() - 1
					&& deltaY > 0)) {
				scrollBy(0, deltaY);
			}
			break;
			
		case MotionEvent.ACTION_UP:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);
			int velocityY = (int) velocityTracker.getYVelocity();
			if (velocityY > SNAP_VELOCITY && mCurScreen > 0) {
				snapToScreen(mCurScreen - 1);
			} else if (velocityY < -SNAP_VELOCITY
					&& mCurScreen < getChildCount() - 1) {
				snapToScreen(mCurScreen + 1);
			} else {
				snapToDestination();
			}
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			break;
			
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return true;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;
			
		case MotionEvent.ACTION_MOVE:
			final int yDiff = (int) Math.abs(mLastMotionY - y);
			if (yDiff > mTouchSlop) {
				if (Math.abs(mLastMotionY - y) / Math.abs(mLastMotionX - x) < 1)
					mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
			
		case MotionEvent.ACTION_CANCEL:
			
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	private void doScrollAction(int whichScreen) {
		if (onScrollToScreen != null) {
			onScrollToScreen.doAction(whichScreen);
		}
	}

	public void setOnScrollToScreen(
			OnScrollToScreenListener paramOnScrollToScreen) {
		onScrollToScreen = paramOnScrollToScreen;
	}

	public abstract interface OnScrollToScreenListener {
		public void doAction(int whichScreen);
	}

	public void setDefaultScreen(int position) {
		mCurScreen = position;
	}
}
