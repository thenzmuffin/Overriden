package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.total.artificial.AiForceList;
import com.total.artificial.ArtificialPilot;
import com.total.artificial.DatabaseAI;

import java.util.List;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class AiConfigAdapter extends RecyclerView.Adapter<AiConfigAdapter.ViewHolder> {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnItemSelectedListener {
        private ArtificialPilot pilot;
//        private boolean firstTime = true;
        public ViewHolder(View view) {
            super(view);
        }
        public void displayItem(ArtificialPilot pilot) {
            this.pilot = pilot;
            TextView text = itemView.findViewById(R.id.unit_name);
            String display = pilot.getHeader().getName()+":"+pilot.getHeader().getRole().toString();
            text.setText(display);

            Spinner decks = itemView.findViewById(R.id.commander_name);
            List<DatabaseAI.ListItem> adapterList;
            try (DatabaseAI dbAi = new DatabaseAI(MainActivity.currentActivity)){
                adapterList = dbAi.getDeckList();
            }

            ArrayAdapter<DatabaseAI.ListItem> ad = new ArrayAdapter<>(
                    itemView.getContext(),
                    R.layout.simple_overide_target_dropdown,
                    adapterList
            );
            // Set simple layout resource file for each item of spinner
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
            decks.setAdapter(ad);
            decks.setOnItemSelectedListener(this);
            if (pilot.getDeck()==null){
                //find the first deck that matches this units role
                for (int i = 0;i < adapterList.size();i++){
                    if (adapterList.get(i).toString().contains(pilot.getHeader().getRole().name())){
                        pilot.setDeck(adapterList.get(i).index);
                        decks.setSelection(i);
                        break;
                    }
                }
            } else {
                // find the right deck in the list
                int selection = 0;
                for (DatabaseAI.ListItem item : adapterList){
                    if (item.index == pilot.getDeck().getId()){
                        break;
                    }
                    selection++;
                }
                decks.setSelection(selection);
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            DatabaseAI.ListItem item = (DatabaseAI.ListItem) adapterView.getSelectedItem();
            pilot.setDeck(item.index);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private final AiForceList force;
    /**
     * Initialize the dataset of the Adapter
     */
    public AiConfigAdapter(AiForceList list) {
        super();
        force = list;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.ai_config_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

//        TargetWeapon weap = force.;

                viewHolder.displayItem(force.getPilotData(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return force.getCount();
    }

}
