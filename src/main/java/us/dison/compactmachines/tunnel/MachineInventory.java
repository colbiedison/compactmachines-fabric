package us.dison.compactmachines.tunnel;

public class MachineInventory {

    public static final MachineInventory EMPTY = new MachineInventory(
            MachineItemInventory.EMPTY
    );

    public final MachineItemInventory item;

    public MachineInventory(MachineItemInventory inv) {
        this.item = inv;
    }

//    public void readNbt(NbtElement tag) {
//        item.readNbt(((NbtCompound) tag).get("item"));
//    }
//
//    public NbtCompound toNbt() {
//        NbtCompound nbt = new NbtCompound();
//        nbt.put("item", item.toNbt());
//
//        return nbt;
//    }

}
