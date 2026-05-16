package com.total.overide;

public class OVRange {
    public static final int pb = 1;
    public static final int sh = 2;
    public static final int me = 3;
    public static final int lo = 4;
    public static final int ex = 5;

    public int pointBlankR;
    public int shortR;
    public int mediumR;
    public int longR;
    public int extremeR;

    public OVRange() {
        super();

    }
    public int getRange(int bracket) {
        int ret = 20;
        switch (bracket) {
            case pb:
                ret = pointBlankR;
                break;
            case sh:
                ret = shortR;
                break;
            case me:
                ret = mediumR;
                break;
            case lo:
                ret = longR;
                break;
            case ex:
                ret = extremeR;
                break;
        }
        return ret;
    }

    public static String convertRangeToString(int range){
        String ret = "Unknown";
        switch (range) {
            case pb:
                ret = "Point Blank";
                break;
            case sh:
                ret = "Short Range";
                break;
            case me:
                ret = "Medium Range";
                break;
            case lo:
                ret = "Long Range";
                break;
            case ex:
                ret = "Extreme Range";
                break;
        }
        return ret;

    }
}
