package com.total.overide;

import static android.view.View.GONE;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.DamageRecordAdapter;
import com.total.overiden.FacingButton;
import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.HeatView;
import com.total.overiden.IUnitDisplay;
import com.total.overiden.MainActivity;
import com.total.overiden.MechWireframeView;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.TooltipDialogFragment;
import com.total.overiden.TurnPhaseFragment;
import com.total.overiden.UnitTypeIconView;


public class UnitDisplayShoot extends UnitDisplay{
    private boolean received = true;
    public UnitDisplayShoot(UnitData data, int forceNumber){
        super(data,forceNumber);
    }
    public void setDisplayFields(View itemView,
                                 View.OnClickListener callback,
                                 TurnPhaseFragment fragment){
        super.setDisplayFields(itemView,callback,fragment);
        RadioButton radio = itemView.findViewById(R.id.received);
        if(received)radio.setChecked(true);
        radio.setOnClickListener(callback);

        radio = itemView.findViewById(R.id.delivered);
        if(!received){
            radio.setChecked(true);
            resetDamageDisplay(radio,itemView);
        }
        radio.setOnClickListener(callback);

        itemView.findViewById(R.id.wireframe).setVisibility(received?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public boolean processButtonClick(View view, View itemView) {
        super.processButtonClick(view, itemView);
        resetDamageDisplay(view,itemView);
        return false;
    }
    private void resetDamageDisplay(View view, View itemView){
        RecyclerView damage_list = itemView.findViewById(R.id.damage_record_list);
        DamageRecordAdapter phaseAdapter = (DamageRecordAdapter) damage_list.getAdapter();
        if (phaseAdapter!=null) {
            if (view.getId() == R.id.received) received = true;
            else if (view.getId() == R.id.delivered) received = false;
            phaseAdapter.setReceived(received);
            itemView.findViewById(R.id.wireframe).setVisibility(received?View.VISIBLE:View.INVISIBLE);
        }
    }
}
