package com.total.overiden;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class PilotHealthView extends View {
    private static final int red = 0xffff1010;
    private static final int green = 0xff10ff10;
    private static final int black = 0xff101010;
    private static final int white = 0xffffffff;
    private static Paint outlinePaint;
    private static Paint backPaint;
    private static Paint filledPaint;
    private static Paint emptyPaint;
//    private int mPilotInjuries = 0;
    private Pilot pilot;

    public PilotHealthView(Context context) {
        super(context);
        init(null, 0);
        createPaints();
    }

    public PilotHealthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        createPaints();
    }

    public PilotHealthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
        createPaints();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private static void createPaints() {
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(black);
        outlinePaint.setTextSize(20F);
        backPaint.setColor(white);
        filledPaint.setColor(red);
        emptyPaint.setColor(green);
    }

    public void setPilot(Pilot pil) {
        pilot = pil;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PilotHealthView, defStyle, 0);

        a.recycle();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int temp = pilot.getInjuries();
        canvas.drawRoundRect(0,0,300,80,5,5,outlinePaint);
        canvas.drawRoundRect(4,4,296,76,5,5,backPaint);
        canvas.drawText(pilot.getPilotName(), 5, 25, outlinePaint);
        canvas.drawText("Gunnery: " + pilot.getGunneryDisplay(), 5, 50, outlinePaint);
        canvas.drawText("Piloting: " + pilot.getPilotSkillDisplay(), 155, 50, outlinePaint);
        if (!pilot.isConscious())canvas.drawText("Unconscious" + pilot.getPilotSkillDisplay(), 155, 25, filledPaint);
        for (int i = 0; i < 6; i++) {
            canvas.drawOval(i * 35 + 5, 55, i * 35 + 25, 75, outlinePaint);
            if (temp > 0) {
                canvas.drawOval(i * 35 + 7, 57, i * 35 + 23, 73, filledPaint);
                temp--;
            } else
                canvas.drawOval(i * 35 + 7, 57, i * 35 + 23, 73, emptyPaint);
        }

    }

}
