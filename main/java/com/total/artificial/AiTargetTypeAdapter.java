package com.total.artificial;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.R;

import java.util.List;

public class AiTargetTypeAdapter extends RecyclerView.Adapter<AiTargetTypeAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // this function is called before text is edited
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // this function is called when text is edited
                targetType.setTag(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // this function is called after text is edited
            }
        };
        private AiCommander.TargetType targetType = null;
        public ViewHolder(@NonNull View view) {
            super(view);
        }
        public void setDisplay(AiCommander.TargetType target){
            targetType = target;
            Spinner spin = itemView.findViewById(R.id.select_target_type);
            ArrayAdapter<AiEnums.Tag> adapter = new ArrayAdapter<>(
                    itemView.getContext(),
                    R.layout.simple_overide_spinner_dropdown,
                    AiEnums.Tag.values()
            );
// Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
            spin.setAdapter(adapter);
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    AiEnums.Tag tag = (AiEnums.Tag)adapterView.getSelectedItem();
                    if (tag!=targetType.getEnum()) {
                        if (tag== AiEnums.Tag.DESIGNATION ||
                                targetType.getEnum()== AiEnums.Tag.DESIGNATION){
                            notifyItemChanged(getAdapterPosition());
                        }
                        targetType.setEnum(tag);
                        if (targetType.getEnum() != AiEnums.Tag.DESIGNATION)
                            targetType.setTag("");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            int index = 0;
            for (AiEnums.Tag tag : AiEnums.Tag.values()) {
                if (tag==target.getEnum()){
                    spin.setSelection(index);
                    break;
                }
                index++;
            }
            itemView.findViewById(R.id.delete).setOnClickListener(arg0 -> deleteItem());

            // if the type is Designation then the user can set the tag to use
            ((EditText)itemView.findViewById(R.id.designation)).setText(target.getTag());
            itemView.findViewById(R.id.designation).setVisibility(targetType.getEnum()== AiEnums.Tag.DESIGNATION?View.VISIBLE:View.GONE);
            ((EditText)itemView.findViewById(R.id.designation)).addTextChangedListener(textWatcher);
        }
        private void deleteItem(){
            types.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
        }
    }
    private final List<AiCommander.TargetType> types;

    public AiTargetTypeAdapter(List<AiCommander.TargetType> list){
        super();

        types = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //target and shoot use the same display for BT players
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.target_select_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       holder.setDisplay(types.get(position));
    }

    @Override
    public int getItemCount() {

        return types.size();
    }
}
