package com.example.rafa.liquidgalaxypoiscontroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.jcraft.jsch.ChannelSftp;
import java.util.ArrayList;

public class DynamicListView extends ListView {
    private static final TypeEvaluator<Rect> sBoundEvaluator;
    private final int INVALID_ID;
    private final int INVALID_POINTER_ID;
    private final int LINE_THICKNESS;
    private final int MOVE_DURATION;
    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE;
    private long mAboveItemId;
    private int mActivePointerId;
    private long mBelowItemId;
    private boolean mCellIsMobile;
    public ArrayList<String> mCheeseList;
    private int mDownX;
    private int mDownY;
    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;
    private boolean mIsMobileScrolling;
    private boolean mIsWaitingForScrollFinish;
    private int mLastEventY;
    private long mMobileItemId;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnScrollListener mScrollListener;
    private int mScrollState;
    private int mSmoothScrollAmountAtEdge;
    private int mTotalOffset;

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.DynamicListView.1 */
    class C01671 implements OnItemLongClickListener {
        C01671() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int pos, long id) {
            DynamicListView.this.mTotalOffset = 0;
            int position = DynamicListView.this.pointToPosition(DynamicListView.this.mDownX, DynamicListView.this.mDownY);
            View selectedView = DynamicListView.this.getChildAt(position - DynamicListView.this.getFirstVisiblePosition());
            DynamicListView.this.mMobileItemId = DynamicListView.this.getAdapter().getItemId(position);
            DynamicListView.this.mHoverCell = DynamicListView.this.getAndAddHoverView(selectedView);
            selectedView.setVisibility(View.INVISIBLE);
            DynamicListView.this.mCellIsMobile = true;
            DynamicListView.this.updateNeighborViewsForID(DynamicListView.this.mMobileItemId);
            return true;
        }
    }

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.DynamicListView.2 */
    class C01682 implements OnPreDrawListener {
        final /* synthetic */ int val$deltaY;
        final /* synthetic */ ViewTreeObserver val$observer;
        final /* synthetic */ long val$switchItemID;
        final /* synthetic */ int val$switchViewStartTop;

        C01682(ViewTreeObserver viewTreeObserver, long j, int i, int i2) {
            this.val$observer = viewTreeObserver;
            this.val$switchItemID = j;
            this.val$deltaY = i;
            this.val$switchViewStartTop = i2;
        }

        public boolean onPreDraw() {
            this.val$observer.removeOnPreDrawListener(this);
            View switchView = DynamicListView.this.getViewForID(this.val$switchItemID);
            DynamicListView.this.mTotalOffset = DynamicListView.this.mTotalOffset + this.val$deltaY;
            switchView.setTranslationY((float) (this.val$switchViewStartTop - switchView.getTop()));
            ObjectAnimator animator = ObjectAnimator.ofFloat(switchView, View.TRANSLATION_Y, new float[]{0.0f});
            animator.setDuration(150);
            animator.start();
            return true;
        }
    }

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.DynamicListView.3 */
    class C01693 implements AnimatorUpdateListener {
        C01693() {
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            DynamicListView.this.invalidate();
        }
    }

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.DynamicListView.4 */
    class C01704 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$mobileView;

        C01704(View view) {
            this.val$mobileView = view;
        }

        public void onAnimationStart(Animator animation) {
            DynamicListView.this.setEnabled(false);
        }

        public void onAnimationEnd(Animator animation) {
            DynamicListView.this.mAboveItemId = -1;
            DynamicListView.this.mMobileItemId = -1;
            DynamicListView.this.mBelowItemId = -1;
            this.val$mobileView.setVisibility(View.VISIBLE);
            DynamicListView.this.mHoverCell = null;
            DynamicListView.this.setEnabled(true);
            DynamicListView.this.invalidate();
        }
    }

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.DynamicListView.5 */
    static class C01715 implements TypeEvaluator<Rect> {
        C01715() {
        }

        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (((float) start) + (((float) (end - start)) * fraction));
        }
    }

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.DynamicListView.6 */
    class C01726 implements OnScrollListener {
        private int mCurrentFirstVisibleItem;
        private int mCurrentScrollState;
        private int mCurrentVisibleItemCount;
        private int mPreviousFirstVisibleItem;
        private int mPreviousVisibleItemCount;

        C01726() {
            this.mPreviousFirstVisibleItem = -1;
            this.mPreviousVisibleItemCount = -1;
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            this.mCurrentFirstVisibleItem = firstVisibleItem;
            this.mCurrentVisibleItemCount = visibleItemCount;
            this.mPreviousFirstVisibleItem = this.mPreviousFirstVisibleItem == -1 ? this.mCurrentFirstVisibleItem : this.mPreviousFirstVisibleItem;
            this.mPreviousVisibleItemCount = this.mPreviousVisibleItemCount == -1 ? this.mCurrentVisibleItemCount : this.mPreviousVisibleItemCount;
            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();
            this.mPreviousFirstVisibleItem = this.mCurrentFirstVisibleItem;
            this.mPreviousVisibleItemCount = this.mCurrentVisibleItemCount;
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            this.mCurrentScrollState = scrollState;
            DynamicListView.this.mScrollState = scrollState;
            isScrollCompleted();
        }

        private void isScrollCompleted() {
            if (this.mCurrentVisibleItemCount > 0 && this.mCurrentScrollState == 0) {
                if (DynamicListView.this.mCellIsMobile && DynamicListView.this.mIsMobileScrolling) {
                    DynamicListView.this.handleMobileCellScroll();
                } else if (DynamicListView.this.mIsWaitingForScrollFinish) {
                    DynamicListView.this.touchEventsEnded();
                }
            }
        }

        public void checkAndHandleFirstVisibleCellChange() {
            if (this.mCurrentFirstVisibleItem != this.mPreviousFirstVisibleItem && DynamicListView.this.mCellIsMobile && DynamicListView.this.mMobileItemId != -1) {
                DynamicListView.this.updateNeighborViewsForID(DynamicListView.this.mMobileItemId);
                DynamicListView.this.handleCellSwitch();
            }
        }

        public void checkAndHandleLastVisibleCellChange() {
            if (this.mCurrentFirstVisibleItem + this.mCurrentVisibleItemCount != this.mPreviousFirstVisibleItem + this.mPreviousVisibleItemCount && DynamicListView.this.mCellIsMobile && DynamicListView.this.mMobileItemId != -1) {
                DynamicListView.this.updateNeighborViewsForID(DynamicListView.this.mMobileItemId);
                DynamicListView.this.handleCellSwitch();
            }
        }
    }

    public DynamicListView(Context context) {
        super(context);
        this.SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
        this.MOVE_DURATION = 150;
        this.LINE_THICKNESS = 15;
        this.mLastEventY = -1;
        this.mDownY = -1;
        this.mDownX = -1;
        this.mTotalOffset = 0;
        this.mCellIsMobile = false;
        this.mIsMobileScrolling = false;
        this.mSmoothScrollAmountAtEdge = 0;
        this.INVALID_ID = -1;
        this.mAboveItemId = -1;
        this.mMobileItemId = -1;
        this.mBelowItemId = -1;
        this.INVALID_POINTER_ID = -1;
        this.mActivePointerId = -1;
        this.mIsWaitingForScrollFinish = false;
        this.mScrollState = 0;
        this.mOnItemLongClickListener = new C01671();
        this.mScrollListener = new C01726();
        init(context);
    }

    public DynamicListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
        this.MOVE_DURATION = 150;
        this.LINE_THICKNESS = 15;
        this.mLastEventY = -1;
        this.mDownY = -1;
        this.mDownX = -1;
        this.mTotalOffset = 0;
        this.mCellIsMobile = false;
        this.mIsMobileScrolling = false;
        this.mSmoothScrollAmountAtEdge = 0;
        this.INVALID_ID = -1;
        this.mAboveItemId = -1;
        this.mMobileItemId = -1;
        this.mBelowItemId = -1;
        this.INVALID_POINTER_ID = -1;
        this.mActivePointerId = -1;
        this.mIsWaitingForScrollFinish = false;
        this.mScrollState = 0;
        this.mOnItemLongClickListener = new C01671();
        this.mScrollListener = new C01726();
        init(context);
    }

    public DynamicListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
        this.MOVE_DURATION = 150;
        this.LINE_THICKNESS = 15;
        this.mLastEventY = -1;
        this.mDownY = -1;
        this.mDownX = -1;
        this.mTotalOffset = 0;
        this.mCellIsMobile = false;
        this.mIsMobileScrolling = false;
        this.mSmoothScrollAmountAtEdge = 0;
        this.INVALID_ID = -1;
        this.mAboveItemId = -1;
        this.mMobileItemId = -1;
        this.mBelowItemId = -1;
        this.INVALID_POINTER_ID = -1;
        this.mActivePointerId = -1;
        this.mIsWaitingForScrollFinish = false;
        this.mScrollState = 0;
        this.mOnItemLongClickListener = new C01671();
        this.mScrollListener = new C01726();
        init(context);
    }

    public void init(Context context) {
        setOnItemLongClickListener(this.mOnItemLongClickListener);
        setOnScrollListener(this.mScrollListener);
        this.mSmoothScrollAmountAtEdge = (int) (15.0f / context.getResources().getDisplayMetrics().density);
    }

    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();
        BitmapDrawable drawable = new BitmapDrawable(getResources(), getBitmapWithBorder(v));
        this.mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        this.mHoverCellCurrentBounds = new Rect(this.mHoverCellOriginalBounds);
        drawable.setBounds(this.mHoverCellCurrentBounds);
        return drawable;
    }

    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(15.0f);
        paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        can.drawBitmap(bitmap, 0.0f, 0.0f, null);
        can.drawRect(rect, paint);
        return bitmap;
    }

    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Config.ARGB_8888);
        v.draw(new Canvas(bitmap));
        return bitmap;
    }

    private void updateNeighborViewsForID(long itemID) {
        int position = getPositionForID(itemID);
        StableArrayAdapter adapter = (StableArrayAdapter) getAdapter();
        this.mAboveItemId = adapter.getItemId(position - 1);
        this.mBelowItemId = adapter.getItemId(position + 1);
    }

    public View getViewForID(long itemID) {
        int firstVisiblePosition = getFirstVisiblePosition();
        StableArrayAdapter adapter = (StableArrayAdapter) getAdapter();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (adapter.getItemId(firstVisiblePosition + i) == itemID) {
                return v;
            }
        }
        return null;
    }

    public int getPositionForID(long itemID) {
        View v = getViewForID(itemID);
        if (v == null) {
            return -1;
        }
        return getPositionForView(v);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mHoverCell != null) {
            this.mHoverCell.draw(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
            case ChannelSftp.SSH_FX_OK /*0*/:
                this.mDownX = (int) event.getX();
                this.mDownY = (int) event.getY();
                this.mActivePointerId = event.getPointerId(0);
                break;
            case ChannelSftp.SSH_FX_EOF /*1*/:
                touchEventsEnded();
                break;
            case ChannelSftp.SSH_FX_NO_SUCH_FILE /*2*/:
                if (this.mActivePointerId != -1) {
                    this.mLastEventY = (int) event.getY(event.findPointerIndex(this.mActivePointerId));
                    int deltaY = this.mLastEventY - this.mDownY;
                    if (this.mCellIsMobile) {
                        this.mHoverCellCurrentBounds.offsetTo(this.mHoverCellOriginalBounds.left, (this.mHoverCellOriginalBounds.top + deltaY) + this.mTotalOffset);
                        this.mHoverCell.setBounds(this.mHoverCellCurrentBounds);
                        invalidate();
                        handleCellSwitch();
                        this.mIsMobileScrolling = false;
                        handleMobileCellScroll();
                        return false;
                    }
                }
                break;
            case ChannelSftp.SSH_FX_PERMISSION_DENIED /*3*/:
                touchEventsCancelled();
                break;
            case ChannelSftp.SSH_FX_NO_CONNECTION /*6*/:
                if (event.getPointerId((event.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8) == this.mActivePointerId) {
                    touchEventsEnded();
                    break;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void handleCellSwitch() {
        int deltaY = this.mLastEventY - this.mDownY;
        int deltaYTotal = (this.mHoverCellOriginalBounds.top + this.mTotalOffset) + deltaY;
        View belowView = getViewForID(this.mBelowItemId);
        View mobileView = getViewForID(this.mMobileItemId);
        View aboveView = getViewForID(this.mAboveItemId);
        boolean isBelow = belowView != null && deltaYTotal > belowView.getTop();
        boolean isAbove = aboveView != null && deltaYTotal < aboveView.getTop();
        if (isBelow || isAbove) {
            View switchView;
            long switchItemID = isBelow ? this.mBelowItemId : this.mAboveItemId;
            if (isBelow) {
                switchView = belowView;
            } else {
                switchView = aboveView;
            }
            int originalItem = getPositionForView(mobileView);
            if (switchView == null) {
                updateNeighborViewsForID(this.mMobileItemId);
                return;
            }
            swapElements(this.mCheeseList, originalItem, getPositionForView(switchView));
            ((BaseAdapter) getAdapter()).notifyDataSetChanged();
            this.mDownY = this.mLastEventY;
            int switchViewStartTop = switchView.getTop();
            mobileView.setVisibility(View.VISIBLE);
            switchView.setVisibility(View.INVISIBLE);
            updateNeighborViewsForID(this.mMobileItemId);
            ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new C01682(observer, switchItemID, deltaY, switchViewStartTop));
        }
    }

    private void swapElements(ArrayList arrayList, int indexOne, int indexTwo) {
        Object temp = arrayList.get(indexOne);
        arrayList.set(indexOne, arrayList.get(indexTwo));
        arrayList.set(indexTwo, temp);
    }

    private void touchEventsEnded() {
        View mobileView = getViewForID(this.mMobileItemId);
        if (this.mCellIsMobile || this.mIsWaitingForScrollFinish) {
            this.mCellIsMobile = false;
            this.mIsWaitingForScrollFinish = false;
            this.mIsMobileScrolling = false;
            this.mActivePointerId = -1;
            if (this.mScrollState != 0) {
                this.mIsWaitingForScrollFinish = true;
                return;
            }
            this.mHoverCellCurrentBounds.offsetTo(this.mHoverCellOriginalBounds.left, mobileView.getTop());
            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(this.mHoverCell, "bounds", sBoundEvaluator, new Object[]{this.mHoverCellCurrentBounds});
            hoverViewAnimator.addUpdateListener(new C01693());
            hoverViewAnimator.addListener(new C01704(mobileView));
            hoverViewAnimator.start();
            return;
        }
        touchEventsCancelled();
    }

    private void touchEventsCancelled() {
        View mobileView = getViewForID(this.mMobileItemId);
        if (this.mCellIsMobile) {
            this.mAboveItemId = -1;
            this.mMobileItemId = -1;
            this.mBelowItemId = -1;
            mobileView.setVisibility(View.VISIBLE);
            this.mHoverCell = null;
            invalidate();
        }
        this.mCellIsMobile = false;
        this.mIsMobileScrolling = false;
        this.mActivePointerId = -1;
    }

    static {
        sBoundEvaluator = new C01715();
    }

    private void handleMobileCellScroll() {
        this.mIsMobileScrolling = handleMobileCellScroll(this.mHoverCellCurrentBounds);
    }

    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();
        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-this.mSmoothScrollAmountAtEdge, 0);
            return true;
        } else if (hoverViewTop + hoverHeight < height || offset + extent >= range) {
            return false;
        } else {
            smoothScrollBy(this.mSmoothScrollAmountAtEdge, 0);
            return true;
        }
    }

    public void setCheeseList(ArrayList<String> cheeseList) {
        this.mCheeseList = cheeseList;
    }
}
