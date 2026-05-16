package com.total.overiden;

import static android.view.View.GONE;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.total.artificial.AiCommanderAdapter;
import com.total.artificial.AiForceList;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InitiativeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitiativeFragment extends Fragment implements IBluetoothMessage {
//    private TwoDSix roll_one;
//    private TwoDSix roll_two;
    public InitiativeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitiativeFragment.
     */
    public static InitiativeFragment newInstance() {
        InitiativeFragment fragment = new InitiativeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
        View view = inflater.inflate(R.layout.fragment_initiative, container, false);

        // Inflate the layout for this fragment
        TextView label = view.findViewById(R.id.force_one_label);
        label.setText(model.getForceList(0).getName());
        label = view.findViewById(R.id.force_two_label);
        label.setText(model.getForceList(1).getName());
        label = view.findViewById(R.id.force_one_mod);
        String mod_one = "Mod: " + model.getForceList(0).getInitiativeModifier();
        label.setText(mod_one);
        label = view.findViewById(R.id.force_two_mod);
        String mod_two = "Mod: " + model.getForceList(1).getInitiativeModifier();
        label.setText(mod_two);
        setDisplay(view,model);
        if (Game.current.getForceTwoType()== Game.PlayerType.AI) {
            AiForceList list = (AiForceList) Game.current.getForce(1);
            RecyclerView recycler = view.findViewById(R.id.commander);
            recycler.setAdapter(new AiCommanderAdapter(list.getCommander()));
            recycler.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        } else {
            view.findViewById(R.id.commander).setVisibility(GONE);
        }
        return view;
    }
    private void setDisplay(View view, TurnViewModel model){
        Game.PlayerType type = model.getGame().getForceOneType();
        int winner = model.getGame().getThisTurn().getInitiative();
        Button button = view.findViewById(R.id.team_one_won);
        if (type!= Game.PlayerType.CLIENT)
            button.setOnClickListener(arg0-> setInitiativeWinner(null,0, true));
        else button.setEnabled(false);

        if (winner==0)button.setBackgroundColor(getResources().getColor(R.color.Chartreuse,null));

        button = view.findViewById(R.id.team_two_won);
        if (type!= Game.PlayerType.CLIENT) button.setOnClickListener(arg0-> setInitiativeWinner(null, 1, true));
        else button.setEnabled(false);
        if (winner==1)button.setBackgroundColor(getResources().getColor(R.color.Chartreuse,null));

        // dice and roll button for team one
        TwoDSixView d6 = view.findViewById(R.id.dice_one);
        if (model.getGame().getThisTurn().getDiceOne()!=null){
            d6.setDice(model.getGame().getThisTurn().getDiceOne(),0);
        }
        if (d6.set()) d6.setVisibility(View.VISIBLE);
        else d6.setVisibility(View.INVISIBLE);
        button = view.findViewById(R.id.roll_dice_one);
        if (type!= Game.PlayerType.CLIENT) button.setOnClickListener(arg0-> rollDice(0));
        else button.setEnabled(false);
        if (winner>=0||d6.set()||type== Game.PlayerType.CLIENT) button.setVisibility(View.INVISIBLE);

        // dice and roll button for team two
        d6 = view.findViewById(R.id.dice_two);
        if (model.getGame().getThisTurn().getDiceTwo()!=null){
            d6.setDice(model.getGame().getThisTurn().getDiceTwo(),0);
        }
        if (d6.set()) d6.setVisibility(View.VISIBLE);
        else d6.setVisibility(View.INVISIBLE);
        button = view.findViewById(R.id.roll_dice_two);
        if (type!= Game.PlayerType.CLIENT) button.setOnClickListener(arg0-> rollDice(1));
        else button.setEnabled(false);
        if (winner>=0||d6.set()||type== Game.PlayerType.CLIENT) button.setVisibility(View.INVISIBLE);

        view.findViewById(R.id.reset).setOnClickListener(arg0-> resetDice());

        compareDice(view);
    }
    public void resetDice(){
        View view = this.getView();
        if (view != null) {
            TwoDSixView dice_one = view.findViewById(R.id.dice_one);
            TwoDSixView dice_two = view.findViewById(R.id.dice_two);
            dice_one.setDice(null, 0);
            dice_two.setDice(null, 0);
            view.findViewById(R.id.dice_one).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.dice_two).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.roll_dice_one).setVisibility(View.VISIBLE);
            view.findViewById(R.id.roll_dice_two).setVisibility(View.VISIBLE);
            TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
            if (model.getGame().getForceTwoType() == Game.PlayerType.BLUETOOTH) {
                //if this is a host device then send the dice roll to the client
                List<String> list = new ArrayList<>();
                list.add("INITPHASE\n");
                list.add("RESETDICE:\n");
                model.getGame().getComms().write(list);
            }
        }
    }
    public void setInitiativeWinner(View view, int team, boolean sendUpdate){
        TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
        model.getGame().getThisTurn().setInitiative(team);
        View currentView = view;
        if (currentView==null)currentView = getView();
        Button button = currentView.findViewById(R.id.team_one_won);
//        button.setOnClickListener(arg0-> setInitiativeWinner(0));
        if (team==0)button.setBackgroundColor(getResources().getColor(R.color.Chartreuse,null));
        else button.setBackgroundColor(getResources().getColor(R.color.MediumPurple,null));
        button = currentView.findViewById(R.id.team_two_won);
//        button.setOnClickListener(arg0-> setInitiativeWinner(1));
        if (team==1)button.setBackgroundColor(getResources().getColor(R.color.Chartreuse,null));
        else button.setBackgroundColor(getResources().getColor(R.color.MediumPurple,null));

        Fragment parent = this.getParentFragment();
        if (parent instanceof GamePlayFragment){
            ((GamePlayFragment)parent).updateStepIndicators(null);
        }
        try (DatabaseGame gameDB = new DatabaseGame(getActivity())) {
            gameDB.updateTurn(null, model.getGame());
        }
        if (sendUpdate && model.getGame().getForceTwoType()== Game.PlayerType.BLUETOOTH) {
            //if this is a host device then send the dice roll to the client
            List<String> list = new ArrayList<>();
            list.add("INITPHASE\n");
            list.add("WINNER:" + team + "\n");
//            list.add("ENDMESSAGE\n");
            model.getGame().getComms().write(list);
//            BluetoothSendThread send = new BluetoothSendThread(null,null,model.getGame().getSocket(), list);
//            send.start();
        }
    }

    public void rollDice(int team){
        TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
        TwoDSixView dice_one = this.getView().findViewById(R.id.dice_one);
        TwoDSixView dice_two = this.getView().findViewById(R.id.dice_two);
        TwoDSix rolledDice = new TwoDSix(2, TwoDSix.RollType.LOCATION);
        int winner = -1;
        if (team==0) {
            dice_one.setVisibility(View.VISIBLE);
            dice_one.setDice(rolledDice,0);
            if(dice_two.set()){
                winner = compareDice(getView());
            }
            if (model.getGame().getForceTwoType()== Game.PlayerType.BLUETOOTH){
                //if this is a host device then send the dice roll to the client
                List<String> list = new ArrayList<>();
                list.add("INITPHASE\n");
                if (winner>=0)
                    list.add("WINNER:"+winner+"\n");
                list.add("DICE1:" + rolledDice.toString() + "\n");
//                list.add("ENDMESSAGE\n");
                model.getGame().getComms().write(list);
//                BluetoothSendThread send = new BluetoothSendThread(null,null,model.getGame().getSocket(),list);
//                send.start();
            }
            this.getView().findViewById(R.id.roll_dice_one).setVisibility(View.INVISIBLE);
        }else {
            dice_two.setVisibility(View.VISIBLE);
            dice_two.setDice(rolledDice,0);
            if(dice_one.set()){
                winner = compareDice(getView());
            }
            if (model.getGame().getForceTwoType()== Game.PlayerType.BLUETOOTH){
                //if this is a host device then send the dice roll to the client
                List<String> list = new ArrayList<>();
                list.add("INITPHASE\n");
                if (winner>=0)
                    list.add("WINNER:"+winner+"\n");
                list.add("DICE2:" + rolledDice.toString() + "\n");
//                list.add("ENDMESSAGE\n");
                model.getGame().getComms().write(list);
//                BluetoothSendThread send = new BluetoothSendThread(null,null,model.getGame().getSocket(),list);
//                send.start();
            }
            this.getView().findViewById(R.id.roll_dice_two).setVisibility(View.INVISIBLE);
        }
    }

    private int compareDice(View view){
        TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);

        view.findViewById(R.id.reset).setVisibility(View.INVISIBLE);
        int winner = -1;
        TwoDSixView dice_one = view.findViewById(R.id.dice_one);
        TwoDSixView dice_two = view.findViewById(R.id.dice_two);
        if(dice_one.set() && dice_two.set()) {
            int teamOne = dice_one.getTotal() + model.getForceList(0).getInitiativeModifier();
            int teamTwo = dice_two.getTotal() + model.getForceList(1).getInitiativeModifier();
            if (teamOne == teamTwo) {
                if (model.getGame().getForceOneType()!= Game.PlayerType.CLIENT)
                    view.findViewById(R.id.reset).setVisibility(View.VISIBLE);
            } else {
                winner = teamOne> teamTwo?0:1;
                setInitiativeWinner( view, winner, false);
            }
        }
        return winner;
    }

//    @Override
//    public void processMessage(List<String> list, String deviceName) {
//        Boolean winnerFound = false;
//        // The message will be a simple set of up to 3 fields, initiative winner, dice one, dice two
//        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
//        Game game = model.getGame();
//        for (String item : list){
//            String[] parts = item.split(":");
//            switch (parts[0]){
//                case "WINNER":
//                    game.getThisTurn().setInitiative(Integer.parseInt(parts[1]));
//                    winnerFound = true;
//                    break;
//                case "DICE1":
//                    game.getThisTurn().setDiceOne(new TwoDSix(parts[1]));
//                    break;
//                case "DICE2":
//                    game.getThisTurn().setDiceTwo(new TwoDSix(parts[1]));
//                    break;
//            }
//        }
//
//        // need to update the view in the UI thread
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            public void run() {
//                TwoDSixView dice_one = getView().findViewById(R.id.dice_one);
//                TwoDSixView dice_two = getView().findViewById(R.id.dice_two);
//                dice_one.setDice(game.getThisTurn().getDiceOne(), 0);
//                dice_two.setDice(game.getThisTurn().getDiceTwo(), 0);
//                setDisplay(getView(),model);
//                getView().invalidate();
//            }
//        });
////        The input thread now restarts itself once it processes a message so this isn't required
////        if (!winnerFound) {
////            // more data will be send, restart the listener
////            BluetoothInputThread thread = new BluetoothInputThread(model.getGame().getSocket(), this);
////            thread.start();
////        }
//    }

    @Override
    public void updateScreen() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                FragmentActivity act = getActivity();
                if (act != null) {
                    TurnViewModel model = new ViewModelProvider(act).get(TurnViewModel.class);
                    Game game = model.getGame();
                    TwoDSixView dice_one = getView().findViewById(R.id.dice_one);
                    TwoDSixView dice_two = getView().findViewById(R.id.dice_two);
                    dice_one.setDice(game.getThisTurn().getDiceOne(), 0);
                    dice_one.invalidate();
                    dice_two.setDice(game.getThisTurn().getDiceTwo(), 0);
                    setDisplay(getView(), model);
                    getView().invalidate();
                }
            }
        });
    }
}