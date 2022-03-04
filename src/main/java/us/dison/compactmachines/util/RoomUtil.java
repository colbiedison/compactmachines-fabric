package us.dison.compactmachines.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.data.persistent.RoomManager;

public abstract class RoomUtil {

    public static BlockPos getCenterPosByID(int id) {
        BlockPos.Mutable pos = new BlockPos.Mutable(255+8,7,255+8); // Center of the first region, southwest of origin
        Direction direction = Direction.NORTH;

        int index = 1;
        for (int i = 2; i <= id; i++) {
            int curLegLength = i / 2;
            for (int legIndex = 0; legIndex < curLegLength; legIndex++) {
                pos.move(direction, 16);
                index++;
                if (index == id) break;
            }
            if (index == id) break;
            direction = direction.rotateCounterclockwise(Direction.Axis.Y);
        }

        return pos.toImmutable();
    }

    public static int nextID(RoomManager roomManager) {
        int i = 1;
        while (roomManager.roomExists(i)) {
            i++;
        }
        return i;
    }

    public static void setCube(World world, BlockPos corner1, BlockPos corner2, BlockState block) {
        setCube(world, new Box(corner1, corner2), block);
    }

    public static void setCube(World world, Box box, BlockState block) {
        BlockPos corner1 = new BlockPos(box.maxX, box.maxY, box.maxZ);
        BlockPos corner2 = new BlockPos(box.minX, box.minY, box.minZ);

        for (BlockPos blockPos : BlockPos.iterate(corner1, corner2)) {
            if (!world.canSetBlock(blockPos)) {
                CompactMachines.LOGGER.error("Can't set block! "+blockPos.toShortString());
                continue;
            }
            world.setBlockState(blockPos, block);
        }
    }

    public static void setWallNBT(World world, BlockPos corner1, BlockPos corner2, int id) {
        setWallNBT(world, new Box(corner1, corner2), id);
    }

    public static void setWallNBT(World world, Box box, int id) {
        BlockPos corner1 = new BlockPos(box.maxX, box.maxY, box.maxZ);
        BlockPos corner2 = new BlockPos(box.minX, box.minY, box.minZ);

        for (BlockPos blockPos : BlockPos.iterate(corner1, corner2)) {
            if (world.getBlockEntity(blockPos) instanceof MachineWallBlockEntity wall)
                wall.setParentID(id);
        }
    }

    public static Box getBox(BlockPos centerPos, int size) {
        int s = size/2 +1;
        BlockPos corner1 = centerPos.add(s, s, s);
        BlockPos corner2 = centerPos.add(-s, -s, -s);
        return new Box(corner1, corner2);
    }

    public static void generateRoom(World world, int id, MachineSize machineSize) {
        generateRoom(world, getCenterPosByID(id), machineSize, id);
    }

    public static void generateRoom(World world, BlockPos centerPos, MachineSize machineSize, int id) {
        if (world.isClient()) return;

        CompactMachines.LOGGER.info("Generating a new room #"+id+" of size "+machineSize.getName()+" ["+machineSize.getSize()+"] at "+centerPos.toShortString());

        ChunkPos chunkPos = new ChunkPos(centerPos);
        ((ServerWorld) world).setChunkForced(chunkPos.x, chunkPos.z, true);

        Box box = getBox(centerPos, machineSize.getSize());
        setCube(world, // Place the wall blocks
                box,
                CompactMachines.BLOCK_WALL_UNBREAKABLE.getDefaultState()
        );

        Box inner = getBox(centerPos, machineSize.getSize()-2);
        setCube(world, // Replace the inside with air
                inner,
                Blocks.AIR.getDefaultState()
        );
        setWallNBT(world, // Set the parentID of the wall blocks
                box,
                id
        );

        ((ServerWorld) world).setChunkForced(chunkPos.x, chunkPos.z, false);
        CompactMachines.LOGGER.info("Done generating room #"+id);
    }

}
