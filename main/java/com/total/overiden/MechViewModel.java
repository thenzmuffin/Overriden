package com.total.overiden;

import androidx.lifecycle.ViewModel;

public class MechViewModel extends ViewModel {
    private IUnitDesign mech = null;
    private WeaponListAdapter adapter = null;

    public void setMech(IUnitDesign mech) {
        this.mech = mech;
    }

    public IUnitDesign getMech() {
        return mech;
    }

    public WeaponListAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(WeaponListAdapter adapter) {
        this.adapter = adapter;
    }
}
