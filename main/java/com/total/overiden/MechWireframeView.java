package com.total.overiden;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.total.overide.OVSegment;
import com.total.overide.OVSegmentInst;

public class MechWireframeView extends View {
    private final Paint outlinePaint;
    private final Paint torsoPaint;
    private final Paint leftTorsoPaint;
    private final Paint rightTorsoPaint;
    private final Paint leftArmPaint;
    private final Paint rightArmPaint;
    private final Paint leftLegPaint;
    private final Paint rightLegPaint;
    private final Paint headPaint;
    private final Paint rearPaint;
    private static boolean loaded = false;
    private IUnitHeader.UnitType type;
    private static Bitmap tankback = null;
    private static Bitmap turretback = null;
    private static Bitmap hoverback = null;
    private static Bitmap tankfront = null;
    private static Bitmap tankleft = null;
    private static Bitmap tankright = null;
    private static Bitmap tankrear = null;
    private static Bitmap tankturret = null;
    private static Bitmap emplacement = null;
    private static Bitmap background;
    private static Bitmap head;
    private static Bitmap centreTorso;
    private static Bitmap rightTorso;
    private static Bitmap leftTorso;
    private static Bitmap rear;
    private static Bitmap leftArm;
    private static Bitmap rightArm;
    private static Bitmap leftLeg;
    private static Bitmap rightLeg;
    private boolean hasTurret = false;

    public MechWireframeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        outlinePaint.setAlpha(1);
        torsoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leftLegPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leftArmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightArmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightLegPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leftTorsoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightTorsoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (!loaded){
            tankback = BitmapFactory.decodeResource(this.getResources(), R.drawable.tank_wireframe, null);
            hoverback = BitmapFactory.decodeResource(this.getResources(), R.drawable.hover_wireframe, null);
            turretback = BitmapFactory.decodeResource(this.getResources(), R.drawable.turret_wireframe, null);
            tankfront = BitmapFactory.decodeResource(this.getResources(), R.drawable.tank_wireframe_f, null);
            tankleft = BitmapFactory.decodeResource(this.getResources(), R.drawable.tank_wireframe_ls, null);
            tankright = BitmapFactory.decodeResource(this.getResources(), R.drawable.tank_wireframe_rs, null);
            tankrear = BitmapFactory.decodeResource(this.getResources(), R.drawable.tank_wireframe_r, null);
            tankturret = BitmapFactory.decodeResource(this.getResources(), R.drawable.tank_wireframe_t, null);
            emplacement = BitmapFactory.decodeResource(this.getResources(), R.drawable.emplacement_wireframe, null);

            background = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm, null);
            head = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_h, null);
            leftArm = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_la, null);
            leftLeg = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_ll, null);
            leftTorso = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_lt, null);
            centreTorso = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_ct, null);
            rightTorso = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_rt, null);
            rightLeg = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_rl, null);
            rightArm = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_ra, null);
            rear = BitmapFactory.decodeResource(this.getResources(), R.drawable.mech_wireframe_sm_r, null);
        }
    }

//    public void calculateSize() {
//
//    }
    private final float[] matrix = {0,0,0,0,0,
                              0,0,0,0,0,
                              0,0,0,0,0,
                              0,0,0,1,0};
    private void setSingle(int state, int offset){
        switch (state){
            case 5: // pristine full green
                matrix[offset] = 0.15f;
                matrix[offset+5] = 0.56f;
                matrix[offset+10] = 0.15f;
                break;
            case 4: // minor - pale green
                matrix[offset] = 0.00f;
                matrix[offset+5] = 1f;
                matrix[offset+10] = 0.00f;
                break;
            case 3: //moderate - yellow
                matrix[offset] = 1f;
                matrix[offset+5] = 0.19f;
                matrix[offset+10] = 0.00f;
                break;
            case 2: //serious - orange
                matrix[offset] = 1f;
                matrix[offset+5] = 0.38f;
                matrix[offset+10] = 0.31f;
                break;
            case 1: //bad - red
                matrix[offset] = 1f;
                matrix[offset+5] = 0.00f;
                matrix[offset+10] = 0.00f;
                break;
            case 0: //gone - black
                matrix[offset] = 0.00f;
                matrix[offset+5] = 0.00f;
                matrix[offset+10] = 0.00f;
                break;
        }
    }
    private void resetSegment(OVSegmentInst inst, Paint paint){
        int armState = Math.floorDiv(inst.getArmourTurnDmg() * 5 + inst.getArmour() - 1, inst.getArmour()); //0 - 5 armour rating
        setSingle(armState,1);
        armState = Math.floorDiv(inst.getStructureTurnDmg() * 5 + inst.getStructure() - 1, inst.getStructure()); //0 - 5 armour rating
        setSingle(armState,0);
        ColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);

    }
    private void resetRear(OVSegmentInst inst_ct,OVSegmentInst inst_lt,OVSegmentInst inst_rt, Paint paint){
        int armState = Math.floorDiv(inst_ct.getRearTurnDmg() * 5 + inst_ct.getArmourRear() - 1, inst_ct.getArmourRear()); //0 - 5 armour rating
        setSingle(armState,1);
        armState = Math.floorDiv(inst_rt.getRearTurnDmg() * 5 + inst_rt.getArmourRear() - 1, inst_rt.getArmourRear()); //0 - 5 armour rating
        setSingle(armState,2);
        armState = Math.floorDiv(inst_lt.getRearTurnDmg() * 5 + inst_lt.getArmourRear() - 1, inst_lt.getArmourRear()); //0 - 5 armour rating
        setSingle(armState,0);
        ColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);

    }
    public void resetState(IUnitData unit){
        OVSegmentInst inst;
        OVSegmentInst left;
        OVSegmentInst right;
        hasTurret = false;
        type = unit.getHeader().getUnitType();
        switch (type){
            case MECH:
                if (unit.getHeader().getType() == ForceList.ForceType.OV) {
                    inst = left = right = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.TORSO);
                } else {
                    inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.CENTRETORSO);
                    left = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.LEFTTORSO);
                    right = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.RIGHTTORSO);
                }
                resetSegment(inst, torsoPaint);
                resetSegment(left, leftTorsoPaint);
                resetSegment(right, rightTorsoPaint);

                resetRear(inst, left, right, rearPaint);

                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.RIGHTARM);
                resetSegment(inst, rightArmPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.LEFTARM);
                resetSegment(inst, leftArmPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.RIGHTLEG);
                resetSegment(inst, rightLegPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.LEFTLEG);
                resetSegment(inst, leftLegPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.HEAD);
                resetSegment(inst, headPaint);
                break;
            case HOVER:
            case TANK:
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.RIGHT);
                resetSegment(inst, rightArmPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.LEFT);
                resetSegment(inst, leftArmPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.FRONT);
                resetSegment(inst, torsoPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.REARSIDE);
                resetSegment(inst, rearPaint);
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.TURRET);
                hasTurret = inst!=null;
                if (hasTurret)
                    resetSegment(inst, headPaint);
                break;
            case BUILDING:
                inst = (OVSegmentInst) unit.getSegment(OVSegment.OVLocation.BUILDING);
                resetSegment(inst, torsoPaint);
                break;
        }
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (type!=null) {
            switch (type) {
                case MECH:
                    canvas.drawBitmap(background, 0, 0, outlinePaint);
                    canvas.drawBitmap(centreTorso, 0, 0, torsoPaint);
                    canvas.drawBitmap(leftTorso, 0, 0, leftTorsoPaint);
                    canvas.drawBitmap(rightTorso, 0, 0, rightTorsoPaint);
                    canvas.drawBitmap(leftArm, 0, 0, leftArmPaint);
                    canvas.drawBitmap(rightArm, 0, 0, rightArmPaint);
                    canvas.drawBitmap(leftLeg, 0, 0, leftLegPaint);
                    canvas.drawBitmap(rightLeg, 0, 0, rightLegPaint);
                    canvas.drawBitmap(head, 0, 0, headPaint);
                    canvas.drawBitmap(rear, 0, 0, rearPaint);
                    break;
                case TANK:
                    canvas.drawBitmap(tankback, 0, 0, outlinePaint);
                case HOVER:
                    canvas.drawBitmap(hoverback, 0, 0, outlinePaint);
                    if (hasTurret) {
                        canvas.drawBitmap(turretback, 0, 0, outlinePaint);
                        canvas.drawBitmap(tankturret, 0, 0, headPaint);
                    }
                    canvas.drawBitmap(tankleft, 0, 0, leftArmPaint);
                    canvas.drawBitmap(tankright, 0, 0, rightArmPaint);
                    canvas.drawBitmap(tankfront, 0, 0, torsoPaint);
                    canvas.drawBitmap(tankrear, 0, 0, rearPaint);
                    break;
                case BUILDING:
                    canvas.drawBitmap(emplacement,0,0,torsoPaint);
                    break;
            }
        }
    }
}
