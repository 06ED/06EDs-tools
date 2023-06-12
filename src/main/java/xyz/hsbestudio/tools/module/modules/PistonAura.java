package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
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

public class PistonAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> allowedBlocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("allowed-blocks")
        .description("Blocks, that allowed to place")
        .build()
    );

    private PlayerEntity target;

    public PistonAura() {
        super(DinnerTools.CATEGORY, "piston-aura", "Trying to place and break crystals into player using pistons.");
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onTick(TickEvent.Post event) {
        if (target == null) {
            target = TargetUtils.getPlayerTarget(5, SortPriority.LowestHealth);
        }
    }

    private class PistonStructure {
        public void update() {
            trap();
        }

        private FindItemResult findAllowed() {
            for (Block block : allowedBlocks.get()) {
                FindItemResult result = InvUtils.findInHotbar(block.asItem());
                if (result.found()) return result;
            }
            return null;
        }

        private void trap() {
            BlockPos trapBlock = target.getBlockPos().up(2);
            FindItemResult result = findAllowed();
            if (WorldUtils.getBlock(trapBlock) != Blocks.AIR || result == null) return;

            BlockUtils.place(trapBlock, result, true, 1000);
        }
    }
}
