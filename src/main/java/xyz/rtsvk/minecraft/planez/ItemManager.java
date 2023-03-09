package xyz.rtsvk.minecraft.planez;

import org.bukkit.inventory.ItemStack;
import xyz.rtsvk.minecraft.planez.items.PlaneSummoner;

import java.util.Objects;

public class ItemManager {

	public static ItemStack YOKE;

	public static void init() {
		YOKE = new PlaneSummoner();
	}

	public static boolean corresponds(ItemStack item1, ItemStack item2) {
		boolean nameMatches = item1.displayName().equals(item2.displayName());
		boolean item1lore = item1.getItemMeta().hasLore();
		boolean item2lore = item2.getItemMeta().hasLore();
		boolean loreMatches = item1lore && item2lore && Objects.equals(item1.lore(), item2.lore());
		return nameMatches && loreMatches;
	}
}
