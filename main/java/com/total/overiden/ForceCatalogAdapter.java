package com.total.overiden;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVDatabaseForce;

import java.util.List;

public class ForceCatalogAdapter extends RecyclerView.Adapter<ForceCatalogAdapter.ViewHolder> {
    public static class CatalogEntry {
        public int key;
        public String name;
        public boolean inUse;
        public ForceList.ForceType forceType;

        public CatalogEntry(int key, String name, boolean inUse,ForceList.ForceType forceType) {
            super();
            this.key = key;
            this.name = name;
            this.inUse = inUse;
            this.forceType = forceType;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    private List<CatalogEntry> catalog;
//    private final AppCompatActivity activity;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = view.findViewById(R.id.textView);
            view.findViewById(R.id.edit).setOnClickListener(this);
            view.findViewById(R.id.delete).setOnClickListener(this);
            view.findViewById(R.id.free_up).setOnClickListener(this);
        }

        public TextView getTextView() {
            return textView;
        }
        @Override
        public void onClick(View view) {
            CatalogEntry cat = catalog.get(this.getAdapterPosition());
            if (view.getId()==R.id.edit) {
                Bundle bundle = new Bundle();
                bundle.putInt("forceKey", cat.key);
                Navigation.findNavController(view).navigate(R.id.action_edit_forcelist, bundle);
            } else if (view.getId()==R.id.delete){
                catalog.remove(this.getAdapterPosition());
                notifyItemRemoved(this.getAdapterPosition());
                try (OVDatabaseForce forceDB = new OVDatabaseForce(MainActivity.currentActivity)) {
                    forceDB.deleteForceList(cat.key);
                }
            } else if (view.getId()==R.id.free_up){
                catalog.get(this.getAdapterPosition()).inUse = false;
                notifyItemChanged(this.getAdapterPosition());
                try (OVDatabaseForce forceDB = new OVDatabaseForce(MainActivity.currentActivity)) {
                    forceDB.setInUse(cat.key);
                }
            }
        }
    }

    /**
     * Initialize the dataset of the Adapter
     */
    public ForceCatalogAdapter( ) {
        try (OVDatabaseForce db = new OVDatabaseForce(MainActivity.currentActivity)) {
            catalog = db.getCatalog(true, null);
        }
//        activity = (AppCompatActivity) act;
    }

    public void refreshCatalog() {
        try (OVDatabaseForce db = new OVDatabaseForce(MainActivity.currentActivity)) {
            catalog = db.getCatalog(true, null);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.force_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        CatalogEntry entry = catalog.get(position);
        String text = entry.name + " : " + (entry.inUse?"In Use":"Free") + "  Type: "
                + (entry.forceType== ForceList.ForceType.OV?"Overide":"Total Warfare");
        viewHolder.getTextView().setText(text);
        viewHolder.getTextView().setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(),R.color.black));
        if (entry.inUse)
            viewHolder.getTextView().setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.Red));
        else
            viewHolder.getTextView().setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.LawnGreen));
        viewHolder.itemView.findViewById(R.id.free_up).setVisibility(entry.inUse?View.VISIBLE:View.GONE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return catalog.size();
    }
}

