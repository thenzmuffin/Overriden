package com.total.overiden;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class HeatView extends androidx.appcompat.widget.AppCompatTextView {
    private int oldLevel = -1;
    public HeatView(Context context) {
        super(context);
        setGravity(TEXT_ALIGNMENT_CENTER);
    }

    public HeatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setGravity(TEXT_ALIGNMENT_CENTER);
    }

    public HeatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(TEXT_ALIGNMENT_CENTER);
    }


    @SuppressLint("ResourceAsColor")
    public void setHeatLevel(int level){
        if (oldLevel != level) {
            oldLevel = level;
//            int w = this.getWidth();
//            int h = this.getHeight();
            this.setTextColor(getResources().getColor(R.color.black, null));
//            this.setTextSize(h - 10);
 //           Paint shade = new Paint(Paint.ANTI_ALIAS_FLAG);
            BitmapDrawable back;
            //= new BitmapDrawable(getResources().getDrawable(R.drawable.heat_white,),Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888));
            int adjustedLevel;
            if (Game.current.isSmartHeat())adjustedLevel = Math.floorDiv(level,5); // color code is in 5 unit blocks
            else adjustedLevel = level;
            switch (adjustedLevel) {
                case 0:
                    back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.heat_white);
//                    shade.setColor(R.color.white);
                    break;
                case 1:
                    back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.heat_yellow);
//                    shade.setColor(R.color.Yellow);
                    break;
                case 2:
                    back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.heat_yellow);
//                    shade.setColor(R.color.Orange);
                    break;
                case 3:
                    back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.heat_orange);
//                    shade.setColor(R.color.Pink);
                    break;
                case 4:
                    back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.heat_red);
//                    shade.setColor(R.color.Red);
                    break;
                case 5:
                    back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.heat_magenta);
//                    shade.setColor(R.color.Maroon);
                    this.setTextColor(getResources().getColor(R.color.white, null));
                    break;
                default:
                    back = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.heat_black);
//                    shade.setColor(R.color.black);
                    this.setTextColor(getResources().getColor(R.color.white, null));
                    break;
            }
            setBackground(back);
            setText("  " + level);
        }

    }
}
