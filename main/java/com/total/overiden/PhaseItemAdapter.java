package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overide.OVWeapon;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class PhaseItemAdapter extends RecyclerView.Adapter<PhaseItemAdapter.ViewHolder> {

    private IUnitData unit;
    private boolean editable = true;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
    //    private final TextView textView;
//        private boolean firstTime = true;
        private TargetWeapon weapon = null;
        public ViewHolder(View view) {
            super(view);
        }
        public void displayItem(TargetWeapon selected) {
            weapon = selected;
            IWeapon gun = selected.getWeapon();
            // we have a TIC not ammo
            CheckBox locked = itemView.findViewById(R.id.locked);

            locked.setChecked(selected.isLocked());
            //reset visibility here to make sure a recycled fragment doesn't hide it due to
            // previously displaying AMS or TAG
            locked.setVisibility(View.VISIBLE);

            locked.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            CheckBox thisLock = (CheckBox) view;
                            //set locked also adds the weapon to the targetdata object or removes it if it is being unlocked.
                            //only take action if the locked status has changed
                            boolean beforeTargetted = false;
                            if (unit.getTurn().isSpottingThisTurn()){
                                // check to see if already had weapons targetted
                                for (TargetData data : unit.getTurn().getAllTargets()){
                                    if (data.hasWeapons()){
                                        beforeTargetted = true;
                                        break;
                                    }
                                }
                            }
                            //                                      if (weapon.isLocked() != thisLock.isChecked()) {
                            weapon.setLocked(thisLock.isChecked());
//                                            locked.invalidate();
                            if (weapon.getTargetData() != null)
                                weapon.getTargetData().updateWeapon(weapon);
                            tpa.updateHeat(index);

                            // check if this unit is a spotter and (un)locking a weapon might impact to hit
                            // numbers for the indirect fire unit(s)
                            if (unit.getTurn().isSpottingThisTurn()){
                                boolean adjustIndirect = false;
                                if (!beforeTargetted && weapon.isLocked()) { // no target before and weapon locked
                                    adjustIndirect = true;
                                } else if (beforeTargetted && !weapon.isLocked()){
                                    adjustIndirect = true;
                                    // check to see if it still has weapons targetted
                                    for (TargetData data : unit.getTurn().getAllTargets()){
                                        if (data.hasWeapons()){
                                            adjustIndirect = false; // still has at least one weapon locked
                                            break;
                                        }
                                    }
                                }
                                if (adjustIndirect){
                                    // find any indirect firing units using this mech as a spotter
                                    tpa.adjustIndirectFirePens(unit);
                                }
                            }
                        }
                    });

            TextView textView = itemView.findViewById(R.id.weapon_name);
            textView.setText(gun.getName());
            textView = itemView.findViewById(R.id.weapon_location);
            textView.setText(gun.getLocationText());
            textView = itemView.findViewById(R.id.heat);
            textView.setText(Integer.toString(gun.getHeat()));
            if (gun.getWeaponType() == OVWeapon.WeaponType.DEFENCE) {
                // defensive weapons like AMS must be treated differently
                textView = itemView.findViewById(R.id.weapon_damage);
                textView.setText(" - ");
                textView = itemView.findViewById(R.id.weapon_target);
                textView.setText(" - ");
                textView = itemView.findViewById(R.id.weapon_to_hit);
                textView.setText(" - ");
                locked.setVisibility(View.GONE);
            } else {
                textView = itemView.findViewById(R.id.weapon_damage);
                textView.setText(gun.getDamageText());
                textView = itemView.findViewById(R.id.weapon_target);
                IUnitData targ = selected.getTarget();
                String targetName = "Unknown";
                if (targ != null) {
                    targetName = targ.getHeader().getName() + " - " + targ.getPilot().getPilotName();
                }
                textView.setText(targetName);
                textView = itemView.findViewById(R.id.weapon_to_hit);
                String toHitLabel = "N/A";
                // enable locking if: to hit number is < 12, or no target selected
                if (selected.getToHit() <= 12 && selected.getTargetData() != null ) {
                    toHitLabel = Integer.toString(selected.getToHit());
                    locked.setEnabled(editable);
                } else {
                    locked.setChecked(false);
                    locked.setEnabled(false);
                }
                textView.setText(toHitLabel);
                textView.setOnClickListener(arg0 -> displayTooltip(selected));
            }
            MultiShotButton multi = itemView.findViewById(R.id.multi);

            if (selected.isMulti()) {
                multi.setVisibility(View.VISIBLE);
                multi.setOptions(selected.getWeapon().getAvailableModes());
                multi.setOnClickListener(arg0 -> updateMode(selected, multi.getSelectedMode()));

//                    ignoreClick = true;
                multi.setSelectedMode(selected.getWeapon().getWeaponMode());
                multi.setEnabled(editable);

            } else multi.setVisibility(View.INVISIBLE);
            textView = itemView.findViewById(R.id.success);
            textView.setText(selected.getStatus()== TargetWeapon.ShotStatus.HIT?"Tagged":"Miss");
            textView.setVisibility(View.GONE);
            Button auto = itemView.findViewById(R.id.auto_button);
            if (auto!=null){
                boolean enableTAG = (gun.getWeaponMode()== IWeapon.WeaponMode.TAG) && (selected.getToHit()<13) && !selected.isLocked();
                auto.setVisibility(enableTAG?View.VISIBLE:View.GONE);
                if (gun.getWeaponMode()== IWeapon.WeaponMode.TAG){
                    // for tag hide the locked flag
                    locked.setVisibility(View.INVISIBLE);
                    //textView is still the success tag for TAG
                    if (selected.isLocked())textView.setVisibility(View.VISIBLE);
                }
                auto.setOnClickListener(arg0 -> fireTag());
            }
        }
        public void displayDestroyed(TargetWeapon selected){
            weapon = selected;
            IWeapon gun = selected.getWeapon();
            TextView textView = itemView.findViewById(R.id.weapon_name);
            textView.setText(gun.getName());
            textView = itemView.findViewById(R.id.weapon_location);
            textView.setText(gun.getLocationText());
            textView = itemView.findViewById(R.id.destroyed_tag);
            if (selected.getWeapon().isJammed())
                textView.setText(R.string.weapon_jammed);
            else
                textView.setText(R.string.weapon_destroyed);
        }
        public void fireTag(){
            // Fire the TAG immediately, no mucking around, once locked the button won't be displayed
            weapon.setLocked(true);
            if (weapon.getTargetData() != null)
                weapon.getTargetData().updateWeapon(weapon);
//            tpa.updateHeat(index); // not needed for TAG (0 heat)
            weapon.shoot("AUTO");
            UpdatePlayerActions.updateTAG(weapon);
            notifyItemChanged(getAdapterPosition());
        }
        public void displayTooltip(TargetWeapon weapon){
            TooltipDialogFragment tooltip = new TooltipDialogFragment(weapon.calculateTargetNumberTooltip());

            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            tooltip.show(mgr, "To Hit Details");
        }
        public void updateMode(TargetWeapon selected, IWeapon.WeaponMode selectedMode){
            selected.getWeapon().setWeaponMode(selectedMode);
            selected.calculateTargetNumber();
            tpa.updateHeat(index);
        }
        public void hideCheck(){
            CheckBox locked = itemView.findViewById(R.id.locked);
            locked.setVisibility(View.INVISIBLE);
            MultiShotButton multi = itemView.findViewById(R.id.multi);
            multi.setVisibility(View.INVISIBLE);
            Button auto = itemView.findViewById(R.id.auto_button);
            auto.setVisibility(View.INVISIBLE);
            ((TextView)itemView.findViewById(R.id.weapon_name)).setText(R.string.name);
            ((TextView)itemView.findViewById(R.id.weapon_location)).setText(R.string.loc);
            ((TextView)itemView.findViewById(R.id.weapon_damage)).setText(R.string.damage);
            ((TextView)itemView.findViewById(R.id.weapon_target)).setText(R.string.target);
            ((TextView)itemView.findViewById(R.id.weapon_to_hit)).setText(R.string.to_hit);
            TextView textView = itemView.findViewById(R.id.heat);
            if (textView!=null)textView.setText(R.string.heat);
        }
    }

    private final TurnPhaseAdapter tpa;
    private final int index;
    /**
     * Initialize the dataset of the Adapter
     *
     */
    public PhaseItemAdapter(IUnitData unit,TurnPhaseAdapter parent,TurnPhaseAdapter.ViewHolderTarget parentViewHolder, int index) {
        super();
        updateContents(unit);
        tpa = parent;
        this.index = index;
    //    this.parentViewHolder = parentViewHolder;
        editable = !unit.getTurn().isPhaseComplete(Turn.Phase.TARGET);
    }
    public void updateContents(IUnitData unit) {
        this.unit = unit;
    }
    public void updateContents() {
        this.notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view;
        switch(viewType) {
            case 2:
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.destroyed_weapon_layout, viewGroup, false);
            break;
            default:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.target_weapon_layout, viewGroup, false);
                break;
        }

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (position == 0){
            // header line
            viewHolder.hideCheck();
        } else if (position <= unit.getTurn().getWeaponList().size()) {
            TargetWeapon weap = unit.getTurn().getWeaponList().get(position-1);
            switch (getItemViewType(position)) {
                case 1:
                viewHolder.displayItem(weap);
                break;
                default:
                viewHolder.displayDestroyed(weap);
            }
        }
    }

    public void setTargetingComplete(boolean complete) {
        editable = !complete;
        // update every item to lock them from being updated
        for (int i = 0;i < getItemCount();i++){
            notifyItemChanged(i);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (position==0) return 0; // header line
        boolean active = unit.getTurn().getWeaponList().get(position-1).getWeapon().isOperational();
        if (active) active = !unit.getTurn().getWeaponList().get(position-1).getWeapon().isJammed();
        return active?1:2;
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return unit.getTurn().getWeaponList().size() + 1;
    }

}
