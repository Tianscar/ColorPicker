/*
 * MIT License
 *
 * Copyright (c) 2021 Tianscar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.tianscar.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

class HueRect extends View {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private static final int[] mColors;

    static {
        int colorCount = 12;
        int colorAngleStep = 360 / colorCount;
        mColors = new int[colorCount + 1];
        float[] hsv = new float[]{0f, 1f, 1f};
        for (int i = 0; i < mColors.length; i++) {
            hsv[0] = 360 - (i * colorAngleStep) % 360;
            if (hsv[0] == 360) hsv[0] = 359;
            mColors[i] = Color.HSVToColor(hsv);
        }
    }

    private final Paint mCursorPaint;
    private boolean mCursorVisible;
    private float mCursorWidth;
    private float mCursorRadius;
    private int mOrientation;

    public void setCursorWidth(float cursorWidth) {
        mCursorWidth = cursorWidth;
        invalidate();
    }

    public float getCursorWidth() {
        return mCursorWidth;
    }

    public void setCursorRadius(float cursorRadius) {
        mCursorRadius = cursorRadius;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        invalidate();
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setCursorVisible(boolean cursorVisible) {
        mCursorVisible = cursorVisible;
        invalidate();
    }

    public boolean isCursorVisible() {
        return mCursorVisible;
    }

    private final Paint mColorPaint;

    private float mHue;

    private float mPosition;

    private OnHueChangedListener mOnHueChangedListener;

    public interface OnHueChangedListener {
        void onHueChanged(float hue);
    }

    public void setOnHueChangedListener(OnHueChangedListener listener) {
        mOnHueChangedListener = listener;
    }

    public OnHueChangedListener getOnHueChangedListener() {
        return mOnHueChangedListener;
    }

    public HueRect(Context context) {
        this(context, null);
    }

    public HueRect(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HueRect(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mColorPaint = new Paint();
        mCursorPaint = new Paint();
        mCursorPaint.setDither(true);
        mCursorPaint.setAntiAlias(true);
        mCursorPaint.setStyle(Paint.Style.STROKE);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HueRect,
                defStyleAttr, 0);
        mCursorVisible = typedArray.getBoolean(R.styleable.HueRect_android_cursorVisible, true);
        mCursorWidth = typedArray.getDimension(R.styleable.HueRect_cursorWidth,
                getResources().getDimension(R.dimen.colorpicker_cursor_width_default));
        mCursorRadius = typedArray.getDimension(R.styleable.HueRect_cursorRadius,
                getResources().getDimension(R.dimen.colorpicker_rect_cursor_radius_default));
        mOrientation = typedArray.getInt(R.styleable.HueRect_android_orientation, VERTICAL);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
        }
        else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMeasuredWidth(), heightSpecSize);
        }
        else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, getMeasuredHeight());
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        LinearGradient luar;
        switch (mOrientation) {
            case VERTICAL: default:
                luar = new LinearGradient(0, 0,
                        0, getMeasuredHeight(),
                        mColors, null, Shader.TileMode.CLAMP);
                break;
            case HORIZONTAL:
                luar = new LinearGradient(0, 0,
                        getMeasuredWidth(), 0,
                        mColors, null, Shader.TileMode.CLAMP);
                break;
        }
        mColorPaint.setShader(luar);
        canvas.drawPaint(mColorPaint);

        if (mCursorVisible) {

            float strokeWidth = Math.max(
                    getResources().getDimension(R.dimen.colorpicker_cursor_width_default),
                    mCursorWidth);

            mCursorPaint.setStrokeWidth(strokeWidth);
            mCursorPaint.setColor(Color.WHITE);

            RectF rectF;
            switch (mOrientation) {
                case VERTICAL: default:
                    rectF = new RectF(strokeWidth / 2,
                            mPosition - strokeWidth * 2, getMeasuredWidth() - strokeWidth / 2,
                            mPosition + strokeWidth * 2);
                    break;
                case HORIZONTAL:
                    rectF = new RectF(mPosition - strokeWidth * 2, strokeWidth / 2,
                            mPosition + strokeWidth * 2,
                            getMeasuredHeight() - strokeWidth / 2);
                    break;
            }

            canvas.drawRoundRect(rectF, mCursorRadius, mCursorRadius, mCursorPaint);

            mCursorPaint.setColor(Color.BLACK);
            mCursorPaint.setStrokeWidth(strokeWidth / 2);

            canvas.drawRoundRect(rectF, mCursorRadius, mCursorRadius, mCursorPaint);

        }

    }

    public void setHue(float hue) {
        changeHue(hue);
        mPosition = getPositionFromHue(hue);
        invalidate();
    }

    public void setPosition(float position) {
        switch (mOrientation) {
            case VERTICAL: default:
                mPosition = MathUtils.clamp(position, 0, getMeasuredHeight());
                break;
            case HORIZONTAL:
                mPosition = MathUtils.clamp(position, 0, getMeasuredWidth());
                break;
        }
        changeHue(getPositionHue(mPosition));
        invalidate();
    }

    private void changeHue(float hue) {
        hue = MathUtils.clamp(hue, 0, 360);
        float oldHue = getHue();
        mHue = hue;
        if (mOnHueChangedListener != null) {
            if (mHue != oldHue) {
                mOnHueChangedListener.onHueChanged(mHue);
            }
        }
    }

    public float getHue() {
        return mHue;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (hasOnClickListeners()) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                switch (mOrientation) {
                    case VERTICAL: default:
                        mPosition = MathUtils.clamp(event.getY(), 0, getMeasuredHeight());
                        break;
                    case HORIZONTAL:
                        mPosition = MathUtils.clamp(event.getX(), 0, getMeasuredWidth());
                        break;
                }
                changeHue(getPositionHue(mPosition));
                invalidate();
                break;
        }
        return true;
    }

    public float getPositionHue(float position) {
        switch (mOrientation) {
            case VERTICAL: default:
                position = MathUtils.clamp(position, 0, getMeasuredHeight());
                return 360 - position / getMeasuredHeight() * 360;
            case HORIZONTAL:
                position = MathUtils.clamp(position, 0, getMeasuredWidth());
                return 360 - position / getMeasuredWidth() * 360;
        }
    }

    public float getPositionFromHue(float hue) {
        hue = MathUtils.clamp(hue, 0, 360);
        switch (mOrientation) {
            case VERTICAL: default:
                return getMeasuredHeight() * (1.0f - hue / 360);
            case HORIZONTAL:
                return getMeasuredWidth() * (1.0f - hue / 360);
        }
    }

}
