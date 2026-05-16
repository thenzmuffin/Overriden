package com.total.overiden;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class BspShootAdapter extends RecyclerView.Adapter<BspShootAdapter.ViewHolder> {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
    //    private final TextView textView;
        public ViewHolder(View view) {
            super(view);
        }
        public void displayItem(BSPStrike.StrikeTarget selected) {
            ((TextView)itemView.findViewById(R.id.target_name)).setText(selected.unit.getHeader().getName() + " - " + selected.unit.getPilot().getPilotName());
            ((TextView)itemView.findViewById(R.id.weapon_to_hit)).setText(Integer.toString(strike.getTargetNumber()));
            Button auto = itemView.findViewById(R.id.auto);
            TwoDSixView dice = itemView.findViewById(R.id.to_hit_dice);
            // the auto button to roll dice should only be visible in:
            // Movement phase for minefields
            // Targeting phase for air cover
            // shooting phase for everything else
            if (selected.status== TargetWeapon.ShotStatus.NOTFIRED && strike.rollThisPhase(phase)){
                auto.setVisibility(View.VISIBLE);
                auto.setOnClickListener(arg0->shoot());
                dice.setVisibility(View.GONE);
            } else {
                auto.setVisibility(View.GONE);
                if(selected.status!= TargetWeapon.ShotStatus.NOTFIRED) {
                    dice.setDice(selected.dice, strike.getTargetNumber());
                    dice.setSize(TwoDSixView.DiceSize.SMALL);
                    dice.setVisibility(View.VISIBLE);
                } else {
                    dice.setVisibility(View.GONE);
                }
            }
        }

        public void shoot(){
//            strike.activateSingle("AUTO", selected);
            strike.activate("AUTO");
            // send a message back to the turnphase adapter level to recalculate the lock icon
            MainActivity.currentActivity.getSupportFragmentManager().setFragmentResult("strikeUpdate",new Bundle());
            notifyItemChanged(getAdapterPosition());
        }
    }


    private final BSPStrike strike;
    private final Turn.Phase phase;
    /**
     * Initialize the dataset of the Adapter
     *
     */
    public BspShootAdapter(BSPStrike strike, Turn.Phase phase) {
        super();
        this.strike = strike;
        this.phase = phase;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.bsp_shoot_layout, viewGroup, false);


        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.displayItem(strike.getTargetList().get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return strike.getTargetList().size();
    }

}
