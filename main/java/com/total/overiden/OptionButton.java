package com.total.overiden;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import java.util.List;

public class OptionButton extends AppCompatButton {
    public static class OptionButtonChoice {
        private String label;
        private String label2 = null;
        private Paint color;
        private String key; //not always used

        public OptionButtonChoice(String label, Paint color) {
            super();
            this.label = label;
            if (label.length()>10){
                label2 = label.substring(10);
                this.label = label.substring(0,10);
            }
            this.color = color;
            this.key = null;
        }
        public OptionButtonChoice(String label, Paint color, String key) {
            super();
            this.label = label;
            if (label.length()>10){
                label2 = label.substring(10);
                this.label = label.substring(0,10);
            }
            this.color = color;
            this.key = key;
        }

        public String getLabel() {
            return label;
        }
        public String getLabel2() {
            return label2;
        }

        public Paint getColor() {
            return color;
        }
        public String getKey(){return key;}
    }

    private static final int black = 0xff101010;
    private static Paint blackPaint = null;
    private int selectedIndex = 0;
    private IEquipment equip = null;
    private List<OptionButtonChoice> modes = null;

    private static void setBlackPaint() {
        if (blackPaint == null) {
            blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            blackPaint.setTextSize(20);
            blackPaint.setFakeBoldText(true);
            blackPaint.setColor(black);
        }
    }

    public OptionButton(@NonNull Context context) {
        super(context);
        setBlackPaint();
    }

    public OptionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBlackPaint();
    }

    public OptionButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBlackPaint();
    }

    public void setOptions(List<OptionButtonChoice> modes) {
        this.modes = modes;

    }

    public void setEquipment(IEquipment equip) {
        this.equip = equip;
        setSelectedIndex(this.equip.getSpecial());
    }
    public IEquipment getEquipment(){
        return equip;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
    public OptionButtonChoice getSelected() {
        return modes.get(selectedIndex);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        Paint paint;
        String label;
        String label2 = null;
        if (modes != null) {
            OptionButtonChoice mode = modes.get(selectedIndex);

            paint = mode.getColor();
            label = mode.getLabel();
            label2 = mode.getLabel2();
        } else {
            paint = new Paint();
            label = "default";
        }
        canvas.drawRoundRect(0, 0, width, height, 5, 5, blackPaint);

        canvas.drawRoundRect(5, 5, width - 5, height - 5, 5, 5, paint);
        canvas.drawText(label, 15, 30, blackPaint);
        if (label2!=null) canvas.drawText(label2, 15, 52, blackPaint);


    }

    @Override
    public boolean performClick() {
        selectedIndex++;
        if (selectedIndex >= modes.size()) selectedIndex = 0;
        if (equip != null)
            equip.setSpecial(selectedIndex);
        return super.performClick();
    }
}
