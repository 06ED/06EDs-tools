package xyz.hsbestudio.tools;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import xyz.hsbestudio.tools.hud.TargetHud;
import xyz.hsbestudio.tools.module.modules.*;

public class DinnerTools extends MeteorAddon {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("06ED's tools");
    public static final HudGroup HUD_GROUP = new HudGroup("06ED's tools");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing 06ED's tools...");

        LOGGER.info("Initializing modules...");
        registerModules();

        LOGGER.info("Initializing hud...");
        registerHud();

        LOGGER.info("Successfully initialized 06ED's tools");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    private void registerModules() {
        Modules.get().add(new RadiusAutoWalk());
        Modules.get().add(new CevBreaker());
        Modules.get().add(new CrystalAuraPlus());
        Modules.get().add(new SurroundPlus());
        Modules.get().add(new FastThrowXP());
        Modules.get().add(new InstaMinePlus());
        Modules.get().add(new DiscordRPC());
        Modules.get().add(new Prefix());
        Modules.get().add(new AnchorAuraPlus());
        Modules.get().add(new ArmorNotifier());
        Modules.get().add(new TntAura());
    }

    private void registerHud() {
        Hud.get().register(TargetHud.INFO);
    }

    @Override
    public String getPackage() {
        return "xyz.hsbestudio.tools";
    }
}
