package us.dison.compactmachines.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;

public class TunnelItem extends Item {

    private TunnelType type;

    public TunnelItem(Settings settings) {
        super(settings);
    }

    public TunnelItem(Settings settings, TunnelType type) {
        super(settings);
        this.type = type;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack s = super.getDefaultStack();
        if (type != null) s.setSubNbt("type", NbtString.of(type.getName()));
        return s;
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound stackNbt = stack.getNbt();
        if (stackNbt != null) {
            NbtElement typeNbt = stackNbt.get("type");
            if (typeNbt != null) {
                String strType = typeNbt.asString();
                TunnelType type = TunnelType.byName(strType);
                if (type != null) {
                    return new TranslatableText("item.compactmachines.tunnels."+type.asString());
                }
            }
        }
        return new TranslatableText("item.compactmachines.tunnels.tunnel");
    }

    public TunnelType getType() {
        return type;
    }
}
