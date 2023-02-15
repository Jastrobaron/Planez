package xyz.rtsvk.minecraft.planez.events;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.rtsvk.minecraft.planez.ItemManager;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class PlaneControlEvent implements Listener {


	private Plugin plugin;
	private  Map<Material, Integer> fuelValues;
	private static final String PLANE_KEY = "plane";
	private static final String SHOOT_COOLDOWN_KEY = "cooldown";

	private static final String SPEED_FACTOR_KEY = "velocity-scalar";

	private double speedUpFactor;
	private int shootCooldownTicks;
	private double maxVelocity;

	public PlaneControlEvent(Plugin p) {
		this.plugin = p;
		this.speedUpFactor = p.getConfig().getDouble(SPEED_FACTOR_KEY);
		this.maxVelocity = p.getConfig().getDouble("max-velocity");
		this.shootCooldownTicks = p.getConfig().getInt("arrow-cooldown-ticks");

		try {
			File json = new File(this.plugin.getDataFolder(), "fuel.json");
			JSONObject obj = (JSONObject) (new JSONParser().parse(new FileReader(json)));

			this.fuelValues = new HashMap<>();
			obj.keySet().forEach(e ->
				this.fuelValues.put(
						Material.getMaterial(e.toString()),
						Integer.parseInt(String.valueOf(obj.get(e)))
				)
			);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onCtl(PlayerInteractEvent e) {

		Action a = e.getAction();
		if (a == Action.PHYSICAL) return;

		if (e.getItem() == null) return;
		if (!ItemManager.corresponds(e.getItem(), ItemManager.YOKE)) return;

		final Player plr = e.getPlayer();
		double speedFactor = this.speedUpFactor;

		if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
			Entity plane = plr.getVehicle();
			if (plane == null) return;
			if (!plane.hasMetadata(PLANE_KEY)) return;

			if (plr.getGameMode() != GameMode.CREATIVE) {
				PlayerInventory inv = plr.getInventory();
				for (ItemStack item : inv) {
					if (item == null) continue;
					if (item.getType() != Material.ARROW) continue;

					if (!plr.hasMetadata(SHOOT_COOLDOWN_KEY)) {
						item.setAmount(item.getAmount() - 1);
						plr.launchProjectile(Arrow.class, plr.getLocation().getDirection().multiply(5));
						plr.playSound(plr.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 1.0f);
						for (Entity nearbyEntity : plr.getNearbyEntities(20, 20, 20)) {
							if (nearbyEntity instanceof Player)
								((Player)nearbyEntity).playSound(plr.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 1.0f);
						}

						int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> plr.removeMetadata(SHOOT_COOLDOWN_KEY, this.plugin), this.shootCooldownTicks);
						plr.setMetadata(SHOOT_COOLDOWN_KEY, new FixedMetadataValue(this.plugin, taskid));
					}
				}
			}
			else {
				plr.launchProjectile(Arrow.class, plr.getLocation().getDirection().multiply(5));
				plr.playSound(plr.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 1.0f);
				for (Entity nearbyEntity : plr.getNearbyEntities(20, 20, 20)) {
					if (nearbyEntity instanceof Player)
						((Player)nearbyEntity).playSound(plr.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.0f);
				}
			}

		}

		else if (a == Action.RIGHT_CLICK_AIR) {
			Entity ent = plr.getVehicle();
			if (ent == null) return;
			if (!ent.hasMetadata(PLANE_KEY)) return;

			Minecart plane = (Minecart) ent;

			if (plr.getGameMode() != GameMode.CREATIVE) {
				int fuelAmt = plane.getMetadata(PLANE_KEY).get(0).asInt();

				// if the tank is empty, consume 1 unit of fuel
				fuelAmt = fuelAmt > 0 ? fuelAmt : consumeFuel(plr);

				if (fuelAmt > 0) {
					speedFactor = this.speedUpFactor;
					plane.setMetadata(PLANE_KEY, new FixedMetadataValue(this.plugin, fuelAmt-1));
					plane.setGravity(false);
				}
				else {
					speedFactor = 0.1;
					plane.setGravity(true);
				}
			}

			double speed = abs(plane.getVelocity());
			if (speed < this.maxVelocity) {
				Vector velocity = plr.getLocation().getDirection().multiply(speedFactor);
				plane.setVelocity(plane.getVelocity().add(velocity));

				if (speedFactor > 1) {
					plr.playSound(plr.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.0f);
					for (Entity nearbyEntity : plr.getNearbyEntities(20, 20, 20)) {
						if (nearbyEntity instanceof Player)
							((Player)nearbyEntity).playSound(plr.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.0f);
					}
				}
			}

		}

		// spawn the plane
		else if (a == Action.RIGHT_CLICK_BLOCK) {
			if (plr.getVehicle() != null) {
				plr.sendMessage(Component.text("You can't spawn airplanes while in some other vehicle!"));
				return;
			}

			Location spawnLoc = plr.getLocation();
			Minecart mc = spawnLoc.getWorld().spawn(spawnLoc, Minecart.class);

			mc.setMaxSpeed(100.0);
			mc.addPassenger(plr);
			mc.setMetadata(PLANE_KEY, new FixedMetadataValue(this.plugin, 0));  // plane's fuel amount
			mc.setGravity(false);
			mc.setFlyingVelocityMod(new Vector(0.95, 0.95, 0.95));
			mc.setInvulnerable(true);
		}
	}

	private double abs(Vector velocity) {
		double x = velocity.getX();
		double y = velocity.getY();
		double z = velocity.getZ();

		return Math.sqrt(x*x + y*y + z*z);
	}

	@EventHandler
	public void onLeave(VehicleExitEvent e) {
		Vehicle ent = e.getVehicle();
		if (!ent.hasMetadata(PLANE_KEY)) return;

		Player plr = (Player) e.getExited();
		Location loc = plr.getLocation();
		loc.setY(loc.getBlockY() + 2);
		plr.teleport(loc);
		ent.remove();
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;

		Player plr = (Player) e.getEntity();
		Entity vehicle = plr.getVehicle();
		if (vehicle == null) return;
		if (vehicle.hasMetadata(PLANE_KEY)) e.setCancelled(true);
	}

	// consumes fuel from player's offhand
	private int consumeFuel(Player plr) {
		PlayerInventory inv = plr.getInventory();
		ItemStack item = inv.getItemInOffHand();

		for (Material e : this.fuelValues.keySet()) {
			if (e != item.getType()) continue;
			item.setAmount(item.getAmount()-1);
			return this.fuelValues.get(e);
		}
		return 0;
	}
}
