package com.total.overiden;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVDatabaseForce;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> catalog;
    private BluetoothDevice chosenDevice = null;
    private View current = null;

    public class UnitTouchListener implements View.OnTouchListener {
        private int index;

        public UnitTouchListener(int index) {
            super();
            this.index = index;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                chosenDevice = catalog.get(index);
                if (current != null)
                    current.setBackgroundColor(view.getResources().getColor(R.color.PapayaWhip, null));
                view.setBackgroundColor(view.getResources().getColor(R.color.PaleGreen, null));
                current = view;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                // place your code here
            }
            return false;
        }
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.textView);
            view.setBackgroundColor(view.getResources().getColor(R.color.PapayaWhip, null));

        }

        public TextView getTextView() {
            return textView;
        }

    }

    /**
     * Initialize the dataset of the Adapter
     */
    public BluetoothDeviceAdapter(Set<BluetoothDevice> devices) {
        super();
        catalog = new ArrayList<>();
        for (BluetoothDevice dev : devices ){
            catalog.add(dev);
        }

    }

    public void refreshCatalog() {

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint({"ResourceAsColor", "MissingPermission"})
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.itemView.setOnTouchListener(new UnitTouchListener(position));
        catalog.get(position);
        viewHolder.getTextView().setText(catalog.get(position).getName());
//        viewHolder.getTextView().setBackgroundColor(entry.inUse?R.color.Red:R.color.Green);
        viewHolder.getTextView().setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(),R.color.black));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return catalog.size();
    }

    public BluetoothDevice getSelectedItem(){
        return chosenDevice;
    }
}

