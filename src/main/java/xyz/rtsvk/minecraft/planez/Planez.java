package xyz.rtsvk.minecraft.planez;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.rtsvk.minecraft.planez.commands.GibSummonerCmd;
import xyz.rtsvk.minecraft.planez.events.PlaneControlEvent;
import xyz.rtsvk.minecraft.planez.items.PlaneSummoner;

import java.io.File;

public final class Planez extends JavaPlugin {

	@Override
	public void onEnable() {
		// Plugin startup login
		getConfig().options().copyDefaults();
		saveDefaultConfig();
		createCustomConfig("fuel.json");

		getServer().getPluginManager().registerEvents(new PlaneControlEvent(this), this);

		ItemManager.init();
		getServer().addRecipe(PlaneSummoner.getRecipe());

		getCommand("planesummoner").setExecutor(new GibSummonerCmd());
	}

	@Override
	public void onDisable() {

	}

	private void createCustomConfig(String file) {
		File customConfigFile = new File(getDataFolder(), file);
		if (!customConfigFile.exists()) {
			customConfigFile.getParentFile().mkdirs();
			saveResource(file, false);
		}
	}
}
