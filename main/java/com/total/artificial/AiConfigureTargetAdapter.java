package com.total.artificial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overiden.MainActivity;
import com.total.overiden.R;
import java.util.List;

public class AiConfigureTargetAdapter extends RecyclerView.Adapter<AiConfigureTargetAdapter.ViewHolder> {


    List<AiTargetChoice> list;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder  {
        private AiTargetChoice target;

        public ViewHolder(View view) {
            super(view);
            target = null;
        }
        public void setDisplay(AiTargetChoice target){
            this.target = target;
            TextView textView = itemView.findViewById(R.id.textView);
            textView.setText(target.toTargetTypeString());
            textView = itemView.findViewById(R.id.id);
            String temp = target.getDesignation();
            textView.setText(temp);
            itemView.findViewById(R.id.edit).setOnClickListener(v->displayDetail());
            itemView.findViewById(R.id.delete).setOnClickListener(v->deleteTarget());
        }

        private void displayDetail(){
            AiTargetDialogFragment adjust = new AiTargetDialogFragment(target);;
            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();

            mgr.setFragmentResultListener("update",MainActivity.currentActivity,new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    // We use a String here, but any type that can be put in a Bundle is supported.
                    notifyItemChanged(getAdapterPosition());
                }
            });

            adjust.show(mgr, "Update Instruction");

        }
        private void deleteTarget(){
            list.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
        }
    }

    /**
     * Initialize the dataset of the Adapter
     */
    public AiConfigureTargetAdapter(List<AiTargetChoice> list) {
        this.list = list;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ai_commander, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.itemView.setOnTouchListener(new UnitTouchListener(position));

        viewHolder.setDisplay(list.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();

    }

}

