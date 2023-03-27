package xyz.hsbestudio.tools;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import xyz.hsbestudio.tools.module.modules.*;

public class DinnerTools extends MeteorAddon {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("06ED's tools");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing 06ED's tools...");

        LOGGER.info("Initializing modules...");
        // Modules
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
        LOGGER.info("Successfully initialized 06ED's tools");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "xyz.hsbestudio.tools";
    }
}
