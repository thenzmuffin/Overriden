package com.total.artificial;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.R;

import java.util.List;

public class AiResultAdapter extends RecyclerView.Adapter<AiResultAdapter.ViewHolder> {
    private class TextListener implements TextWatcher{
        private AiInstruction.MoveChoice move;
        TextListener(AiInstruction.MoveChoice move){
            super();
            this.move = move;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            move.setNextInstruction(Integer.parseInt(charSequence.toString()));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    List<AiInstruction.MoveChoice> list;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, TextWatcher {
        private AiInstruction.MoveChoice move;

        public ViewHolder(View view) {
            super(view);
            move = null;
        }
        public void setDisplay(AiInstruction.MoveChoice move){
            this.move = move;
            ((EditText)itemView.findViewById(R.id.label)).setText(move.toString());
            ((EditText)itemView.findViewById(R.id.label)).addTextChangedListener(this);
//            ((EditText)itemView.findViewById(R.id.next_instruction)).setText(Integer.toString(move.getNextInstruction()));
//            ((EditText)itemView.findViewById(R.id.next_instruction)).addTextChangedListener(new TextListener(move));
            NumberPicker next = itemView.findViewById(R.id.next_instruction);
            next.setMaxValue(40);
            next.setMinValue(0);
            next.setValue(this.move.getNextInstruction());
            next.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    move.setNextInstruction(numberPicker.getValue());
                }
            });
        }
        @Override
        public void onClick(View view) {
            int i = this.getAdapterPosition();
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            move.setLabel(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    /**
     * Initialize the dataset of the Adapter
     */
    public AiResultAdapter(List<AiInstruction.MoveChoice> list) {
        this.list = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ai_result, viewGroup, false);

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
    public void setInstructions(List<AiInstruction.MoveChoice> list){
        this.list = list;
    }
}

