package com.total.overide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.IEquipment;
import com.total.overiden.IUnitDesign;
import com.total.overiden.R;
import com.total.overiden.UnitCatalogAdapter;

import java.util.ArrayList;
import java.util.List;

public class OVWeaponAdapter  extends RecyclerView.Adapter<OVWeaponAdapter.ViewHolder> {


    public static class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnItemSelectedListener {
        private final TextView textView;
        private final Spinner spinner;
        private OVWeaponInstance inst = null;
        private Integer[] groups = {0,1,2,3,4,5,6,7,8,9};
        private Context context;
        private OVUnitDesign mech;
private  ConfigTicsFragment frag;
        public ViewHolder(View view, Context context, OVUnitDesign mech, ConfigTicsFragment frag) {
            super(view);
            // Define click listener for the ViewHolder's View
            this.context = context;
            this.mech = mech;
            this.frag = frag;
            textView = (TextView) view.findViewById(R.id.textView);
            spinner = (Spinner) view.findViewById(R.id.ticGroup);
            ArrayAdapter<Integer> ad = new ArrayAdapter<>(
                    view.getContext(),
                    android.R.layout.simple_spinner_item,
                    groups
            );

            // Set simple layout resource file for each item of spinner
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
            spinner.setAdapter(ad);
        }

        public void setRowContents(OVWeaponInstance inst) {
            textView.setText(inst.getName() + " : " + inst.getLocation().toScreen());
            spinner.setSelection(inst.getTic());
            this.inst = inst;
            spinner.setOnItemSelectedListener(this);
        }
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (inst.getTic() != i) {
                OVDatabaseUnit db = new OVDatabaseUnit(context);
                mech.updateTic(inst, i);
                // immediately update the database??
                db.updateDesign(mech);
                frag.refreshTicList();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private List<OVWeaponInstance> weapons;
    private IUnitDesign mech;

    private ConfigTicsFragment frag;
    public OVWeaponAdapter(IUnitDesign mech, ConfigTicsFragment frag){
        super();
        this.mech = mech;
        this.weapons = new ArrayList<>();
        this.frag = frag;
        for (int i = 0;i < mech.getEquipment().size();i++){
            if (mech.getEquipment().get(i).getType() == OVEquipment.EquipmentType.WEAPON){
                this.weapons.add((OVWeaponInstance)mech.getEquipment().get(i));
            }
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ovweapon_row_item, viewGroup, false);

        return new ViewHolder(view, viewGroup.getContext(), (OVUnitDesign)mech, frag);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setRowContents(weapons.get(position));
    }

    @Override
    public int getItemCount() {
        return weapons.size();
    }
}
