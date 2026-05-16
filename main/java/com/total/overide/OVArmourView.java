package com.total.overide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.total.overiden.ForceList;
import com.total.overiden.IUnitHeader;
import com.total.overiden.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OVArmourView extends View {
    private enum ArmourType {
        OVMECH,
        TWMECH;
    }
    private class ArmourStorage{
        public int armour;
        public int armourTurnDmg;
        public int armourDmg;
    }

    private class PipCatalog{
        public Point[] pipList;
        public OVSegment.OVLocation location;
        public boolean rear;
        public PipCatalog(OVSegment.OVLocation location,Point[] pipList, boolean rear){
            super();
            this.pipList = pipList;
            this.location = location;
            this.rear = rear;
        }
    }
    private final Paint textPaint;
    private final Paint outlinePaint;
    private final Paint filledPaint;
    private final Paint emptyPaint;
    private final Paint savedPaint;
    private final Paint buttonPaint;
    private ArmourType armourType;
    private final Point[] cHeadA = {new Point(144, 32), new Point(144, 22), new Point(132, 32), new Point(156, 32), new Point(120, 20)};
    private final Point[] cTorsoA = {new Point(144, 92), new Point(144, 102), new Point(132, 92), new Point(156, 92), new Point(168, 92),
            new Point(120, 92), new Point(108, 92), new Point(180, 92), new Point(192, 92), new Point(96, 92),
            new Point(132, 102), new Point(156, 102), new Point(168, 102), new Point(120, 102), new Point(108, 102),
            new Point(180, 102), new Point(192, 102), new Point(96, 102), new Point(100, 82), new Point(188, 82)};
    private final Point[] cRearA = {new Point(144, 335), new Point(144, 325), new Point(132, 335), new Point(156, 335), new Point(132, 325),
            new Point(156, 325), new Point(138, 345), new Point(150, 345)};
    private final Point[] cLeftArmA = {new Point(30, 112), new Point(42, 112), new Point(31, 102), new Point(43, 102), new Point(32, 92),
            new Point(44, 92), new Point(33, 82), new Point(45, 82), new Point(34, 72), new Point(46, 72),
            new Point(35, 62), new Point(47, 62)};
    private final Point[] cLeftLegA = {new Point(76, 292), new Point(88, 295), new Point(79, 282), new Point(91, 285), new Point(82, 272),
            new Point(94, 275), new Point(85, 262), new Point(97, 265), new Point(88, 252), new Point(100, 255),
            new Point(91, 242), new Point(103, 245), new Point(94, 232), new Point(106, 235), new Point(102, 222)};
    private final Point[] cRightArmA = {new Point(257, 112), new Point(245, 112), new Point(256, 102), new Point(244, 102), new Point(255, 92),
            new Point(243, 92), new Point(254, 82), new Point(241, 82), new Point(253, 72), new Point(240, 72),
            new Point(252, 62), new Point(239, 62)};
    private final Point[] cRightLegA = {new Point(214, 292), new Point(202, 295), new Point(211, 282), new Point(199, 285), new Point(208, 272),
            new Point(196, 275), new Point(205, 262), new Point(193, 265), new Point(202, 252), new Point(190, 255),
            new Point(199, 242), new Point(187, 245), new Point(196, 232), new Point(184, 235), new Point(188, 222)};
    private final Point[] cHeadS = {new Point(144, 52), new Point(144, 22)};
    private final Point[] cTorsoS = {new Point(144, 128), new Point(144, 138), new Point(132, 128), new Point(156, 128), new Point(156, 138),
            new Point(132, 138), new Point(138, 148), new Point(150, 148), new Point(138, 158), new Point(150, 158)};
    private final Point[] cLeftArmS = {new Point(36, 129), new Point(35, 139), new Point(34, 149), new Point(33, 159), new Point(32, 169),
            new Point(31, 179)};
    private final Point[] cRightArmS = {new Point(252, 129), new Point(253, 139), new Point(254, 149), new Point(255, 159), new Point(256, 169),
            new Point(257, 179)};
    private final Point[] cLeftLegS = {new Point(78, 306), new Point(76, 316), new Point(73, 326), new Point(71, 336), new Point(69, 346),
            new Point(66, 356), new Point(64, 366)};
    private final Point[] cRightLegS = {new Point(210, 306), new Point(212, 316), new Point(215, 326), new Point(217, 336), new Point(219, 346),
            new Point(222, 356), new Point(224, 366)};
    private List<PipCatalog> pips;
    private List<PipCatalog> pipsStruct;
    private final GestureDetector detector;
    private boolean locked = true;
    static class MyListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(@NonNull MotionEvent event) {
            Log.d("TAG", "onDown: ");

            // don't return false here or else none of the other
            // gestures will work
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            return true;
        }
    }

    public static class Hitbox {
        public OVSegment.OVLocation loc;
        public RectF hit;

        public Hitbox(OVSegment.OVLocation loc, RectF hit) {
            super();
            this.loc = loc;
            this.hit = hit;
        }
    }

    protected static class Hitboxes {
        public List<Hitbox> boxes;

        public Hitboxes() {
            super();
            boxes = new ArrayList<Hitbox>();

        }
        public void addHitBoxes(OVUnitDesign unit, boolean structure){
// TODO: Add hitboxes for vehicles and emplacements
            if (unit.getHeader().getType()== ForceList.ForceType.OV) {
                if (boxes.size()==7)return; // this means it is already set up for OV
                boxes.clear();
                boxes.add(new Hitbox(OVSegment.OVLocation.HEAD, new RectF(198, 33, 252, 96)));
                boxes.add(new Hitbox(OVSegment.OVLocation.REAR, new RectF(198, 487, 252, 535)));
                boxes.add(new Hitbox(OVSegment.OVLocation.TORSO, new RectF(144, 123, 306, 255)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTARM, new RectF(45, 93, 88, 186)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTLEG, new RectF(114, 333, 177, 460)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTARM, new RectF(320, 93, 405, 186)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTLEG, new RectF(276, 333, 339, 460)));
            } else if (!structure){
                if (boxes.size()==11)return; // this means it is already set up for TW Armour
                boxes.clear();
                boxes.add(new Hitbox(OVSegment.OVLocation.HEAD, new RectF(197, 30, 245, 150)));
                boxes.add(new Hitbox(OVSegment.OVLocation.CTREAR, new RectF(195,600,245,766)));
                boxes.add(new Hitbox(OVSegment.OVLocation.CENTRETORSO, new RectF(197, 164, 245, 342)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTARM, new RectF(21, 111, 70, 296)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTLEG, new RectF(76, 222, 118, 307)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTARM, new RectF(340, 111, 418, 296)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTLEG, new RectF(184, 222, 226, 307)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTTORSO, new RectF(103, 124, 173, 306)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LTREAR, new RectF(120,653,173,719)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTTORSO, new RectF(272, 124, 336, 306)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RTREAR, new RectF(263,653,328,719)));
            } else {
                if (boxes.size()==8)return; // this means it is already set up for TW struc
                boxes.clear();
                boxes.add(new Hitbox(OVSegment.OVLocation.HEAD, new RectF(197,31,245,102)));
                boxes.add(new Hitbox(OVSegment.OVLocation.CENTRETORSO, new RectF(203,126,242,291)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTARM, new RectF(48,102,113,329)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTLEG, new RectF(128,315,190,564)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTARM, new RectF(341,102,399,329)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTLEG, new RectF(265,315,344,564)));
                boxes.add(new Hitbox(OVSegment.OVLocation.LEFTTORSO, new RectF(133,95,187,280)));
                boxes.add(new Hitbox(OVSegment.OVLocation.RIGHTTORSO, new RectF(264,95,315,280)));
            }
        }

    }

    protected Hitboxes mvArmHits;
    protected OVUnitDesign unit;
    private boolean displayStructure = false;
    private Bitmap background;
    private Bitmap turret = null;
    public OVArmourView(Context c, AttributeSet attrs) {
        super(c, attrs);
        armourType = ArmourType.OVMECH;
        this.setMeasuredDimension(450,795);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pips = new ArrayList<>();
        pipsStruct = new ArrayList<>();
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        savedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getResources().getColor(R.color.black,null));
        outlinePaint.setColor(getResources().getColor(R.color.black,null));
        filledPaint.setColor(getResources().getColor(R.color.Red,null));
        emptyPaint.setColor(getResources().getColor(R.color.white,null));
        savedPaint.setColor(getResources().getColor(R.color.Orange,null));
        buttonPaint.setColor(getResources().getColor(R.color.Green,null));
        detector = new GestureDetector(getContext(), new MyListener());
        mvArmHits = new Hitboxes();


        textPaint.setTextSize(50);

    }

    private static Point[] generateArmourArray(String[] points) {
        Point[] armour = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            String[] list = points[i].split(",");
            armour[i] = new Point(Integer.parseInt(list[0]), Integer.parseInt(list[1]));
        }
        return armour;
    }
    public void setMech(OVUnitDesign unit) {
        ForceList.ForceType old = this.unit!=null?this.unit.getHeader().getType():null;
        this.unit = unit;
        switch (unit.getHeader().getUnitType()) {
            case MECH:
                switch (unit.getHeader().getType()) {
                    case TW:
                        armourType = ArmourType.TWMECH;
                        if (old != ForceList.ForceType.TW) {
                            pips.clear();
                            pipsStruct.clear();
                            background = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_armour, null);
                            background = Bitmap.createScaledBitmap(background, 450, 795, false);
                            pips.add(new PipCatalog(OVSegment.OVLocation.HEAD,
                                    generateArmourArray(getResources().getStringArray(R.array.twheadarmour)), false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.CENTRETORSO,
                                    generateArmourArray(getResources().getStringArray(R.array.twtorsoarmour)), false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.LEFTARM,
                                    generateArmourArray(getResources().getStringArray(R.array.twleftarmarmour)), false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.LEFTLEG,
                                    generateArmourArray(getResources().getStringArray(R.array.twleftlegarmour)), false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.RIGHTARM,
                                    generateArmourArray(getResources().getStringArray(R.array.twrightarmarmour)), false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.RIGHTLEG,
                                    generateArmourArray(getResources().getStringArray(R.array.twrightlegarmour)), false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.LEFTTORSO,
                                    generateArmourArray(getResources().getStringArray(R.array.twltarmour)), false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.RIGHTTORSO,
                                    generateArmourArray(getResources().getStringArray(R.array.twrtarmour)), false));

                            pips.add(new PipCatalog(OVSegment.OVLocation.LTREAR,
                                    generateArmourArray(getResources().getStringArray(R.array.twltrarmour)), true));
                            pips.add(new PipCatalog(OVSegment.OVLocation.RTREAR,
                                    generateArmourArray(getResources().getStringArray(R.array.twrtrarmour)), true));
                            pips.add(new PipCatalog(OVSegment.OVLocation.CTREAR,
                                    generateArmourArray(getResources().getStringArray(R.array.twreartorsoarmour)), true));

                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.HEAD,
                                    generateArmourArray(getResources().getStringArray(R.array.twheadstruc)), false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.CENTRETORSO,
                                    generateArmourArray(getResources().getStringArray(R.array.twctstruc)), false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.LEFTARM,
                                    generateArmourArray(getResources().getStringArray(R.array.twlastruc)), false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.LEFTLEG,
                                    generateArmourArray(getResources().getStringArray(R.array.twllstruc)), false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.RIGHTARM,
                                    generateArmourArray(getResources().getStringArray(R.array.twrastruc)), false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.RIGHTLEG,
                                    generateArmourArray(getResources().getStringArray(R.array.twrlstruc)), false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.LEFTTORSO,
                                    generateArmourArray(getResources().getStringArray(R.array.twltstruc)), false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.RIGHTTORSO,
                                    generateArmourArray(getResources().getStringArray(R.array.twrtstruc)), false));
                        }
                        break;
                    default: // OV and catch anything else
                        armourType = ArmourType.OVMECH;
                        if (old != ForceList.ForceType.OV) {
                            pips.clear();
                            background = BitmapFactory.decodeResource(this.getResources(), R.drawable.blankovarmour, null);
                            background = Bitmap.createScaledBitmap(background, 450, 633, false);
                            pips.add(new PipCatalog(OVSegment.OVLocation.HEAD, cHeadA, false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.TORSO, cTorsoA, false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.LEFTARM, cLeftArmA, false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.LEFTLEG, cLeftLegA, false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.RIGHTARM, cRightArmA, false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.RIGHTLEG, cRightLegA, false));
                            pips.add(new PipCatalog(OVSegment.OVLocation.REAR, cRearA, true));

                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.HEAD, cHeadS, false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.TORSO, cTorsoS, false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.LEFTARM, cLeftArmS, false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.LEFTLEG, cLeftLegS, false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.RIGHTARM, cRightArmS, false));
                            pipsStruct.add(new PipCatalog(OVSegment.OVLocation.RIGHTLEG, cRightLegS, false));
                            // remove errors by assigning values but won't be used
                        }
                }
                break;
            case HOVER:
            case WHEEL:
                background = BitmapFactory.decodeResource(this.getResources(), R.drawable.hoverarmourblank, null);
            case TANK:
                armourType = ArmourType.OVMECH; // this will do for now, need to fix though
                if (unit.getHeader().getUnitType()== IUnitHeader.UnitType.TANK)
                    background = BitmapFactory.decodeResource(this.getResources(), R.drawable.tankarmourblank, null);
                background = Bitmap.createScaledBitmap(background, 450, 795, false);
                if (turret==null){
                    turret = BitmapFactory.decodeResource(this.getResources(), R.drawable.tankturretblank, null);
                    turret = Bitmap.createScaledBitmap(turret, 450, 795, false);
                }
                pips.add(new PipCatalog(OVSegment.OVLocation.FRONT,
                        generateArmourArray(getResources().getStringArray(R.array.twfrontarmour)), false));
                pips.add(new PipCatalog(OVSegment.OVLocation.LEFT,
                        generateArmourArray(getResources().getStringArray(R.array.twleftsidearmour)), false));
                pips.add(new PipCatalog(OVSegment.OVLocation.RIGHT,
                        generateArmourArray(getResources().getStringArray(R.array.twrightsidearmour)), false));
                pips.add(new PipCatalog(OVSegment.OVLocation.REARSIDE,
                        generateArmourArray(getResources().getStringArray(R.array.twrearsidearmour)), false));
                pips.add(new PipCatalog(OVSegment.OVLocation.TURRET,
                        generateArmourArray(getResources().getStringArray(R.array.twturretarmour)), false));

                pipsStruct.add(new PipCatalog(OVSegment.OVLocation.FRONT,
                        generateArmourArray(getResources().getStringArray(R.array.twfrontstruct)), false));
                pipsStruct.add(new PipCatalog(OVSegment.OVLocation.LEFT,
                        generateArmourArray(getResources().getStringArray(R.array.twleftsidestruct)), false));
                pipsStruct.add(new PipCatalog(OVSegment.OVLocation.RIGHT,
                        generateArmourArray(getResources().getStringArray(R.array.twrightsidestruct)), false));
                pipsStruct.add(new PipCatalog(OVSegment.OVLocation.REARSIDE,
                        generateArmourArray(getResources().getStringArray(R.array.twrearsidestruc)), false));
                pipsStruct.add(new PipCatalog(OVSegment.OVLocation.TURRET,
                        generateArmourArray(getResources().getStringArray(R.array.twturretstruc)), false));
                break;
            case VTOL:
                break;
            case BUILDING:
                armourType = ArmourType.OVMECH; // this will do for now, need to fix though
                background = BitmapFactory.decodeResource(this.getResources(), R.drawable.emplacementarmourblank, null);
                pips.add(new PipCatalog(OVSegment.OVLocation.BUILDING,
                        generateArmourArray(getResources().getStringArray(R.array.twemplacementarmour)), false));
                pipsStruct.add(new PipCatalog(OVSegment.OVLocation.BUILDING,
                        generateArmourArray(getResources().getStringArray(R.array.twfrontstruct)), false));
                break;
        }
        mvArmHits.addHitBoxes(unit,displayStructure);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(background, 0, 0, outlinePaint);
        if (unit.getSegment(OVSegment.OVLocation.TURRET)!=null){
            canvas.drawBitmap(turret, 0, 0, outlinePaint);
        }
//        // draw structure
        if (armourType==ArmourType.OVMECH) {
            for (PipCatalog cat : pips){
                OVSegment seg = unit.getSegment(cat.location);
                if (seg!=null){
                    drawArmourPips(canvas, cat.pipList, getArmourDamage(seg,cat.rear), 12);
                }
            }

            for (PipCatalog cat : pipsStruct){
                OVSegment seg = unit.getSegment(cat.location);
                if (seg!=null)drawArmourPips(canvas, cat.pipList, getStructureDamage(seg), 12);
            }

         } else {
            if (!displayStructure) {
                for (PipCatalog cat : pips){
                    OVSegment seg = unit.getSegment(cat.location);
                    if (seg!=null){
                        drawArmourPips(canvas, cat.pipList, getArmourDamage(seg,cat.rear), 12);
                    }
                }
            } else {
                for (PipCatalog cat : pipsStruct){
                    OVSegment seg = unit.getSegment(cat.location);
                    if (seg!=null){
                        drawArmourPips(canvas, cat.pipList, getStructureDamage(seg), 12);
                    }
                }
            }
            canvas.drawRoundRect(10, 5, 60, 55, 5, 5, buttonPaint);
            canvas.drawText(displayStructure ? "S" : "A", 15, 50, textPaint);
        }
        if (!locked) {
            canvas.drawRoundRect(100, 5, 160, 55, 5, 5, buttonPaint);
            canvas.drawText(plus ? "+" : "-", 115, 50, textPaint);
        }
    }
    private ArmourStorage getArmourDamage(OVSegment segment, boolean rear){
        ArmourStorage store = new ArmourStorage();
        if (!rear)
            store.armour = segment.getArmour();
        else
            store.armour = segment.getArmourRear();
        if (segment instanceof OVSegmentInst) {
            if (!rear) {
                store.armourDmg = ((OVSegmentInst) segment).getArmourDmg();
                store.armourTurnDmg = ((OVSegmentInst) segment).getArmourTurnDmg();
            }else{
                store.armourTurnDmg = ((OVSegmentInst) segment).getRearTurnDmg();
                store.armourDmg = ((OVSegmentInst) segment).getRearDmg();
            }
        }
        return store;
    }
    private ArmourStorage getStructureDamage(OVSegment segment){
        ArmourStorage store = new ArmourStorage();
        store.armour = segment.getStructure();
        if (segment instanceof OVSegmentInst) {
                store.armourDmg = ((OVSegmentInst) segment).getStructureDmg();
                store.armourTurnDmg = ((OVSegmentInst) segment).getStructureTurnDmg();
        }
        return store;
    }

    private void drawArmourPips(Canvas g, Point[] pLocations, ArmourStorage store, int size) {
        int lTemp = store.armourTurnDmg;
        int lSaved = store.armourDmg;
        for (int i = 0; i < store.armour && pLocations.length > i; i++) {
            g.drawOval((int) (pLocations[i].x * 1.5), (int) (pLocations[i].y * 1.5), (int) (pLocations[i].x * 1.5) + size, (int) (pLocations[i].y * 1.5) + size, outlinePaint);
            Paint detPaint = null;
            if (lTemp <= 0) {
                if (lSaved <= 0)
                    detPaint = savedPaint;
                else
                    detPaint = filledPaint;

            } else {
                detPaint = emptyPaint;
            }
            lSaved--;

            lTemp--;
            g.drawOval((int) (pLocations[i].x * 1.5) + 2, (int) (pLocations[i].y * 1.5) + 2, (int) (pLocations[i].x * 1.5) + size - 2, (int) (pLocations[i].y * 1.5) + size - 2, detPaint);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event);
        if (armourType==ArmourType.TWMECH &&
                event.getX()>10 && event.getX()<60 &&
                event.getY()>5 && event.getY()<55){
            displayStructure = !displayStructure;
            // reset the hitboxes
            mvArmHits.addHitBoxes(unit,displayStructure);

            background = BitmapFactory.decodeResource(this.getResources(), displayStructure?R.drawable.mech_structure:R.drawable.mech_armour, null);
            if (displayStructure) background = Bitmap.createScaledBitmap(background, 450, 633, false);
            else background = Bitmap.createScaledBitmap(background, 450, 795, false);
            invalidate();
        } else {
            checkClick(event.getX(), event.getY());

            if (event.getX() > 100 && event.getX() < 160 && event.getY() > 5 && event.getY() < 55) {
                plus = !plus;
                buttonPaint.setColor(getResources().getColor(plus ? R.color.Green : R.color.Blue, null));

                invalidate();
            }
        }
        return result;
    }

    private boolean plus = true;

    protected boolean checkClick(float x, float y) {
        boolean lHit = false;
        if (locked) return false;
        for (Iterator<Hitbox> it = mvArmHits.boxes.iterator(); it.hasNext() && !lHit; ) {
            Hitbox box = it.next();
            if (box.hit.contains(x, y)) {
                if (unit instanceof TWUnitData){
                    unit.getSegment(box.loc).incrementDamage(displayStructure,plus);
                } else {
                    OVSegment seg = unit.getSegment(box.loc);
                    if (seg instanceof OVSegmentInst)
                        updateArmourInst((OVSegmentInst) seg, box.loc == OVSegment.OVLocation.REAR);
                    else updateArmour(seg, box.loc == OVSegment.OVLocation.REAR);

                    lHit = true;
                }
                break;
            }

        }

        invalidate();

        return lHit;
    }
    private void updateArmourInst(OVSegmentInst seg,boolean rear){
        if (plus) {
            if (rear) {
                if ( seg.getRearTurnDmg() > 0)
                    seg.setRearTurnDmg(seg.getRearTurnDmg() - 1);
                else if (seg.getStructureTurnDmg() > 0)
                    seg.setStructureTurnDmg(seg.getStructureTurnDmg() - 1);
            } else {
                if (seg.getArmourTurnDmg() > 0)
                    seg.setArmourTurnDmg(seg.getArmourTurnDmg() - 1);
                else if (seg.getStructureTurnDmg() > 0)
                    seg.setStructureTurnDmg(seg.getStructureTurnDmg() - 1);
            }
        } else {
            if (rear) {
                // for the rear don't adjust structure
                if ( seg.getRearTurnDmg() < seg.getArmourRear()) {
                    seg.setRearTurnDmg(seg.getRearTurnDmg() + 1);
                    if (seg.getRearTurnDmg()>seg.getRearDmg())
                        seg.setRearDmg(seg.getRearTurnDmg());
                }
            } else {
                if (seg.getStructureTurnDmg() < seg.getStructure()) {
                    seg.setStructureTurnDmg(seg.getStructureTurnDmg() + 1);
                    if (seg.getStructureTurnDmg()>seg.getStructureDmg())
                        seg.setStructureDmg(seg.getStructureTurnDmg());
                } else if (seg.getArmourTurnDmg() < seg.getArmour()) {
                    seg.setArmourTurnDmg(seg.getArmourTurnDmg() + 1);
                    if (seg.getArmourTurnDmg()>seg.getArmourDmg())
                        seg.setArmourDmg(seg.getArmourTurnDmg());
                }
            }
        }
        seg.setSentFlag(false);
    }
    private void updateArmour(OVSegment seg,boolean rear){
        if (!plus) {
            if (rear) {
                if (seg.getArmourRear() > 0)
                    seg.setRearArmour(seg.getArmourRear() - 1);
            } else {
                if (seg.getArmour() > 0)
                    seg.setArmour(seg.getArmour() - 1, seg.getStructure());
            }
        } else {
            if (rear) {
                if (seg.getArmourRear() < (seg.getStructure() * 2))
                    seg.setRearArmour(seg.getArmourRear() + 1);
            } else {
                if (seg.getArmour() < (seg.getStructure() * 2))
                    seg.setArmour(seg.getArmour() + 1, seg.getStructure());
            }
        }
    }
    public void setLocked(boolean locked){
        this.locked = locked;
        invalidate();
    }
}
