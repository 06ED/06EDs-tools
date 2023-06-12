package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import xyz.hsbestudio.tools.DinnerTools;

public class AutoLog extends Module {
    enum Mode {
        TotemPops,
        HP,
        Durability
    }

    public AutoLog() {
        super(DinnerTools.CATEGORY, "auto-log", "Automatically log in different events");
    }

    @EventHandler
    private void onEntity(EntityAddedEvent event) {
        
    }
}
