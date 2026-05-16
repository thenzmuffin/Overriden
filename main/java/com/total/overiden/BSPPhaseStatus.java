package com.total.overiden;

public class BSPPhaseStatus {
    private boolean moveComplete;
    private boolean targetComplete;
    private boolean shootComplete;

    BSPPhaseStatus(boolean listExists){
        super();
        moveComplete = !listExists;
        targetComplete = !listExists;
        shootComplete = !listExists;
    }

    public boolean isPhaseComplete(Turn.Phase phase){
        switch (phase){
            case MOVE: return moveComplete;
            case TARGET: return targetComplete;
            case SHOOT: return shootComplete;
            default:
                return true;
        }
    }

    public void setPhaseComplete(Turn.Phase phase, boolean status) {
        switch (phase) {
            case MOVE:
                moveComplete = status;
                break;
            case TARGET:
                targetComplete = status;
                break;
            case SHOOT:
                shootComplete = status;
                break;
        }
    }

    public boolean isMoveComplete() {
        return moveComplete;
    }

    public void setMoveComplete(boolean moveComplete) {
        this.moveComplete = moveComplete;
    }

    public boolean isTargetComplete() {
        return targetComplete;
    }

    public void setTargetComplete(boolean targetComplete) {
        this.targetComplete = targetComplete;
    }

    public boolean isShootComplete() {
        return shootComplete;
    }

    public void setShootComplete(boolean shootComplete) {
        this.shootComplete = shootComplete;
    }
}
