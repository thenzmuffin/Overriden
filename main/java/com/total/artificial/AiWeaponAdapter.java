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

public class AiWeaponAdapter extends RecyclerView.Adapter<AiWeaponAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
//        private final TextWatcher textWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // this function is called before text is edited
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // this function is called when text is edited
//                targetType.setTag(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // this function is called after text is edited
//            }
//        };
        private AiEnums.WeaponPriority weaponPriority = null;
        public ViewHolder(@NonNull View view) {
            super(view);
        }
        public void setDisplay(AiEnums.WeaponPriority target){
            weaponPriority = target;
            Spinner spin = itemView.findViewById(R.id.select_target_type);
            ArrayAdapter<AiEnums.WeaponPriority> adapter = new ArrayAdapter<AiEnums.WeaponPriority>(
                    itemView.getContext(),
                    R.layout.simple_overide_spinner_dropdown,
                    AiEnums.WeaponPriority.values()
            );
// Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
            spin.setAdapter(adapter);
//            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                    AiCommander.Tag tag = (AiCommander.Tag)adapterView.getSelectedItem();
//                    if (tag!=targetType.getEnum()) {
//                        if (tag== AiCommander.Tag.DESIGNATION ||
//                                targetType.getEnum()== AiCommander.Tag.DESIGNATION){
//                            notifyItemChanged(getAdapterPosition());
//                        }
//                        targetType.setEnum(tag);
//                        if (targetType.getEnum() != AiCommander.Tag.DESIGNATION)
//                            targetType.setTag("");
//                    }
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//            });
            int index = 0;
            for (AiEnums.WeaponPriority item : AiEnums.WeaponPriority.values()) {
                if (item==target){
                    spin.setSelection(index);
                    break;
                }
                index++;
            }
            itemView.findViewById(R.id.delete).setOnClickListener(arg0 -> deleteItem());

            // if the type is Designation then the user can set the tag to use
            itemView.findViewById(R.id.designation).setVisibility(View.GONE);
        }
        private void deleteItem(){
            types.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
        }
    }
    private final List<AiEnums.WeaponPriority> types;

    public AiWeaponAdapter(List<AiEnums.WeaponPriority> list){
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
