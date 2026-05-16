package com.total.artificial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overiden.IUnitData;
import com.total.overiden.R;

import java.util.ArrayList;
import java.util.List;

public class AiUnitListAdapter extends RecyclerView.Adapter<AiUnitListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ListItem unit;
        public ViewHolder(@NonNull View view) {
            super(view);
        }
        public void setDisplay(ListItem target){
            unit = target;
             // if the type is Designation then the user can set the tag to use
            ((TextView)itemView.findViewById(R.id.unitDescription)).setText(target.unit.getHeader().getName());
            CheckBox check = itemView.findViewById(R.id.unitSelected);
            if (editable) {
                check.setChecked(target.selected);
                check.setOnClickListener(arg0 -> this.unitSelected());
            } else {
                check.setVisibility(View.GONE);
            }

        }
        public void unitSelected(){
// Do we need to record the change of state?
            unit.selected = (((CheckBox)itemView.findViewById(R.id.unitSelected)).isChecked());
        }
    }
    public static class ListItem{
        private final IUnitData unit;
        private boolean selected;
        ListItem(IUnitData unit){
            super();
            this.unit = unit;
            selected = false;
        }
    }
    private final List<ListItem> result_list;
    private boolean editable = true;

    public AiUnitListAdapter(List<IUnitData> list){
        super();

        result_list = new ArrayList<>();
        for (IUnitData unit : list){
            result_list.add(new ListItem(unit));
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //target and shoot use the same display for BT players
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.unit_select_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       holder.setDisplay(result_list.get(position));
    }

    @Override
    public int getItemCount() {

        return result_list.size();
    }

    public List<IUnitData> getSelectedItems(List<IUnitData> updateList){
        List<IUnitData> retList;
        if (updateList!=null){
            retList = updateList;
            retList.clear(); // list needs to be reset
        } else retList = new ArrayList<>();
        for (ListItem item : result_list){
            if (item.selected)retList.add(item.unit);
        }

        return retList;
    }
    public void updateSelectedItems(List<IUnitData> updateList){
        // if the selected list is not empty then the list contents should be updated with only the selected items
        result_list.clear();
        for (IUnitData unit : updateList) {
            result_list.add(new ListItem(unit));
        }
        notifyDataSetChanged();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
