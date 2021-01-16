package de.jeff_media.AngelChestPlus;

public class PendingConfirm {

    public final int chestId;
    public final TeleportAction action;

    public PendingConfirm(int chestId, TeleportAction action) {
        this.chestId=chestId;
        this.action=action;
    }

    @Override
    public String toString() {
        return "PendingConfirm{chestId="+chestId+",action="+action+"}";
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof PendingConfirm) {
            PendingConfirm otherConfirm = (PendingConfirm) other;
            return this.chestId == otherConfirm.chestId && this.action == otherConfirm.action;
        }
        return false;
    }

}
