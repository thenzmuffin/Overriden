package com.total.overiden;

public interface IAutoCheck extends IChildLink{
    String getDescription(); // text to display to the user for this roll
    void setSuccess(boolean passed); // true if the roll passed, false if it failed - must carry out the required action if there is one
    int getToHit();

    TargetWeapon.ShotStatus getStatus(); //uses shot status but this is now a generic hit/miss/not yet rolled flag

    void setRoll(TwoDSix rolled);
    TwoDSix getRoll();
}
