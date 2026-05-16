package com.total.scenario;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.total.overiden.MainActivity;
import com.total.overiden.R;

public class Terrain {
    public enum TerrainPics{
        ASBUILDING1(R.drawable.building1),
        ASBUILDING2(R.drawable.building2),
        ASBUILDING3(R.drawable.building3),
        ASBUILDING4(R.drawable.building4),
        ASBUILDING5(R.drawable.building5),
        ASBUILDING6(R.drawable.building6),
        ASBUILDING7(R.drawable.building7),
        SSHILL1(R.drawable.hill1),
        SSHILL2(R.drawable.hill2),
        SSHILL3(R.drawable.hill3),
        SSHILL4(R.drawable.hill4),
        SSHILL5(R.drawable.hill5),
        SSRIVER1(R.drawable.river1),
        SSRIVEREND2(R.drawable.riverend2),
        SSRIVEREND1(R.drawable.riverend1),
        SSRIVER2(R.drawable.river2),
        SSRIVER3(R.drawable.river3),
        SSRIVER4(R.drawable.river4),
        SSCANYONEND1(R.drawable.canyonend1),
        SSCANYONEND2(R.drawable.canyonend2),
        SSCANYON1(R.drawable.canyon1),
        SSCANYON2(R.drawable.canyon2),
        SSCANYON3(R.drawable.canyon3),
        SSCANYON4(R.drawable.canyon4),
        SSBRIDGE(R.drawable.bridge),
        SSWAYY(R.drawable.waypointy),
        SSWAYR(R.drawable.waypointr),
        SSWAYG(R.drawable.waypointg),
        WOODS(R.drawable.tree);
        private int rid;
        TerrainPics(int id){
            rid = id;
        }
        public int getRid(){return rid;}
    }
    private long key = -1;
    private String name;
    private TerrainPics id;
    private Bitmap picture;
    private boolean shape = false;
    public Terrain(long key,String name,String id, boolean shape){
        super();
        this.key = key;
        this.name = name;
        this.id = TerrainPics.valueOf(id);
        picture = BitmapFactory.decodeResource(MainActivity.currentActivity.getResources(), this.id.getRid(), null);
        this.shape = shape;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id.name();
    }

    public Bitmap getPicture() {
        return picture;
    }

    public boolean isShape() {
        return shape;
    }
}
