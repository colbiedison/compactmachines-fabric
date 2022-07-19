package us.dison.compactmachines;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.dison.compactmachines.block.MachineBlock;
import us.dison.compactmachines.block.MachineWallBlock;
import us.dison.compactmachines.block.TunnelWallBlock;
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.block.entity.TunnelWallBlockEntity;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.block.entity.MachineBlockEntity;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.item.PSDItem;
import us.dison.compactmachines.item.TunnelItem;

import java.util.ArrayList;

public class CompactMachines implements ModInitializer {

    public static final String MODID = "compactmachines";
    public static final Logger LOGGER = LogManager.getLogger();


    // Biome & biome key
    private static final Biome CMBIOME = createCMBiome();
    public static final RegistryKey<Biome> CMBIOME_KEY = RegistryKey.of(Registry.BIOME_KEY, new Identifier(MODID, "compactmachines"));

    // Item/Block ID's
    public static final Identifier ID_TINY = new Identifier(MODID, "machine_tiny");
    public static final Identifier ID_SMALL = new Identifier(MODID, "machine_small");
    public static final Identifier ID_NORMAL = new Identifier(MODID, "machine_normal");
    public static final Identifier ID_LARGE = new Identifier(MODID, "machine_large");
    public static final Identifier ID_GIANT = new Identifier(MODID, "machine_giant");
    public static final Identifier ID_MAXIMUM = new Identifier(MODID, "machine_maximum");
    public static final Identifier ID_WALL_UNBREAKABLE = new Identifier(MODID, "solid_wall");
    public static final Identifier ID_WALL = new Identifier(MODID, "wall");
    public static final Identifier ID_WALL_TUNNEL = new Identifier(MODID, "tunnel_wall");
    public static final Identifier ID_PSD = new Identifier(MODID, "personal_shrinking_device");
    public static final Identifier ID_TUNNEL = new Identifier(MODID, "tunnel");
    // Block settings
    public static final FabricBlockSettings SETTINGS_BLOCK_MACHINE = FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool();
    public static final FabricBlockSettings SETTINGS_BLOCK_WALL = FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool();
    // Block
    public static final MachineBlock BLOCK_MACHINE_TINY = new MachineBlock(SETTINGS_BLOCK_MACHINE, MachineSize.TINY);
    public static final MachineBlock BLOCK_MACHINE_SMALL = new MachineBlock(SETTINGS_BLOCK_MACHINE, MachineSize.SMALL);
    public static final MachineBlock BLOCK_MACHINE_NORMAL = new MachineBlock(SETTINGS_BLOCK_MACHINE, MachineSize.NORMAL);
    public static final MachineBlock BLOCK_MACHINE_LARGE = new MachineBlock(SETTINGS_BLOCK_MACHINE, MachineSize.LARGE);
    public static final MachineBlock BLOCK_MACHINE_GIANT = new MachineBlock(SETTINGS_BLOCK_MACHINE, MachineSize.GIANT);
    public static final MachineBlock BLOCK_MACHINE_MAXIMUM = new MachineBlock(SETTINGS_BLOCK_MACHINE, MachineSize.MAXIMUM);
    public static final MachineWallBlock BLOCK_WALL_UNBREAKABLE = new MachineWallBlock(SETTINGS_BLOCK_WALL, false);
    public static final MachineWallBlock BLOCK_WALL = new MachineWallBlock(SETTINGS_BLOCK_WALL, true);
    public static final TunnelWallBlock BLOCK_WALL_TUNNEL = new TunnelWallBlock(SETTINGS_BLOCK_WALL, false);
   
    // Item Settings
    public static final FabricItemSettings SETTINGS_ITEM = new FabricItemSettings();
    // Item
    public static final Item ITEM_TUNNEL = new TunnelItem(SETTINGS_ITEM); 
    public static final Item ITEM_PSD = new PSDItem(SETTINGS_ITEM);
    public static final Item ITEM_MACHINE_TINY = new BlockItem(BLOCK_MACHINE_TINY, SETTINGS_ITEM);
    public static final Item ITEM_MACHINE_SMALL = new BlockItem(BLOCK_MACHINE_SMALL, SETTINGS_ITEM);
    public static final Item ITEM_MACHINE_NORMAL = new BlockItem(BLOCK_MACHINE_NORMAL, SETTINGS_ITEM);
    public static final Item ITEM_MACHINE_LARGE = new BlockItem(BLOCK_MACHINE_LARGE, SETTINGS_ITEM);
    public static final Item ITEM_MACHINE_GIANT = new BlockItem(BLOCK_MACHINE_GIANT, SETTINGS_ITEM);
    public static final Item ITEM_MACHINE_MAXIMUM = new BlockItem(BLOCK_MACHINE_MAXIMUM, SETTINGS_ITEM);
    public static final Item ITEM_WALL_UNBREAKABLE = new BlockItem(BLOCK_WALL_UNBREAKABLE, SETTINGS_ITEM);
    public static final Item ITEM_WALL = new BlockItem(BLOCK_WALL, SETTINGS_ITEM);
    public static final Item ITEM_WALL_TUNNEL = new BlockItem(BLOCK_WALL_TUNNEL, SETTINGS_ITEM);


    // Item group
    public static final ItemGroup CM_ITEMGROUP = FabricItemGroupBuilder.create(
            new Identifier(MODID, "title"))
            .icon(() -> new ItemStack(BLOCK_MACHINE_NORMAL))
            .appendItems(stacks -> {
                final ItemStack redstoneStack = new ItemStack(ITEM_TUNNEL);
                redstoneStack.setSubNbt("type", NbtString.of("Redstone"));
                final ItemStack itemStack = new ItemStack(ITEM_TUNNEL);
                itemStack.setSubNbt("type", NbtString.of("Item"));
                final ItemStack energyStack = new ItemStack(ITEM_TUNNEL);
                energyStack.setSubNbt("type", NbtString.of("Energy"));
                stacks.add(new ItemStack(ITEM_PSD));
                stacks.add(new ItemStack(ITEM_MACHINE_TINY));
                stacks.add(new ItemStack(ITEM_MACHINE_SMALL));
                stacks.add(new ItemStack(ITEM_MACHINE_NORMAL));
                stacks.add(new ItemStack(ITEM_MACHINE_LARGE));
                stacks.add(new ItemStack(ITEM_MACHINE_GIANT));
                stacks.add(new ItemStack(ITEM_MACHINE_MAXIMUM));
                stacks.add(new ItemStack(ITEM_WALL_UNBREAKABLE));
                stacks.add(new ItemStack(ITEM_WALL));
                // omitting Wall Tunnel on purpose
                stacks.add(redstoneStack);
                stacks.add(itemStack);
                stacks.add(energyStack);
            })
            .build();


    // BlockEntityType
    public static BlockEntityType<MachineBlockEntity> MACHINE_BLOCK_ENTITY;
    public static BlockEntityType<MachineWallBlockEntity> MACHINE_WALL_BLOCK_ENTITY;
    public static BlockEntityType<TunnelWallBlockEntity> TUNNEL_WALL_BLOCK_ENTITY;

    // Room manager (persistent data storage)
    private static RoomManager roomManager = null;

    // Dimension
    public static ServerWorld cmWorld = null;


    @Override
    public void onInitialize() {

        // REGISTER room manager callbacks
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.equals(cmWorld)) {
                roomManager.onServerWorldLoad(cmWorld);
            }
        });
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (world.equals(cmWorld)) {
                roomManager.onServerWorldTick(cmWorld);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            cmWorld = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, new Identifier(MODID, "compactmachinesdim")));
            roomManager = RoomManager.get(cmWorld);
        });

        // REGISTER Biome
        Registry.register(BuiltinRegistries.BIOME, CMBIOME_KEY.getValue(), CMBIOME);

        // REGISTER BlockEntityType
        MACHINE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":machine_block_entity", FabricBlockEntityTypeBuilder.create(
                MachineBlockEntity::new,
                BLOCK_MACHINE_TINY,
                BLOCK_MACHINE_SMALL,
                BLOCK_MACHINE_NORMAL,
                BLOCK_MACHINE_LARGE,
                BLOCK_MACHINE_GIANT,
                BLOCK_MACHINE_MAXIMUM
        ).build(null));
        TUNNEL_WALL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":tunnel_wall_block_entity", FabricBlockEntityTypeBuilder.create(
                TunnelWallBlockEntity::new, BLOCK_WALL_TUNNEL
        ).build(null));
        MACHINE_WALL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":machine_wall_block_entity", FabricBlockEntityTypeBuilder.create(
                MachineWallBlockEntity::new, BLOCK_WALL_UNBREAKABLE
        ).build(null));

        // REGISTER Fabric Transfer API blocks
        ItemStorage.SIDED.registerForBlockEntity(
                (machineEntity, direction) -> {
                    ArrayList<InventoryStorage> targets = new ArrayList<>();
                    for (Tunnel tunnel : roomManager.getRoomByNumber(machineEntity.getMachineID()).getTunnels()) {
                        if (cmWorld.getBlockEntity(tunnel.getPos()) instanceof TunnelWallBlockEntity tunnelEntity) {
                            tunnelEntity.setConnected(false);
                            if (tunnel.getFace().toDirection() == null) continue;
                            if (!tunnel.getFace().toDirection().equals(direction)) continue;
                            if (tunnelEntity.getInternalTransferTarget() instanceof Inventory inv) {
                                tunnelEntity.setConnected(true);
                                targets.add(InventoryStorage.of(inv, null));
                            }
                        }
                    }

                    if (targets.size() < 1) return null;
                    return targets.get( (int) (Math.random() * targets.size()) );
                },
                MACHINE_BLOCK_ENTITY
        );

        // REGISTER Block
        Registry.register(Registry.BLOCK, ID_TINY, BLOCK_MACHINE_TINY);
        Registry.register(Registry.BLOCK, ID_SMALL, BLOCK_MACHINE_SMALL);
        Registry.register(Registry.BLOCK, ID_NORMAL, BLOCK_MACHINE_NORMAL);
        Registry.register(Registry.BLOCK, ID_LARGE, BLOCK_MACHINE_LARGE);
        Registry.register(Registry.BLOCK, ID_GIANT, BLOCK_MACHINE_GIANT);
        Registry.register(Registry.BLOCK, ID_MAXIMUM, BLOCK_MACHINE_MAXIMUM);
        Registry.register(Registry.BLOCK, ID_WALL_UNBREAKABLE, BLOCK_WALL_UNBREAKABLE);
        Registry.register(Registry.BLOCK, ID_WALL, BLOCK_WALL);
        Registry.register(Registry.BLOCK, ID_WALL_TUNNEL, BLOCK_WALL_TUNNEL);

        // REGISTER Item
        Registry.register(Registry.ITEM, ID_TINY,       ITEM_MACHINE_TINY);
        Registry.register(Registry.ITEM, ID_SMALL,      ITEM_MACHINE_SMALL);
        Registry.register(Registry.ITEM, ID_NORMAL,     ITEM_MACHINE_NORMAL);
        Registry.register(Registry.ITEM, ID_LARGE,      ITEM_MACHINE_LARGE);
        Registry.register(Registry.ITEM, ID_GIANT,      ITEM_MACHINE_GIANT);
        Registry.register(Registry.ITEM, ID_MAXIMUM,    ITEM_MACHINE_MAXIMUM);
        Registry.register(Registry.ITEM, ID_WALL_UNBREAKABLE, ITEM_WALL_UNBREAKABLE);
        Registry.register(Registry.ITEM, ID_WALL,       ITEM_WALL);
        Registry.register(Registry.ITEM, ID_WALL_TUNNEL,ITEM_WALL_TUNNEL);
        Registry.register(Registry.ITEM, ID_PSD, ITEM_PSD);
        Registry.register(Registry.ITEM, ID_TUNNEL, ITEM_TUNNEL);
        //Registry.register(Registry.ITEM, ID_REDSTONE_TUNNEL, ITEM_REDSTONE_TUNNEL);
        //Registry.register(Registry.ITEM, ID_ITEM_TUNNEL, ITEM_ITEM_TUNNEL);
        //Registry.register(Registry.ITEM, ID_ENERGY_TUNNEL, ITEM_ENERGY_TUNNEL);
        
        LOGGER.info("CompactMachines initialized");
    }



    public static Biome createCMBiome() {
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();
        GenerationSettings.Builder generationSettings = new GenerationSettings.Builder();

        return (new Biome.Builder())
                .precipitation(Biome.Precipitation.NONE)
                .category(Biome.Category.NONE)
                .temperature(0.8f)
                .downfall(0f)
                .effects((new BiomeEffects.Builder())
                        .waterColor(0x3f76e4)
                        .waterFogColor(0x050533)
                        .fogColor(0xc0d8ff)
                        .skyColor(0x77adff)
                        .build())
                .spawnSettings(spawnSettings.build())
                .generationSettings(generationSettings.build())
                .build();
    }

    public static RoomManager getRoomManager() {
        return roomManager;
    }


}
