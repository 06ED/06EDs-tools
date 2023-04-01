package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import xyz.hsbestudio.tools.DinnerTools;
import xyz.hsbestudio.tools.utils.WorldUtils;

import java.util.List;

public class TntAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTrap = settings.createGroup("Trap");
    private final SettingGroup sgPause = settings.createGroup("Pause");

    private final Setting<SortPriority> targetPriority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to select the player to target.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Range, where module will be targeted in.")
        .min(1)
        .max(7)
        .defaultValue(4)
        .build()
    );

    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("place-delay")
        .description("The tick delay between placing tnt.")
        .max(20)
        .min(0)
        .defaultValue(3)
        .build()
    );
    private final Setting<Integer> blownUpDelay = sgGeneral.add(new IntSetting.Builder()
        .name("blown-up-delay")
        .description("The tick delay between placing tnt.")
        .max(20)
        .min(0)
        .defaultValue(3)
        .build()
    );

    // Trap
    private final Setting<Boolean> trap = sgTrap.add(new BoolSetting.Builder()
        .name("trap")
        .description("Will be enemies trapped before blown up.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> trapDelay = sgTrap.add(new IntSetting.Builder()
        .name("trap-delay")
        .description("The tick delay between creating trap.")
        .max(20)
        .min(0)
        .defaultValue(3)
        .visible(trap::get)
        .build()
    );
    private final Setting<List<Block>> blockList = sgTrap.add(new BlockListSetting.Builder()
        .name("trap-blocks")
        .description("Blocks, that will be used to trap a player")
        .visible(trap::get)
        .build()
    );

    // Pause
    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-eat")
        .description("Pauses while eating.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-drink")
        .description("Pauses while drinking potions.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-mine")
        .description("Pauses while mining blocks.")
        .defaultValue(false)
        .build()
    );

    public TntAura() {
        super(DinnerTools.CATEGORY, "TNT-aura", "Automatically traps player and blown up him by tnt.");
    }

    public PlayerEntity target;
    private TrapStructure structure;
    private int tickTrapDelay;
    private int tickPlaceDelay;
    private int tickBlownUpDelay;

    @Override
    public void onActivate() {
        super.onActivate();

        tickTrapDelay = 0;
        tickPlaceDelay = 0;
        tickBlownUpDelay = 0;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onTick(TickEvent.Post event) {
        if (PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get())) return;
        if (target == null) {
            target = TargetUtils.getPlayerTarget(range.get(), targetPriority.get());
            structure = null;
            return;
        }
        if (structure == null) structure = new TrapStructure(target, blockList.get());

        if (trapDelay.get() == tickTrapDelay || trapDelay.get() == 0 && trap.get()) {
            structure.build(true);
            tickTrapDelay = 0;
        }
        BlockPos tntPos = new BlockPos(target.getPos()).add(0, 2, 0);
        if (placeDelay.get() == tickPlaceDelay || placeDelay.get() == 0) placeTnt(tntPos);
        if (blownUpDelay.get() == tickBlownUpDelay || blownUpDelay.get() == 0) blownUpTnt(tntPos);

        if (TargetUtils.isBadTarget(target, range.get())) target = null;
        updateTickCounter();
    }

    private void placeTnt(BlockPos pos) {
        FindItemResult result = InvUtils.findInHotbar(Items.TNT);
        if (!result.found()) return;

        BlockUtils.place(pos, result, true, 100);
        tickPlaceDelay = 0;
    }

    private void blownUpTnt(BlockPos pos) {
        FindItemResult result = InvUtils.findInHotbar(Items.FLINT_AND_STEEL);
        if (!result.found() || mc.interactionManager == null || mc.player == null) return;
        int preSlot = mc.player.getInventory().selectedSlot;

        if (result.isOffhand())
            mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
        else {
            InvUtils.swap(result.slot(), true);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
            InvUtils.swap(preSlot, true);
        }
        tickBlownUpDelay = 0;
    }

    private void updateTickCounter() {
        if (trap.get()) tickTrapDelay++;
        tickPlaceDelay++;
        tickBlownUpDelay++;
    }

    private record TrapStructure(PlayerEntity target, List<Block> allowedBlocksToPlace) {
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
