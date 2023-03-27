package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import xyz.hsbestudio.tools.DinnerTools;
import xyz.hsbestudio.tools.utils.ArmorUtils;

public class ArmorNotifier extends Module {
    public enum NotifyType {
        Chat,
        Toast,
        ChatAndToast
    }

    SettingGroup sgGeneral = settings.getDefaultGroup();

    Setting<Integer> durability = sgGeneral.add(new IntSetting.Builder()
        .name("durability")
        .description("How low armor can be to notify you.")
        .defaultValue(20)
        .min(0)
        .max(100)
        .build()
    );

    Setting<NotifyType> notifyType = sgGeneral.add(new EnumSetting.Builder<NotifyType>()
        .name("notify-type")
        .description("How to notify you about your armor")
        .defaultValue(NotifyType.ChatAndToast)
        .build()
    );

    private boolean helmetNotified;
    private boolean chestPlateNotified;
    private boolean leggingsNotified;
    private boolean bootsNotified;


    public ArmorNotifier() {
        super(DinnerTools.CATEGORY, "armor-notifier", "Say you when your armor is low.");

        helmetNotified = false;
        chestPlateNotified = false;
        leggingsNotified = false;
        bootsNotified = false;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        for (ItemStack item : mc.player.getArmorItems())
            if (ArmorUtils.checkDurability(item, durability.get())) {
                if (ArmorUtils.isHelmet(item) && !helmetNotified) {
                    notify("Hour helmet is low!");
                    helmetNotified = true;
                } else if (ArmorUtils.isChestPlate(item) && !chestPlateNotified) {
                    notify("Hour chestplate is low!");
                    chestPlateNotified = true;
                } else if (ArmorUtils.isLeggings(item) && !leggingsNotified) {
                    notify("Your leggings is low!");
                    leggingsNotified = true;
                } else if (ArmorUtils.isBoots(item) && !bootsNotified) {
                    notify("Your boots is low!");
                    bootsNotified = true;
                }

            if (!ArmorUtils.checkDurability(item, durability.get())) {
                if (ArmorUtils.isHelmet(item) && helmetNotified) helmetNotified = false;
                if (ArmorUtils.isChestPlate(item) && chestPlateNotified) chestPlateNotified = false;
                if (ArmorUtils.isLeggings(item) && leggingsNotified) leggingsNotified = false;
                if (ArmorUtils.isBoots(item) && bootsNotified) bootsNotified = false;
            }
        }
    }

    private void notify(String warningMessage) {
        switch (notifyType.get()) {
            case Chat -> warning(warningMessage);
            case Toast -> mc.getToastManager().add(new MeteorToast(Items.NETHERITE_CHESTPLATE, "Warning!", warningMessage));
            case ChatAndToast -> {
                warning(warningMessage);
                mc.getToastManager().add(new MeteorToast(Items.NETHERITE_CHESTPLATE, "Warning!", warningMessage));
            }
        }
    }
}
