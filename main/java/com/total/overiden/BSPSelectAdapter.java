package com.total.overiden;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVMtfReader;
import java.util.List;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class BSPSelectAdapter extends RecyclerView.Adapter<BSPSelectAdapter.ViewHolder> {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
    //    private final TextView textView;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
//            textView = (TextView) view.findViewById(R.id.textView);
        }
        public void setDisplay(BSPStrikeTemplate template){
            String displayName = template.getType().toString() + ":" + template.getName();
            ((TextView)itemView.findViewById(R.id.textView)).setText(displayName);
            itemView.setOnTouchListener(this);
            if (getAdapterPosition()==selected)itemView.setBackgroundColor(MainActivity.currentActivity.getResources().getColor(R.color.Pink,null));
            else itemView.setBackgroundColor(MainActivity.currentActivity.getResources().getColor(R.color.WhiteSmoke,null));
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int old = selected;
            selected = getAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selected);
            return false;
        }
    }

    private final List<BSPStrikeTemplate> list;
    private int selected = 0;
    /**
     * Initialize the dataset of the Adapter
     *
     */
    public BSPSelectAdapter( ) {
        super();
        list = OVMtfReader.readBSPTemplates(MainActivity.currentActivity.getResources().openRawResource(R.raw.bsptemplates));
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.text_row_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setDisplay(list.get(position));
    }

    public BSPStrikeTemplate getSelected(){
        return list.get(selected);
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return list.size();
    }
}
