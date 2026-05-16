package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class BluetoothPhaseAdapter extends RecyclerView.Adapter<BluetoothPhaseAdapter.ViewHolder>{
    private final Fragment fragment;

    public class ViewHolder extends RecyclerView.ViewHolder{

        protected IUnitData unit;

        public ViewHolder(@NonNull View view) {
            super(view);
        }

        public void setDisplay(IUnitData unit) {
            this.unit = unit;
            TextView text = itemView.findViewById(R.id.unitName);
            if (text != null) {
                String display = unit.getHeader().getName() + " - " + unit.getPilot().getPilotName();
                text.setText(display);
            }
            text = itemView.findViewById(R.id.tmm);
            if (text != null) {
                text.setText(String.format(Locale.getDefault(Locale.Category.FORMAT), "%d", unit.getCurrentTMM()));
            }
            // Set the damage list if needed
            RecyclerView damage_list = itemView.findViewById(R.id.damage_record_list);
            if (damage_list != null) {
                DamageRecordAdapter phaseAdapter = new DamageRecordAdapter(unit, (TurnPhaseFragment) fragment, true);
                damage_list.setAdapter(phaseAdapter);
                damage_list.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            damage_list = itemView.findViewById(R.id.target_data_list);
            if (damage_list != null) {
                DisplayTargetsAdapter phaseAdapter = new DisplayTargetsAdapter(unit.getTurn());
                damage_list.setAdapter(phaseAdapter);
                damage_list.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }
            int bgColor = unit.getTurn().isPhaseComplete(phase)?R.color.LightGreen:R.color.LightCoral;
            itemView.setBackgroundColor(itemView.getResources().getColor(bgColor,null));
            if (phase== Turn.Phase.PHYSICAL){
                setPhysicalDisplay();
            }
        }

        private void setPhysicalDisplay() {
            TargetWeapon physWeapon = unit.getTurn().getPhysicalAttack();
            TextView textTarget = itemView.findViewById(R.id.target);
            TextView textTargetTmm = itemView.findViewById(R.id.target_tmm);
            TextView textTargetOther = itemView.findViewById(R.id.other);
            TextView textWeaponName = itemView.findViewById(R.id.weapon_name);
            TextView textWeaponToHit = itemView.findViewById(R.id.weapon_to_hit);
            TextView textWeaponDamage = itemView.findViewById(R.id.weapon_damage);
            if (physWeapon != null) {
                textTarget.setText(physWeapon.getTarget().getHeader().getName());
                textTargetTmm.setText(String.format(Locale.getDefault(Locale.Category.FORMAT), "%d", physWeapon.getTargetData().getTargetMovementMod()));
                textTargetOther.setText(String.format(Locale.getDefault(Locale.Category.FORMAT), "%d", physWeapon.getTargetData().getOther()));
                textWeaponName.setVisibility(View.VISIBLE);
                textWeaponToHit.setVisibility(View.VISIBLE);
                textWeaponDamage.setVisibility(View.VISIBLE);
                textWeaponName.setText(physWeapon.getWeapon().getName());
                textWeaponToHit.setText(String.format(Locale.getDefault(Locale.Category.FORMAT), "%d", physWeapon.getToHit()));
                textWeaponDamage.setText(physWeapon.getWeapon().getDamageText());
            } else {
                textTarget.setText(R.string.none);
                textTargetTmm.setText(R.string.na);
                textTargetOther.setText(R.string.na);
                textWeaponName.setVisibility(View.INVISIBLE);
                textWeaponToHit.setVisibility(View.INVISIBLE);
                textWeaponDamage.setVisibility(View.INVISIBLE);
            }
        }
    }
    private final Turn.Phase phase;
    public BluetoothPhaseAdapter(Turn.Phase phase, Fragment frag){
        super();
        this.phase = phase;
        fragment = frag;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (phase){
            case MOVE:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bluetooth_move_phase, parent, false);
                break;
            case TARGET:
                //target and shoot use the same display for BT players
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.bluetooth_target_phase, parent, false);
                break;
            case SHOOT:
                //target and shoot use the same display for BT players
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.bluetooth_shoot_phase, parent, false);
                break;
            case PHYSICAL:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.bluetooth_physical_phase, parent, false);
                break;
            case RESOLVE:
            default: // putting this in removes the "maybe null" warning
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.bluetooth_resolve_phase, parent, false);
                break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
        holder.setDisplay(model.getForceList(1).getUnit(position));
    }

    @Override
    public int getItemCount() {
        TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
        return model.getForceList(1).getCount();
    }
}
