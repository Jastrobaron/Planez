package xyz.rtsvk.minecraft.planez;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import xyz.rtsvk.minecraft.planez.items.PlaneSummoner;

public class ItemManager {

	public static ItemStack YOKE;

	public static void init() {
		YOKE = new PlaneSummoner();
	}

	public static boolean corresponds(ItemStack item1, ItemStack item2) {
		return item1.displayName().equals(item2.displayName());
	}
}
