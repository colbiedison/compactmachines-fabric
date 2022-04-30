package us.dison.compactmachines.tunnel;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.util.math.Direction;

import java.util.Arrays;

public class MachineItemInventory {

    public static final MachineItemInventory EMPTY = new MachineItemInventory(new InventoryStorage[6]);

    private final InventoryStorage[] inventories;

    public MachineItemInventory(InventoryStorage[] invs) {
        this.inventories = invs;
        if (this == EMPTY)
            Arrays.fill(inventories, null);
    }


    public InventoryStorage get(Direction side) {
        if (side == null) return null;
        return inventories[side.ordinal()];
    }

    public void set(Direction side, InventoryStorage inv) {
        if (side == null) return;
        inventories[side.ordinal()] = inv;
    }

}
