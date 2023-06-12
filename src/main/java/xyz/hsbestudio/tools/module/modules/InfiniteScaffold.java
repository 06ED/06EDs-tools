package xyz.hsbestudio.tools.module.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import xyz.hsbestudio.tools.DinnerTools;
import xyz.hsbestudio.tools.utils.WorldUtils;

public class InfiniteScaffold extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Range, from which can take powder snow.")
        .min(1)
        .max(7)
        .defaultValue(5)
        .build()
    );

    public InfiniteScaffold() {
        super(DinnerTools.CATEGORY, "infinite-scaffold", "Places and takes snow to walk infinite time.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        BlockPos roundedBlockPos = new BlockPos((int) Math.round(mc.player.getX()), (int) Math.round(mc.player.getY()), (int) Math.round(mc.player.getY()));
        findSnow(roundedBlockPos);

        FindItemResult snowBucket = InvUtils.findInHotbar(Items.POWDER_SNOW_BUCKET);
        if (!snowBucket.found()) {
            ChatUtils.error("You haven't powder snow bucket in your hotbar. Disabling...");
            toggle();
            return;
        }

        if (WorldUtils.getBlock(roundedBlockPos) != Blocks.AIR) return;
        BlockUtils.place(roundedBlockPos, snowBucket, true, 1000);
    }

    private void findSnow(BlockPos pos) {
        FindItemResult result = InvUtils.findInHotbar(Items.BUCKET);
        if (!result.found() || mc.player == null) return;

        BlockPos selectedBlock;
        for (int x = -range.get(); x <= range.get(); x++)
            for (int y = -range.get(); y <= range.get(); y++)
                for (int z = -range.get(); z <= range.get(); z++)
                    if (WorldUtils.getBlock(selectedBlock = pos.add(x, y, z)) == Blocks.POWDER_SNOW)
                        WorldUtils.interaction(selectedBlock, result, mc.player.getInventory().selectedSlot);
    }
}
