package com.total.overiden;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HealthPipsView extends View {
    private int health = 0;
    private int damage = 0;
    private final int radius = 20;
    private final int pipMargin = 2;
    public HealthPipsView(Context context) {
        super(context);
        this.setMeasuredDimension(80,45);
    }

    public HealthPipsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setMeasuredDimension(80,45);
    }

    public HealthPipsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setMeasuredDimension(80,45);
    }

    public HealthPipsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < health; i++) {
            drawPip(canvas, i * ((radius * 2) + pipMargin) + radius, radius, i < damage);
        }
    }
    private void drawPip(Canvas canvas, int x, int y, boolean damaged) {
        Paint borderColor = new Paint();
        Paint pipColor = new Paint();
        borderColor.setColor(getResources().getColor(R.color.white,null));
        if (damaged)
            pipColor.setColor(getResources().getColor(R.color.Red,null));
        else
            pipColor.setColor(getResources().getColor(R.color.Green,null));

        canvas.drawCircle(x,y,radius,borderColor);
        canvas.drawCircle(x,y,radius - 2 ,pipColor);
    }
}
