package com.example.myapplication2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class RoundImage extends androidx.appcompat.widget.AppCompatImageView {

    private int oldX = 0;
    private int oldY = 0;
    //View的宽高
    private int mWidth;
    private int mHeight;
    //屏幕宽高
    private int mScreenWidth;
    private int mScreenHeight;
    //View的中心坐标点
    private int mDefaultX;
    private int mDefaultY;
    //粘性方向，默认左右方向
    private @StickyDirection
    int mStickyDirection = STICKY_LEFT_RIGHT;
    //skip策略，默认Tough模式
    private @SkipStrategy
    int mSkipStrategy = SKIP_TOUGH;
    //粘住时触发
    private OnStickyListener mStickyListener;
    //进入和离开skip区域时触发
    private OnSkipStateListener mSkipStateListener;
    //重叠区域List
    private final ArrayList<Rect> skipRects = new ArrayList<>();

    //是否在重叠区域
    private boolean isInSkip = false;

    public static final int STICKY_LEFT = 1;   //粘左边
    public static final int STICKY_RIGHT = 2;  //粘右边
    public static final int STICKY_LEFT_RIGHT = 3;  //哪边近粘哪边

    @IntDef({STICKY_LEFT, STICKY_RIGHT, STICKY_LEFT_RIGHT})
    public @interface StickyDirection {
    }

    public static final int SKIP_TOUGH = 1;   //不可拖动
    public static final int SKIP_SOFT = 2;   //可拖动，释放后划走

    @IntDef({SKIP_TOUGH, SKIP_SOFT})
    public @interface SkipStrategy {
    }


    public RoundImage(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RoundImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RoundImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public RoundImage setStickyDirection(@StickyDirection int direction) {
        mStickyDirection = direction;
        return this;
    }

    public RoundImage setSkipStrategy(@SkipStrategy int skipStrategy) {
        mSkipStrategy = skipStrategy;
        return this;
    }

    public RoundImage setStickyListener(OnStickyListener stickyListener) {
        mStickyListener = stickyListener;
        return this;
    }

    public RoundImage setSkipStateListener(OnSkipStateListener skipStateListener) {
        mSkipStateListener = skipStateListener;
        return this;
    }

    private void init(Context context) {
        mScreenWidth = getScreenWidth(context);
        mScreenHeight = getScreenHeight(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mWidth == 0F || mHeight == 0F) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
        }
        mDefaultX = getLeft() + mWidth / 2;
        mDefaultY = getTop() + mHeight / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                float oldTX = getTranslationX();
                float oldTY = getTranslationY();
                setTranslationX(oldTX + event.getX() - mWidth * 1.0f / 2);
                setTranslationY(oldTY + event.getY() - mHeight * 1.0f / 2);
                oldX = x;
                oldY = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mSkipStrategy == SKIP_TOUGH && isInSkipRange(x, y)) {
                    return super.onTouchEvent(event);
                } else if (mSkipStrategy == SKIP_SOFT) {
                    if (isInSkipRange(x, y)) {
                        if(mSkipStateListener != null) mSkipStateListener.onInSkip(this);
                    } else {
                        if(mSkipStateListener != null) mSkipStateListener.onOutSkip(this);
                    }
                }
                float oldTX = getTranslationX();
                float oldTY = getTranslationY();
                setTranslationX(oldTX + x - oldX);
                setTranslationY(oldTY + y - oldY);
                oldX = x;
                oldY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                sticky();
                break;
            }
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public RoundImage setSkipRange(View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        skipRects.add(rect);
        return this;
    }

    public RoundImage setSkipRange(Rect rect) {
        skipRects.add(rect);
        return this;
    }

    private void sticky() {
        switch (mStickyDirection) {
            case STICKY_LEFT:
                animate().translationX(mWidth / 2 - mDefaultX).setDuration(200).setListener(mAnimatorAdapter).start();
                break;
            case STICKY_RIGHT:
                animate().translationX(mScreenWidth - mWidth / 2 - mDefaultX).setDuration(200).setListener(mAnimatorAdapter).start();
                break;
            default:
                float dx = getX() + mWidth / 2;
                if (dx > mScreenWidth / 2) {
                    animate().translationX(mScreenWidth - mWidth / 2 - mDefaultX).setDuration(200).setListener(mAnimatorAdapter).start();
                } else {
                    animate().translationX(mWidth / 2 - mDefaultX).setDuration(200).setListener(mAnimatorAdapter).start();
                }
                break;
        }
    }

    private final AnimatorListenerAdapter mAnimatorAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (mSkipStrategy == SKIP_SOFT) {
                if (isInSkipRange((int) getX() + mWidth / 2, (int) getY() + mHeight / 2)) {
                    if(mSkipStateListener != null) mSkipStateListener.onInSkip(RoundImage.this);
                } else {
                    if(mSkipStateListener != null) mSkipStateListener.onOutSkip(RoundImage.this);
                }
            }
            if (mStickyListener != null) {
                mStickyListener.sticky(RoundImage.this);
            }
        }
    };

    private static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    private static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    //不可移出屏幕边界
    private boolean isInScreen(int x, int y) {
        int addW = mWidth / 2;
        int addH = mHeight / 2;
        Rect rect = new Rect(addW, addH, mScreenWidth - addW, mScreenHeight - addH);
        return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
    }

    //判断当前手指所在区域是否处于不可移动区域
    private boolean isPointInRect(int x, int y, Rect rect) {
        int addW = mWidth / 2;
        int addH = mHeight / 2;
        return x > rect.left - addW && x < rect.right + addW && y > rect.top - addH && y < rect.bottom + addH;
    }

    //是否处于需要跳过的区域
    private boolean isInSkipRange(int x, int y) {
        if (!isInScreen(x, y)) {
            return true;
        }
        for (Rect skipRect : skipRects) {
            if (isPointInRect(x, y, skipRect)) {
                return true;
            }
        }
        return false;
    }


    public interface OnStickyListener {
        void sticky(View view);
    }

    public interface OnSkipStateListener {
        void onInSkip(@NonNull RoundImage view);
        void onOutSkip(@NonNull RoundImage view);
    }

    private float getCenterX() {
        return getX() + mWidth / 2;
    }

    private float getCenterY() {
        return getY() + mHeight / 2;
    }

    private void getWindow(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
            }
        } catch (Exception e) {

        }
    }

}
