package com.total.artificial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.Game;
import com.total.overiden.MainActivity;
import com.total.overiden.R;

import java.util.ArrayList;
import java.util.List;

public class AiCommanderAdapter extends RecyclerView.Adapter<AiCommanderAdapter.ViewHolder>{
    public class ViewHolder extends RecyclerView.ViewHolder {
        private AiInstruction instruction;

        public ViewHolder(@NonNull View view) {
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
            if (instruction.getSelected() < 0 ) {
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
            int imageId = instruction.getTactic().getImageId();
            ImageView moveType = itemView.findViewById(R.id.moveType);
            moveType.setImageResource(imageId);
        }

        public void choiceMade(int next) {
            instruction.setSelected(next);
            AiInstruction nextInstruction = commander.findInstructionById(next);
            while (nextInstruction!=null && nextInstruction.isResolved()) {
                // this is an auto resolved and it isn't yet resolved then action it now.
                if (nextInstruction.getSelectedChoice().getNextInstruction() > 0) {
                    // there is another instruction to load
                    nextInstruction = commander.findInstructionById(nextInstruction.getSelectedChoice().getNextInstruction());
                    onScreenInstructions.add(0,nextInstruction);
                    notifyItemInserted(0/*onScreenInstructions.size() - 1*/);
                } else {
                    // this is the end point of the decision tree
                    commander.setCurrentTactic(instruction.getTactic());
                    try(DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)){
                        db.saveLiveCommander(((AiForceList)Game.current.getForce(1)));
                    }
                    break;
                }
            }
            notifyItemChanged(getAdapterPosition());
        }

//        private void resolveInstructions() {
////                a negative return means the units movement has been resolved
//            commander.setCurrentTactic(instruction.getTactic());
//            try(DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)){
//                db.saveLiveCommander(((AiForceList) Game.current.getForce(1)));
//            }
//        }
    }
    public class TargetViewHolder extends ViewHolder{
        private AiInstruction instruction;
        public TargetViewHolder(@NonNull View view) {
            super(view);
        }
        public void setDisplay(AiInstruction instruction){
            this.instruction = instruction;
        }
    }
    private final AiCommander commander;
    private final List<AiInstruction> onScreenInstructions;
    public AiCommanderAdapter(AiCommander commander){
        super();
        this.commander = commander;
        onScreenInstructions = new ArrayList<>();
        commander.fillOnScreenInstructions(onScreenInstructions);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.artificial_move_phase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       holder.setDisplay(onScreenInstructions.get(position));
    }

    @Override
    public int getItemCount() {

        return onScreenInstructions.size();
    }
}
