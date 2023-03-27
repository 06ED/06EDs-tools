package xyz.hsbestudio.tools.utils;

import meteordevelopment.meteorclient.systems.modules.Modules;
import xyz.hsbestudio.tools.module.modules.CevBreaker;
import xyz.hsbestudio.tools.module.modules.CrystalAuraPlus;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CrystalUtils {
    static CrystalAuraPlus crystalAuraPlus = Modules.get().get(CrystalAuraPlus.class);

    public static int getPlaceDelay() {
        if (isBurrowBreaking()) return crystalAuraPlus.burrowBreakDelay.get();
        else if (isSurroundBreaking()) return crystalAuraPlus.surroundBreakDelay.get();
        else return crystalAuraPlus.placeDelay.get();
    }

    public static void attackCrystal(Entity entity) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        // Attack
        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));

        if (crystalAuraPlus.renderSwing.get()) mc.player.swingHand(Hand.MAIN_HAND);
        if (!crystalAuraPlus.hideSwings.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        crystalAuraPlus.attacks++;

        getBreakDelay();

        if (crystalAuraPlus.debug.get()) crystalAuraPlus.warning("Breaking");
    }

    // Damage Ignores

    public static boolean targetJustPopped() {
        if (crystalAuraPlus.targetPopInvincibility.get()) {
            return !crystalAuraPlus.targetPoppedTimer.passedMillis(crystalAuraPlus.targetPopInvincibilityTime.get());
        }

        return false;
    }

    public static boolean shouldIgnoreSelfPlaceDamage() {
        return (crystalAuraPlus.PDamageIgnore.get() == CrystalAuraPlus.DamageIgnore.Always
            || (crystalAuraPlus.PDamageIgnore.get() == CrystalAuraPlus.DamageIgnore.WhileSafe && (EntityUtilsPlus.isSurrounded(mc.player, EntityUtilsPlus.BlastResistantType.Any) || EntityUtilsPlus.isBurrowed(mc.player, EntityUtilsPlus.BlastResistantType.Any)))
            || (crystalAuraPlus.selfPopInvincibility.get() && crystalAuraPlus.selfPopIgnore.get() != CrystalAuraPlus.SelfPopIgnore.Break && !crystalAuraPlus.selfPoppedTimer.passedMillis(crystalAuraPlus.selfPopInvincibilityTime.get())));
    }

    public static boolean shouldIgnoreSelfBreakDamage() {
        return (crystalAuraPlus.BDamageIgnore.get() == CrystalAuraPlus.DamageIgnore.Always
            || (crystalAuraPlus.BDamageIgnore.get() == CrystalAuraPlus.DamageIgnore.WhileSafe && (EntityUtilsPlus.isSurrounded(mc.player, EntityUtilsPlus.BlastResistantType.Any) || EntityUtilsPlus.isBurrowed(mc.player, EntityUtilsPlus.BlastResistantType.Any)))
            || (crystalAuraPlus.selfPopInvincibility.get() && crystalAuraPlus.selfPopIgnore.get() != CrystalAuraPlus.SelfPopIgnore.Place && !crystalAuraPlus.selfPoppedTimer.passedMillis(crystalAuraPlus.selfPopInvincibilityTime.get())));
    }

    private static void getBreakDelay() {
        if (isSurroundHolding() && crystalAuraPlus.surroundHoldMode.get() != CrystalAuraPlus.SlowMode.Age) {
            crystalAuraPlus.breakTimer = crystalAuraPlus.surroundHoldDelay.get();
        } else if (crystalAuraPlus.slowFacePlace.get() && crystalAuraPlus.slowFPMode.get() != CrystalAuraPlus.SlowMode.Age && isFacePlacing() && crystalAuraPlus.bestTarget != null && crystalAuraPlus.bestTarget.getY() < crystalAuraPlus.placingCrystalBlockPos.getY()) {
            crystalAuraPlus.breakTimer = crystalAuraPlus.slowFPDelay.get();
        } else crystalAuraPlus.breakTimer = crystalAuraPlus.breakDelay.get();
    }

    // Face Place
    public static boolean shouldFacePlace(BlockPos crystal) {
        // Checks if the provided crystal position should face place to any target
        for (PlayerEntity target : crystalAuraPlus.targets) {
            BlockPos pos = target.getBlockPos();
            if (crystalAuraPlus.CevPause.get() && Modules.get().isActive(CevBreaker.class)) return false;
            if (crystalAuraPlus.KAPause.get() && (Modules.get().isActive(KillAura.class))) return false;
            if (EntityUtilsPlus.isFaceTrapped(target, EntityUtilsPlus.BlastResistantType.Any)) return false;
            if (crystalAuraPlus.surrHoldPause.get() && isSurroundHolding()) return false;

            if (crystal.getY() == pos.getY() + 1 && Math.abs(pos.getX() - crystal.getX()) <= 1 && Math.abs(pos.getZ() - crystal.getZ()) <= 1) {
                if (EntityUtils.getTotalHealth(target) <= crystalAuraPlus.facePlaceHealth.get()) return true;

                for (ItemStack itemStack : target.getArmorItems()) {
                    if (itemStack == null || itemStack.isEmpty()) {
                        if (crystalAuraPlus.facePlaceArmor.get()) return true;
                    }
                    else {
                        if ((float) (itemStack.getMaxDamage() - itemStack.getDamage()) / itemStack.getMaxDamage() * 100 <= crystalAuraPlus.facePlaceDurability.get()) return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isFacePlacing() {
        return (crystalAuraPlus.facePlace.get() || crystalAuraPlus.forceFacePlace.get().isPressed());
    }

    // Burrow Break

    public static boolean shouldBurrowBreak(BlockPos crystal) {
        BlockPos pos = crystalAuraPlus.bestTarget.getBlockPos();

        if (!isBurrowBreaking()) return false;

        return ((crystal.getY() == pos.getY() - 1 || crystal.getY() == pos.getY()) && Math.abs(pos.getX() - crystal.getX()) <= 1 && Math.abs(pos.getZ() - crystal.getZ()) <= 1);
    }

    public static boolean isBurrowBreaking() {
        if (crystalAuraPlus.burrowBreak.get() || crystalAuraPlus.forceBurrowBreak.get().isPressed()) {
            if (crystalAuraPlus.bestTarget != null && EntityUtilsPlus.isBurrowed(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Mineable)) {
                switch (crystalAuraPlus.burrowBWhen.get()) {
                    case BothTrapped -> {
                        return EntityUtilsPlus.isBothTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case AnyTrapped -> {
                        return EntityUtilsPlus.isAnyTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case TopTrapped -> {
                        return EntityUtilsPlus.isTopTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case FaceTrapped -> {
                        return EntityUtilsPlus.isFaceTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case Always -> {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    // Surround Break
    public static boolean shouldSurroundBreak(BlockPos crystal) {
        BlockPos pos = crystalAuraPlus.bestTarget.getBlockPos();

        // Checking right criteria
        if (!isSurroundBreaking()) return false;

        // Checking valid crystal position
        return
            (EntityUtilsPlus.isBedrock(pos.north(1))
                && (crystal.equals(pos.north(2))
                || (crystalAuraPlus.surroundBHorse.get() && (crystal.equals(pos.north(2).west()) || crystal.equals(pos.north(2).east())))
                || (crystalAuraPlus.surroundBDiagonal.get() && (crystal.equals(pos.north().west()) || crystal.equals(pos.north().east())))
            ))

                || (EntityUtilsPlus.isBedrock(pos.south(1))
                && (crystal.equals(pos.south(2))
                || (crystalAuraPlus.surroundBHorse.get() && (crystal.equals(pos.south(2).west()) || crystal.equals(pos.south(2).east())))
                || (crystalAuraPlus.surroundBDiagonal.get() && (crystal.equals(pos.south().west()) || crystal.equals(pos.south().east())))
            ))

                || (EntityUtilsPlus.isBedrock(pos.west(1))
                && (crystal.equals(pos.west(2))
                || (crystalAuraPlus.surroundBHorse.get() && (crystal.equals(pos.west(2).north()) || crystal.equals(pos.west(2).south())))
                || (crystalAuraPlus.surroundBDiagonal.get() && (crystal.equals(pos.west().north()) || crystal.equals(pos.west().south())))
            ))

                || (EntityUtilsPlus.isBedrock(pos.east(1))
                && (crystal.equals(pos.east(2))
                || (crystalAuraPlus.surroundBHorse.get() && (crystal.equals(pos.east(2).north()) || crystal.equals(pos.east(2).south())))
                || (crystalAuraPlus.surroundBDiagonal.get() && (crystal.equals(pos.east().north()) || crystal.equals(pos.east().south())))
            ));
    }

    public static boolean isSurroundBreaking() {
        if (crystalAuraPlus.surroundBreak.get() || crystalAuraPlus.forceSurroundBreak.get().isPressed()) {
            if (crystalAuraPlus.bestTarget != null && EntityUtilsPlus.isSurrounded(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Mineable)) {
                switch (crystalAuraPlus.surroundBWhen.get()) {
                    case BothTrapped -> {
                        return EntityUtilsPlus.isBothTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case AnyTrapped -> {
                        return EntityUtilsPlus.isAnyTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case TopTrapped -> {
                        return EntityUtilsPlus.isTopTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case FaceTrapped -> {
                        return EntityUtilsPlus.isFaceTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                    }
                    case Always -> {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isSurroundHolding() {
        if (crystalAuraPlus.surroundHold.get() && crystalAuraPlus.bestTarget != null && !EntityUtilsPlus.isSurrounded(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any)) {
            switch (crystalAuraPlus.surroundHWhen.get()) {
                case BothTrapped -> {
                    return EntityUtilsPlus.isBothTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                }
                case AnyTrapped -> {
                    return EntityUtilsPlus.isAnyTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                }
                case TopTrapped -> {
                    return EntityUtilsPlus.isTopTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                }
                case FaceTrapped -> {
                    return EntityUtilsPlus.isFaceTrapped(crystalAuraPlus.bestTarget, EntityUtilsPlus.BlastResistantType.Any);
                }
                case Always -> {
                    return true;
                }
            }
        }

        return false;
    }
}
