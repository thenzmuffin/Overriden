package com.total.overiden;

import java.util.List;

public interface IChildLink {
    enum ChildType{
        DAMAGE, //IDamageRecord types
        CHECK, //Check Types

    }
    ChildType getRecordType();
    long getKey();
    void setParent(IChildLink parent);
    IChildLink getParent();
    List<IChildLink> getChildren();
    void reverseCrit(UnitTurn turn);
}
