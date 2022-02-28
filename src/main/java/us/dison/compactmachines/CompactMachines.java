package us.dison.compactmachines;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
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
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.block.entity.MachineBlockEntity;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.item.PSDItem;

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
    public static final Identifier ID_PSD = new Identifier(MODID, "personal_shrinking_device");

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

    // Item group
    public static final ItemGroup CM_ITEMGROUP = FabricItemGroupBuilder.build(
            new Identifier(MODID, "title"),
            () -> new ItemStack(BLOCK_MACHINE_NORMAL)
    );
    // Item settings
    public static final FabricItemSettings SETTINGS_ITEM = new FabricItemSettings().group(CM_ITEMGROUP);
    // Item
    public static final Item ITEM_PSD = new PSDItem(SETTINGS_ITEM);

    // BlockEntityType
    public static BlockEntityType<MachineBlockEntity> MACHINE_BLOCK_ENTITY;
    public static BlockEntityType<MachineWallBlockEntity> MACHINE_WALL_BLOCK_ENTITY;

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
                MachineBlockEntity::new, BLOCK_MACHINE_TINY
        ).build(null));
        MACHINE_WALL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + "machine_wall_block_entity", FabricBlockEntityTypeBuilder.create(
                MachineWallBlockEntity::new, BLOCK_WALL_UNBREAKABLE
        ).build(null));

        // REGISTER Block
        Registry.register(Registry.BLOCK, ID_TINY, BLOCK_MACHINE_TINY);
        Registry.register(Registry.BLOCK, ID_SMALL, BLOCK_MACHINE_SMALL);
        Registry.register(Registry.BLOCK, ID_NORMAL, BLOCK_MACHINE_NORMAL);
        Registry.register(Registry.BLOCK, ID_LARGE, BLOCK_MACHINE_LARGE);
        Registry.register(Registry.BLOCK, ID_GIANT, BLOCK_MACHINE_GIANT);
        Registry.register(Registry.BLOCK, ID_MAXIMUM, BLOCK_MACHINE_MAXIMUM);
        Registry.register(Registry.BLOCK, ID_WALL_UNBREAKABLE, BLOCK_WALL_UNBREAKABLE);
        Registry.register(Registry.BLOCK, ID_WALL, BLOCK_WALL);

        // REGISTER Item
        Registry.register(Registry.ITEM, ID_TINY,       new BlockItem(Registry.BLOCK.get(ID_TINY), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_SMALL,      new BlockItem(Registry.BLOCK.get(ID_SMALL), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_NORMAL,     new BlockItem(Registry.BLOCK.get(ID_NORMAL), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_LARGE,      new BlockItem(Registry.BLOCK.get(ID_LARGE), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_GIANT,      new BlockItem(Registry.BLOCK.get(ID_GIANT), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_MAXIMUM,    new BlockItem(Registry.BLOCK.get(ID_MAXIMUM), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_WALL_UNBREAKABLE,   new BlockItem(Registry.BLOCK.get(ID_WALL_UNBREAKABLE), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_WALL,       new BlockItem(Registry.BLOCK.get(ID_WALL), SETTINGS_ITEM));
        Registry.register(Registry.ITEM, ID_PSD, ITEM_PSD);

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
