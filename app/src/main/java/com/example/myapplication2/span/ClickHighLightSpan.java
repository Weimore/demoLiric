package com.example.myapplication2.span;

import android.graphics.Color;
import android.os.Parcel;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * 点击背景高亮的span
 */
public class ClickHighLightSpan extends ClickSpan {

    public ClickHighLightSpan(View.OnClickListener listener) {
        super(listener);
    }

    public ClickHighLightSpan(@NonNull Parcel src) {
        super(src);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.bgColor = Color.parseColor("#991CC38D");
    }
}
