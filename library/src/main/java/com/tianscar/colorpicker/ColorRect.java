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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.math.MathUtils;

class ColorRect extends View {

    private final Paint mCursorPaint;
    private boolean mCursorVisible;
    private float mCursorWidth;

    private final Paint mColorPaint;

    private final float[] mColorHSV = {1.0f, 1.0f, 1.0f};

    private volatile float positionX, positionY;

    private volatile float mSaturation, mValue;

    private OnSaturationChangedListener mOnSaturationChangedListener;

    public interface OnSaturationChangedListener {
        void onSaturationChanged(float saturation);
    }

    public void setOnSaturationChangedListener(OnSaturationChangedListener listener) {
        mOnSaturationChangedListener = listener;
    }

    public OnSaturationChangedListener getOnSaturationChangedListener() {
        return mOnSaturationChangedListener;
    }

    private OnValueChangedListener mOnValueChangedListener;

    public interface OnValueChangedListener {
        void onValueChanged(float value);
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        mOnValueChangedListener = listener;
    }

    public OnValueChangedListener getOnValueChangedListener() {
        return mOnValueChangedListener;
    }

    public float getSaturation() {
        return mSaturation;
    }

    public void setSaturation(float saturation) {
        changeSaturation(saturation);
        positionX = getPositionXFromSaturation(saturation);
        invalidate();
    }

    private void changeSaturation(float saturation) {
        saturation = MathUtils.clamp(saturation, 0, 1);
        float oldSaturation = getSaturation();
        mSaturation = saturation;
        if (mOnSaturationChangedListener != null) {
            if (mSaturation != oldSaturation) {
                mOnSaturationChangedListener.onSaturationChanged(mSaturation);
            }
        }
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        changeValue(value);
        positionY = getPositionYFromValue(value);
        invalidate();
    }

    private void changeValue(float value) {
        value = MathUtils.clamp(value, 0, 1);
        float oldValue = getValue();
        mValue = value;
        if (mOnValueChangedListener != null) {
            if (mValue != oldValue) {
                mOnValueChangedListener.onValueChanged(mValue);
            }
        }
    }

    public void setCursorWidth(float cursorWidth) {
        mCursorWidth = cursorWidth;
        invalidate();
    }

    public float getCursorWidth() {
        return mCursorWidth;
    }

    public void setCursorVisible(boolean cursorVisible) {
        mCursorVisible = cursorVisible;
        invalidate();
    }

    public boolean isCursorVisible() {
        return mCursorVisible;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
        changeSaturation(getPositionXSaturation(positionX));
        invalidate();
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
        changeValue(getPositionYValue(positionY));
        invalidate();
    }

    public ColorRect(Context context) {
        this(context, null);
    }

    public ColorRect(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorRect(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mColorPaint = new Paint();
        mCursorPaint = new Paint();
        mCursorPaint.setDither(true);
        mCursorPaint.setAntiAlias(true);
        mCursorPaint.setStyle(Paint.Style.STROKE);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorRect,
                defStyle, 0);
        mCursorVisible = typedArray.getBoolean(R.styleable.ColorRect_android_cursorVisible, true);
        mCursorWidth = typedArray.getDimension(R.styleable.ColorRect_cursorWidth,
                getResources().getDimension(R.dimen.colorpicker_cursor_width_default));
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
        LinearGradient luar = new LinearGradient(0, 0,
                0, getMeasuredHeight(), 0xFFFFFFFF, 0xFF000000, Shader.TileMode.CLAMP);
        int color = Color.HSVToColor(mColorHSV);
        LinearGradient dalam = new LinearGradient(0, 0,
                getMeasuredWidth(), 0, 0xFFFFFFFF, color, Shader.TileMode.CLAMP);
        mColorPaint.setShader(luar);
        canvas.drawPaint(mColorPaint);
        mColorPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        mColorPaint.setShader(dalam);
        canvas.drawPaint(mColorPaint);

        float strokeWidth = Math.max(
                getResources().getDimension(R.dimen.colorpicker_cursor_width_default),
                mCursorWidth);

        if (mCursorVisible) {
            mCursorPaint.setColor(Color.WHITE);
            mCursorPaint.setStrokeWidth(strokeWidth);
            canvas.drawCircle(positionX, positionY, strokeWidth * 2, mCursorPaint);
            mCursorPaint.setColor(Color.BLACK);
            mCursorPaint.setStrokeWidth(strokeWidth / 2);
            canvas.drawCircle(positionX, positionY, strokeWidth * 2, mCursorPaint);
        }

    }

    public void setHue(float hue) {
        hue = MathUtils.clamp(hue, 0, 360);
        mColorHSV[0] = hue;
        invalidate();
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
                positionX = MathUtils.clamp(event.getX(), 0, getMeasuredWidth());
                positionY = MathUtils.clamp(event.getY(), 0, getMeasuredHeight());
                changeSaturation(getPositionXSaturation(positionX));
                changeValue(getPositionYValue(positionY));
                invalidate();
                break;
        }
        return true;
    }

    public float getPositionXSaturation(float x) {
        x = MathUtils.clamp(x, 0, getMeasuredWidth());
        return 1.0f / getMeasuredWidth() * x;
    }

    public float getPositionXFromSaturation(float saturation) {
        saturation = MathUtils.clamp(saturation, 0, 1);
        return getMeasuredWidth() * saturation;
    }

    public float getPositionYValue(float y) {
        y = MathUtils.clamp(y, 0, getMeasuredHeight());
        return 1.0f - 1.0f / getMeasuredHeight() * y;
    }

    public float getPositionYFromValue(float value) {
        value = MathUtils.clamp(value, 0, 1);
        return getMeasuredHeight() * (1.0f - value);
    }

}
