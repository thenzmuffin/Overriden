package com.total.artificial;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVDatabaseForce;
import com.total.overide.OVDatabaseUnit;
import com.total.overide.OVUnitDesign;
import com.total.overide.UnitData;
import com.total.overiden.ForceList;
import com.total.overiden.IRefreshFragment;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitHeader;
import com.total.overiden.MainActivity;
import com.total.overiden.MechViewModel;
import com.total.overiden.R;
import com.total.overiden.TurnViewModel;
import com.total.overiden.ViewForceListFragment;

import java.util.List;


public class AiListAdapter extends RecyclerView.Adapter<AiListAdapter.ViewHolder> {
    private List<DatabaseAI.ListItem> list;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private DatabaseAI.ListItem item;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View



        }

        public void setDisplay(DatabaseAI.ListItem item) {
            this.item = item;
            TextView textView = itemView.findViewById(R.id.textView);
            textView.setText(item.toString());
            itemView.findViewById(R.id.free_up).setVisibility(View.GONE);
            if (isDeck)
                itemView.findViewById(R.id.edit).setOnClickListener(v -> callback.editDeck(item.index));
            else
                itemView.findViewById(R.id.edit).setOnClickListener(v -> callback.editCommander(item.index));
            Button but = itemView.findViewById(R.id.delete);
            but.setOnClickListener(arg0 -> deleteItem());

        }
        public void deleteItem(){

            try(DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)) {
                db.deleteDeck(item.index);
            }
            refreshCatalog();
            notifyItemRemoved(getAdapterPosition());
        }

    }

    private AiSelectionFragment callback;
    private boolean isDeck;

    /**
     * Initialize the dataset of the Adapter
     */
    public AiListAdapter(AiSelectionFragment callback, boolean deck) {
        this.callback = callback;
        isDeck = deck;
        refreshCatalog();
    }

    public void refreshCatalog() {

            try(DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)) {
                if (isDeck)
                    list = db.getDeckList();
                else
                    list = db.getCommanderList();
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
        DatabaseAI.ListItem item = list.get(position);
        viewHolder.setDisplay(item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }



}

