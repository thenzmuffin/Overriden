package com.total.overiden;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.List;

public class FacingButton extends AppCompatButton  {
//    private static final int white = 0xffffffff;
//    private static final int grey = 0xff808080;
    private static final int black = 0xff101010;
    private final Paint blackPaint;
    private final Paint highlight;

    private int selectedIndex = 0;

    private List<TargetData.LocTable> modes = null;

    public FacingButton(@NonNull Context context) {
        super(context);
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlight = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    public FacingButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlight = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public FacingButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlight = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

//    public void setOptions(List<TargetData.LocTable> modes){
//        this.modes = modes;
//
//    }
    public void setBasicOptions(ForceList.ForceType type){
        if (modes==null) {
            modes = new ArrayList<>();
            modes.add(TargetData.LocTable.FRONT);
            if (type == ForceList.ForceType.TW){
                modes.add(TargetData.LocTable.LEFT);
                modes.add(TargetData.LocTable.RIGHT);
            }
            modes.add(TargetData.LocTable.REAR);
        }
    }
    public TargetData.LocTable getSelectedMode() {
        return modes.get(selectedIndex);
    }
    public void setSelectedMode(TargetData.LocTable selected) {
        for (int i = 0;i < modes.size();i++){
            if (selected == modes.get(i)){
                selectedIndex = i;
                setButtonColor();
                break;
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

        TargetData.LocTable mode = modes.get(selectedIndex);

            blackPaint.setColor(black);
        canvas.drawRoundRect(0,0,width,height, 5, 5, blackPaint);

        canvas.drawRoundRect(5,5,width - 5,height - 5, 5, 5, highlight);

        blackPaint.setTextSize(20);
        blackPaint.setFakeBoldText(true);

        canvas.drawText(mode.toString(),15,30,blackPaint);


    }
private void setButtonColor(){
    int colID;
    switch (selectedIndex){
        case 0:
            colID = R.color.Chocolate;
            break;
        case 3:
            colID = R.color.Brown;
            break;
        case 2:
            colID = R.color.DarkGreen;
            break;
        case 1:
            colID = R.color.CornflowerBlue;
            break;
        default:
            colID = R.color.Wheat;
            break;
    }
    highlight.setColor(getResources().getColor(colID,null));
}
    @Override
    public boolean performClick() {
        selectedIndex++;
        if (selectedIndex >= modes.size())selectedIndex = 0;
        setButtonColor();
        return super.performClick();
    }
}
