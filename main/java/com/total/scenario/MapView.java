package com.total.scenario;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.total.overiden.MainActivity;
import com.total.overiden.R;

public class MapView extends View implements View.OnDragListener, View.OnTouchListener {
    private int width, length;
    private final Paint paintBlack;
    private final Paint paintBackground;
    private ScenarioMap map;
    private ScenarioTerrain selected = null;
    private boolean twist = false;
    private double origAngle;
    // current state
    private final boolean snapToGrid = true;
    public MapView(Context context) {
        super(context);
        paintBlack = new Paint();
        paintBlack.setColor(getResources().getColor(R.color.black,null));
        paintBackground = new Paint();
        paintBackground.setColor(getResources().getColor(R.color.white,null));
        map = null;
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paintBlack = new Paint();
        paintBlack.setColor(getResources().getColor(R.color.black,null));
        paintBackground = new Paint();
        paintBackground.setColor(getResources().getColor(R.color.white,null));
        map = null;
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paintBlack = new Paint();
        paintBlack.setColor(getResources().getColor(R.color.black,null));
        paintBackground = new Paint();
        paintBackground.setColor(getResources().getColor(R.color.white,null));
        map = null;
    }

//    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        paintBlack = new Paint();
//        paintBlack.setColor(getResources().getColor(R.color.black,null));
//        paintBackground = new Paint();
//        paintBackground.setColor(getResources().getColor(R.color.white,null));
//        map = null;
//    }

    public void setMap(ScenarioMap map){
        this.map = map;
        paintBackground.setColor(getResources().getColor(map.getBackgroundColour(),null));
    }

    public ScenarioMap getMap() {
        return map;
    }

    private void adjustDimensions(){
        width = getWidth();
        length = getHeight();
        int widthFoot = Math.floorDiv(width,map.getWidth());
        int lengthFoot = Math.floorDiv(length,map.getLength());
        if (widthFoot < lengthFoot)lengthFoot = widthFoot;
        // get rid of any awkward lengths
        lengthFoot = Math.floorDiv(lengthFoot,3)*3;
        map.setScale(lengthFoot);
        width = lengthFoot * map.getWidth();
        length = lengthFoot * map.getLength();
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        adjustDimensions();
        canvas.drawRect(0,0,width,length,paintBlack);
        canvas.drawRect(4,4,width-4,length-4,paintBackground);
        int spacing = Math.floorDiv(width,map.getWidth()*3);
        for (int i = 1;i < map.getWidth()*3;i++){
            if (Math.floorMod(i,3)==0){
                canvas.drawRect((i * spacing) - 2,0,(i * spacing) + 2,length,paintBlack);
            } else {
                canvas.drawLine(i * spacing,
                        0,
                        i * spacing,
                        length, paintBlack);
            }
        }
        spacing = Math.floorDiv(length,map.getLength()*3);
        for (int i = 1;i < map.getLength()*3;i++) {
            if (Math.floorMod(i,3)==0){
                canvas.drawRect(0,(i * spacing) - 2,width,(i * spacing) + 2,paintBlack);
            } else {
            canvas.drawLine(0, i * spacing,
                    width,
                    i * spacing,
                    paintBlack);
        }
        }

        for (ScenarioTerrain terrain : map.getTerrain() ){
            terrain.drawTerrain(canvas);
        }
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        switch (dragEvent.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
                return true;
            case DragEvent.ACTION_DROP:
                // what do we do here?
                try (ScenarioDB db = new ScenarioDB(MainActivity.currentActivity)) {
                    float xLoc = dragEvent.getX();
                    float yLoc = dragEvent.getY();
                    ScenarioTerrain terry = ScenarioTerrain.newInstance(db.getTerrain(dragEvent.getClipData().getItemAt(0).getText().toString()),
                            0,xLoc, yLoc);
                    if (snapToGrid && terry.canSnapToGrid()){
                        xLoc = getSnapLocation(xLoc,Math.floorDiv(width,map.getWidth()*3));
                        yLoc = getSnapLocation(yLoc,Math.floorDiv(length,map.getLength()*3));
                        terry.setLocation(xLoc, yLoc);
                    }
//                    ScenarioTerrain test = new ScenarioTerrain(db.getTerrain(dragEvent.getClipData().getItemAt(0).getText().toString()));
//                    test.setLocation(dragEvent.getX(), dragEvent.getY());
                    map.addTerrain(terry);
                }
                this.invalidate();
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                for (ScenarioTerrain terrain : map.getTerrain() ){
                    if (terrain.amIHere(motionEvent.getX(), motionEvent.getY())) {
                        selected = terrain;
                        selected.setSelectionPoint(motionEvent.getX(), motionEvent.getY());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (selected!=null)selected.resetSelectionPoint();
                selected = null;
                twist = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i("Overiden",motionEvent.toString());
                if (selected != null && selected.canTwist()) {
                    twist = true;
                    origAngle = Math.atan2(motionEvent.getX(1) - motionEvent.getX(0), motionEvent.getY(1) - motionEvent.getY(0));
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.i("Overiden",motionEvent.toString());
                if (twist) {
                    twist = false;
                    if (selected != null) selected.setAngle();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (twist){
                    if (selected != null){
                        // we need to find the starting point of each pointer and translate their movement into an angle of movement
                        double newAngle = Math.atan2(motionEvent.getX(1) - motionEvent.getX(0), motionEvent.getY(1) - motionEvent.getY(0));
                        if (selected.setAngleTemp(origAngle - newAngle )){
                            invalidate();
                        }
                    }
                } else if (selected != null){
                    float xLoc = motionEvent.getX();
                    float yLoc = motionEvent.getY();
                    if (snapToGrid && selected.canSnapToGrid()){
                        xLoc = getSnapLocation(xLoc,Math.floorDiv(width,map.getWidth()*3));
                        yLoc = getSnapLocation(yLoc,Math.floorDiv(length,map.getLength()*3));
                    }
                    if (selected.setLocation(xLoc, yLoc)) {
                        this.invalidate();
                    }
                }

                break;
        }
        return true;
    }

    private float getSnapLocation(Float coord, int spacing){
        int closestX = Math.floorDiv(coord.intValue(),spacing);
        int modX = Math.floorMod(coord.intValue(),spacing);
        if (modX * 2 >= spacing)closestX++;
        return (float)(closestX*spacing);
    }
    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
