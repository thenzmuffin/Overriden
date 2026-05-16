package com.total.overiden;

public interface IMechViewer {
    IUnitDesign getDisplayMech();
    WeaponListAdapter getWeaponListAdapter();
    void setWeaponListAdapter(WeaponListAdapter adapter);
}
