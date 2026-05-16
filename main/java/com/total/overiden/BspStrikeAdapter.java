package com.total.overiden;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class BspStrikeAdapter extends RecyclerView.Adapter<BspStrikeAdapter.ViewHolder> {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private BSPStrike card = null;
    //    private final TextView textView;
        public ViewHolder(View view) {

            super(view);
            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            mgr.setFragmentResultListener("redisplay",MainActivity.currentActivity, (requestKey, bundle) -> {
                // We use a String here, but any type that can be put in a Bundle is supported.
                int strikeKey = bundle.getInt("strikeKey", -1);
                int index = 0;
                BSPStrike strike = null;
                // find the right card to update
                for (int i = 0;i<cards.size();i++){
                    if (strikeKey==cards.get(i).getKey()){
                        strike = cards.get(i);
                        index = i;
                        break;
                    }
                }
                // update the found card
                if (strike !=null){
                    // for area effect weapons that have just exploded we should apply
                    // damage to all the designated targets before redisplaying
                    if (phase== Turn.Phase.SHOOT && strike.getDamageType().equalsIgnoreCase("ae")){
                        strike.activate("HIT");
                    }
                    notifyItemChanged(index);
                    UpdatePlayerActions.bspCardUpdate(strike);
                }
                mgr.setFragmentResult("strikeUpdate",new Bundle());
            });
        }
        public void displayItem(BSPStrike selected) {
            card = selected;
            TextView label = itemView.findViewById(R.id.card_name);
            label.setText(card.getName());
            ((TextView)itemView.findViewById(R.id.card_type)).setText(card.getType().toString());
            ((TextView)itemView.findViewById(R.id.target_number)).setText(String.format(Locale.getDefault(Locale.Category.FORMAT), "%d", card.getTargetNumber()));
            ((TextView)itemView.findViewById(R.id.dmg_value_groupings)).setText(card.getDVG());
            ((TextView)itemView.findViewById(R.id.dmg_type)).setText(card.getDamageType());

            itemView.setBackground(AppCompatResources.getDrawable(MainActivity.currentActivity,card.getType().getColour()));
            // determine if the activate button should be displayed
            // 1) shoot phase and the bsp is "landing"
            // 2) Targeting phase and the card hasn't been activated
            // 3) move phase and the card hasn't been activated
            boolean displayButton = !card.isTargeted();
            RecyclerView targetList = itemView.findViewById(R.id.target_list);
            if (card.getTargetList().isEmpty()){
                // no target list so display the target location instead just in case
                targetList.setVisibility(View.GONE);
                itemView.findViewById(R.id.target_loc).setVisibility(View.VISIBLE);
                displayButton = displayButton || phase== Turn.Phase.SHOOT;
            } else {
                itemView.findViewById(R.id.target_loc).setVisibility(View.GONE);
                targetList.setVisibility(View.VISIBLE);
                targetList.setAdapter(new BspShootAdapter(card, phase));
                targetList.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }
            targetList.setVisibility(!card.isTargeted()?View.GONE:View.VISIBLE);
            boolean displayList = card.isTargeted() && !card.getTargetList().isEmpty();
            itemView.findViewById(R.id.scroll).setVisibility(!displayList?View.GONE:View.VISIBLE);
            switch (phase) {
                case SETUP:
                    itemView.findViewById(R.id.activate).setVisibility(View.GONE);
                    break;
                case MOVE:
                    ((Button)itemView.findViewById(R.id.activate)).setText(R.string.triggered);
                    itemView.findViewById(R.id.activate).setVisibility(card.isTargeted()?View.GONE:View.VISIBLE);
                    itemView.findViewById(R.id.activate).setOnClickListener(arg0 -> activateCard());
                    break;
                default:
                    ((Button)itemView.findViewById(R.id.activate)).setText(R.string.activate);
                    itemView.findViewById(R.id.activate).setVisibility(displayButton?View.VISIBLE:View.GONE);
                    itemView.findViewById(R.id.activate).setOnClickListener(arg0 -> activateCard());
            }


        }
        public void activateCard(){
            // activate can do different things:
            // Mines   - select target and detonate immediately (move phase)
            // AE BSPs - if in the targeting phase select a location, if shooting then resolve
            //           scatter then select anything that was hit
            // others  - select target ready for resolution next phase (targeting phase only)

            boolean targetMechs = true;
            if (card.getType().getNoOfTargets()==0){
                if (phase== Turn.Phase.TARGET) {
                    targetMechs = false;
                } else {
                    //resolve the shooting now to determine scatter? or do it in the dialog?
                    card.resolveAreaEffectAttack();
                }
            }
            // activate the card, this can mean different things for different cards
            SelectTargetsDialogFragment dialog = new SelectTargetsDialogFragment(card, forceIndex, targetMechs);
            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            dialog.show(mgr,"Select Target(s)");
            // different cards need different input.  A mine needs to specify the unit that has set it off
            // a strike targets a specific mech, a bomb or artillery strike targets a location
            // Strafing has a strip (a set of units) and air cover targets another air strike

            // Solution: Trigger a dialog box that feeds back to the TurnPhaseAdapter (or turn phase fragment?)

        }
    }

    private final List<BSPStrike> cards;
    private final Turn.Phase phase;
    private final int forceIndex;
    /**
     * Initialize the dataset of the Adapter
     *
     */
    public BspStrikeAdapter(List<BSPStrike> cards, Turn.Phase phase, int forceIndex) {
        super();
        this.cards = cards;
        this.phase = phase;
        this.forceIndex = forceIndex;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.bsp_strike_card_layout, viewGroup, false);


        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.displayItem(cards.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cards.size();
    }

}
