package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import xyz.hsbestudio.tools.DinnerTools;
import xyz.hsbestudio.tools.utils.WorldUtils;

import java.util.List;

public class TntAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Range, where module will be targeted in.")
        .min(1)
        .max(7)
        .defaultValue(4)
        .build()
    );
    private final Setting<SortPriority> targetPriority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to select the player to target.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );
    private final Setting<List<Block>> blockList = sgGeneral.add(new BlockListSetting.Builder()
        .name("trap-blocks")
        .description("Blocks, that will be used to trap a player")
        .build()
    );

    public TntAura() {
        super(DinnerTools.CATEGORY, "TNT-aura", "Automatically traps player and blown up him by tnt.");
    }

    public PlayerEntity target;
    private Structure structure;

    @EventHandler
    @SuppressWarnings("unused")
    private void onTick(TickEvent.Post event) {
        if (target == null) {
            target = TargetUtils.getPlayerTarget(range.get(), targetPriority.get());
            return;
        }
        if (structure == null) structure = new Structure(target, blockList.get());

        structure.build(true);
    }

    private record Structure(PlayerEntity target, List<Block> allowedBlocksToPlace) {
        public void build(boolean rotate) {
            // Place block under player
            place(new BlockPos(target.getPos().add(0, -1, 0)), rotate);

            // Place surround
            for (int i = 0; i < 3; i++) createSurround(target.getBlockY() + i, rotate);

            // Place block above player
            place(new BlockPos(target.getPos().add(0, 3, 0)), rotate);
        }

        private void createSurround(int y, boolean rotate) {
            double x = target.getX();
            double z = target.getZ();

            List<BlockPos> surroundPoses = List.of(
                new BlockPos(x + 1, y, z),
                new BlockPos(x - 1, y, z),
                new BlockPos(x, y, z + 1),
                new BlockPos(x, y, z - 1)
            );

            for (BlockPos pos : surroundPoses) {
                Block block = WorldUtils.getBlock(pos);
                if (block != Blocks.AIR) continue;

                FindItemResult result = getResult();
                if (result == null) continue;

                BlockUtils.place(pos, result, rotate, 80);
            }
        }

        private FindItemResult getResult() {
            for (Block findBlock : allowedBlocksToPlace)
                if (InvUtils.findInHotbar(findBlock.asItem()).found())
                    return InvUtils.findInHotbar(findBlock.asItem());

            return null;
        }

        private void place(BlockPos pos, boolean rotate) {
            Block block = WorldUtils.getBlock(pos);
            if (block == Blocks.AIR) {
                FindItemResult result = getResult();
                if (result != null) BlockUtils.place(pos, result, rotate, 80);
            }
        }
    }
}
