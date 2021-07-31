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
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

public class RectColorPicker extends LinearLayout {

    public interface OnColorPickedListener {
        void onColorPicked(RectColorPicker picker, int color);
    }

    private OnColorPickedListener mOnColorPickedListener;

    public void setOnColorPickedListener(OnColorPickedListener listener) {
        mOnColorPickedListener = listener;
    }

    public OnColorPickedListener getOnColorPickedListener() {
        return mOnColorPickedListener;
    }

    public void detectColorPicked(int color) {
        if (mOnColorPickedListener != null) {
            mOnColorPickedListener.onColorPicked(this, color);
        }
    }

    private final ColorRect colorRect;
    private final HueRect hueRect;

    public final static class Order {

        private Order() {}

        public final static int ASCENDING = 0;
        public final static int DESCENDING = 1;

    }

    private volatile int mOrder;

    public int getOrder() {
        return mOrder;
    }

    private int mColorAlpha;

    public void setCursorWidth(float cursorWidth) {
        colorRect.setCursorWidth(cursorWidth);
        hueRect.setCursorWidth(cursorWidth);
        invalidate();
    }

    public float getCursorWidth() {
        return colorRect.getCursorWidth();
    }

    public void setCursorRadius(float cursorRadius) {
        hueRect.setCursorRadius(cursorRadius);
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        switch (orientation) {
            case HORIZONTAL:
                hueRect.setOrientation(HueRect.VERTICAL);
                break;
            case VERTICAL:
                hueRect.setOrientation(HueRect.HORIZONTAL);
                break;
        }
    }

    public void setCursorVisible(boolean cursorVisible) {
        colorRect.setCursorVisible(cursorVisible);
        hueRect.setCursorVisible(cursorVisible);
    }

    public boolean isCursorVisible() {
        return colorRect.isCursorVisible();
    }

    public RectColorPicker(Context context) {
        this(context, null);
    }

    public RectColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RectColorPicker(final Context context, final AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        colorRect = new ColorRect(context, attrs, defStyleAttr);
        hueRect = new HueRect(context, attrs, defStyleAttr);
        hueRect.setOnHueChangedListener(new HueRect.OnHueChangedListener() {
            @Override
            public void onHueChanged(float hue) {
                colorRect.setHue(hue);
                detectColorPicked(getColor());
            }
        });
        colorRect.setOnSaturationChangedListener(new ColorRect.OnSaturationChangedListener() {
            @Override
            public void onSaturationChanged(float saturation) {
                detectColorPicked(getColor());
            }
        });
        colorRect.setOnValueChangedListener(new ColorRect.OnValueChangedListener() {
            @Override
            public void onValueChanged(float value) {
                detectColorPicked(getColor());
            }
        });
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RectColorPicker,
                defStyleAttr, 0);
        setCursorVisible(typedArray.getBoolean(R.styleable.RectColorPicker_android_cursorVisible, true));
        setCursorWidth(typedArray.getDimension(R.styleable.RectColorPicker_cursorWidth,
                getResources().getDimension(R.dimen.colorpicker_cursor_width_default)));
        setCursorRadius(typedArray.getDimension(R.styleable.RectColorPicker_cursorRadius,
                getResources().getDimension(R.dimen.colorpicker_rect_cursor_radius_default)));
        setOrientation(typedArray.getInt(R.styleable.RectColorPicker_android_orientation, HORIZONTAL));
        final float initHue = typedArray.getFloat(R.styleable.RectColorPicker_hue, 0);
        final float initSaturation = typedArray.getFloat(R.styleable.RectColorPicker_saturation, 0);
        final float initValue = typedArray.getFloat(R.styleable.RectColorPicker_value, 1);
        final int initOrder = typedArray.getInt(R.styleable.RectColorPicker_order,
                Order.ASCENDING);
        final float initHueRectWeight = typedArray.getFloat(R.styleable.RectColorPicker_hueRectWeight
                , 8);
        final float initColorRectWeight = typedArray.getFloat(R.styleable.RectColorPicker_colorRectWeight
        , 2);
        typedArray.recycle();
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams params1 = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                colorRect.setLayoutParams(params1);
                LayoutParams params2 = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                hueRect.setLayoutParams(params2);
                setColorRectWeight(initColorRectWeight);
                setHueRectWeight(initHueRectWeight);
                setHue(initHue);
                setSaturation(initSaturation);
                setValue(initValue);
                setOrder(initOrder);
            }
        });
    }

    public void setHueRectWeight(float weight) {
        Utils.setWeight(hueRect, weight);
    }

    public void setColorRectWeight(float weight) {
        Utils.setWeight(colorRect, weight);
    }

    public void ascending() {
        setOrder(Order.ASCENDING);
    }

    public void descending() {
        setOrder(Order.DESCENDING);
    }

    public void reverse() {
        switch (mOrder) {
            case Order.ASCENDING: default:
                descending();
                break;
            case Order.DESCENDING:
                ascending();
                break;
        }
    }

    public void setOrder(int order) {
        removeAllViews();
        mOrder = order;
        switch (order) {
            case Order.ASCENDING: default:
                addView(colorRect);
                addView(hueRect);
                break;
            case Order.DESCENDING:
                addView(hueRect);
                addView(colorRect);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
        }
        else if(widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMeasuredWidth(), heightSpecSize);
        }
        else if(heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, getMeasuredHeight());
        }
    }

    public void setColor(int color) {
        mColorAlpha = Color.alpha(color);
        float[] colorHSV = new float[3];
        Color.colorToHSV(color, colorHSV);
        colorRect.setHue(colorHSV[0]);
        hueRect.setHue(colorHSV[0]);
        colorRect.setSaturation(colorHSV[1]);
        colorRect.setValue(colorHSV[2]);
    }

    private int getColor() {
        return mColorAlpha << 24 | (Color.HSVToColor(new float[] {
                hueRect.getHue(), colorRect.getSaturation(), colorRect.getValue()
        }) & 0x00FFFFFF);
    }

    public float getAlpha() {
        return mColorAlpha;
    }

    public float getHue() {
        return hueRect.getHue();
    }

    public float getSaturation() {
        return colorRect.getSaturation();
    }

    public float getValue() {
        return colorRect.getValue();
    }

    public void setAlpha(int alpha) {
        mColorAlpha = alpha;
    }

    public void setHue(final float hue) {
        colorRect.setHue(hue);
        hueRect.post(new Runnable() {
            @Override
            public void run() {
                hueRect.setHue(hue);
            }
        });
    }

    public void setSaturation(final float saturation) {
        colorRect.post(new Runnable() {
            @Override
            public void run() {
                colorRect.setSaturation(saturation);
            }
        });
    }

    public void setValue(final float value) {
        colorRect.post(new Runnable() {
            @Override
            public void run() {
                colorRect.setValue(value);
            }
        });
    }

    static class ColorRect extends View {

        private final Paint mCursorPaint;
        private boolean mCursorVisible;
        private float mCursorWidth;

        private final Paint mColorPaint;

        private final float[] mColorHSV = { 1.0f, 1.0f, 1.0f };

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
            mCursorVisible = true;
            mCursorWidth = getResources().getDimension(R.dimen.colorpicker_cursor_width_default);
            post(new Runnable() {
                @Override
                public void run() {
                    setHue(0);
                    setSaturation(0);
                    setValue(1);
                }
            });
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

        private float safeCursorWidth() {
            return Math.max(
                    getResources().getDimension(R.dimen.colorpicker_cursor_width_default),
                    mCursorWidth);
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float strokeWidth = safeCursorWidth();

            LinearGradient luar = new LinearGradient(0, 0,
                    0, getMeasuredHeight(), 0xFFFFFFFF, 0xFF000000, Shader.TileMode.CLAMP);
            int color = Color.HSVToColor(mColorHSV);
            LinearGradient dalam = new LinearGradient(0, 0,
                    getMeasuredWidth(), 0, 0xFFFFFFFF, color, Shader.TileMode.CLAMP);
            mColorPaint.setShader(luar);
            canvas.drawRect(strokeWidth * 3, strokeWidth * 3,
                    getMeasuredWidth() - strokeWidth * 3, getMeasuredHeight() - strokeWidth * 3,
                    mColorPaint);
            mColorPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
            mColorPaint.setShader(dalam);
            canvas.drawRect(strokeWidth * 3, strokeWidth * 3,
                    getMeasuredWidth() - strokeWidth * 3, getMeasuredHeight() - strokeWidth * 3,
                    mColorPaint);

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
                    float strokeWidth = safeCursorWidth();
                    positionX = MathUtils.clamp(event.getX(),
                            strokeWidth * 3, getMeasuredWidth() - strokeWidth * 3);
                    positionY = MathUtils.clamp(event.getY(),
                            strokeWidth * 3, getMeasuredHeight() - strokeWidth * 3);
                    changeSaturation(getPositionXSaturation(positionX));
                    changeValue(getPositionYValue(positionY));
                    invalidate();
                    break;
            }
            return true;
        }

        public float getPositionXSaturation(float x) {
            float strokeWidth = safeCursorWidth();
            x = MathUtils.clamp(x, strokeWidth * 3, getMeasuredWidth() - strokeWidth * 3);
            return 1.0f / (getMeasuredWidth() - strokeWidth * 6) * (x - strokeWidth * 3);
        }

        public float getPositionXFromSaturation(float saturation) {
            float strokeWidth = safeCursorWidth();
            saturation = MathUtils.clamp(saturation, 0, 1);
            return strokeWidth * 3 + (getMeasuredWidth() - strokeWidth * 6) * saturation;
        }

        public float getPositionYValue(float y) {
            float strokeWidth = safeCursorWidth();
            y = MathUtils.clamp(y, strokeWidth * 3, getMeasuredHeight() - strokeWidth * 3);
            return 1.0f - 1.0f / (getMeasuredHeight() - strokeWidth * 6) * (y - strokeWidth * 3);
        }

        public float getPositionYFromValue(float value) {
            float strokeWidth = safeCursorWidth();
            value = MathUtils.clamp(value, 0, 1);
            return strokeWidth * 3 + (getMeasuredHeight() - strokeWidth * 6) * (1.0f - value);
        }

    }

    static class HueRect extends View {

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
            mCursorVisible = true;
            mCursorWidth = getResources().getDimension(R.dimen.colorpicker_cursor_width_default);
            mCursorRadius = getResources().getDimension(R.dimen.colorpicker_rect_cursor_radius_default);
            mOrientation = VERTICAL;
            post(new Runnable() {
                @Override
                public void run() {
                    setHue(0);
                }
            });
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

        private float safeCursorWidth() {
            return Math.max(
                    getResources().getDimension(R.dimen.colorpicker_cursor_width_default),
                    mCursorWidth);
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float strokeWidth = safeCursorWidth();

            LinearGradient luar;
            switch (mOrientation) {
                case VERTICAL: default:
                    luar = new LinearGradient(0, getMeasuredHeight(),
                            0, 0,
                            mColors, null, Shader.TileMode.CLAMP);
                    mColorPaint.setShader(luar);
                    canvas.drawRect(strokeWidth * 2, strokeWidth * 3,
                            getMeasuredWidth() - strokeWidth * 2,
                            getMeasuredHeight() - strokeWidth * 3,
                            mColorPaint);
                    break;
                case HORIZONTAL:
                    luar = new LinearGradient(getMeasuredWidth(), 0,
                            0, 0,
                            mColors, null, Shader.TileMode.CLAMP);
                    mColorPaint.setShader(luar);
                    canvas.drawRect(strokeWidth * 3, strokeWidth * 2,
                            getMeasuredWidth() - strokeWidth * 3,
                            getMeasuredHeight() - strokeWidth * 2,
                            mColorPaint);
                    break;
            }

            if (mCursorVisible) {

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
                    float strokeWidth = safeCursorWidth();
                    switch (mOrientation) {
                        case VERTICAL: default:
                            mPosition = MathUtils.clamp(event.getY(),
                                    strokeWidth * 3, getMeasuredHeight() - strokeWidth * 3);
                            break;
                        case HORIZONTAL:
                            mPosition = MathUtils.clamp(event.getX(),
                                    strokeWidth * 3, getMeasuredWidth() - strokeWidth * 3);
                            break;
                    }
                    changeHue(getPositionHue(mPosition));
                    invalidate();
                    break;
            }
            return true;
        }

        public float getPositionHue(float position) {
            float strokeWidth = safeCursorWidth();
            switch (mOrientation) {
                case VERTICAL: default:
                    position = MathUtils.clamp(position, strokeWidth * 3,
                            getMeasuredHeight() - strokeWidth * 3);
                    return (position - strokeWidth * 3) /
                            (getMeasuredHeight() - strokeWidth * 6) * 360;
                case HORIZONTAL:
                    position = MathUtils.clamp(position, strokeWidth * 3,
                            getMeasuredWidth() - strokeWidth * 3);
                    return (position - strokeWidth * 3) /
                            (getMeasuredWidth() - strokeWidth * 6) * 360;
            }
        }

        public float getPositionFromHue(float hue) {
            hue = MathUtils.clamp(hue, 0, 360);
            float strokeWidth = safeCursorWidth();
            switch (mOrientation) {
                case VERTICAL: default:
                    return strokeWidth * 3 + (getMeasuredHeight() - strokeWidth * 6) * hue / 360;
                case HORIZONTAL:
                    return strokeWidth * 3 + (getMeasuredWidth() - strokeWidth * 6) * hue / 360;
            }
        }

    }
}

