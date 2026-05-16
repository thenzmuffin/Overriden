package com.total.scenario;

import com.total.overiden.R;

import java.util.ArrayList;
import java.util.List;

public class ScenarioMap {
    /*
     * This map class holds a map for a scenario.
     */
    private long key = -1;
    private List<ScenarioTerrain> listTerrain;
    private int backgroundColour;
    private int width; /* map width in feet */
    private int length; /* map width in feet */
    public ScenarioMap(){
        super();
        listTerrain = new ArrayList<>();
        backgroundColour = R.color.SandyBrown;
        width = length = 4;
    }
    public ScenarioMap(long key, int width, int length){
        super();
        listTerrain = new ArrayList<>();
        backgroundColour = R.color.SandyBrown;
        this.key = key;
        this.width = width;
        this.length = length;
    }

    public int getBackgroundColour() {
        return backgroundColour;
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }
    public List<ScenarioTerrain> getTerrain(){
        return listTerrain;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }
    public String getName(){return "Map";}
    public void addTerrain(ScenarioTerrain terrain){
        listTerrain.add(terrain);
    }

    public void setScale(int foot){
        for (ScenarioTerrain terrain : listTerrain){
            terrain.setScale(foot);
        }
    }
}
