package com.total.overiden;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import java.util.List;

public class MultiShotButton extends AppCompatButton  {
    private static final int white = 0xffffffff;
//    private static final int grey = 0xff808080;
    private static final int black = 0xff101010;
    private final Paint blackPaint;

    private int selectedIndex = 0;

    private List<IWeapon.WeaponMode> modes = null;

    public MultiShotButton(@NonNull Context context) {
        super(context);
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    public MultiShotButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public MultiShotButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setOptions(List<IWeapon.WeaponMode> modes){
        this.modes = modes;

    }

    public IWeapon.WeaponMode getSelectedMode() {
        return modes.get(selectedIndex);
    }
    public void setSelectedMode(IWeapon.WeaponMode selected) {
        if (modes!=null) {
            for (int i = 0; i < modes.size(); i++) {
                if (selected == modes.get(i)) {
                    selectedIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        IWeapon.WeaponMode mode = modes.get(selectedIndex);
//        super.onDraw(canvas);
        Paint paint = mode.getColor(this);

    //    int color = 0xffff1010;
     //   if (black == color)blackPaint.setColor(white);
       // else
            blackPaint.setColor(black);
        canvas.drawRoundRect(0,0,width,height, 5, 5, blackPaint);

        canvas.drawRoundRect(5,5,width - 5,height - 5, 5, 5, paint);

        blackPaint.setTextSize(20);
        blackPaint.setFakeBoldText(true);

        canvas.drawText(mode.getLabel(),15,30,blackPaint);


    }

    @Override
    public boolean performClick() {
        selectedIndex++;
        if (selectedIndex >= modes.size())selectedIndex = 0;
        return super.performClick();
    }
}
