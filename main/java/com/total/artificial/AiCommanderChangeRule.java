package com.total.artificial;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.Game;
import com.total.overiden.IUnitData;
import com.total.overiden.MainActivity;

import java.util.List;

public class AiCommanderChangeRule {
    private final int key;
    private final int newCard;
    private boolean checked = false;
    private int result = 0;
    private final AiEnums.AiCommanderChangeType rule;
    public AiCommanderChangeRule(String data){
        super();
        String[] parts = data.split(",");
        key = Integer.parseInt(parts[0]);
        newCard = Integer.parseInt(parts[1]);
        rule = AiEnums.AiCommanderChangeType.valueOf(parts[2]);
    }
    public AiCommanderChangeRule(Cursor cur){
        super();
        key = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_ID);
        newCard = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_COMM_CHANGE_NEXT);
        rule = AiEnums.AiCommanderChangeType.valueOf(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_COMM_CHANGE_TYPE));
    }
    public boolean checkChangeCard() {
        List<IUnitData> myDead = Game.current.getAiForce().getDestroyed();
        List<IUnitData> theirDead = Game.current.getForce(0).getDestroyed();
        // return true if card needs to be changed
        if (!checked) {
            AlertDialog.Builder builder;
            AlertDialog dialog;
            String question = "";
            switch (rule) {
                case LOST_MORE: //lost more units
                case LOST_FEWER: // lost fewer units
                    checked = true;
                    // b: have I lost more value of units than my enemy?
                    // c: has my enemy lost more than me?
                    int myLost = getLostValue(myDead);
                    int theirLost = getLostValue(theirDead);
                    if ((rule == AiEnums.AiCommanderChangeType.LOST_MORE && myLost < theirLost) ||
                            (rule == AiEnums.AiCommanderChangeType.LOST_FEWER && myLost > theirLost))
                        return true;
                    break;
                case OBJECTIVE: //objective
                    // a: are the forces objectives complete?
                    question = "Are AIs objectives complete?";
                // d: are enemies within a certain range?
                // e: are any enemies in the rear arc of/behind one of my units?
                // f: are any TMM 3+ enemies within range?
                case FAST_ENEMY_CLOSE:
                    if (rule == AiEnums.AiCommanderChangeType.FAST_ENEMY_CLOSE)
                        question = "Are any enemy units with TMM 3+ within 24 inches of any of AIs units?";
                case ENEMY_CLOSE:
                    if (rule == AiEnums.AiCommanderChangeType.ENEMY_CLOSE)
                        question = "Are any enemy units within 24 inches of any of AIs units?";
                case ENEMY_BEHIND:
                    // we've already checked the result
                    if (result>0) return result==1;

                    if (rule == AiEnums.AiCommanderChangeType.ENEMY_BEHIND)
                        question = "Are any enemy units behind any of AIs units?";
                    builder = new AlertDialog.Builder(MainActivity.currentActivity);
                    builder.setMessage(question)
                            .setTitle("Resolve Commander Rule").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User taps OK button.
                                    result = 1;
                                    checked = true;
                                    MainActivity.currentActivity.getSupportFragmentManager().setFragmentResult("commanderrule",new Bundle());
                                }
                            });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancels the dialog.
                            result = 2;
                            checked = true;
                            MainActivity.currentActivity.getSupportFragmentManager().setFragmentResult("commanderrule",new Bundle());
                        }
                    });
                    dialog = builder.create();
//                    FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
//
//                    mgr.setFragmentResultListener("commanderrule",MainActivity.currentActivity,new FragmentResultListener() {
//                        @Override
//                        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
//                            // We use a String here, but any type that can be put in a Bundle is supported.
//                            checked = true;
//                        }
//                    });

                    dialog.show();
                    break;
            }
        }
        return false;
    }
    public int getNewCard(){
        return newCard;
    }
    public AiEnums.AiCommanderChangeType getRule(){
        return rule;
    }
    private int getLostValue(List<IUnitData> dead){
        // initially this will check weight of lost units, can be updated to include PV/BV, forced withdrawal etc
        int lost = 0;
        for (IUnitData killed : dead){
            lost += killed.getHeader().getMass();
        }
        return lost;
    }

    public boolean isChecked() {
        return checked;
    }

    public int getKey() {
        return key;
    }
}
