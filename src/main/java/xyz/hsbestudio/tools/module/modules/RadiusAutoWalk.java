package xyz.hsbestudio.tools.module.modules;


import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalGetToBlock;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import xyz.hsbestudio.tools.DinnerTools;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.concurrent.ThreadLocalRandom;

public class RadiusAutoWalk extends Module {
    private static int gotoX;
    private static int gotoZ;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgStartPos = settings.createGroup("Start radius position");

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("Auto walk radius")
        .min(0)
        .defaultValue(500)
        .build()
    );

    private final Setting<Integer> startX = sgStartPos.add(new IntSetting.Builder()
        .name("start X")
        .description("Start X position")
        .defaultValue(0)
        .build()
    );
    private final Setting<Integer> startZ = sgStartPos.add(new IntSetting.Builder()
        .name("start Z")
        .description("Start Z position")
        .defaultValue(0)
        .build()
    );

    public RadiusAutoWalk() {
        super(DinnerTools.CATEGORY, "radius-auto-walk", "Random auto walking in selected radius");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        startPathing();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (mc.player.getPos().x == gotoX && mc.player.getPos().z == gotoZ) startPathing();
    }

    private void startPathing() {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone.getPathingBehavior().isPathing()) baritone.getPathingBehavior().cancelEverything();

        gotoX = ThreadLocalRandom.current().nextInt(startX.get() - radius.get(), startX.get() + radius.get());
        gotoZ = ThreadLocalRandom.current().nextInt(startZ.get() - radius.get(), startZ.get() + radius.get());

        BlockPos pos = new BlockPos(gotoX, 319, gotoZ);
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalGetToBlock(pos));
    }
}
