package com.total.artificial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVRange;
import com.total.overiden.MainActivity;
import com.total.overiden.MoveButton;
import com.total.overiden.R;
import com.total.overiden.TargetData;

import java.util.List;


public class AiIfRangeAdapter extends RecyclerView.Adapter<AiIfRangeAdapter.ViewHolder> {
    private List<TargetData> list;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder  implements AdapterView.OnItemSelectedListener{
        private TargetData item;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View



        }

        public void setDisplay(TargetData item) {
            this.item = item;
            TextView textView = itemView.findViewById(R.id.textView);
            textView.setText(item.getShooter().toString());
            setRangeButton("PB", OVRange.pb, itemView.findViewById(R.id.point_blank));
            setRangeButton("Sh", OVRange.sh, itemView.findViewById(R.id.short_range));
            setRangeButton("Me", OVRange.me, itemView.findViewById(R.id.medium_range));
            setRangeButton("Lo", OVRange.lo, itemView.findViewById(R.id.long_range));
            setRangeButton("Ex", OVRange.ex, itemView.findViewById(R.id.extreme_range));
        }

        private void setRangeButton(String label, int range, MoveButton button) {
            button.setupButton(label, "", MainActivity.currentActivity.getResources().getColor(R.color.Green, null));
            button.setChecked(range == item.getRange());
            button.setOnClickListener(arg0->rangeSelected(range));
        }
        private void rangeSelected(int range){
            item.setRange(range);
        }
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (adapterView.getId()==R.id.point_blank){
                item.setRange(OVRange.pb);
            } else if (adapterView.getId()==R.id.short_range){
                item.setRange(OVRange.sh);
            } else if (adapterView.getId()==R.id.medium_range){
                item.setRange(OVRange.me);
            } else if (adapterView.getId()==R.id.long_range){
                item.setRange(OVRange.lo);
            } else if (adapterView.getId()==R.id.extreme_range){
                item.setRange(OVRange.ex);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    /**
     * Initialize the dataset of the Adapter
     */
    public AiIfRangeAdapter(List<TargetData> list) {
        this.list = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ai_if_range_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        TargetData item = list.get(position);
        viewHolder.setDisplay(item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<TargetData> list){
        this.list = list;
        notifyDataSetChanged();
    }

}

