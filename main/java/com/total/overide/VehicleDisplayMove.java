package com.total.overide;

import static android.view.View.GONE;

import android.view.View;

import com.total.overiden.MoveButton;
import com.total.overiden.R;
import com.total.overiden.UnitMove;

public class VehicleDisplayMove extends UnitDisplayMove{
    public VehicleDisplayMove(UnitData data, int forceNumber){
        super(data,forceNumber);

    }
    protected void setupMoveButtons(View itemView,
                                    View.OnClickListener callback){
        ((MoveButton)itemView.findViewById(R.id.move_mode_still)).
                setupButton("Hold", "Position", 0xff101010);
//        ((MoveButton)itemView.findViewById(R.id.move_mode_crawl)).
//                setupButton("Crawl", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.CRAWL)), 0xffffffff);
        ((MoveButton)itemView.findViewById(R.id.move_mode_walk)).
                setupButton("Cruise", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.WALK)), 0xffffffff);
        ((MoveButton)itemView.findViewById(R.id.move_mode_run)).
                setupButton("Flank", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.RUN)), 0xffffff10);
//        ((MoveButton)itemView.findViewById(R.id.move_mode_jump)).
//                setupButton("Jump", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.JUMP)), 0xffff1010);
        //set listeners
        itemView.findViewById(R.id.move_mode_still).setOnClickListener(callback);
        itemView.findViewById(R.id.move_mode_walk).setOnClickListener(callback);
        itemView.findViewById(R.id.move_mode_run).setOnClickListener(callback);
    }

    @Override
    protected void hideNonUseableMoveButtons(View itemView) {
        // vehicles can't jump or crawl
        itemView.findViewById(R.id.move_mode_jump).setVisibility(GONE);
        itemView.findViewById(R.id.move_mode_crawl).setVisibility(GONE);
        boolean immobile = unit.getAdjustedMovement(UnitMove.MoveType.WALK)==0;
        itemView.findViewById(R.id.move_mode_walk).setVisibility(immobile ? GONE:View.VISIBLE);
        // if a crew stunned hit has occurred then the unit cannot move at flank speed
        if (!immobile)immobile = unit.getState().isStunned();
        itemView.findViewById(R.id.move_mode_run).setVisibility(immobile ? GONE:View.VISIBLE);
        // Vehicles don't have prone option
        itemView.findViewById(R.id.prone).setVisibility(GONE);
        // Vehicles don't have a heat level so don't display the current heat button
        itemView.findViewById(R.id.heat_label).setVisibility(View.INVISIBLE);
    }

}
