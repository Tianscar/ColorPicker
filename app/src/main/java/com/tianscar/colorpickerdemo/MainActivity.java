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

package com.tianscar.colorpickerdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.tianscar.colorpicker.RectColorPicker;

public class MainActivity extends AppCompatActivity {

    private RectColorPicker rectColorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rectColorPicker = findViewById(R.id.rect_colorpicker);

        rectColorPicker.setOnColorPickedListener(new RectColorPicker.OnColorPickedListener() {
            @Override
            public void onColorPicked(RectColorPicker picker, int color) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(colorToHexString(color));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.horizontal_ascending:
                rectColorPicker.setOrientation(LinearLayout.HORIZONTAL);
                rectColorPicker.setOrder(RectColorPicker.ASCENDING);
                break;
            case R.id.horizontal_descending:
                rectColorPicker.setOrientation(LinearLayout.HORIZONTAL);
                rectColorPicker.setOrder(RectColorPicker.DESCENDING);
                break;
            case R.id.vertical_ascending:
                rectColorPicker.setOrientation(LinearLayout.VERTICAL);
                rectColorPicker.setOrder(RectColorPicker.ASCENDING);
                break;
            case R.id.vertical_descending:
                rectColorPicker.setOrientation(LinearLayout.VERTICAL);
                rectColorPicker.setOrder(RectColorPicker.DESCENDING);
                break;
            default:
                return super.onOptionsItemSelected(item);

        }

        return true;

    }

    @NonNull
    public static String colorToHexString (int color) {
        return String.format("#%08X", color);
    }

}