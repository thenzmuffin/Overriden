package com.total.overiden;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overide.OVSegment;

/*
 * Display a list of items for a single unit.  Can be weapons or damage
 */
public class DamageRecordAdapter extends RecyclerView.Adapter<DamageRecordAdapter.ViewHolder> {

    private UnitTurn unitTurn;
    private int dmgCount = 0;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
    //    private final TextView textView;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
//            textView = (TextView) view.findViewById(R.id.textView);
        }

        public void setText(String text){
            TextView view = itemView.findViewById(R.id.textView);
            if (view != null)
                view.setText(text  );

        }
        public void setDetailText(String text){
            TextView view = itemView.findViewById(R.id.critCheck);
            if (view != null)
                view.setText(text  );
        }
        public void setText(DamageRecord.DamageGrouping group){
            // set up display for a single line of damage
            String text = group.damage + " Damage to " + group.getConvertedLocation().toString();
            text += " Loc:"; // + record.getDamage().get(count - 1).getLocation().getTotal();
            setText(text);
            setDice(group.getLocation());
            setDetailText(" ");
        }
        public void setText(CriticalHit crit,String desc){
            setText(desc);
            setDice(crit.getClusterDice(),8);
            setDetailText(crit.getDescription());
            // for edge abilities unit can try again with damage against an opponent
            Pilot.EdgeSkill skill = unitTurn.getUnit().getPilot().hasEdge(unitTurn, Pilot.EdgeTrigger.REROLL_CRIT, Turn.Phase.SHOOT,received);
            if (skill!= Pilot.EdgeSkill.NONE || crit.isEdgeUsed()){
                // make sure we are looking at a listing for damage against an enemy not received damage
                itemView.findViewById(R.id.edgeButton).setVisibility(View.VISIBLE);
                if (crit.isEdgeUsed()) {
                    itemView.findViewById(R.id.edgeButton).setBackground(AppCompatResources.getDrawable(MainActivity.currentActivity,R.drawable.edge_grey));
                    itemView.findViewById(R.id.edgeButton).setOnClickListener(null);
                } else {
                    itemView.findViewById(R.id.edgeButton).setBackground(AppCompatResources.getDrawable(MainActivity.currentActivity,R.drawable.edge));
                    itemView.findViewById(R.id.edgeButton).setOnClickListener(arg0 -> useEdge(crit, skill));
                }

            }
        }
        private void useEdge(CriticalHit crit, Pilot.EdgeSkill skill){
            // use edge to reroll a crit
            unitTurn.getUnit().getPilot().spendEdge(skill,1);

            crit.reverseCrit(unitTurn);
            unitTurn.getUnit().getSegment(crit.getLocation()).checkForCrit(unitTurn.getUnit(), crit, null);
        }
        public void setText(IDamageRecord data){
            TextView view = itemView.findViewById(R.id.textView);
            itemView.findViewById(R.id.edgeButton).setVisibility(View.GONE);

            if (data instanceof CriticalHit){
                setText((CriticalHit)data, "Critical: " );
            } else if (data instanceof DamageMessage){
                view.setText(((DamageMessage)data).getDescription());
                TwoDSix roll = data.getClusterDice();
                if (roll != null)setDice(roll);
            } else if (data instanceof DamageRecord) {
                int heat = ((DamageRecord) data).getHeatDamage();
                String heatMsg = (heat>0)?"+" + heat + " Heat":"";
                String display = data.getWeapon().getName() + " : " + data.getTotalDamage() + heatMsg + " to the ";
                view.setText(display);
            } else view.setText("Unknown Damage Type");
        }
        public void setHighlight(boolean highlight){
            TextView view = itemView.findViewById(R.id.textView);
            if (highlight) {
                view.setBackgroundColor(MainActivity.currentActivity.getColor(R.color.OrangeRed));
                view.setTextColor(MainActivity.currentActivity.getColor(R.color.black));
            }
        }
        public void setText(GenericCheck check){
            // set up display for PSR type line
            TextView view = itemView.findViewById(R.id.check_name);
            view.setText(check.getDescription());
            view = itemView.findViewById(R.id.check_pass_no);
            view.setText(Integer.toString(check.getToHit()));
            view.setOnClickListener(arg0 -> displayTooltip(check));
            boolean checked = check.getStatus() != TargetWeapon.ShotStatus.NOTFIRED;
            TwoDSixView dSix = itemView.findViewById(R.id.to_hit_dice);
            dSix.setVisibility(checked?View.VISIBLE:View.INVISIBLE);
            itemView.findViewById(R.id.success).setVisibility(checked?View.VISIBLE:View.INVISIBLE);
            itemView.findViewById(R.id.hit_button).setVisibility(checked||displayOnly?View.INVISIBLE:View.VISIBLE);
            itemView.findViewById(R.id.miss_button).setVisibility(checked||displayOnly?View.INVISIBLE:View.VISIBLE);
            itemView.findViewById(R.id.auto_button).setVisibility(checked||displayOnly?View.INVISIBLE:View.VISIBLE);
            if (!checked){
                ((Button)itemView.findViewById(R.id.miss_button)).setText("Fail");
                ((Button)itemView.findViewById(R.id.hit_button)).setText("Pass");
                itemView.findViewById(R.id.miss_button).setOnClickListener(arg0 -> clickAttack("FAIL", check));
                itemView.findViewById(R.id.hit_button).setOnClickListener(arg0 -> clickAttack("PASS", check));
                itemView.findViewById(R.id.auto_button).setOnClickListener(arg0 -> clickAttack("AUTO", check));
            } else {
                if (check.getRoll() != null) {
                    dSix.setDice(check.getRoll(),check.getToHit());
                    dSix.setSize(TwoDSixView.DiceSize.SMALL);
                }
                else dSix.setVisibility(View.INVISIBLE);
                TextView hitMissInd = itemView.findViewById(R.id.hit_miss_ind);
                switch (check.getStatus()){
                    case HIT:
                        ((TextView)itemView.findViewById(R.id.success)).setText("Pass");
                        hitMissInd.setBackgroundColor(itemView.getResources().getColor(R.color.Chartreuse,null));
                        break;
                    case MISS:
                        ((TextView)itemView.findViewById(R.id.success)).setText("Fail");
                        hitMissInd.setBackgroundColor(itemView.getResources().getColor(R.color.Red,null));
                        break;
                }
            }
        }
        public void displayTooltip(GenericCheck check){
            TooltipAdjustDialogFragment tooltip =
                    new TooltipAdjustDialogFragment(check.calculateTargetNumberTooltip(),
                            check,
                            DamageRecordAdapter.this,
                            getAdapterPosition());

            //FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            if (fragment!=null) {
                FragmentManager mgr = fragment.getChildFragmentManager();
                tooltip.show(mgr, "Pass No. Details");
            }
        }
        private void clickAttack(String button, GenericCheck check){
            if (button.equals("AUTO")){
                check.setRoll(new TwoDSix());
            } else {
                check.setSuccess(button.equals("PASS"));
            }
//          if we just update the check record then we may miss damage applied to the mech
//          or pilot or a change in its state
            UpdatePlayerActions.updateCheckRecord(unitTurn.getUnit());
            notifyItemChanged(this.getAdapterPosition());
            if (fragment!=null)fragment.updateStepIndicators();
        }
        public void setDice(TwoDSix dice){
            setDice(dice,0);
        }
        public void setDice(TwoDSix dice, int target){
            TwoDSixView view = itemView.findViewById(R.id.cluster_dice);
            if (view!= null){
                view.setDice(dice,target);
                view.setSize(TwoDSixView.DiceSize.SMALL);
            }
        }
    }

    private final TurnPhaseFragment fragment;
    private final boolean displayOnly;
    private boolean received = true;
    /**
     * Initialize the dataset of the Adapter
     *
     */
    public DamageRecordAdapter(IUnitData unit, TurnPhaseFragment frag, boolean displayOnly) {
        super();
        updateContents(unit.getTurn());
        fragment = frag;
        this.displayOnly = displayOnly;
    }
    public void updateContents(UnitTurn unit) {
        this.unitTurn = unit;
        updateContents();
    }
    public void updateContents() {
        this.notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View                 view;
        if (viewType == 4) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.check_row_item, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.cluster_damage_row_item, viewGroup, false);
        }

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        String text = null;
        int count = position;
        // this list displays all damage records from the current turn:
        if (count < dmgCount) {
            for (IDamageRecord record : unitTurn.getDamageRecords(received)) {
                if (record instanceof DamageRecord) {
                    int rows = record.getDamage().size();
                    if (record.getClusterDice() != null || rows > 1) {
                        if (count == 0) {
                            if (record.getClusterDice() == null){
                                String msg = "head shot with damage transfer";
                                if (((DamageRecord) record).getGroupingLocation(0)!= OVSegment.OVLocation.HEAD)
                                    msg = "Automatic Cluster";
                                // head shot with damage transfer
                                viewHolder.setText( ((DamageRecord) record).getWeaponName() + " : " + msg);
                            } else {
                                //standard cluster
                                viewHolder.setText( ((DamageRecord) record).getWeaponName() + " : " + "cluster");
                                viewHolder.setDice(record.getClusterDice());
                                viewHolder.setDetailText("Total Damage: " + record.getTotalDamage());
                            }
                        } else if (count <= rows) {
                            viewHolder.setText(record.getDamage().get(count - 1));
                        }
                        count -= rows;
                    }else if (count == 0 && rows == 1) {
//                        if (record.getClusterDice() != null){
//                            text = ((DamageRecord) record).getWeaponName() + " : " + "cluster";
//                            viewHolder.setDice(record.getClusterDice());
//                        } else {
                        text = ((DamageRecord) record).getWeaponName() + " : " + record.getDamage().get(0).damage + " Damage to " + record.getDamage().get(0).getConvertedLocation().toString();
                        text += " Loc:"; // + record.getDamage().get(0).getLocation().getTotal();
                        viewHolder.setText(text);
                        viewHolder.setDice(record.getDamage().get(0).getLocation());
//                        }
                    }
                } else if (count == 0) {
                    viewHolder.setText( record ); //((DamageMessage) record).getDescription());
                    viewHolder.setHighlight( record instanceof CriticalHit);
                }
                count--;
                if (count < 0) break;
            }
//            viewHolder.setText(text);
        } else {
            GenericCheck check = unitTurn.getTurnChecks().get(count - dmgCount);
            viewHolder.setText(check);
        }
    }

    /*
     * return values 1 - header line for a damage record with multiple groups
     *               2 - damage group
     *               3 - damage message
     *               4 - Check line
     */
    @Override
    public int getItemViewType(int position) {
        /* item types:
         * 0: simple text
         * 1: top line for cluster damage
         * 2: single line for cluster damage
         * 3: damage line when not cluster
         * 4: - check type, PSR
         */
        int count = position;
        // this list displays all damage records from the current turn:
        if (count < dmgCount) {
            for (IDamageRecord record : unitTurn.getDamageRecords(received)) {

                if (record instanceof DamageRecord) {
                    int rows = record.getDamage().size();
                    if (record.getClusterDice() != null || rows > 1) {
                        // more than one damage group so must be a cluster - first line is a cluster,
                        // following are individual damage group
                        if (count == 0) return 1;
                        else if (count <= rows) return 2;
                        count -= rows;
                    }
                    if (count == 0) return 2; // single line of damage
                } else if (count == 0) return 3; // damage message
                count--;
            }
        } else {
//            GenericCheck check = unit.getTurn().getTurnChecks().get(count - dmgCount);
            return 4;
        }
        return 0;
    }
    private IDamageRecord getDamage(int position){
        return unitTurn.getDamageRecords(received).get(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        int count = 0;
        // line count is complicated a damagemessage is one line a damagerecord is either one
        // line if there is a single damage group or 1 + the number of damage groups
        for (IDamageRecord record : unitTurn.getDamageRecords(received)) {
            if (record instanceof DamageRecord) {
                int rows = record.getDamage().size();
                count += rows;
                if (record.getClusterDice() != null || rows > 1) count++;
            } else count++;
        }
        // record how many lines there are of damage to save scrolling through them when a check line is asked for
        dmgCount = count;
        count += unitTurn.getTurnChecks().size();

        return count;
    }

    public void setReceived(boolean received) {
        if (received!= this.received) {
            this.received = received;
            notifyDataSetChanged();
        }
    }
}
