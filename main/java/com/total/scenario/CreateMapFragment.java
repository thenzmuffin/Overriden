package com.total.scenario;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.total.overiden.BSPSelectAdapter;
import com.total.overiden.MainActivity;
import com.total.overiden.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateMapFragment extends Fragment implements View.OnClickListener {


    public CreateMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CreateMapFragment.
     */

    public static CreateMapFragment newInstance() {
        CreateMapFragment fragment = new CreateMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try (ScenarioDB db = new ScenarioDB(getContext())){
            if(db.getCatalog().isEmpty()){
//                reload terrain
                db.addTerrain(new Terrain(-1,"AS Building 1","ASBUILDING1",false));
                db.addTerrain(new Terrain(-1,"AS Building 2","ASBUILDING2",false));
                db.addTerrain(new Terrain(-1,"AS Building 3","ASBUILDING3",false));
                db.addTerrain(new Terrain(-1,"AS Building 4","ASBUILDING4",false));
                db.addTerrain(new Terrain(-1,"AS Building 5","ASBUILDING5",false));
                db.addTerrain(new Terrain(-1,"AS Building 6","ASBUILDING6",false));
                db.addTerrain(new Terrain(-1,"AS Building 7","ASBUILDING7",false));

                db.addTerrain(new Terrain(-1,"SS River End 1","SSRIVEREND1",false));
                db.addTerrain(new Terrain(-1,"SS River End 2","SSRIVEREND2",false));
                db.addTerrain(new Terrain(-1,"SS River 1","SSRIVER1",false));
                db.addTerrain(new Terrain(-1,"SS River 2","SSRIVER2",false));
                db.addTerrain(new Terrain(-1,"SS River 3","SSRIVER3",false));
                db.addTerrain(new Terrain(-1,"SS River 4","SSRIVER4",false));
                db.addTerrain(new Terrain(-1,"SS Canyon End 1","SSCANYONEND1",false));
                db.addTerrain(new Terrain(-1,"SS Canyon End 2","SSCANYONEND2",false));
                db.addTerrain(new Terrain(-1,"SS Canyon 1","SSCANYON1",false));
                db.addTerrain(new Terrain(-1,"SS Canyon 2","SSCANYON2",false));
                db.addTerrain(new Terrain(-1,"SS Canyon 3","SSCANYON3",false));
                db.addTerrain(new Terrain(-1,"SS Canyon 4","SSCANYON4",false));

                db.addTerrain(new Terrain(-1,"SS Bridge","SSBRIDGE",false));

                db.addTerrain(new Terrain(-1,"SS Hill 1","SSHILL1",false));
                db.addTerrain(new Terrain(-1,"SS Hill 2","SSHILL2",false));
                db.addTerrain(new Terrain(-1,"SS Hill 3","SSHILL3",false));
                db.addTerrain(new Terrain(-1,"SS Hill 4","SSHILL4",false));
                db.addTerrain(new Terrain(-1,"SS Hill 5","SSHILL5",false));

                db.addTerrain(new Terrain(-1,"SS Waypoint","SSWAYY",false));
                db.addTerrain(new Terrain(-1,"SS Waypoint","SSWAYR",false));
                db.addTerrain(new Terrain(-1,"SS Waypoint","SSWAYG",false));

                db.addTerrain(new Terrain(-1,"Woods", "WOODS", true));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_map, container, false);
        MapView map = view.findViewById(R.id.mapDisplay);
        map.setMap(new ScenarioMap());
        map.setOnDragListener(map);
        map.setOnTouchListener(map);

        // Set up the list of available terrain elements to add to the map
        RecyclerView terrain = view.findViewById(R.id.terrainList);
        TerrainAdapter adapter = new TerrainAdapter();
        terrain.setAdapter(adapter);
        terrain.setLayoutManager(new LinearLayoutManager(view.getContext()));

        view.findViewById(R.id.save).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        MapView map = getView().findViewById(R.id.mapDisplay);
        try(ScenarioDB db = new ScenarioDB(MainActivity.currentActivity)){
            db.saveMap(map.getMap());
        }
    }
}