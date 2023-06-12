package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import xyz.hsbestudio.tools.DinnerTools;
import xyz.hsbestudio.tools.utils.PlayerUtilsPlus;
import xyz.hsbestudio.tools.utils.WorldUtils;
import xyz.hsbestudio.tools.utils.WrapUtils;

public class AnchorAuraPlus extends Module {
    public enum PlaceMode {
        Above,
        AboveAndBelow,
        All,
        PredictMovement
    }

    public enum RotationMode {
        Place,
        Break,
        Both,
        None
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlaceBreak = settings.createGroup("Place & Break");
    private final SettingGroup sgAutomation = settings.createGroup("Automation");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The radius in which players get targeted.")
        .defaultValue(4)
        .min(0)
        .sliderMax(5)
        .build()
    );
    private final Setting<SortPriority> targetPriority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to select the player to target.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );
    private final Setting<RotationMode> rotationMode = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
        .name("rotation-mode")
        .description("The mode to rotate you server-side.")
        .defaultValue(RotationMode.Both)
        .build()
    );
    private final Setting<Double> maxDamage = sgGeneral.add(new DoubleSetting.Builder().name("max-self-damage").description("The maximum self-damage allowed.").defaultValue(8).build());
    private final Setting<Double> minHealth = sgGeneral.add(new DoubleSetting.Builder().name("min-health").description("The minimum health you have to be for Anchor Aura to work.").defaultValue(15).build());
    private final Setting<Boolean> antiStuck = sgGeneral.add(new BoolSetting.Builder().name("anti-stuck").description("Prevent getting stuck when glowstone is placed on the target's head.").defaultValue(true).build());


    // Place & Break
    private final Setting<Boolean> place = sgPlaceBreak.add(new BoolSetting.Builder()
        .name("place")
        .description("Allows Anchor Aura to place anchors.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> placeDelay = sgPlaceBreak.add(new IntSetting.Builder().name("place-delay").description("The tick delay between placing anchors.").defaultValue(4).min(0).max(20).visible(place::get).build());
    private final Setting<Safety> placeMode = sgPlaceBreak.add(new EnumSetting.Builder<Safety>().name("place-mode").description("The way anchors are allowed to be placed near you.").defaultValue(Safety.Safe).visible(place::get).build());
    private final Setting<Double> placeRange = sgPlaceBreak.add(new DoubleSetting.Builder().name("place-range").description("The radius in which anchors are placed in.").defaultValue(5).min(0).sliderMax(5).visible(place::get).build());
    private final Setting<PlaceMode> placePositions = sgPlaceBreak.add(new EnumSetting.Builder<PlaceMode>().name("placement-positions").description("Where the Anchors will be placed on the entity.").defaultValue(PlaceMode.AboveAndBelow).visible(place::get).build());

    private final Setting<Integer> breakDelay = sgPlaceBreak.add(new IntSetting.Builder().name("break-delay").description("The tick delay between breaking anchors.").defaultValue(10).min(0).max(10).build());
    private final Setting<Safety> breakMode = sgPlaceBreak.add(new EnumSetting.Builder<Safety>().name("break-mode").description("The way anchors are allowed to be broken near you.").defaultValue(Safety.Safe).build());
    private final Setting<Double> breakRange = sgPlaceBreak.add(new DoubleSetting.Builder().name("break-range").description("The radius in which anchors are broken in.").defaultValue(5).min(0).sliderMax(5).build());

    // Automation
    private final Setting<Boolean> breakSelfTrap = sgAutomation.add(new BoolSetting.Builder().name("break-self-trap").description("Break target's self-trap before placing/breaking.").defaultValue(true).build());
    private final Setting<Boolean> breakBurrow = sgAutomation.add(new BoolSetting.Builder().name("break-burrow").description("Break target's burrow before placing/breaking.").defaultValue(true).build());
    private final Setting<Boolean> fastPlaceAfter = sgAutomation.add(new BoolSetting.Builder().name("fast-place-after-break").description("Place the next anchor immediately after breaking target's self-trap.").defaultValue(true).build());
    private final Setting<Boolean> renderAutomation = sgAutomation.add(new BoolSetting.Builder().name("render-break").description("Render mining self-trap/burrow.").defaultValue(true).build());

    // Pause

    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder().name("pause-on-eat").description("Pauses while eating.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder().name("pause-on-drink").description("Pauses while drinking potions.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder().name("pause-on-mine").description("Pauses while mining blocks.").defaultValue(false).build());

    // Render
    private final Setting<Boolean> renderDamageText = sgRender.add(new BoolSetting.Builder()
        .name("render-damage")
        .description("Renders crystal damage text in the block overlay.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> damageColor = sgRender.add(new ColorSetting.Builder()
        .name("damage-color")
        .description("The color of the damage text.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(renderDamageText::get)
        .build()
    );

    private final Setting<Double> damageTextScale = sgRender.add(new DoubleSetting.Builder()
        .name("damage-scale")
        .description("How big the damage text should be.")
        .defaultValue(1.25)
        .min(1)
        .sliderMax(4)
        .visible(renderDamageText::get)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
    private final Setting<Boolean> renderPlace = sgRender.add(new BoolSetting.Builder().name("render-place").description("Renders the block where it is placing an anchor.").defaultValue(true).build());
    private final Setting<SettingColor> placeSideColor = sgRender.add(new ColorSetting.Builder().name("place-side-color").description("The side color for positions to be placed.").defaultValue(new SettingColor(255, 0, 0, 75)).visible(renderPlace::get).build());
    private final Setting<SettingColor> placeLineColor = sgRender.add(new ColorSetting.Builder().name("place-line-color").description("The line color for positions to be placed.").defaultValue(new SettingColor(255, 0, 0, 255)).visible(renderPlace::get).build());
    private final Setting<Boolean> renderBreak = sgRender.add(new BoolSetting.Builder().name("render-break").description("Renders the block where it is breaking an anchor.").defaultValue(true).build());
    private final Setting<SettingColor> breakSideColor = sgRender.add(new ColorSetting.Builder().name("break-side-color").description("The side color for anchors to be broken.").defaultValue(new SettingColor(255, 0, 0, 75)).visible(renderBreak::get).build());
    private final Setting<SettingColor> breakLineColor = sgRender.add(new ColorSetting.Builder().name("break-line-color").description("The line color for anchors to be broken.").defaultValue(new SettingColor(255, 0, 0, 255)).visible(renderBreak::get).build());

    private int placeDelayLeft;
    private int breakDelayLeft;
    private PlayerEntity target;
    private boolean sentTrapMine;
    private boolean sentBurrowMine;
    private boolean sentAntiStuck;
    private Vector3d vec3;

    public AnchorAuraPlus() {
        super(DinnerTools.CATEGORY, "anchor-aura+", "Anchor Aura v2.");
    }

    @Override
    public void onActivate() {
        placeDelayLeft = 0;
        breakDelayLeft = 0;
        target = null;
        sentTrapMine = false;
        sentBurrowMine = false;
        sentAntiStuck = false;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.world.getDimension().respawnAnchorWorks()) {
            error("You are in the Nether... disabling.");
            toggle();
            return;
        }

        if (PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get())) return;
        if (EntityUtils.getTotalHealth(mc.player) <= minHealth.get()) return;

        if (TargetUtils.isBadTarget(target, targetRange.get()))
            target = TargetUtils.getPlayerTarget(targetRange.get(), targetPriority.get());
        if (TargetUtils.isBadTarget(target, targetRange.get())) return;

        FindItemResult anchor = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
        FindItemResult glowStone = InvUtils.findInHotbar(Items.GLOWSTONE);

        if (!anchor.found() || !glowStone.found()) return;

        //Anti Stuck
        if (antiStuck.get() && !sentAntiStuck) {
            if (findBreakPos(target.getBlockPos()) == null && findPlacePos(target.getBlockPos()) == null && WorldUtils.getBlock(target.getBlockPos().up(2)) == Blocks.GLOWSTONE) {
                FindItemResult pick = findPick();
                if (pick.found()) {
                    WrapUtils.updateSlot(pick.slot());
                    PlayerUtilsPlus.doPacketMine(target.getBlockPos().up(2));
                    sentAntiStuck = true;
                    return;
                }
            }
        }
        if (sentAntiStuck && WorldUtils.getBlock(target.getBlockPos().up(2)) != Blocks.GLOWSTONE) sentAntiStuck = false;

        //Anti Self Trap
        if (breakSelfTrap.get() && !sentTrapMine) {
            if (findBreakPos(target.getBlockPos()) == null && findPlacePos(target.getBlockPos()) == null && PlayerUtilsPlus.isTrapBlock(target.getBlockPos().up(2))) {
                FindItemResult pick = findPick();
                if (pick.found()) {
                    WrapUtils.updateSlot(pick.slot());
                    info("Breaking " + target.getEntityName() + "'s self-trap.");
                    PlayerUtilsPlus.doPacketMine(target.getBlockPos().up(2));
                    sentTrapMine = true;
                    return;
                }
            }
        }
        if (sentTrapMine && !PlayerUtilsPlus.isTrapBlock(target.getBlockPos().up(2))) {
            if (fastPlaceAfter.get()) placeDelayLeft = 0; breakDelayLeft = 0;
            sentTrapMine = false;
        }

        //Anti Burrow
        if (breakBurrow.get() && !sentBurrowMine && PlayerUtilsPlus.isBurrowed(target, true)) {
            FindItemResult pick = findPick();
            if (pick.found()) {
                WrapUtils.updateSlot(pick.slot());
                info("Breaking " + target.getEntityName() + "'s burrow");
                PlayerUtilsPlus.doPacketMine(target.getBlockPos());
                sentBurrowMine = true;
                return;
            }
        }
        if (sentBurrowMine && PlayerUtilsPlus.isBurrowed(target, true)) return;
        else sentBurrowMine = false;


        if (breakDelayLeft >= breakDelay.get()) {
            BlockPos breakPos = findBreakPos(target.getBlockPos());
            if (breakPos != null) {
                breakDelayLeft = 0;

                if (rotationMode.get() == RotationMode.Both || rotationMode.get() == RotationMode.Break) {
                    Rotations.rotate(Rotations.getYaw(breakPos), Rotations.getPitch(breakPos), 50, () -> breakAnchor(breakPos, anchor, glowStone));
                } else breakAnchor(breakPos, anchor, glowStone);
            }
        }

        if (placeDelayLeft >= placeDelay.get() && place.get()) {
            BlockPos placePos = findPlacePos(target.getBlockPos());

            if (placePos != null) {
                placeDelayLeft = 0;
                BlockUtils.place(placePos, anchor, (rotationMode.get() == RotationMode.Place || rotationMode.get() == RotationMode.Both), 50);
            }
        }

        placeDelayLeft++;
        breakDelayLeft++;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onRender(Render3DEvent event) {
        if (target == null) return;

        if (renderPlace.get()) {
            BlockPos placePos = findPlacePos(target.getBlockPos());
            if (placePos == null) return;
            event.renderer.box(placePos, placeSideColor.get(), placeLineColor.get(), shapeMode.get(), 0);
        }

        if (renderBreak.get()) {
            BlockPos breakPos = findBreakPos(target.getBlockPos());
            if (breakPos == null) return;
            event.renderer.box(breakPos, breakSideColor.get(), breakLineColor.get(), shapeMode.get(), 0);
        }

        if (renderDamageText.get()) {
            BlockPos pos = findBreakPos(target.getBlockPos());
            if (pos == null) return;
            vec3 = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        if (renderAutomation.get() && target != null) {
            if (sentBurrowMine) event.renderer.box(target.getBlockPos(), breakSideColor.get(), breakLineColor.get(), shapeMode.get(), 0);
            if (sentTrapMine) event.renderer.box(target.getBlockPos().up(2), breakSideColor.get(), breakLineColor.get(), shapeMode.get(), 0);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onRender2D(Render2DEvent event) {
        if (vec3 == null || target == null || !renderDamageText.get()) return;

        if (NametagUtils.to2D(vec3, damageTextScale.get())) {
            NametagUtils.begin(vec3);
            TextRenderer.get().begin(1, false, true);
            double renderDamage = DamageUtils.anchorDamage(target, new Vec3d(vec3.x(), vec3.y(), vec3.z()));

            String text = String.format("%.1f", renderDamage);
            double w = TextRenderer.get().getWidth(text) * 0.5;
            TextRenderer.get().render(text, -w, 0, damageColor.get(), true);

            TextRenderer.get().end();
            NametagUtils.end();
        }
    }

    private BlockPos findPlacePos(BlockPos targetPlacePos) {
        switch (placePositions.get()) {
            case All -> {
                if (isValidPlace(targetPlacePos.down())) return targetPlacePos.down();
                else if (isValidPlace(targetPlacePos.up(2))) return targetPlacePos.up(2);
                else if (isValidPlace(targetPlacePos.add(1, 0, 0))) return targetPlacePos.add(1, 0, 0);
                else if (isValidPlace(targetPlacePos.add(-1, 0, 0))) return targetPlacePos.add(-1, 0, 0);
                else if (isValidPlace(targetPlacePos.add(0, 0, 1))) return targetPlacePos.add(0, 0, 1);
                else if (isValidPlace(targetPlacePos.add(0, 0, -1))) return targetPlacePos.add(0, 0, -1);
                else if (isValidPlace(targetPlacePos.add(1, 1, 0))) return targetPlacePos.add(1, 1, 0);
                else if (isValidPlace(targetPlacePos.add(-1, -1, 0))) return targetPlacePos.add(-1, -1, 0);
                else if (isValidPlace(targetPlacePos.add(0, 1, 1))) return targetPlacePos.add(0, 1, 1);
                else if (isValidPlace(targetPlacePos.add(0, 0, -1))) return targetPlacePos.add(0, 0, -1);
            }
            case Above -> {
                if (isValidPlace(targetPlacePos.up(2))) return targetPlacePos.up(2);
            }
            case AboveAndBelow -> {
                if (isValidPlace(targetPlacePos.down())) return targetPlacePos.down();
                else if (isValidPlace(targetPlacePos.up(2))) return targetPlacePos.up(2);
            }
            case PredictMovement -> {
                double yaw = target.getYaw();
                // TODO finish logic
            }
        }
        return null;
    }

    private BlockPos findBreakPos(BlockPos targetPos) {
        if (isValidBreak(targetPos.down())) return targetPos.down();
        else if (isValidBreak(targetPos.up(2))) return targetPos.up(2);
        else if (isValidBreak(targetPos.add(1, 0, 0))) return targetPos.add(1, 0, 0);
        else if (isValidBreak(targetPos.add(-1, 0, 0))) return targetPos.add(-1, 0, 0);
        else if (isValidBreak(targetPos.add(0, 0, 1))) return targetPos.add(0, 0, 1);
        else if (isValidBreak(targetPos.add(0, 0, -1))) return targetPos.add(0, 0, -1);
        else if (isValidBreak(targetPos.add(1, 1, 0))) return targetPos.add(1, 1, 0);
        else if (isValidBreak(targetPos.add(-1, -1, 0))) return targetPos.add(-1, -1, 0);
        else if (isValidBreak(targetPos.add(0, 1, 1))) return targetPos.add(0, 1, 1);
        else if (isValidBreak(targetPos.add(0, 0, -1))) return targetPos.add(0, 0, -1);
        return null;
    }

    private boolean getDamagePlace(BlockPos pos) {
        return placeMode.get() == Safety.Suicide || DamageUtils.bedDamage(mc.player, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) <= maxDamage.get();
    }

    private boolean getDamageBreak(BlockPos pos) {
        return breakMode.get() == Safety.Suicide || DamageUtils.anchorDamage(mc.player, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) <= maxDamage.get();
    }

    private boolean isValidPlace(BlockPos pos) {
        return mc.world.getBlockState(pos).isAir() && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(pos)) <= placeRange.get() && getDamagePlace(pos);
    }

    private boolean isValidBreak(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(pos)) <= breakRange.get() && getDamageBreak(pos);
    }

    private void breakAnchor(BlockPos pos, FindItemResult anchor, FindItemResult glowStone) {
        if (mc.world == null || mc.player == null) return;
        if (pos == null || mc.world.getBlockState(pos).getBlock() != Blocks.RESPAWN_ANCHOR) return;

        mc.player.setSneaking(false);
        int preSlot = mc.player.getInventory().selectedSlot;

        WorldUtils.interaction(pos, glowStone, preSlot);
        WorldUtils.interaction(pos, anchor, preSlot);
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    private FindItemResult findPick() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof PickaxeItem);
    }
}
