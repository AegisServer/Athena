package net.aegis.athena.utils;

import lombok.Data;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import net.minecraft.world.effect.MobEffect;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.potion.CraftPotionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.aegis.athena.utils.Nullables.isNullOrAir;

public class ItemUtils {

	public static ItemStack getTool(Player player, EquipmentSlot hand) {
		return player.getPlayer().getInventory().getItem(hand);
	}

	public static ItemStack getTool(Player player) {
		return getTool(player, (Material) null);
	}

	public static ItemStack getTool(Player player, Material material) {
		Player _player = player.getPlayer();
		ItemStack mainHand = _player.getInventory().getItemInMainHand();
		ItemStack offHand = _player.getInventory().getItemInOffHand();
		if (!isNullOrAir(mainHand) && (material == null || mainHand.getType() == material))
			return mainHand;
		else if (!isNullOrAir(offHand) && (material == null || offHand.getType() == material))
			return offHand;
		return null;
	}

	public static ItemStack getToolRequired(Player player) {
		ItemStack item = getTool(player);
		if (isNullOrAir(item))
			throw new InvalidInputException("You are not holding anything");
		return item;
	}

	public static EquipmentSlot getHandWithTool(Player player) {
		return getHandWithTool(player, null);
	}

	public static EquipmentSlot getHandWithTool(Player player, Material material) {
		Player _player = player.getPlayer();
		ItemStack mainHand = _player.getInventory().getItemInMainHand();
		ItemStack offHand = _player.getInventory().getItemInOffHand();
		if (!isNullOrAir(mainHand) && (material == null || mainHand.getType() == material))
			return EquipmentSlot.HAND;
		else if (!isNullOrAir(offHand) && (material == null || offHand.getType() == material))
			return EquipmentSlot.OFF_HAND;
		return null;
	}

	public static EquipmentSlot getHandWithToolRequired(Player player) {
		EquipmentSlot hand = getHandWithTool(player);
		if (hand == null)
			throw new InvalidInputException("You are not holding anything");
		return hand;
	}

	public static final Map<PotionEffectType, String> fixedPotionNames = Map.of(
			PotionEffectType.SLOW, "SLOWNESS",
			PotionEffectType.FAST_DIGGING, "HASTE",
			PotionEffectType.SLOW_DIGGING, "MINING_FATIGUE",
			PotionEffectType.INCREASE_DAMAGE, "STRENGTH",
			PotionEffectType.HEAL, "INSTANT_HEALTH",
			PotionEffectType.HARM, "INSTANT_DAMAGE",
			PotionEffectType.JUMP, "JUMP_BOOST",
			PotionEffectType.CONFUSION, "NAUSEA",
			PotionEffectType.DAMAGE_RESISTANCE, "RESISTANCE"
	);

	public static String getFixedPotionName(PotionEffectType effect) {
		return fixedPotionNames.getOrDefault(effect, effect.getName());
	}

//	@Data
//	public static class PotionWrapper {
//		private List<MobEffectInstance> effects = new ArrayList<>();
//
//		private PotionWrapper() {}
//
//		@NotNull
//		public static PotionWrapper of(ItemStack item) {
//			if (!(item.getItemMeta() instanceof PotionMeta potionMeta))
//				return new PotionWrapper();
//
//			return of(toNMS(potionMeta.getBasePotionData()), potionMeta.getCustomEffects());
//		}
//
//		@NotNull
//		public static PotionWrapper of(AreaEffectCloudApplyEvent event) {
//			final Potion potion = toNMS(event.getEntity().getBasePotionData());
//			final List<PotionEffect> customEffects = event.getEntity().getCustomEffects();
//			return of(potion, customEffects);
//		}

//		@NotNull
//		public static PotionWrapper of(Potion potion, List<PotionEffect> customEffects) {
//			final PotionWrapper wrapper = new PotionWrapper();
//			wrapper.getEffects().addAll(potion.getEffects());
//			wrapper.getEffects().addAll(customEffects.stream().map(PotionWrapper::toNMS).toList());
//			return wrapper;
//		}
//
//		public boolean hasNegativeEffects() {
//			return effects.stream().anyMatch(effect -> !effect.getEffect().isBeneficial());
//		}

//		public boolean hasOnlyBeneficialEffects() {
//			return !hasNegativeEffects();
//		}

//		public boolean isSimilar(ItemStack item) {
//			return isSimilar(PotionWrapper.of(item));
//		}
//
//		public boolean isSimilar(PotionWrapper wrapper) {
//			for (MobEffectInstance effect : effects)
//				if (!wrapper.getEffects().contains(effect))
//					return false;
//
//			for (MobEffectInstance effect : wrapper.getEffects())
//				if (!effects.contains(effect))
//					return false;
//
//			return true;
//		}

//		@NotNull
//		public static MobEffectInstance toNMS(PotionEffect effect) {
//			return new MobEffectInstance(toNMS(effect.getType()), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles());
//		}

//		@NotNull
//		public static MobEffect toNMS(PotionEffectType effect) {
//			final MobEffect nmsEffect = MobEffect.byId(effect.getId());
//			if (nmsEffect != null)
//				return nmsEffect;
//
//			throw new InvalidInputException("Unknown potion type " + effect);
//		}
//
//		@NotNull
//		public static Potion toNMS(PotionData basePotionData) {
//			return Registry.POTION.get(ResourceLocation.tryParse(CraftPotionUtil.fromBukkit(basePotionData)));
//		}
//	}

}
