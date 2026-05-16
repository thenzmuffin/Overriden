package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class BspCounterAdapter extends RecyclerView.Adapter<BspCounterAdapter.ViewHolder> {
    private static class CoverItem{
        private boolean used;
        private final BSPStrike card;
        private BSPStrike target;
        CoverItem(BSPStrike card){
            this.card = card;
            used = false;
            target = null;
        }
        @NonNull
        public String toString(){
            if (card == null) return "None";
            return card.getType().toString();
        }
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnItemSelectedListener {
        private BSPStrike card = null;
        private CoverItem selected = null;
    //    private final TextView textView;
        public ViewHolder(View view) {
            super(view);
        }
        public void displayItem(BSPStrike selected) {
            card = selected;
            TextView label = itemView.findViewById(R.id.card_name);
            label.setText(card.getName());
            if (Game.current.isBlindBSP())
                ((TextView)itemView.findViewById(R.id.card_type)).setText(R.string.unknown);
            else
                ((TextView)itemView.findViewById(R.id.card_type)).setText(card.getType().toString());
            ((TextView)itemView.findViewById(R.id.target_number)).setText(String.format(Locale.getDefault(Locale.Category.FORMAT), "%d", card.getTargetNumber()));
            ((TextView)itemView.findViewById(R.id.dmg_value_groupings)).setText(card.getDVG());
            ((TextView)itemView.findViewById(R.id.dmg_type)).setText(card.getDamageType());
            itemView.setBackground(AppCompatResources.getDrawable(MainActivity.currentActivity,R.drawable.red_frame));

            Spinner cover = itemView.findViewById(R.id.counter_card);
            ArrayAdapter<CoverItem> ad = new ArrayAdapter<>(
                    itemView.getContext(),
                    R.layout.simple_overide_spinner_dropdown,
                    fillList(this.selected)
            );
            // Set simple layout resource file for each item of spinner
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
            cover.setAdapter(ad);
            cover.setSelection(0);
            cover.setOnItemSelectedListener(this);

        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (selected!=null){
                if (adapterView.getSelectedItem()==selected)return;
                selected.used = false;
                selected.card.setTargeted(false);
                selected.target  = null;
            }
            selected = (CoverItem) adapterView.getSelectedItem();
            selected.used = true;
            selected.card.setTargeted(true);
            selected.target = card;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
    private final List<CoverItem> list;

    private final List<BSPStrike> cards;
 //   private final List<BSPStrike> counters;
    /**
     * Initialize the dataset of the Adapter
     *
     */
    public BspCounterAdapter(List<BSPStrike> cards, List<BSPStrike> counters) {
        super();
        this.cards = cards;
//        this.counters = counters;
        list = new ArrayList<>();
        for (BSPStrike strike : counters)list.add(new CoverItem(strike));
    }
    private List<CoverItem> fillList(CoverItem current){
        List<CoverItem> dropdown = new ArrayList<>();
        dropdown.add(new CoverItem(null));
        for (CoverItem item : list){
            if (current != item && !item.used)dropdown.add(item);
        }
        return dropdown;
    }
    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.bsp_counter_card_layout, viewGroup, false);


        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.displayItem(cards.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void complete(){
        //also need to actually resolve the counters
        for (CoverItem cov : list){
            if (cov.used){
                cov.card.resolveCounter(cov.target);
            }
        }
    }
}
