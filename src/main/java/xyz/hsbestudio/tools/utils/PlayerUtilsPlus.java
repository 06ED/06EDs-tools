package xyz.hsbestudio.tools.utils;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PlayerUtilsPlus {
    public static double distanceFromEye(Entity entity) {
        double feet = distanceFromEye(entity.getX(), entity.getY(), entity.getZ());
        double head = distanceFromEye(entity.getX(), entity.getY() + entity.getHeight(), entity.getZ());
        return Math.min(head, feet);
    }

    public static void doPacketMine(BlockPos targetPos) {
        if (mc.player == null) return;

        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
        WrapUtils.swingHand(false);
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
    }

    public static double distanceFromEye(BlockPos blockPos) {
        return distanceFromEye(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double distanceFromEye(Vec3d vec3d) {
        return distanceFromEye(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static double distanceFromEye(double x, double y, double z) {
        double f = (mc.player.getX() - x);
        double g = (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - y);
        double h = (mc.player.getZ() - z);
        return Math.sqrt(f * f + g * g + h * h);
    }

    public static void togglingModules(Setting<Boolean> toggleModules, Setting<List<Module>> modules, ArrayList<Module> toActivate) {
        if (toggleModules.get() && !modules.get().isEmpty()) {
            for (Module module : modules.get()) {
                if (module.isActive()) {
                    module.toggle();
                    toActivate.add(module);
                }
            }
        }
    }

    public static boolean isTrapBlock(BlockPos pos) {
        return WorldUtils.getBlock(pos) == Blocks.OBSIDIAN || WorldUtils.getBlock(pos) == Blocks.ENDER_CHEST;
    }

    public static boolean isAnvilBlock(BlockPos pos) {
        return WorldUtils.getBlock(pos) == Blocks.ANVIL || WorldUtils.getBlock(pos) == Blocks.CHIPPED_ANVIL || WorldUtils.getBlock(pos) == Blocks.DAMAGED_ANVIL;
    }

    public static boolean isBurrowed(PlayerEntity p, boolean holeCheck) {
        BlockPos pos = p.getBlockPos();
        if (holeCheck && !WrapUtils.isInHole(p)) return false;
        return WorldUtils.getBlock(pos) == Blocks.ENDER_CHEST || WorldUtils.getBlock(pos) == Blocks.OBSIDIAN || isAnvilBlock(pos);
    }

    public static double[] directionSpeed(float speed) {
        if (mc.player == null) return new double[0];
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw);

        if (forward != 0.0F) {
            if (side > 0.0F) {
                yaw += ((forward > 0.0F) ? -45 : 45);
            } else if (side < 0.0F) {
                yaw += ((forward > 0.0F) ? 45 : -45);
            }

            side = 0.0F;

            if (forward > 0.0F) {
                forward = 1.0F;
            } else if (forward < 0.0F) {
                forward = -1.0F;
            }
        }

        double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        double cos = Math.cos(Math.toRadians(yaw + 90.0F));
        double dx = forward * speed * cos + side * speed * sin;
        double dz = forward * speed * sin - side * speed * cos;

        return new double[] { dx, dz };
    }

    public static Direction direction(float yaw){
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;

        if (yaw >= 315 || yaw < 45) return Direction.SOUTH;
        else if (yaw >= 45 && yaw < 135) return Direction.WEST;
        else if (yaw >= 135 && yaw < 225) return Direction.NORTH;
        else if (yaw >= 225 && yaw < 315) return Direction.EAST;

        return Direction.SOUTH;
    }
}
