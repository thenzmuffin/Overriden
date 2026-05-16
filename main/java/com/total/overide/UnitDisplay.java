package com.total.overide;

import static android.view.View.GONE;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overiden.DamageRecordAdapter;
import com.total.overiden.FacingButton;
import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.HeatView;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDisplay;
import com.total.overiden.MainActivity;
import com.total.overiden.MechWireframeView;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.TooltipDialogFragment;
import com.total.overiden.TurnPhaseFragment;
import com.total.overiden.UnitTypeIconView;

import java.util.Locale;


public class UnitDisplay implements IUnitDisplay {
    protected final UnitData unit;
    protected final int forceNumber;
    public UnitDisplay(UnitData data, int forceNumber){
        super();
        unit = data;
        this.forceNumber = forceNumber;
    }
    public void setDisplayFields(View itemView,
                                 View.OnClickListener callback,
                                 TurnPhaseFragment fragment){
        UnitTypeIconView icon = itemView.findViewById(R.id.unit_icon);
        if (icon!=null)icon.setUnit(unit);
        String display = unit.getHeader().getName() + " - " + unit.getPilot().getPilotName();
        ((TextView)itemView.findViewById(R.id.unitName)).setText(display);

        HeatView heat = itemView.findViewById(R.id.heat_label);
        if (heat != null) {
            heat.setHeatLevel(unit.getState().getHeat());
            heat.setOnClickListener(callback);
        }
        // Set the damage list if needed
        RecyclerView damage_list = itemView.findViewById(R.id.damage_record_list);
        if (damage_list != null) {
            DamageRecordAdapter phaseAdapter = new DamageRecordAdapter(unit, fragment, false);
            damage_list.setAdapter(phaseAdapter);
            damage_list.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        Spinner otherSpinner = itemView.findViewById(R.id.other);
        if (otherSpinner != null && otherSpinner.getAdapter()==null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    itemView.getContext(),
                    R.array.other_values_array,
                    R.layout.simple_overide_spinner_dropdown
            );
// Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
            otherSpinner.setAdapter(adapter);
        }
        MechWireframeView frame = itemView.findViewById(R.id.wireframe);
        if (frame!=null){
//                frame.calculateSize();
            frame.resetState(unit);
        }
        TextView withdrawal = itemView.findViewById(R.id.forcedWithdrawal);
        if (withdrawal != null)withdrawal.setVisibility(unit.getState().isForcedWithdrawal()?View.VISIBLE:GONE);
        if (itemView.findViewById(R.id.tsm_activated)!=null) {
            if (unit.getHeader().isTsm() && unit.getState().getTsmBonus() > 0)
                itemView.findViewById(R.id.tsm_activated).setVisibility(View.VISIBLE);
            else
                itemView.findViewById(R.id.tsm_activated).setVisibility(View.GONE);
        }
        //add status indicators
        LinearLayout status = itemView.findViewById(R.id.status);
        if (status!=null) {
            if (unit.getState().isStunned()) status.addView(getTag("Stunned", 40));
            if (unit.getState().getTurret() == OVState.TurretState.JAMMED)
                status.addView(getTag("Turret\nJammed", 70));
            else if (unit.getState().getTurret() == OVState.TurretState.LOCKED)
                status.addView(getTag("Turret\nLocked", 70));
            if (unit.isImmobile()) status.addView(getTag("Immobile", 40));
        }
        updateEdgePoints(itemView);
    }
    private void updateEdgePoints(View itemView){
        TextView edgePoints = itemView.findViewById(R.id.edgePoints);
        if (edgePoints != null) {
            if (unit.getPilot().isNamedPilot()) {
                int num = unit.getPilot().getEdge();
                edgePoints.setText(String.format(Locale.getDefault(), "%d", num));
                edgePoints.setVisibility(View.VISIBLE);
                edgePoints.setBackground(convertToIcon(R.drawable.edge));
            } else {
                edgePoints.setVisibility(GONE);
            }
        }
    }
    protected Drawable convertToIcon(int rid){
        Drawable pict = AppCompatResources.getDrawable(MainActivity.currentActivity, rid);
        if (pict instanceof BitmapDrawable)pict = new BitmapDrawable(MainActivity.currentActivity.getResources(), Bitmap.createScaledBitmap(((BitmapDrawable) pict).getBitmap(),50,50,true));
        return pict;
    }
    private TextView getTag(String label, int height){
        TextView tag = new TextView(MainActivity.currentActivity);
        tag.setText(label);
        tag.setGravity(Gravity.CENTER);
        tag.setTextColor(MainActivity.currentActivity.getColor(R.color.black));
        tag.setTextSize(20);
        tag.setTypeface(null, Typeface.BOLD);
        tag.setBackgroundColor(MainActivity.currentActivity.getColor(R.color.Red));
        tag.setWidth(100);
        tag.setHeight(height);
        return tag;
    }
    public void setStatusComplete(View itemView, boolean complete){

    }
    public boolean setPhaseComplete(View itemView){
        return false;
    }
    public void updateScreen(View itemView){
        updateEdgePoints(itemView);
    }

    public void changeTarget(View itemView, View.OnClickListener callback) {

    }

    @Override
    public void setListCallbacks(View itemView, AdapterView.OnItemSelectedListener callback) {
        Spinner otherSpinner = itemView.findViewById(R.id.other);
        if (otherSpinner!=null)
            otherSpinner.setOnItemSelectedListener(callback);
    }
    protected void setTargetList(ForceList list, View itemView) {
        Spinner targetSpin = itemView.findViewById(R.id.target);
        // population of the list of valid targets
        if (unit != null) {
            unit.getTurn().populateTargetList(targetSpin,list,null);
        }
    }
    public boolean updateHexValue(View itemView, int change){
        return false;
    }

    @Override
    public boolean processButtonClick(View view, View itemView) {
//        MainActivity.currentActivity.playSound(MainActivity.Sounds.CLICK);
        if (view.getId()==R.id.heat_label) displayHeatScale();

        return false;
    }
    private void displayHeatScale(){
        String description;
        if (Game.current.isSmartHeat())
            description = MainActivity.currentActivity.getApplicationContext().getString(R.string.heat_scale);
        else
            description = MainActivity.currentActivity.getApplicationContext().getString(R.string.ov_heat_scale);
        TooltipDialogFragment tooltip = new TooltipDialogFragment(description);;
        FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
        tooltip.show(mgr, "Turn Heat");
    }
    protected void displayHexButtons(boolean display, View itemView, View.OnClickListener callback){
        itemView.findViewById(R.id.sub_five).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.sub_one).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.add_five).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.add_one).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.value).setVisibility(display?View.VISIBLE:GONE);
        if (display){
            itemView.findViewById(R.id.sub_five).setOnClickListener(callback);
            itemView.findViewById(R.id.sub_one).setOnClickListener(callback);
            itemView.findViewById(R.id.add_five).setOnClickListener(callback);
            itemView.findViewById(R.id.add_one).setOnClickListener(callback);
        }

    }
    public void setTargetDetails(View itemView, View.OnClickListener callback){
        /*
         * This method is required everytime the target is changed. We need to:
         *   update header fields for target (TMM, partial, range, other, facing)
         *   This method does update the weapons list for unlocked weapons only
         */
        TargetData targetData = unit.getTurn().getPrimaryTarget();
        // partial cover flag
        CheckBox partial =  itemView.findViewById(R.id.partial_cover);
        if (partial != null){
            partial.setChecked(targetData.isPartialCover());
            partial.setOnClickListener(callback);
        }

        // set the tmm display
        TextView text = itemView.findViewById(R.id.tmm_display);
        String desc = Integer.toString(targetData.getTargetMovementMod());
        text.setText(desc);

        // set up the facing button
        FacingButton facing = itemView.findViewById(R.id.facing);
        if (facing!=null) {
            facing.setBasicOptions(unit.getHeader().getType());
            facing.setSelectedMode(targetData.getFacing());
            facing.setOnClickListener(callback);
        }

        // update other selection
        int other = targetData.getOther();
        Spinner otherSpinner = itemView.findViewById(R.id.other);
        otherSpinner.setSelection(other + 3);

    }
    public int[] processListSelection(int listId, View itemView){
        return new int[] {-1}; //no refresh required
    }

    @Override
    public IUnitData getUnitData() {
        return unit;
    }

}
