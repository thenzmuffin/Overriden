package com.total.overiden;

import android.view.View;
import android.widget.AdapterView;

import com.total.overide.OVSegment;
import com.total.overide.OVState;

import java.util.List;

public interface IUnitDisplay{
    // methods for displaying units
    void setStatusComplete(View itemView, boolean complete);
    void updateScreen(View itemView);
    void setDisplayFields(View view, View.OnClickListener callback, TurnPhaseFragment fragment);
    boolean setPhaseComplete(View itemView);
    void changeTarget(View itemView, View.OnClickListener callback);
    void setListCallbacks(View itemView, AdapterView.OnItemSelectedListener callback);
//    void updateHexValue(View itemView, int change);
    boolean processButtonClick(View view, View itemView); // returns true if the current panel needs to be redrawn
    int[] processListSelection(int listId, View itemView);
    IUnitData getUnitData();
}
