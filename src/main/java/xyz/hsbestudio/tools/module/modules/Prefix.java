package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import xyz.hsbestudio.tools.DinnerTools;

public class Prefix extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> prefixText = sgGeneral.add(new StringSetting.Builder()
        .name("prefix-text")
        .description("Custom prefix text for addon modules.")
        .defaultValue("06ED's tools")
        .onChanged(cope -> setPrefix())
        .build()
    );

    private final Setting<SettingColor> prefixColor = sgGeneral.add(new ColorSetting.Builder()
        .name("prefix-color")
        .description("Custom prefix text for addon modules.")
        .defaultValue(new SettingColor(255, 117, 129))
        .onChanged(cope -> setPrefix())
        .build()
    );

    public Prefix() {
        super(DinnerTools.CATEGORY, "prefix", "Custom chat prefix");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        setPrefix();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        ChatUtils.unregisterCustomPrefix("xyz.hsbestudio.tools.modules");
    }

    public void setPrefix() {
        if (isActive()) ChatUtils.registerCustomPrefix("xyz.hsbestudio.tools.modules", this::getPrefix);
    }

    public Text getPrefix() {
        MutableText main = Text.literal("");
        MutableText prefix = Text.literal(prefixText.get());

        prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(prefixColor.get().getPacked())));

        main.append("[");
        main.append(prefix);
        main.append("] ");

        return main;
    }
}
