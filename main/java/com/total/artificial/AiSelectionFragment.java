package com.total.artificial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.total.overiden.MainActivity;
import com.total.overiden.R;

import java.io.FileNotFoundException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AiSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AiSelectionFragment extends Fragment  {

    public AiSelectionFragment() {
        // Required empty public constructor
    }

    public static AiSelectionFragment newInstance(String param1, String param2) {
        AiSelectionFragment fragment = new AiSelectionFragment();
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
        View view =  inflater.inflate(R.layout.fragment_ai_selection, container, false);
        RecyclerView recycler = view.findViewById(R.id.ai_list);
        recycler.setAdapter(new AiListAdapter(this, true));
        recycler.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        view.findViewById(R.id.load_ai).setOnClickListener(arg0 -> loadAiFile());

        recycler = view.findViewById(R.id.commander_list);
        recycler.setAdapter(new AiListAdapter(this, false));
        recycler.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        return view;
    }

    public void loadAiFile(){
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
                ArtificialDeck.readAi(getContext().getContentResolver().openInputStream(selectedFile));
//                dbai.saveDeck(deck);
                if (getView()!=null) {
                    RecyclerView recycler = getView().findViewById(R.id.ai_list);
                    AiListAdapter adapter = (AiListAdapter) recycler.getAdapter();
                    if (adapter != null) {
                        adapter.refreshCatalog();
                        adapter.notifyDataSetChanged();
                    }
                    recycler = getView().findViewById(R.id.commander_list);
                    adapter = (AiListAdapter) recycler.getAdapter();
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
        NavHostFragment.findNavController(AiSelectionFragment.this)
                .navigate(R.id.action_EditDeck,bundle);
    }
    public void editCommander(int commanderId){
        Bundle bundle = new Bundle();
        bundle.putInt("commanderId", commanderId);
        NavHostFragment.findNavController(AiSelectionFragment.this)
                .navigate(R.id.action_EditCommander,bundle);
    }

}