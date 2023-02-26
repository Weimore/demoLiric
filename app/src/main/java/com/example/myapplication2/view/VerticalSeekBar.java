package com.example.myapplication2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class VerticalSeekBar extends View {

    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 0;

    private int startColor = Color.parseColor("#FAD0C4");
    private int middleColor = Color.parseColor("#FAD0C4");
    private int endColor = Color.parseColor("#FFD1FF");
    private int thumbColor = Color.WHITE;
    private final int[] colorArray = {startColor, middleColor, endColor};
    private float x, y;
    private float mRadius;
    private float progress;
    private float sLeft, sTop, sRight, sBottom;
    private float sWidth, sHeight;
    private float shadowWidth;
    private final Paint paintPb = new Paint();
    private final RectF rectBlackBg = new RectF();
    private final Paint thumbPaint = new Paint();
    protected OnStateChangeListener onStateChangeListener;

    public VerticalSeekBar(Context context) {
        this(context, null);
        init();
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
        int h = getMeasuredHeight();
        int w = getMeasuredWidth();
        shadowWidth = 4;
        mRadius = (w - shadowWidth) / 2F;
        sLeft = w * 0.25f; // 背景左边缘坐标
        sRight = w * 0.75f;// 背景右边缘坐标
        sTop = 0;
        sBottom = h;
        sWidth = sRight - sLeft; // 背景宽度
        sHeight = sBottom - sTop; // 背景高度
        x = (float) w / 2;//圆心的x坐标
        y = (float) (0.01 * progress) * sHeight;//圆心y坐标

        // init paint
        thumbPaint.setAntiAlias(true);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(thumbColor);
        // 添加阴影效果
        thumbPaint.setShadowLayer(shadowWidth, 0, 0, Color.GRAY);

        rectBlackBg.set(sLeft, sTop, sRight, sBottom);
        paintPb.setAntiAlias(true);
        paintPb.setStyle(Paint.Style.FILL);
    }

    public void setColor(int startColor, int middleColor, int endColor, int thumbColor, int thumbBorderColor) {
        this.startColor = startColor;
        this.middleColor = middleColor;
        this.endColor = endColor;
        this.thumbColor = thumbColor;
        colorArray[0] = startColor;
        colorArray[1] = middleColor;
        colorArray[2] = endColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawCircle(canvas);
        paintPb.reset();
    }

    private void drawBackground(Canvas canvas) {
        // 设置渲染器
        LinearGradient linearGradient = new LinearGradient(sLeft, sTop, sWidth, sHeight, colorArray, null, Shader.TileMode.MIRROR);
        paintPb.setShader(linearGradient);
        canvas.drawRoundRect(rectBlackBg, sWidth / 2, sWidth / 2, paintPb);
    }

    private void drawCircle(Canvas canvas) {
        y = Math.max(y, mRadius);// 判断thumb边界
        y = Math.min(y, sHeight - mRadius);
        canvas.drawCircle(x, y, mRadius, thumbPaint);
    }

    float indicatorOffset = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.y = event.getY();
        progress = (y) / sHeight * MAX_VALUE;
        if (progress < MIN_VALUE) {
            this.progress = MIN_VALUE;
        } else if (progress > MAX_VALUE) {
            this.progress = MAX_VALUE;
        }

        indicatorOffset = sHeight / MAX_VALUE * progress - mRadius * 1.5F;
        indicatorOffset = indicatorOffset < 0 ? 0 : (Math.min(indicatorOffset, sHeight - mRadius * 2));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (onStateChangeListener != null) {
                    onStateChangeListener.onStartTouch(this);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (onStateChangeListener != null) {
                    onStateChangeListener.onStopTrackingTouch(this, progress);
                }
                setProgress(progress);
                this.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (onStateChangeListener != null) {
                    onStateChangeListener.onStateChangeListener(this, progress, indicatorOffset);
                }
                setProgress(progress);
                this.invalidate();
                break;
        }

        return true;
    }


    public interface OnStateChangeListener {
        void onStartTouch(View view);

        void onStateChangeListener(View view, float progress, float indicatorOffset);

        void onStopTrackingTouch(View view, float progress);
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        y = (float) (0.01 * progress) * sHeight;//圆心y坐标
        Log.d("progress",progress + "");
        invalidate();
    }
}