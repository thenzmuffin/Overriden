package com.total.overiden;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overide.OVDatabaseForce;
import com.total.overide.OVDatabaseUnit;
import com.total.overide.OVUnitDesign;
import com.total.overide.UnitData;


public class ForceListAdapter extends RecyclerView.Adapter<ForceListAdapter.ViewHolder> implements TextWatcher {

    private ForceList list;
    private final Fragment fragment;
    private int selected = -1;
    private View lastSelected = null;
    private int forceKey;


    public class UnitTouchListener implements View.OnTouchListener {
        private final int index;

        public UnitTouchListener(int index) {
            super();
            this.index = index;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                MechViewModel mech = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
                if (mech.getMech() != null) {
                    IUnitData data = list.getUnit(index);
                    mech.setMech(data);
                }

                if (fragment instanceof IRefreshFragment){
                    ((IRefreshFragment)fragment).resetData(null);
                } else {
                    if (lastSelected != null) lastSelected.setSelected(false);
                }
                selected = index;
                view.setSelected(true);
                lastSelected = view;
            }
//            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//                // place your code here
//            }
            return false;
        }
    }

    public void clearSelected(){
        if (lastSelected != null){
            lastSelected.setSelected(false);
            lastSelected = null;
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

            textView = view.findViewById(R.id.textView);


        }

        public TextView getTextView() {
            return textView;
        }


    }

    /**
     * Initialize the dataset of the Adapter
     */
    public ForceListAdapter(Fragment frag, int forceKey, ForceList.ForceType forceType) {
        fragment = frag;
        this.forceKey = forceKey;
        if (forceKey >= 0) {
            refreshCatalog();
        } else {
            list = new ForceList(forceType);
            try(OVDatabaseForce db = new OVDatabaseForce(fragment.getActivity())) {
                db.addHeader(list);
            }
            this.forceKey = list.getKey();
        }

    }

    public void refreshCatalog() {
        if (fragment instanceof ViewForceListFragment){
            TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
            list = model.getForceList(forceKey);
        } else {
            try(OVDatabaseForce db = new OVDatabaseForce(fragment.getActivity())) {
                // not in a game so don't need ai list wrapper
                list = db.getList(forceKey, false);
            }
        }
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

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.itemView.setOnTouchListener(new UnitTouchListener(position));
        int indent = list.getForceItemIndent(position);
        StringBuilder ind = new StringBuilder();
        for(int i = 0;i<indent;i++) ind.append(" - ");
        if (list.getForceItem(position,Turn.Phase.SETUP) instanceof IUnitData) {
            IUnitData data = (IUnitData)list.getForceItem(position,Turn.Phase.SETUP);
            IUnitHeader head = data.getHeader();
            String pilotName = ind + head.getVariant() + " - " + head.getName() +  " : " + data.getPilot().getPilotName();

            viewHolder.getTextView().setText(pilotName);
        } else if (list.getForceItem(position,Turn.Phase.SETUP) instanceof ForceList){
            ForceList subList = (ForceList)list.getForceItem(position,Turn.Phase.SETUP);
            String text = ind + subList.getName();
            viewHolder.getTextView().setText(text);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.getDisplayCount(Turn.Phase.SETUP);

    }

    public String getForceName(){
        return list.getName();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence name, int i, int i1, int i2) {
        if (!list.getName().contentEquals(name)){
            list.setName(name.toString());
            // immediately update on the database
            try(OVDatabaseForce db = new OVDatabaseForce(fragment.getActivity())) {
                db.addHeader(list);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
    public IUnitData addUnit(int designKey){
        ForceList selectedList = list.getSelectedGroup(selected);
        // create a new unit instance
        UnitData data;
//        Read the template design from the catalog
        OVUnitDesign design;
        try (OVDatabaseUnit db = new OVDatabaseUnit(fragment.getActivity())) {
            design = db.getUnitDesign(designKey, list.getType());
        }
        // create a new instance of the design and add to the force list
        try (OVDatabaseForce dbForce = new OVDatabaseForce(fragment.getActivity())) {
            data = dbForce.getUnitData(dbForce.addDesignToForce(selectedList, design), false);
        }
        selectedList.addUnit(data);
//        dbForce.updateItems(list);
        //this.notifyItemInserted(list.getCount()-1);
        notifyItemInserted(selectedList.getCount()-1);
//        notifyDataSetChanged();
        return data;
    }

    public int getForceKey() {
        return forceKey;
    }

    public void removeUnit(){
        if (selected >= 0 && selected < list.getDisplayCount(Turn.Phase.SETUP)){
            try (OVDatabaseForce dbForce = new OVDatabaseForce(fragment.getActivity())) {
                clearSelected();
                list.removeUnit(selected);
                dbForce.updateItems(list);
            }
            this.notifyItemRemoved(selected);
            selected = -1;
        }
    }
    public String getTypeDescriptor(){
        switch(list.getType()){
            case OV:
                return "Override";
            case TW:
                return "Total Warfare";
        }
        return "Unknown!";
    }

    public void addGroup(String name){
        int index = list.addGroup(selected, name);
        notifyItemInserted(index);
    }
}

