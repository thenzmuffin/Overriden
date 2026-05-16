package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVTic;


public class DisplayTargetsAdapter extends RecyclerView.Adapter<DisplayTargetsAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {

        protected IUnitData unit;

        public ViewHolder(@NonNull View view) {
            super(view);
        }
        public void setDisplay(TargetData data){
//            TextView text = itemView.findViewById(R.id.textView);
//            text.setText(data.getName());
            ((TextView)itemView.findViewById(R.id.target)).setText(data.getName());
            String text;
            switch (data.getRange()){
                case 1: text = "Range: Point Blank";break;
                case 2: text = "Range: Short";break;
                case 3: text = "Range: Medium";break;
                case 4: text = "Range: Long";break;
                case 5: text = "Range: Extreme";break;
                default: text = "Range: Unknown";break;
            };
            ((TextView)itemView.findViewById(R.id.range)).setText(text);
            text = "TMM: "+ data.getTargetMovementMod();
            ((TextView)itemView.findViewById(R.id.tmm)).setText(text);
            text = "Other Mods: " + data.getOther();
            ((TextView)itemView.findViewById(R.id.other)).setText(text);
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.Blue,null));
        }
        public void setDisplay(TargetWeapon weapon){
            ((TextView)itemView.findViewById(R.id.weapon_name)).setText(weapon.getWeapon().getName());
            String text;
            if (weapon.getStatus()== TargetWeapon.ShotStatus.NOTFIRED) {
                text = "To Hit: " + weapon.getToHit();
            } else {
                text = weapon.getStatus().toString();
            }
            ((TextView)itemView.findViewById(R.id.weapon_to_hit)).setText(text);
            text = "Dmg: " + weapon.getWeapon().getDamageText();
            ((TextView)itemView.findViewById(R.id.weapon_damage)).setText(text);
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.Cyan,null));
        }
    }

    private UnitTurn turn;

    public DisplayTargetsAdapter(UnitTurn ut) {
        super();
        turn = ut;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 2)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.blue_target_item, parent, false);
        else
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.blue_target_header, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int pos = position;
        for (TargetData data : turn.getAllTargets()){
            if (!data.hasWeapons()) continue;
            if (pos == 0) {
                holder.setDisplay(data);
                break;
            }
            pos--;
            if (data.isExpanded()) {
                if (data.getWeapons().size() > pos) {
                    holder.setDisplay(data.getWeapons().get(pos));
                    break;
                }
                pos -= data.getWeapons().size();
            }
            if (pos < 0) break;//shouldn't be needed but just in case
        }
    }
    @Override
    public int getItemViewType(int position) {
        int pos = position;
        for (TargetData data : turn.getAllTargets()){
            if (!data.hasWeapons()) continue;
            if (pos == 0) return 1; //this position is a TargetData object
            pos--;
            if (data.isExpanded()) {
                if (data.getWeapons().size() > pos) return 2;

                pos -= data.getWeapons().size();
            }
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        int pos = 0;
        for (TargetData data : turn.getAllTargets()){
            if (!data.hasWeapons()) continue;
            pos++; //add one for the targetData
            if (data.isExpanded())pos += data.getWeapons().size();
        }
        return pos;
    }
    public void updateContents(UnitTurn unitTurn) {
        this.turn = unitTurn;
        this.notifyDataSetChanged();
    }
}
