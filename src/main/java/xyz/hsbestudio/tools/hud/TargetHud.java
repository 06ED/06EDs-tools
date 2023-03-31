package xyz.hsbestudio.tools.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import xyz.hsbestudio.tools.DinnerTools;
import xyz.hsbestudio.tools.module.modules.AnchorAuraPlus;
import xyz.hsbestudio.tools.module.modules.CrystalAuraPlus;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TargetHud extends HudElement {
    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(DinnerTools.HUD_GROUP, "target-hud", "displays info about targeted player", TargetHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<Boolean> copyYaw = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-yaw")
        .description("Makes the player model's yaw equal to yours.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> copyPitch = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-pitch")
        .description("Makes the player model's pitch equal to yours.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> customYaw = sgGeneral.add(new IntSetting.Builder()
        .name("custom-yaw")
        .description("Custom yaw for when copy yaw is off.")
        .defaultValue(0)
        .range(-180, 180)
        .sliderRange(-180, 180)
        .visible(() -> !copyYaw.get())
        .build()
    );

    private final Setting<Integer> customPitch = sgGeneral.add(new IntSetting.Builder()
        .name("custom-pitch")
        .description("Custom pitch for when copy pitch is off.")
        .defaultValue(0)
        .range(-90, 90)
        .sliderRange(-90, 90)
        .visible(() -> !copyPitch.get())
        .build()
    );
    private final Setting<Boolean> renderNickname = sgGeneral.add(new BoolSetting.Builder()
        .name("render-nickname")
        .description("Would be target's player nickname render.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("nickname-color")
        .description("Color of nickname od targeted player.")
        .visible(renderNickname::get)
        .build()
    );

    private final List<PlayerEntity> targets = List.of(
        Modules.get().get(CrystalAuraPlus.class).bestTarget,
        Modules.get().get(AnchorAuraPlus.class).target
    );

    public TargetHud() {
        super(INFO);
    }

    private PlayerEntity getTarget() {
        for (PlayerEntity entity : targets) if (entity != null) return entity;
        return null;
    }

    @Override
    public void render(HudRenderer renderer) {
        super.render(renderer);
        setSize(50 * scale.get(), 75 * scale.get());

        renderer.post(() -> {
            PlayerEntity player = getTarget();
            if (player == null) return;

            // Draw player
            float yaw = copyYaw.get() ? MathHelper.wrapDegrees(player.prevYaw + (player.getYaw() - player.prevYaw) * mc.getTickDelta()) : (float) customYaw.get();
            float pitch = copyPitch.get() ? player.getPitch() : (float) customPitch.get();

            InventoryScreen.drawEntity((int) (x + (25 * scale.get())), (int) (y + (66 * scale.get())), (int) (30 * scale.get()), -yaw, -pitch, player);

            // Draw nickname
            renderer.text(player.getEntityName(), x, y, color.get(), false);
        });
    }
}
