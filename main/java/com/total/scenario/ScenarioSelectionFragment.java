package com.total.scenario;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overiden.Scenario;
import com.total.artificial.AiListAdapter;
import com.total.artificial.DatabaseAI;
import com.total.overiden.MainActivity;
import com.total.overiden.R;

import java.io.FileNotFoundException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScenarioSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScenarioSelectionFragment extends Fragment  {

    public ScenarioSelectionFragment() {
        // Required empty public constructor
    }

    public static ScenarioSelectionFragment newInstance(String param1, String param2) {
        ScenarioSelectionFragment fragment = new ScenarioSelectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_scenario_selection, container, false);
        RecyclerView recycler = view.findViewById(R.id.scenario_list);
        recycler.setAdapter(new ScenarioListAdapter(this));
        recycler.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        view.findViewById(R.id.load_scenario).setOnClickListener(arg0 -> loadScenarioFile());

        return view;
    }

    public void loadScenarioFile(){
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedFile = data.getData(); //The uri with the location of the file
        if (selectedFile != null && getContext() != null) {
            try (DatabaseAI dbai = new DatabaseAI(MainActivity.currentActivity)) {
                //ArtificialDeck deck =
                Scenario.loadScenario(getContext().getContentResolver().openInputStream(selectedFile));
//                dbai.saveDeck(deck);
                if (getView()!=null) {
                    RecyclerView recycler = getView().findViewById(R.id.scenario_list);
                    ScenarioListAdapter adapter = (ScenarioListAdapter) recycler.getAdapter();
                    if (adapter != null) {
                        adapter.refreshCatalog();
                        adapter.notifyDataSetChanged();
                    }

                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void editDeck(int deckId){
        Bundle bundle = new Bundle();
        bundle.putInt("deckId", deckId);
        NavHostFragment.findNavController(ScenarioSelectionFragment.this)
                .navigate(R.id.action_EditDeck,bundle);
    }
    public void editCommander(int commanderId){
        Bundle bundle = new Bundle();
        bundle.putInt("commanderId", commanderId);
        NavHostFragment.findNavController(ScenarioSelectionFragment.this)
                .navigate(R.id.action_EditCommander,bundle);
    }

}