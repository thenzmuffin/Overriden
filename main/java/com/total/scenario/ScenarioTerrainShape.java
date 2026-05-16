package com.total.scenario;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;

import com.total.overiden.MainActivity;
import com.total.overiden.R;

import java.util.ArrayList;
import java.util.List;

/*
 * This class holds woods and any other variable shaped terrain that is added
 */
public class ScenarioTerrainShape extends ScenarioTerrain {
    private List<Point> coords;
    private static Paint shade = null;
    private int selected = 0;
    public ScenarioTerrainShape(Terrain store) {
        super(store);
        coords = new ArrayList<>();
        if (shade==null){
            shade = new Paint();
            shade.setColor(MainActivity.currentActivity.getResources().getColor(R.color.PaleGreen,null));
        }

    }

    public ScenarioTerrainShape(Terrain terrain, double angle, float x, float y) {
        super(terrain, angle, x, y);
        coords = new ArrayList<>();
        coords.add(new Point((int)x,(int)y));
        coords.add(new Point((int)(x + 60),(int)(y+60)));
        coords.add(new Point((int)(x + 60),(int)(y-60)));
        if (shade==null){
            shade = new Paint();
            shade.setColor(MainActivity.currentActivity.getResources().getColor(R.color.PaleGreen,null));
        }
    }

    @Override
    public void setSelectionPoint(float x, float y) {
        // calculate the distance from this point to each corner and determine which is closest,
        // this will be the corner selected
        selected = 0;
        int closestDistance = -1;
        // pick the corner to be adjusted
        for (int i = 0;i < coords.size();i++){
            int distance = (int) (Math.pow(coords.get(i).x - (int)x,2) + Math.pow(coords.get(i).y - (int)y,2));
            if (closestDistance<0 || closestDistance > distance){
                selected = i;
                closestDistance = distance;
            }
            Float.floatToIntBits(x);
        }

    }
    @Override
    public void drawTerrain(Canvas canvas){
        /* Draw this piece of terrain on the supplied canvas */
//        if (terrain.getPicture() == null) {
        Path shape = new Path();
        boolean first = true;
        shape.reset();
        if (coords.size()>2) {
            for (Point point : coords) {
                super.drawTerrain(canvas, point.x, point.y);
                if (first) {
                    shape.moveTo(point.x, point.y);
                    first = false;
                } else {
                    shape.lineTo(point.x, point.y);
                }
            }
        } else {
            // assume size of 2
        }
        shape.close();

        canvas.drawPath(shape, shade);
//        } else {
//            canvas.drawBitmap(rotateBitmap(scaledImage,(float)(angle + tempAngle)),x,y,new Paint());
//        }
    }
    @Override
    public boolean amIHere(float clickX, float clickY){
        Path shape = new Path();
        boolean first = true;
        shape.reset();
        for (Point point : coords) {
            if (first) {
                shape.moveTo(point.x, point.y);
                first = false;
            } else {
                shape.lineTo(point.x, point.y);
            }
        }
        shape.close();
        RectF rectF = new RectF();
        shape.computeBounds(rectF, true);
        //we need a polygon for our current location
        return rectF.contains(clickX,clickY);
    }
    @Override
    public boolean canSnapToGrid(){return true;}
    @Override
    public boolean setLocation(float x, float y) {
        // update the location of the currently selected point with the new coordinates
        boolean changed = false;
        if (selected >=0 && selected < coords.size()){
            Point point =coords.get(selected);
            if (point.x != (int)x || point.y != (int)y) {
                point.x = (int) x;
                point.y = (int) y;
                changed = true;
            }
        }
        return changed;
    }
}
