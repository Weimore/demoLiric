package com.example.myapplication2;

import android.graphics.Color;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public class ClickHighLightSpan extends ClickableSpan implements ParcelableSpan {

    private View.OnClickListener listener;

    public ClickHighLightSpan(View.OnClickListener listener) {
        this.listener = listener;
    }

    /**
     * Constructs a {@link android.text.style.URLSpan} from a parcel.
     */
    public ClickHighLightSpan(@NonNull Parcel src) {
    }

    @Override
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    /**
     * @hide
     */
    private int getSpanTypeIdInternal() {
        return 1001;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.bgColor = Color.parseColor("#0000FFF");
    }

    /**
     * @hide
     */
    public void writeToParcelInternal(@NonNull Parcel dest, int flags) {
    }

    @Override
    public void onClick(View widget) {
        if (listener != null) {
            listener.onClick(widget);
        }
    }
}
