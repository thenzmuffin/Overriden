package com.total.artificial;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.total.overide.OVHeader;
import com.total.overiden.MainActivity;
import com.total.overiden.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AiConfigureDeckFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AiConfigureDeckFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String ARG_PARAM1 = "deckId";

    private String mParam1;
    private int selectedCard = 0;
    private ArtificialDeck deck;

    public AiConfigureDeckFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArtificialConfigureDeckFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AiConfigureDeckFragment newInstance(int deck) {
        AiConfigureDeckFragment fragment = new AiConfigureDeckFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, deck);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try (DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)) {
                deck = db.loadDeck(getArguments().getInt(ARG_PARAM1));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artificial_configure_deck, container, false);
        ((EditText)view.findViewById(R.id.deck_name)).setText(deck.getDeckName());
        ArrayAdapter<OVHeader.UnitRole> adRole = new ArrayAdapter<>(
                view.getContext(),
                R.layout.simple_overide_spinner_dropdown,
                OVHeader.UnitRole.values()
        );
        adRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)view.findViewById(R.id.deck_type)).setAdapter(adRole);
        int num = 0;
        for (OVHeader.UnitRole role : OVHeader.UnitRole.values()){
            if (role == deck.getDeckRole()) break;
            num++;
        }

        ((Spinner)view.findViewById(R.id.deck_type)).setSelection(num);
        List<AiUnitCard> list = deck.getCards();
        ArrayAdapter<AiUnitCard> ad = new ArrayAdapter<>(
                view.getContext(),
                R.layout.simple_overide_spinner_dropdown,
                list
        );
        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        ((Spinner)view.findViewById(R.id.card_selection)).setAdapter(ad);
        ((Spinner) view.findViewById(R.id.card_selection)).setOnItemSelectedListener(this);
        view.findViewById(R.id.new_card).setOnClickListener(this);
        RecyclerView instructions = view.findViewById(R.id.instruction_list);
        instructions.setAdapter(new AiInstructionAdapter(deck.getCards().get(selectedCard).getMainMoves()));
        view.findViewById(R.id.add_instruction).setOnClickListener(this);
        view.findViewById(R.id.save).setOnClickListener(this);
        view.findViewById(R.id.weapon_settings).setOnClickListener(this);
        instructions.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayAdapter<AiEnums.PriorityModType> adapter = new ArrayAdapter<>(
                view.getContext(),
                R.layout.simple_overide_spinner_dropdown,
                AiEnums.PriorityModType.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)view.findViewById(R.id.priority_type)).setAdapter(adapter);

        setCardStartByTactic(view);
        return view;
    }
    private void setCardStartByTactic(View view){
        setupStart(view,R.id.card_start_ag,deck.getCards().get(selectedCard).getTactic(AiEnums.Tactic.AGGRESSIVE));
        setupStart(view,R.id.card_start_ba,deck.getCards().get(selectedCard).getTactic(AiEnums.Tactic.BALANCED));
        setupStart(view,R.id.card_start_ca,deck.getCards().get(selectedCard).getTactic(AiEnums.Tactic.CAUTIOUS));
        setupStart(view,R.id.card_start_de,deck.getCards().get(selectedCard).getTactic(AiEnums.Tactic.DEFENSIVE));
    }
    private void setupStart(View view, int id, int index){
        EditText text = view.findViewById(id);
        if (text!=null){
            text.setText(Integer.toString(index));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.new_card){
            deck.getCards().add(new AiUnitCard("1"));
            view.findViewById(R.id.new_card).invalidate();
        } else if (view.getId()==R.id.add_instruction){
            int addNo = deck.getCards().get(selectedCard).getMainMoves().size()+1;
            deck.getCards().get(selectedCard).getMainMoves().add(new AiInstruction(addNo));
            RecyclerView rec = getView().findViewById(R.id.instruction_list);
            rec.getAdapter().notifyItemInserted(addNo);
        } else if (view.getId()==R.id.weapon_settings){
            // Dialog pop up to add weapon data for the card
            AiCardDialogFragment card = new AiCardDialogFragment(deck.getCards().get(selectedCard));
            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();

            mgr.setFragmentResultListener("card",MainActivity.currentActivity,new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    // We use a String here, but any type that can be put in a Bundle is supported.

                }
            });
            card.show(mgr, "Update Weapons");
        } else if (view.getId()==R.id.save){
            try (DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)){
                //update the deck values
                Spinner spin = getView().findViewById(R.id.deck_type);
                OVHeader.UnitRole role = (OVHeader.UnitRole)spin.getSelectedItem();
                deck.setDeckRole(role);
                EditText edit = getView().findViewById(R.id.deck_name);
                String name = edit.getText().toString();
                deck.setDeckName(name);
                db.saveDeck(deck);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        View mainView = getView();
        selectedCard = i;
        if (mainView!=null) {
            RecyclerView instructions = mainView.findViewById(R.id.instruction_list);
            AiInstructionAdapter adapter = ((AiInstructionAdapter) instructions.getAdapter());
            adapter.setInstructions(deck.getCards().get(i).getMainMoves());
            adapter.notifyDataSetChanged();
            String display = Integer.toString(deck.getCards().get(i).getPriority());
            ((EditText)mainView.findViewById(R.id.priority)).setText(display);
            display = Integer.toString(deck.getCards().get(i).getPriorityMod());
            ((EditText)mainView.findViewById(R.id.priority_mod)).setText(display);
            int index = 0;
            AiEnums.PriorityModType mod = AiEnums.PriorityModType.valueOf(deck.getCards().get(i).getPriorityModType());
            for (; index< AiEnums.WeaponPriority.values().length; index++){
                if (AiEnums.PriorityModType.values()[index]==mod){
                    break;
                }
            }
            ((Spinner)mainView.findViewById(R.id.priority_type)).setSelection(index);
            setCardStartByTactic(mainView);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}