package xyz.rtsvk.minecraft.planez.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.rtsvk.minecraft.planez.items.PlaneSummoner;

public class GibSummonerCmd implements CommandExecutor {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (sender instanceof Player) {
			Player plr = (Player) sender;
			if (!plr.isOp()) plr.sendMessage("You don't have the permission to do that.");
			else plr.getInventory().addItem(new PlaneSummoner());
		}
		else sender.sendMessage("you are not a player!");
		return true;
	}
}
