package com.total.scenario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.total.artificial.AiSelectionFragment;
import com.total.artificial.DatabaseAI;
import com.total.overiden.MainActivity;
import com.total.overiden.R;
import com.total.overiden.Scenario;

import java.util.List;


public class ScenarioListAdapter extends RecyclerView.Adapter<ScenarioListAdapter.ViewHolder> {
    private List<ScenarioDB.ScenCat> list;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ScenarioDB.ScenCat item;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View



        }

        public void setDisplay(ScenarioDB.ScenCat item) {
            this.item = item;
            TextView textView = itemView.findViewById(R.id.textView);
            textView.setText(item.name);
            itemView.findViewById(R.id.free_up).setVisibility(View.GONE);

//            itemView.findViewById(R.id.edit).setOnClickListener(v -> callback.editDeck(item.key));

            Button but = itemView.findViewById(R.id.delete);
            but.setOnClickListener(arg0 -> deleteItem());

        }
        public void deleteItem(){

            try(ScenarioDB db = new ScenarioDB(MainActivity.currentActivity)) {
                db.deleteScenario(item.key);
            }
            refreshCatalog();
            notifyItemRemoved(getAdapterPosition());
        }

    }

    private ScenarioSelectionFragment callback;
    private boolean isDeck;

    /**
     * Initialize the dataset of the Adapter
     */
    public ScenarioListAdapter(ScenarioSelectionFragment callback) {
        this.callback = callback;

        refreshCatalog();
    }

    public void refreshCatalog() {

            try(ScenarioDB db = new ScenarioDB(MainActivity.currentActivity)) {
                    list = db.getScenarioCatalog(false);
            }

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.force_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        ScenarioDB.ScenCat item = list.get(position);
        viewHolder.setDisplay(item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }



}

