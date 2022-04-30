package us.dison.compactmachines.tunnel;

public class MachineInventory {

    public static final MachineInventory EMPTY = new MachineInventory(
            MachineItemInventory.EMPTY
    );

    public final MachineItemInventory item;

    public MachineInventory(MachineItemInventory inv) {
        this.item = inv;
    }


}
