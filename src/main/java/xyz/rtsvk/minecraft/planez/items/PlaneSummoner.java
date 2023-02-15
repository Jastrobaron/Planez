package xyz.rtsvk.minecraft.planez.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PlaneSummoner extends ItemStack {

	public PlaneSummoner() {
		super(Material.STICK);

		ItemMeta meta = this.getItemMeta();
		if (meta != null) {
			meta.displayName(Component.text("Plane Summoner"));
			meta.lore(Arrays.asList(
				Component.text("Use it to spawn your very own airplane!")
			));
		}
		this.setItemMeta(meta);
	}

	public static Recipe getRecipe() {
		ShapedRecipe rec = new ShapedRecipe(NamespacedKey.minecraft("yoke"), new PlaneSummoner());
		rec.shape("DSD", "IBI", "GNG");
		rec.setIngredient('D', Material.EMERALD);
		rec.setIngredient('G', Material.GOLD_INGOT);
		rec.setIngredient('S', Material.STICK);
		rec.setIngredient('B', Material.BLAZE_ROD);
		rec.setIngredient('I', Material.IRON_BLOCK);
		rec.setIngredient('N', Material.NETHER_STAR);
		return rec;
	}
}
