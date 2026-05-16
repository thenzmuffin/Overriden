package com.total.scenario;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.BitmapFactory;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.DragStartHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVMtfReader;
import com.total.overiden.BSPStrikeTemplate;
import com.total.overiden.MainActivity;
import com.total.overiden.R;

import java.util.ArrayList;
import java.util.List;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class TerrainAdapter extends RecyclerView.Adapter<TerrainAdapter.ViewHolder> {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnTouchListener*/{
        private Terrain template;
    //    private final TextView textView;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
//            textView = (TextView) view.findViewById(R.id.textView);
        }
        public void setDisplay(Terrain template){
            this.template = template;
            ((TextView)itemView.findViewById(R.id.description)).setText(template.getName());
            ImageView image = itemView.findViewById(R.id.tile);
            image.setImageBitmap(template.getPicture());
     //       itemView.setOnTouchListener(this);
            itemView.setOnLongClickListener( v -> {

                // Create a new ClipData. This is done in two steps to provide clarity. The
                // convenience method ClipData.newPlainText() can create a plain text
                // ClipData in one step.

                // Create a new ClipData.Item from the ImageView object's tag.
                ClipData.Item item = new ClipData.Item( template.getId());

                // Create a new ClipData using the tag as a label, the plain text MIME type,
                // and the already-created item. This creates a new ClipDescription object
                // within the ClipData and sets its MIME type to "text/plain".
                ClipData dragData = new ClipData(
                        template.getName(),
                        new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                        item);

                // Instantiate the drag shadow builder. We use this imageView object
                // to create the default builder.
                ImageView img = itemView.findViewById(R.id.tile);
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(img);

                // Start the drag.
                v.startDragAndDrop(dragData,  // The data to be dragged.
                        myShadow,  // The drag shadow builder.
                        null,      // No need to use local data.
                        0          // Flags. Not currently used, set to 0.
                );

                // Indicate that the long-click is handled.
                return true;
            });
            itemView.setBackgroundColor(MainActivity.currentActivity.getResources().getColor(R.color.WhiteSmoke,null));
        }

    }

    private List<Terrain> list;

    /**
     * Initialize the dataset of the Adapter
     *
     */
    public TerrainAdapter( ) {
        super();
        try (ScenarioDB db = new ScenarioDB(MainActivity.currentActivity)) {
            list = db.getCatalog();
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.terrain_row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setDisplay(list.get(position));
    }

    @Override
    public int getItemViewType(int position) {

        return 1;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return list.size();
    }
}
