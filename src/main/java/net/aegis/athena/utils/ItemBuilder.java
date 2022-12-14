package net.aegis.athena.utils;

import de.tr7zw.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;
import static net.aegis.athena.utils.StringUtils.colorize;

@SuppressWarnings({"UnusedReturnValue", "ResultOfMethodCallIgnored", "CopyConstructorMissesField", "deprecation"})
public class ItemBuilder implements Cloneable, Supplier<ItemStack> {
	private ItemStack itemStack;
	private ItemMeta itemMeta;
	@Getter
	private final List<String> lore = new ArrayList<>();
	private boolean doLoreize = true;
	private boolean update;

	public ItemBuilder(Material material) {
		this(new ItemStack(material));
	}

	public ItemBuilder(Material material, int amount) {
		this(new ItemStack(material, amount));
	}

	public ItemBuilder(ItemBuilder itemBuilder) {
		this(itemBuilder.build());
		this.doLoreize = itemBuilder.doLoreize;
	}

	public ItemBuilder(ItemStack itemStack) {
		this(itemStack, false);
	}

	public ItemBuilder(ItemStack itemStack, boolean update) {
		this.itemStack = update ? itemStack : itemStack.clone();
		this.itemMeta = itemStack.getItemMeta() == null ? null : itemStack.getItemMeta();
		if (itemMeta != null && itemMeta.getLore() != null)
			this.lore.addAll(itemMeta.getLore());
		this.update = update;
	}

	public ItemBuilder material(Material material) {
		itemStack.setType(material);
		itemMeta = itemStack.getItemMeta();
		return this;
	}

	public Material material() {
		return itemStack.getType();
	}

	public ItemBuilder amount(int amount) {
		itemStack.setAmount(amount);
		return this;
	}

	public ItemBuilder color(ColorType colorType) {
		final Material newMaterial = Objects.requireNonNull(colorType.switchColor(itemStack.getType()), "Could not determine color of " + itemStack.getType());
		itemStack.setType(newMaterial);
		return this;
	}

	@Deprecated
	public ItemBuilder durability(int durability) {
		return durability(Integer.valueOf(durability).shortValue());
	}

	@Deprecated
	public ItemBuilder durability(short durability) {
		itemStack.setDurability(durability);
		return this;
	}

	public ItemBuilder damage(int damage) {
		if (!(itemMeta instanceof Damageable damageable)) throw new UnsupportedOperationException("Cannot apply durability to non-damageable item");
		damageable.setDamage(damage);
		return this;
	}

	private static Component removeItalicIfUnset(Component component) {
		if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET)
			component = component.decoration(TextDecoration.ITALIC, false);
		return component;
	}

	private static List<Component> removeItalicIfUnset(ComponentLike... components) {
		return AdventureUtils.asComponentList(components).stream().map(ItemBuilder::removeItalicIfUnset).collect(Collectors.toList());
	}

	private static List<Component> removeItalicIfUnset(List<? extends ComponentLike> components) {
		return AdventureUtils.asComponentList(components).stream().map(ItemBuilder::removeItalicIfUnset).collect(Collectors.toList());
	}

	public String name() {
		return itemMeta.getDisplayName();
	}

	public ItemBuilder name(@Nullable String displayName) {
		if (displayName == null)
			itemMeta.setDisplayName(null);
		else
			itemMeta.setDisplayName(colorize("&f" + displayName));
		return this;
	}

	public ItemBuilder name(@Nullable ComponentLike componentLike) {
		if (componentLike != null)
			itemMeta.displayName(removeItalicIfUnset(componentLike.asComponent()));
		return this;
	}

	public ItemBuilder resetName() {
		return name((String) null);
	}

	@Deprecated
	public ItemBuilder resetLore() {
		this.lore.clear(); // TODO: doesn't work
		return this;
	}

	public ItemBuilder setLore(String... lore) {
		return setLore(List.of(lore));
	}

	public ItemBuilder setLore(List<String> lore) {
		this.lore.clear();
		if (lore != null)
			this.lore.addAll(lore);
		return this;
	}

	public ItemBuilder lore(String... lore) {
		return lore(Arrays.asList(lore));
	}

	public ItemBuilder lore(Collection<String> lore) {
		if (lore != null)
			this.lore.addAll(lore);
		return this;
	}

	public ItemBuilder lore(int line, String text) {
		while (lore.size() < line)
			lore.add("");

		lore.set(line - 1, colorize(text));
		return this;
	}

	public ItemBuilder loreRemove(int line) {
		if (isNullOrEmpty(lore)) throw new InvalidInputException("Item does not have lore");
		if (line - 1 > lore.size()) throw new InvalidInputException("Line " + line + " does not exist");

		lore.remove(line - 1);
		return this;
	}

	// overridden by all string lore
	public ItemBuilder componentLore(ComponentLike... components) {
		itemMeta.lore(removeItalicIfUnset(components));
		return this;
	}

	// overridden by all string lore
	public ItemBuilder componentLore(List<? extends ComponentLike> components) {
		itemMeta.lore(removeItalicIfUnset(components));
		return this;
	}

	public @NotNull List<Component> componentLore() {
		return itemMeta.hasLore() ? Objects.requireNonNull(itemMeta.lore()) : new ArrayList<>();
	}

	public ItemBuilder loreize(boolean doLoreize) {
		this.doLoreize = doLoreize;
		return this;
	}

	public ItemBuilder enchant(Enchantment enchantment) {
		return enchant(enchantment, 1);
	}

	public ItemBuilder enchant(Enchantment enchantment, int level) {
		return enchant(enchantment, level, true);
	}

	public ItemBuilder enchantMax(Enchantment enchantment) {
		return enchant(enchantment, enchantment.getMaxLevel(), true);
	}

	public ItemBuilder enchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
		if (itemStack.getType() == Material.ENCHANTED_BOOK)
			((EnchantmentStorageMeta) itemMeta).addStoredEnchant(enchantment, level, ignoreLevelRestriction);
		else
			itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction);

		return this;
	}

	public ItemBuilder enchantRemove(Enchantment enchantment) {
		itemMeta.removeEnchant(enchantment);
		return this;
	}

	public ItemBuilder enchants(ItemStack item) {
		if (item.getItemMeta() != null)
			item.getItemMeta().getEnchants().forEach((enchant, level) -> itemMeta.addEnchant(enchant, level, true));
		return this;
	}

	public ItemBuilder glow() {
		enchant(Enchantment.ARROW_INFINITE);
		itemFlags(ItemFlag.HIDE_ENCHANTS);
		return this;
	}

	public ItemBuilder glow(boolean glow) {
		return glow ? glow() : this;
	}

	public boolean isGlowing() {
		return itemMeta.hasEnchant(Enchantment.ARROW_INFINITE) && itemMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
	}

	public ItemBuilder unbreakable() {
		itemMeta.setUnbreakable(true);
		return this;
	}

	public ItemBuilder itemFlags(ItemFlag... flags) {
		itemMeta.addItemFlags(flags);
		return this;
	}

	public ItemBuilder itemFlags(List<ItemFlag> flags) {
		return itemFlags(flags.toArray(ItemFlag[]::new));
	}

	public ItemBuilder itemFlags(ItemFlags flags) {
		return itemFlags(flags.get());
	}

	@AllArgsConstructor
	public enum ItemFlags {
		HIDE_ALL(itemFlag -> itemFlag.name().startsWith("HIDE_")),
		;

		private final Predicate<ItemFlag> predicate;

		public List<ItemFlag> get() {
			return Arrays.stream(ItemFlag.values()).filter(predicate).toList();
		}
	}

	// Custom meta types

	// Leather armor

	public Color dyeColor() {
		if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta)
			return leatherArmorMeta.getColor();
		return null;
	}

	public ItemBuilder dyeColor(Color color) {
		if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta)
			leatherArmorMeta.setColor(color);
		return this;
	}


	public ItemBuilder dyeColor(String hex) {
		return dyeColor(ColorType.hexToBukkit(hex));
	}

	// Potions

	public ItemBuilder potionType(PotionType potionType) {
		return potionType(potionType, false, false);
	}

	public ItemBuilder potionType(PotionType potionType, boolean extended, boolean upgraded) {
		((PotionMeta) itemMeta).setBasePotionData(new PotionData(potionType, extended, upgraded));
		return this;
	}

	public ItemBuilder potionEffect(PotionEffect potionEffect) {
		((PotionMeta) itemMeta).addCustomEffect(potionEffect, true);
		return this;
	}

	public ItemBuilder potionEffectColor(Color color) {
		((PotionMeta) itemMeta).setColor(color);
		return this;
	}

	// Fireworks

	public ItemBuilder fireworkPower(int power) {
		((FireworkMeta) itemMeta).setPower(power);
		return this;
	}

	public ItemBuilder fireworkEffect(FireworkEffect... effect) {
		((FireworkMeta) itemMeta).addEffects(effect);
		return this;
	}

	@Deprecated
	public ItemBuilder skullOwner(String name) {
		((SkullMeta) itemMeta).setOwner(name);
		return this;
	}

	@Deprecated
	public ItemBuilder skullOwnerActual(OfflinePlayer offlinePlayer) {
		((SkullMeta) itemMeta).setOwningPlayer(offlinePlayer);
		return this;
	}

	public @Nullable OfflinePlayer skullOwner() {
		return ((SkullMeta) itemMeta).getOwningPlayer();
	}

	public @Nullable String skullOwnerName() {
		return ((SkullMeta) itemMeta).getOwner();
	}

	// Banners

	public ItemBuilder pattern(DyeColor color, PatternType pattern) {
		return pattern(new Pattern(color, pattern));
	}

	public ItemBuilder pattern(Pattern pattern) {
		BannerMeta bannerMeta = (BannerMeta) itemMeta;
		bannerMeta.addPattern(pattern);
		return this;
	}

	// Maps

	public ItemBuilder mapId(int id) {
		return mapId(id, null);
	}

	public ItemBuilder mapId(int id, MapRenderer renderer) {
		MapMeta mapMeta = (MapMeta) itemMeta;
		mapMeta.setMapId(id);
		MapView view = Bukkit.getServer().getMap(id);
		if (view == null) {
			Athena.log("View for map id " + id + " is null");
		} else if (renderer != null)
			view.addRenderer(renderer);
		mapMeta.setMapView(view);
		return this;
	}

	public int getMapId() {
		MapMeta mapMeta = (MapMeta) itemMeta;
		return mapMeta.getMapId();
	}

	public ItemBuilder createMapView(World world) {
		return createMapView(world, null);
	}

	public ItemBuilder createMapView(World world, MapRenderer renderer) {
		MapMeta mapMeta = (MapMeta) itemMeta;
		MapView view = Bukkit.getServer().createMap(world);
		if (renderer != null)
			view.addRenderer(renderer);
		mapMeta.setMapView(view);
		return this;
	}

	// Shulker Boxes

	public ItemBuilder shulkerBox(List<ItemStack> items) {
		return shulkerBox(items.toArray(ItemStack[]::new));
	}

	public ItemBuilder shulkerBox(ItemBuilder... builders) {
		return shulkerBox(Arrays.stream(builders).map(ItemBuilder::build).toList());
	}

	public ItemBuilder shulkerBox(ItemStack... items) {
		shulkerBox(box -> box.getInventory().setContents(items));
		return this;
	}

	public ItemBuilder shulkerBox(Consumer<ShulkerBox> consumer) {
		BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
		ShulkerBox box = (ShulkerBox) blockStateMeta.getBlockState();
		consumer.accept(box);
		blockStateMeta.setBlockState(box);
		return this;
	}

	public List<@Nullable ItemStack> shulkerBoxContents() {
		BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
		ShulkerBox box = (ShulkerBox) blockStateMeta.getBlockState();
		return Arrays.asList(box.getInventory().getContents());
	}

	public List<@Nullable ItemStack> nonAirShulkerBoxContents() {
		return shulkerBoxContents().stream().filter(Nullables::isNotNullOrAir).collect(Collectors.toList());
	}

	public ItemBuilder clearShulkerBox() {
		return shulkerBox(box -> box.getInventory().clear());
	}

	// Books

	public ItemBuilder bookTitle(String title) {
		return bookTitle(new JsonBuilder(title));
	}

	public ItemBuilder bookTitle(@Nullable ComponentLike title) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		if (title != null)
			bookMeta.title(title.asComponent());
		return this;
	}

	public ItemBuilder bookAuthor(String author) {
		return bookAuthor(new JsonBuilder(author));
	}

	public ItemBuilder bookAuthor(@Nullable ComponentLike author) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		if (author != null)
			bookMeta.author(author.asComponent());
		return this;
	}

	public ItemBuilder bookPage(int page, String content) {
		return bookPage(page, new JsonBuilder(content));
	}

	public ItemBuilder bookPage(int page, ComponentLike content) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		bookMeta.page(page, content.asComponent());
		return this;
	}

	public ItemBuilder bookPages(String... pages) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		bookMeta.addPages(Arrays.stream(pages).map(message -> new JsonBuilder(message).asComponent()).toArray(Component[]::new));
		return this;
	}

	public ItemBuilder bookPages(ComponentLike... pages) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		bookMeta.addPages(Arrays.stream(pages).map(ComponentLike::asComponent).toArray(Component[]::new));
		return this;
	}

	public ItemBuilder bookPages(List<Component> pages) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		bookMeta.pages(pages);
		return this;
	}

	public ItemBuilder bookPageRemove(int page) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		final List<Component> pages = new ArrayList<>(bookMeta.pages());
		pages.remove(page - 1);
		bookMeta.pages(pages);
		return this;
	}

	public ItemBuilder bookGeneration(Generation generation) {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		bookMeta.setGeneration(generation);
		return this;
	}

	public ItemBuilder bookMeta(BookMeta bookMeta) {
		itemMeta = bookMeta;
		return this;
	}

	public String getBookPlainContents() {
		final BookMeta bookMeta = (BookMeta) itemMeta;
		return bookMeta.pages().stream().map(AdventureUtils::asPlainText).collect(Collectors.joining(" "));
	}

	// Entities

	public ItemBuilder spawnEgg(EntityType entityType) {
		return material(Material.valueOf(entityType.getKey().getKey().toUpperCase() + "_SPAWN_EGG"));
	}


	public ItemBuilder attribute(Attribute attribute, AttributeModifier value) {
		itemMeta.addAttributeModifier(attribute, value);
		return this;
	}

	// Building //

	@Override
	public ItemStack get() {
		return build();
	}

	public ItemStack build() {
		if (update) {
			buildLore();
			if (itemMeta != null)
				itemStack.setItemMeta(itemMeta);
			return itemStack;
		} else {
			ItemStack result = itemStack.clone();
			buildLore();
			if (itemMeta != null)
				result.setItemMeta(itemMeta);
			return result;
		}
	}

	public void buildLore() {
		if (lore.isEmpty())
			return; // don't override Component lore
		lore.removeIf(Objects::isNull);
		List<String> colorized = new ArrayList<>();
		for (String line : lore)
			if (doLoreize)
				colorized.addAll(StringUtils.loreize(colorize(line)));
			else
				colorized.add(colorize(line));
		itemMeta.setLore(colorized);

		itemStack.setItemMeta(itemMeta);
		itemMeta = itemStack.getItemMeta();
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public ItemBuilder clone() {
		itemStack.setItemMeta(itemMeta);
		ItemBuilder builder = new ItemBuilder(itemStack.clone());
		builder.lore(lore);
		builder.loreize(doLoreize);
		return builder;
	}

	/** Static helpers */

	public static ItemBuilder oneOf(ItemStack item) {
		return new ItemBuilder(item).amount(1);
	}

	public static ItemStack setName(ItemStack item, String name) {
		return new ItemBuilder(item, true).name(name).build();
	}

	public static ItemStack setDurability(ItemStack item, double percentage) {
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof Damageable damageable) {
			double maxDurability = item.getType().getMaxDurability();
			double damage = (percentage / 100.0) * maxDurability;
			damageable.setDamage((int) damage);

			item.setItemMeta((ItemMeta) damageable);
		}

		return item;
	}

	public ItemBuilder nbt(Consumer<NBTItem> consumer) {
		final NBTItem nbtItem = nbtItem();
		consumer.accept(nbtItem);
		itemStack = nbtItem.getItem();
		itemMeta = itemStack.getItemMeta();
		return this;
	}

	@NotNull
	private NBTItem nbtItem() {
		return new NBTItem(build());
	}

	@AllArgsConstructor
	public enum ItemSetting {
		/**
		 * Whether an item can be dropped
		 */
		DROPPABLE(true),
		/**
		 * Whether an item can be placed in an item frame
		 */
		FRAMEABLE(true),
		/**
		 * Whether an item can be placed
		 */
		PLACEABLE(true),
		/**
		 * Whether an item can be stored in containers
		 */
		STORABLE(true),
		/**
		 * Whether an item can be put in the {@code /trash}
		 */
		TRASHABLE(true),
		//		/**
//		 * Whether an item can be sold in shops
//		 */
//		TRADEABLE(true) {
//			@Override
//			public boolean of(ItemBuilder builder, boolean orDefault) {
//				if (Backpacks.isBackpack(builder.build()))
//					return false;
//
//				return super.of(builder, orDefault);
//			}
//		},
		MCMMOABLE(true),
		;

		private final boolean orDefault;

		public String getKey() {
			return name().toLowerCase();
		}

		public boolean of(ItemBuilder builder, boolean orDefault) {
			NBTItem item = builder.nbtItem();
			if (!item.hasKey(getKey()))
				return orDefault;

			return item.getBoolean(getKey());
		}

		public final boolean of(ItemBuilder builder) {
			return of(builder, orDefault);
		}
	}

	public ItemBuilder setting(ItemSetting setting, boolean value) {
		return nbt(nbt -> nbt.setBoolean(setting.getKey(), value));
	}

	public ItemBuilder unset(ItemSetting setting) {
		return nbt(nbt -> nbt.removeKey(setting.getKey()));
	}

	public boolean is(ItemSetting setting) {
		return setting.of(this);
	}

	public boolean isNot(ItemSetting setting) {
		return !is(setting);
	}

	public boolean is(ItemSetting setting, boolean orDefault) {
		return setting.of(this, orDefault);
	}

	public ItemBuilder undroppable() {
		return setting(ItemSetting.DROPPABLE, false);
	}

	public ItemBuilder unframeable() {
		return setting(ItemSetting.FRAMEABLE, false);
	}

	public ItemBuilder unplaceable() {
		return setting(ItemSetting.PLACEABLE, false);
	}

	public ItemBuilder unstorable() {
		return setting(ItemSetting.STORABLE, false);
	}

	public ItemBuilder untrashable() {
		return setting(ItemSetting.TRASHABLE, false);
	}

//	public ItemBuilder untradeable() {
//		return setting(ItemSetting.TRADEABLE, false);
//	}

//	public ItemBuilder modelId(int id) {
//		if (id > 0)
//			nbt(item -> item.setInteger(CustomModel.NBT_KEY, id));
//		return this;
//	}
//
//	public int modelId() {
//		NBTItem nbtItem = nbtItem();
//		final Integer modelId = nbtItem.getInteger(CustomModel.NBT_KEY);
//		return modelId == null ? 0 : modelId;
//	}

//	public static class ModelId {
//
//		public static int of(ItemStack item) {
//			if (isNullOrAir(item))
//				return 0;
//
//			return of(new ItemBuilder(item));
//		}
//
//		public static int of(ItemBuilder item) {
//			return item.modelId();
//		}
//
//		public static boolean hasModelId(ItemStack item) {
//			return of(item) != 0;
//		}
//
//		public static boolean hasModelId(ItemBuilder item) {
//			return of(item) != 0;
//		}
//
//	}

}