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

public class AiInstructionAdapter extends RecyclerView.Adapter<AiInstructionAdapter.ViewHolder> {


    List<AiInstruction> list;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder  {
        private AiInstruction move;

        public ViewHolder(View view) {
            super(view);
            move = null;
        }
        public void setDisplay(AiInstruction move){
            this.move = move;
            TextView textView = itemView.findViewById(R.id.textView);
            textView.setText(move.getQuestion());
            textView = itemView.findViewById(R.id.id);
            String temp = Integer.toString(move.getIndex());
            textView.setText(temp);

            itemView.findViewById(R.id.edit).setOnClickListener(v->displayDetail());
            itemView.findViewById(R.id.delete).setOnClickListener(v->deleteInstruction());

        }

        private void displayDetail(){
            AiInstructionDialogFragment adjust = new AiInstructionDialogFragment(move);;
            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();

            mgr.setFragmentResultListener("instruction",MainActivity.currentActivity,new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    // We use a String here, but any type that can be put in a Bundle is supported.
                    notifyItemChanged(getAdapterPosition());
                }
            });
            adjust.show(mgr, "Update Instruction");
        }
        private void deleteInstruction(){
            list.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
        }
    }

    /**
     * Initialize the dataset of the Adapter
     */
    public AiInstructionAdapter(List<AiInstruction> list) {
        this.list = list;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ai_instruction, viewGroup, false);

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
    public void setInstructions(List<AiInstruction> list){
        this.list = list;
    }
}

