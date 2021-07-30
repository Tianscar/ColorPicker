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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

    private int mDividerSize;

    public int getDividerSize() {
        return mDividerSize;
    }

    public void setDividerSize(int dividerSize) {
        mDividerSize = dividerSize;
    }

    private final ColorRect colorRect;
    private final HueRect hueRect;
    private final FrameLayout divider;

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
        divider = new FrameLayout(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RectColorPicker,
                defStyleAttr, 0);
        setCursorVisible(typedArray.getBoolean(R.styleable.RectColorPicker_android_cursorVisible, true));
        setCursorWidth(typedArray.getDimension(R.styleable.RectColorPicker_cursorWidth,
                getResources().getDimension(R.dimen.colorpicker_cursor_width_default)));
        setCursorRadius(typedArray.getDimension(R.styleable.RectColorPicker_cursorRadius,
                getResources().getDimension(R.dimen.colorpicker_rect_cursor_radius_default)));
        setOrientation(typedArray.getInt(R.styleable.RectColorPicker_android_orientation, HORIZONTAL));
        setDividerSize((int) typedArray.getDimension(R.styleable.RectColorPicker_dividerSize,
                getResources().getDimension(R.dimen.rect_colorpicker_divider_width_default)));
        typedArray.recycle();
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams params1 = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                params1.weight = 1;
                colorRect.setLayoutParams(params1);
                addView(colorRect);
                LayoutParams params2 = new LayoutParams(mDividerSize, mDividerSize);
                divider.setLayoutParams(params2);
                divider.setBackgroundColor(Color.TRANSPARENT);
                addView(divider);
                LayoutParams params3 = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                params3.weight = 9;
                hueRect.setLayoutParams(params3);
                addView(hueRect);
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

}

