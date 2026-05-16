package com.total.overiden;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVDatabaseForce;
import java.util.List;

public class GameListAdapter extends RecyclerView.Adapter<GameListAdapter.ViewHolder> {

    public static class GameCatalogEntry {
        public int key;
        public String name;

        public GameCatalogEntry(int key, String name) {
            super();
            this.key = key;
            this.name = name;
        }
    }

    List<GameCatalogEntry> list;
    private final Fragment fragment;

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
            ((Button)view.findViewById(R.id.edit)).setText(R.string.play);
            view.findViewById(R.id.delete).setOnClickListener(this);
        }

        public TextView getTextView() {
            return textView;
        }
        @Override
        public void onClick(View view) {
            int i = this.getAdapterPosition();
            if (i>=0) {
                GameCatalogEntry cat = list.get(i);
                if (view.getId() == R.id.edit) {
                    int gameID = list.get(this.getAdapterPosition()).key;
                    Game bluetoothCheck;
                    try (DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity)) {
                        bluetoothCheck = gameDB.loadGame(gameID);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("gameID", gameID);
                    bundle.putString("gameName",bluetoothCheck.getName());
                    if (bluetoothCheck.getForceTwoType()!= Game.PlayerType.BLUETOOTH)
                        NavHostFragment.findNavController(fragment)
                            .navigate(R.id.action_start_game, bundle);
                    else
                        if (bluetoothCheck.getForceOneType()!= Game.PlayerType.CLIENT)
                            NavHostFragment.findNavController(fragment)
                                .navigate(R.id.action_reconnect, bundle);
                        else
                            NavHostFragment.findNavController(fragment)
                                    .navigate(R.id.action_client, bundle);

                } else if (view.getId() == R.id.delete) {
                    list.remove(this.getAdapterPosition());
                    notifyItemRemoved(this.getAdapterPosition());
                    Game game;
                    try(DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity)) {
                        game = gameDB.deleteGame(cat.key);
                    }
                    if (game != null) {
                        try (OVDatabaseForce dbForce = new OVDatabaseForce(MainActivity.currentActivity)) {
                            game.getForce(0).setInUse(false);
                            dbForce.addHeader(game.getForce(0));
                            game.getForce(1).setInUse(false);
                            dbForce.addHeader(game.getForce(1));
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize the dataset of the Adapter
     */
    public GameListAdapter(List<GameCatalogEntry> list, Fragment frag) {
        this.list = list;
        fragment = frag;
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
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.itemView.setOnTouchListener(new UnitTouchListener(position));

        viewHolder.getTextView().setText(list.get(position).name);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();

    }

}

