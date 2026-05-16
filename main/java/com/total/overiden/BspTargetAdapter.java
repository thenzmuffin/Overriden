package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class BspTargetAdapter extends RecyclerView.Adapter<BspTargetAdapter.ViewHolder> {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
    //    private final TextView textView;
        public ViewHolder(View view) {
            super(view);
        }
        public void displayItem(IUnitData selected) {
            String text = selected.getHeader().getName() + " - " + selected.getPilot().getPilotName();
            ((TextView)itemView.findViewById(R.id.target_name)).setText(text);
            OptionButton status = itemView.findViewById(R.id.select_status);
            status.setOptions(strike.getTargetOptions());
            // clear the listener to avoid a loop
            status.setOnClickListener(null);
            // the get range function will return -1 for none (should be 0)
            // and then the actual range for all valid ranges which is offset by not targeted
            status.setSelectedIndex(strike.getUnitSelectedRange(selected)+1);
            status.setOnClickListener(arg0->unitTargeted(selected));
        }

        public void unitTargeted(IUnitData selected){
            OptionButton status = itemView.findViewById(R.id.select_status);
//            if (strike.isSingleTarget() && !strike.getTargetList().isEmpty()){
//                // there can only ever be at most one unit selected that is being cleared
//                refreshUnit(strike.getTargetList().get(0).unit.getKey());
//            }
            strike.updateTargets(selected,Integer.parseInt(status.getSelected().getKey()));
            notifyItemChanged(getAdapterPosition());
        }
    }


    private final BSPStrike strike;
    private final int forceIndex;
    /**
     * Initialize the dataset of the Adapter
     *
     */
    public BspTargetAdapter(BSPStrike strike, int forceIndex) {
        super();
        this.strike = strike;
        this.forceIndex = forceIndex;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.bsp_target_layout, viewGroup, false);


        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.displayItem(Game.current.getForce(forceIndex).getUnit(position));

    }
//    public void refreshUnit(int key){
//        for (int i = 0;i < Game.current.getForce(forceIndex).getCount();i++){
//            if (Game.current.getForce(forceIndex).getUnit(i).getKey()==key){
//                notifyItemChanged(i);
//                break;
//            }
//        }
//    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return Game.current.getForce(forceIndex).getCount();
    }

}
