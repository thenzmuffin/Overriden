package com.total.artificial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.DamageRecordAdapter;
import com.total.overiden.Game;
import com.total.overiden.IRefreshFragment;
import com.total.overiden.IUnitData;
import com.total.overiden.MainActivity;
import com.total.overiden.R;
import com.total.overiden.ShootItemAdapter;
import com.total.overiden.Turn;
import com.total.overiden.UnitMove;
import com.total.overiden.UpdatePlayerActions;

import java.util.ArrayList;
import java.util.List;

public class AiPhaseAdapter extends RecyclerView.Adapter<AiPhaseAdapter.MoveViewHolder> {

    public class MoveViewHolder extends RecyclerView.ViewHolder {

        private AiInstruction instruction;
        private int spinnerId = 0;

        public MoveViewHolder(@NonNull View view) {
            super(view);
        }

        public void setDisplay(AiInstruction instruction) {
            this.instruction = instruction;
            TextView text = itemView.findViewById(R.id.instruction);

            if (text != null) {
                String display = instruction.getQuestion();
                text.setText(display);
            }
            LinearLayout buttons = itemView.findViewById(R.id.buttons);
            buttons.removeAllViews();
            switch (instruction.getResAction()) {
                case LIST:
                    //add a list of ranges allowing range to be specified for a selected enemy
                    Spinner range = new Spinner(itemView.getContext());
                    spinnerId = View.generateViewId();
                    range.setId(spinnerId);
                    ArrayAdapter<AiInstruction.MoveChoice> adapter = new ArrayAdapter<>(
                            itemView.getContext(),
                            R.layout.simple_overide_spinner_dropdown,
                            instruction.getHolders()
                    );
// Specify the layout to use when the list of choices appears.
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
                    range.setAdapter(adapter);
                    if (instruction.getListItem() > 0) {
                        range.setSelection(instruction.getListItem());
                        range.setEnabled(false);
                    }
                    buttons.addView(range);
                    Button butt = new Button(itemView.getContext());
                    butt.setText(R.string.confirmed);
                    buttons.addView(butt);
                    butt.setOnClickListener(arg0 -> choiceMade(1));
                    break;
                case UNIT_LIST:
                    Button dialogButton = new Button(itemView.getContext());
                    dialogButton.setText(instruction.getHolders().get(0).toString());
                    buttons.addView(dialogButton);

                    dialogButton.setEnabled(instruction.getSelected() < 0);
                    dialogButton.setOnClickListener(arg0 -> getListChoice());
                    break;
                case UNIT:
                    // add a list of enemies to pick the closest
                    Spinner close = new Spinner(itemView.getContext());
                    spinnerId = View.generateViewId();
                    close.setId(spinnerId);
                    ArrayAdapter<IUnitData> adapterClose = new ArrayAdapter<>(
                            itemView.getContext(),
                            R.layout.simple_overide_spinner_dropdown,
                            Game.current.getForce(0).getAllUnits()
//                            card.getCommander().getAnalysis()
                    );
// Specify the layout to use when the list of choices appears.
                    adapterClose.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
                    close.setAdapter(adapterClose);
                    if (instruction.getListItem() > 0) {
                        close.setSelection(instruction.getListItem());
                        close.setEnabled(false);
                    }
                    buttons.addView(close);
//                  Flow to assigning buttons
                default:
                    if (instruction.getSelected() < 0) {
                        for (AiInstruction.MoveChoice choice : instruction.getHolders()) {
                            Button newButton = new Button(itemView.getContext());
                            newButton.setText(choice.toString());
                            buttons.addView(newButton);
                            newButton.setOnClickListener(arg0 -> choiceMade(choice.getNextInstruction()));
                        }
                    } else {
                        Button newButton = new Button(itemView.getContext());
                        newButton.setText(instruction.getSelectedChoice().toString());
                        buttons.addView(newButton);
                        newButton.setEnabled(false);
                    }
            }
            // translate the instruction move into the closest move possible for this unit
            UnitMove.MoveType move = instruction.getMove();
            // if the move type is undefined that means use the last valid move type
            if (move == UnitMove.MoveType.NONE) move = currentMoveType;
            if (move == UnitMove.MoveType.JUMP &&
                    (card.getState().isProne() || !card.getHeader().canJump()))
                move = UnitMove.MoveType.RUN;
            // save the last move type
            currentMoveType = move;
            //set the move type image
            int imageId = move.getImageId();
            ImageView moveType = itemView.findViewById(R.id.moveType);
            moveType.setImageResource(imageId);

        }

        public void getListChoice() {
            AiUnitListSelectDialog dia = new AiUnitListSelectDialog((AiTargetInstruction) instruction);
            if (dia.validToDisplay()) {
                FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();

                mgr.setFragmentResultListener("selected", MainActivity.currentActivity, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                        // there should only be one outcome on a unit list, so get it and set the instruction which is next

//                        instruction.setSelected(instruction.getHolders().get(((AiTargetInstruction) instruction).getSelectedList().isEmpty() ? 0 : 1).getNextInstruction());
                        card.setTargetList(((AiTargetInstruction) instruction).getSelectedList());
                        // execute the choice made method to move to the next instruction
                        choiceMade(instruction.getSelected());
                    }
                });

                dia.show(mgr, "Select Enemy Units");
            }
        }

        public void choiceMade(int nextIn) {
            int next = nextIn;
            Spinner spin = itemView.findViewById(spinnerId);
            if (spin != null) {
                if (instruction.getResAction() == AiEnums.ResolutionAction.LIST) {
                    // The selected option specifies the follow on instruction in this case so
                    // get the choice that was made
                    next = ((AiInstruction.MoveChoice) spin.getSelectedItem()).getNextInstruction();
                    // save the item that was selected so it will display again next time the list is generated
                    instruction.setListItem(spin.getSelectedItemPosition());

                } else if (instruction.getResAction() == AiEnums.ResolutionAction.UNIT) {
                    // when using the unit instruction type the dropdown list should specify the
                    // designated target so assign that here before doing normal processing
                    card.addDesignatedTarget(instruction.getDesignation(),
                            (AiUnitAnalysis) spin.getSelectedItem());
                    // save the item that was selected so it will display again next time the list is generated
                    instruction.setListItem(spin.getSelectedItemPosition());
                }
                spin.setEnabled(false);

            }
            instruction.setSelected(next);
            AiInstruction nextInstruction = instruction;
            while (nextInstruction.isResolved()) {
                int nextIndex = nextInstruction.getSelectedChoice().getNextInstruction();
                // this is an auto resolved and it isn't yet resolved then action it now.
                if (nextIndex > 0) {
                    // there is another instruction to load
                    if (!nextInstruction.getSelectedChoice().isDeckLink()) {
                        if (usingPreMoves) {
                            nextInstruction = card.getNextPreMove(nextIndex);
                            if (nextInstruction==null) {// no matching pre-move found, get the first instruction from the pilot card
                                card.getCurrentCard().fillOnScreenInstructions(onScreenInstructions);
                                usingPreMoves = false;
                            }
                        }else
                            nextInstruction = card.getCurrentCard().findInstructionById(nextIndex);
                    } else {
//                      this instruction is saying load a new deck - how do we do that from here?
                        card.setDeck(nextIndex);
                        card.drawNextCard();
                        notifyDataSetChanged();
                        // bail out all the way in order that the card selection starts again
                        return;
                    }
//                    onScreenInstructions.add(nextInstruction);
                    onScreenInstructions.add(0, nextInstruction);
                    notifyItemInserted(0 /*onScreenInstructions.size() - 1*/);
                } else {
                    if (usingPreMoves){
                        card.getCurrentCard().fillOnScreenInstructions(onScreenInstructions);
                        usingPreMoves = false;
                        notifyDataSetChanged();
                    } else
                    // this is the end point of the decision tree
                        resolveInstructions();
                    break;
                }
            }

            // refresh the current item to update the buttons - does this work if we added other instructions above?
            notifyItemChanged(getAdapterPosition());

        }

        private void resolveInstructions() {
            resolveMovement();
            refresh.resetData(itemView);
        }
    }


    private final ArtificialPilot card;
    private final IRefreshFragment refresh;
    private UnitMove.MoveType currentMoveType = UnitMove.MoveType.NONE;
    private final List<AiInstruction> onScreenInstructions;
    private boolean usingPreMoves = true; //true if the current top instruction is from pre-moves

    public static RecyclerView.Adapter<?> newInstance(Turn.Phase phase, ArtificialPilot card, IRefreshFragment refresh) {
        RecyclerView.Adapter<?> ret;
        switch (phase) {
            case TARGET:
//                ret = new AiTargetAdapter(card,refresh);
                card.updateTargetList();
                ret = new AiUnitListAdapter(card.getTargetList());
                break;
            case SHOOT:
                ret = new ShootItemAdapter(card, null);
                break;
            case RESOLVE:
            case PHYSICAL:
                ret = new DamageRecordAdapter(card, null, false);
                break;
            default:
                ret = new AiPhaseAdapter(card, refresh);
        }
        return ret;
    }

    public AiPhaseAdapter(ArtificialPilot card, IRefreshFragment refresh) {
        super();
        this.card = card;
        this.refresh = refresh;
        onScreenInstructions = new ArrayList<>();
        // for the instructions to display, first get the pre-moves supplied by the commander (if any)
        // and then move on to the instructions as provided on the current card.
        if (card.getPreMoves(onScreenInstructions)) {
            card.getCurrentCard().fillOnScreenInstructions(onScreenInstructions);
            usingPreMoves = false;
        }
    }

    @NonNull
    @Override
    public MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artificial_move_phase, parent, false);
        return new MoveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveViewHolder holder, int position) {
        holder.setDisplay(onScreenInstructions.get(position));
    }

    @Override
    public int getItemCount() {

        return onScreenInstructions.size();
    }
    private void resolveMovement() {
//                a negative return means the units movement has been resolved

        UnitMove.MoveType move = UnitMove.MoveType.WALK;
        // the list of instructions are in reverse order
        for (AiInstruction inst : onScreenInstructions) {
            if (inst.getMove() != UnitMove.MoveType.NONE) {
                move = inst.getMove();
                break;
            }
        }
        if (move == UnitMove.MoveType.JUMP &&
                (card.getState().isProne() || !card.getHeader().canJump()))
            move = UnitMove.MoveType.RUN;
        card.getTurn().getMoveData().setType(move, false);
        card.getTurn().setHexesMoved(card.getAdjustedMovement(move));
        card.getTurn().getMoveData().setMoveLocked(true);
        UpdatePlayerActions.moveActionCompleted(card, false);
    }


}
