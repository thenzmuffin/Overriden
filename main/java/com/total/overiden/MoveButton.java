package com.total.overiden;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;


public class MoveButton extends androidx.appcompat.widget.AppCompatRadioButton {
    private static final int red = 0xffff1010;
    private static final int white = 0xffffffff;
    private static final int grey = 0xff808080;
    private static final int black = 0xff101010;

    private int color = 0xffff1010;
    private String label = "";
    private String label2 = "";
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MoveButton(Context context) {
        super(context);
    }

    public MoveButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoveButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setupButton(String label_one, String label_two, int color){
        label = label_one;
        label2 = label_two;
        this.color = color;
        invalidate();  // display options have changed so need to redraw the button.
    }
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        if (black == color && this.isChecked())blackPaint.setColor(white);
        else blackPaint.setColor(black);
        if (this.isChecked()) paint.setColor(color);
        else paint.setColor(grey);
        canvas.drawRoundRect(0,0,getWidth(),getHeight(), 5, 5, blackPaint);
        canvas.drawRoundRect(5,5,getWidth()-5,getHeight()-5, 5, 5, paint);
        blackPaint.setTextSize(20);
        blackPaint.setFakeBoldText(true);
        canvas.drawText(label,15,25,blackPaint);
        canvas.drawText(label2,15,50,blackPaint);

    }
    @Override
    public boolean performClick(){

        return super.performClick();
    }
}
