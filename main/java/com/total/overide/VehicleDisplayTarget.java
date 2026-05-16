package com.total.overide;

import android.view.View;

import com.total.overiden.R;
import com.total.overiden.TurnPhaseFragment;

public class VehicleDisplayTarget extends UnitDisplayTarget{
    public VehicleDisplayTarget(UnitData data, int forceNumber) {
        super(data, forceNumber);
    }
    public void setDisplayFields(View itemView,
                                 View.OnClickListener callback,
                                 TurnPhaseFragment fragment) {
        super.setDisplayFields(itemView, callback, fragment);
        // vehilces don't have heat so hide heat buttons
        itemView.findViewById(R.id.heat_label).setVisibility(View.INVISIBLE);
        itemView.findViewById(R.id.predicted_heat).setVisibility(View.INVISIBLE);
        itemView.findViewById(R.id.textView9).setVisibility(View.INVISIBLE);
    }
}
