package com.total.overiden;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

public class LockButton extends AppCompatCheckBox {

    public LockButton(@NonNull Context context) {
        super(context);
    }

    public LockButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LockButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        BitmapDrawable back;
        if (isChecked()) back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.lockedicon);
        else back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.unlockedicon);
        canvas.drawBitmap(back.getBitmap(),0,0,new Paint(Paint.ANTI_ALIAS_FLAG));
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        if (this.isChecked()) paint.setColor(color);
//        else paint.setColor(grey);
//        Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        if (black == color && this.isChecked())blackPaint.setColor(white);
//        else blackPaint.setColor(black);
//        canvas.drawRoundRect(0,0,90,70, 5, 5, blackPaint);
//
//        canvas.drawRoundRect(5,5,85,65, 5, 5, paint);
//
//        blackPaint.setTextSize(20);
//        blackPaint.setFakeBoldText(true);
//        canvas.drawText(label,15,25,blackPaint);
//        canvas.drawText(label2,15,50,blackPaint);
    }
}
