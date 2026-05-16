package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVAmmunition;
import com.total.overide.OVCoreEquipment;
import com.total.overide.OVDatabaseForce;
import com.total.overide.OVDatabaseUnit;
import com.total.overide.OVSegment;

import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {
//    private Fragment fragment;
    private boolean editMode = false;

    public class ViewHolder extends RecyclerView.ViewHolder  implements AdapterView.OnItemSelectedListener {
        IEquipment equipment = null;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void setDetails(IEquipment equip){
            equipment = equip;
            TextView description = itemView.findViewById(R.id.textView);
            String desc = equip.getName() + " - " + equip.getLocation().toString();
            description.setText(desc);

            HealthPipsView pips = itemView.findViewById(R.id.pips);
            pips.setHealth(equip.getHealth());
            pips.setDamage(equip.getDamage());
            if (editMode){pips.setOnClickListener(arg0 -> changeDamage());}
            else pips.setOnClickListener(null);
            Button button = itemView.findViewById(R.id.edit);
            button.setVisibility(editMode?View.VISIBLE:View.GONE);
            button.setOnClickListener(arg0 -> updateEquipmentState(equip, getAdapterPosition()));

            Spinner ammoSpinner = itemView.findViewById(R.id.ammo_type);
            if (ammoSpinner != null) {
                if (updateUnit && equip instanceof OVAmmunition) {
                    IWeapon.WeaponMode[] availableAmmoTypes = ((OVAmmunition) equip).getAvailableAmmoTypes();
                    if (availableAmmoTypes.length < 2)
                        ammoSpinner.setVisibility(View.GONE);
                    else {
                        ammoSpinner.setVisibility(View.VISIBLE);
                        ArrayAdapter<IWeapon.WeaponMode> adapter = new ArrayAdapter<>(
                                itemView.getContext(),
                                R.layout.simple_overide_spinner_dropdown,
                                availableAmmoTypes
                        );
// Specify the layout to use when the list of choices appears.
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
                        ammoSpinner.setAdapter(adapter);
                        ammoSpinner.setOnItemSelectedListener(null);
                        for(int i = 0;i<availableAmmoTypes.length;i++){
                            if (availableAmmoTypes[i]==((OVAmmunition) equip).getAmmoType()){
                                ammoSpinner.setSelection(i);
                                break;
                            }
                        }
                        ammoSpinner.setOnItemSelectedListener(this);
                    }
                } else {
                    ammoSpinner.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            // this method should only be called for ammunition but just to be sure...
            if (equipment instanceof OVAmmunition) {
                IWeapon.WeaponMode selectedMode = (IWeapon.WeaponMode) adapterView.getSelectedItem();
                if (selectedMode != null) {
                    ((OVAmmunition)equipment).setAmmoType(selectedMode);
                }
            }

            // update the database?
            MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
            if (mViewModel.getMech() instanceof IUnitData) {
                try (OVDatabaseForce forceDB = new OVDatabaseForce(MainActivity.currentActivity)) {
                    forceDB.addEquipment(mViewModel.getMech().getEquipment().get(getAdapterPosition()),
                            mViewModel.getMech().getHeader().getKey());
                }
            } else {
                try (OVDatabaseUnit unitDB = new OVDatabaseUnit(MainActivity.currentActivity)) {
                    unitDB.addEquipment(mViewModel.getMech().getEquipment().get(getAdapterPosition()),
                            mViewModel.getMech().getHeader().getKey());
                }
            }
            //refresh this line just in case the ammo type has changed the quantity
            TextView text = itemView.findViewById(R.id.textView);
            String desc = equipment.getName() + " - " + equipment.getLocation().toString();
            if (text != null)text.setText(desc);
        }

        private void changeDamage() {
            // TODO : create logic for CoreEquipment (where status isn't just on or off)
            if (!(equipment instanceof OVCoreEquipment)) {
                equipment.setStatus(!equipment.isOperational());
                itemView.findViewById(R.id.pips).invalidate();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
    private boolean updateUnit = false;
    private OVSegment.OVLocation locationDisplayed = OVSegment.OVLocation.NONE;
    public EquipmentAdapter(boolean updatable) {
//        OVDatabaseUnit db = new OVDatabaseUnit(frag.getActivity());
//        fragment = frag;
        // update flag, can't update units in the middle of a game
//        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        updateUnit = Game.current==null && updatable;
    }
    @NonNull
    @Override
    public EquipmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder( LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.equipment_row_item, viewGroup, false) );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        holder.setDetails(getEquipmentList().get(position));

    }

    @Override
    public int getItemCount() {
//        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        return getEquipmentList().size();
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        for (int i = 0;i < getItemCount();i++)
            notifyItemChanged(i);
    }

    public void updateEquipmentState(IEquipment equip, int position){
        if (equip.getHealth()>1) {
            OVCoreEquipment core = (OVCoreEquipment) equip;
            int full = equip.getHealth();
            if (core.getDamage()==full)core.setDamage(0);
            else core.setDamage(core.getDamage()+1);
        } else {
            // one health means just use operational flag
            equip.setStatus(!equip.isOperational());
        }
        notifyItemChanged(position);
    }
    public void setLocationDisplayed(OVSegment.OVLocation loc){
        if (locationDisplayed != loc) {
            locationDisplayed = loc;
            notifyDataSetChanged();

        }
    }

    private List<IEquipment> getEquipmentList(){
        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        if (locationDisplayed== OVSegment.OVLocation.NONE)
            return mViewModel.getMech().getEquipment();
        else
            return mViewModel.getMech().getSegment(locationDisplayed).getEquipment();
    }
}
