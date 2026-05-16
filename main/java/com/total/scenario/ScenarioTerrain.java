package com.total.scenario;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class ScenarioTerrain {
    private long key = -1;
    //    private List<Point> corners;
    private Terrain terrain;
    //    private Bitmap image;
    private Bitmap scaledImage;
    private String name;
    private double angle = 0;
    private double tempAngle = 0; //used while an item is being twisted
    private float x = 0, y = 0;
    private float xSelection = 0, ySelection = 0;
    private final double oneFoot = 220;
    private double scale = 1;

    public static ScenarioTerrain newInstance(Terrain store, double angle, float x, float y) {
        if (store.isShape()) {
            return new ScenarioTerrainShape(store, angle, x, y);
        }
        return new ScenarioTerrain(store, angle, x, y);
    }

    public ScenarioTerrain(Terrain store) {
        super();
//        corners = new ArrayList<>();
        terrain = store;
        scaledImage = Bitmap.createScaledBitmap(terrain.getPicture(),
                (int) (terrain.getPicture().getWidth() * scale),
                (int) (terrain.getPicture().getHeight() * scale),
                false);

        name = "Terrain";
    }

    public ScenarioTerrain(Terrain terrain, double angle, float x, float y) {
        super();
        //       corners = new ArrayList<>();

        this.terrain = terrain;

//        scaledImage = Bitmap.createScaledBitmap(terrain.getPicture(),
//                (int)(terrain.getPicture().getWidth() * scale),
//                (int)(terrain.getPicture().getHeight() * scale),
//                false);
        scaledImage = rotateBitmap(Bitmap.createScaledBitmap(terrain.getPicture(),
                (int) (terrain.getPicture().getWidth() * scale),
                (int) (terrain.getPicture().getHeight() * scale),
                false), (float) (angle + tempAngle));

        name = "Terrain";
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void drawTerrain(Canvas canvas) {
        /* Draw this piece of terrain on the supplied canvas */
        canvas.drawBitmap(scaledImage, x - xSelection, y - ySelection, new Paint());
//        canvas.drawBitmap(rotateBitmap(scaledImage,(float)(angle + tempAngle)),x,y,new Paint());
    }

    public void drawTerrain(Canvas canvas, int x, int y) {
        /* Draw this piece of terrain on the supplied canvas */

//        canvas.drawBitmap(rotateBitmap(scaledImage,(float)(angle + tempAngle)),
        canvas.drawBitmap(scaledImage,
                x - (Math.floorDiv(scaledImage.getWidth(), 2)),
                y - (Math.floorDiv(scaledImage.getHeight(), 2)), new Paint());
    }

    public String getName() {
        return name;
    }

    public boolean setLocation(float x, float y) {
        float adjustedX = x - xSelection;
        float adjustedY = y - ySelection;
        if (terrain.getPicture() != null) {
            adjustedX -= (scaledImage.getWidth() / (float) 2);
            adjustedY -= (scaledImage.getHeight() / (float) 2);
        }
        boolean changed = Math.abs(adjustedX) != Math.abs(this.x) &&
                Math.abs(adjustedY) != Math.abs(this.y);
        this.x = adjustedX;
        this.y = adjustedY;
        return changed;
    }

    public boolean amIHere(float clickX, float clickY) {
        //we need a polygon for our current location
        return (clickX > x && (clickX - x) < scaledImage.getWidth() &&
                clickY > y && (clickY - y) < scaledImage.getHeight());
    }

    public boolean canSnapToGrid() {
        return false;
    }

    public boolean canTwist() {
        return true;
    }

    public boolean setAngleTemp(double angleAdjust) {
        boolean changed = tempAngle != angleAdjust;
        tempAngle = angleAdjust;
        if (changed)
            // rescale and rotate
            scaledImage = rotateBitmap(Bitmap.createScaledBitmap(terrain.getPicture(),
                    (int) (terrain.getPicture().getWidth() * scale),
                    (int) (terrain.getPicture().getHeight() * scale),
                    false), (float) (angle + tempAngle));
        return changed;
    }

    public void setSelectionPoint(float x, float y) {
        // record the difference between the selection point and the reference point for this map object
        xSelection = (float)((x - this.x)*scale);
        ySelection = (float)((y - this.y)*scale);
    }

    public void resetSelectionPoint(){
       xSelection = ySelection = 0;
    }
    public void setAngle(){
        angle = angle + tempAngle;
        tempAngle = 0;
    }

    public void setScale(int footLength) {
        double tempScale = (double)footLength / oneFoot;
        if (tempScale != scale) {
            scale = tempScale;
            scaledImage = rotateBitmap(Bitmap.createScaledBitmap(terrain.getPicture(),
                    (int)(terrain.getPicture().getWidth() * scale),
                    (int)(terrain.getPicture().getHeight() * scale),
                    false),(float)(angle + tempAngle));
//            scaledImage = Bitmap.createScaledBitmap(terrain.getPicture(),
//                    (int)(terrain.getPicture().getWidth() * scale),
//                    (int)(terrain.getPicture().getHeight() * scale),
//                    false);
        }
    }
    private Bitmap rotateBitmap(Bitmap source, float angle) {
        if (angle==0) return source;
        Matrix matrix = new Matrix();
        matrix.postRotate((float)Math.toDegrees(angle));
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Terrain getType() {
        return terrain;
    }

    public double getAngle() {
        return angle;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }
}
