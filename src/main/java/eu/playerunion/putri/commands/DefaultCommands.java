package eu.playerunion.putri.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.omg.CosNaming._BindingIteratorImplBase;

import eu.playerunion.putri.Main;
import eu.playerunion.putri.workers.ServerWorkers;

public class DefaultCommands implements CommandExecutor, TabExecutor {
	
	private Main plugin = Main.getInstance();
	
	private ServerWorkers serverWorkers = this.plugin.getServerWorkers();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("ping")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			
			p.sendMessage("§f» §2Jelenlegi pinged: §e" + p.getPing());
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("back")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			
			if(this.serverWorkers.getSavedLocation(p) == null) {
				sender.sendMessage("§cNincs elmentett előző helyed!");
				
				return true;
			}
			
			Location loc = this.serverWorkers.getSavedLocation(p);
			
			p.sendMessage("§bVisszatérés az előző helyszínre...");
			p.teleport(loc);
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("home")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			String prefix = "§aOtthon §f» ";
			UUID uuid = p.getUniqueId();
			
			if(!this.serverWorkers.getHomes().containsKey(uuid)) {
				p.sendMessage(prefix + "§2Jelenleg nincs egyetlen otthonod sem!");
				
				return true;
			}
			
			HashMap<String, Location> homeList = this.serverWorkers.getHomes().get(uuid);
			
			if(homeList.size() == 1) {
				String name = null;
				
				for(String homeName : homeList.keySet()) {
					name = homeName;
					
					break;
				}
				
				Location home = homeList.get(name);
				
				p.teleport(home);
				
				if(p.getVehicle() != null) {
					if(p.getVehicle() instanceof Horse || p.getVehicle() instanceof Donkey || p.getVehicle() instanceof Mule) {
						Entity horse = p.getVehicle();
						
						p.leaveVehicle();
						horse.teleport(home);
						horse.addPassenger(p);
					}
				}
				
				p.sendMessage(prefix + "§2Haza teleportáltál!");
				
				return true;
			}
			
			if(args.length == 0) {
				String name = "home";
				
				Location home = homeList.get(name);
				
				p.teleport(home);
				p.getLocation().setPitch(home.getPitch());
				p.getLocation().setY(home.getYaw());
				
				if(p.getVehicle() != null) {
					if(p.getVehicle() instanceof Horse || p.getVehicle() instanceof Donkey || p.getVehicle() instanceof Mule) {
						Entity horse = p.getVehicle();
						
						p.leaveVehicle();
						horse.teleport(home);
						horse.addPassenger(p);
					}
				}
				
				p.sendMessage(prefix + "§2Haza teleportáltál!");
				
				return true;
			}
			
			if(args.length == 1) {
				String homeName = args[0];
				
				if(!homeList.containsKey(homeName)) {
					p.sendMessage(prefix + "§2Ismeretlen otthont adtál meg!");
					
					return true;
				}
				
				Location home = homeList.get(homeName);
				
				p.teleport(home);
				
				if(p.getVehicle() != null) {
					if(p.getVehicle() instanceof Horse || p.getVehicle() instanceof Donkey || p.getVehicle() instanceof Mule) {
						Entity horse = p.getVehicle();
						
						p.leaveVehicle();
						horse.teleport(home);
						horse.addPassenger(p);
					}
				}
				
				p.sendMessage(prefix + "§2Sikeresen elteleportáltál a(z) §e" + homeName + " §2nevû otthonodhoz!");
				
				return true;
			}
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("sethome")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			String prefix = "§aOtthon §f» ";
			UUID uuid = p.getUniqueId();
			Location loc = p.getLocation();
			
			loc.add(0.5, 0.0, 0.5);
			
			if(args.length == 0) {
				HashMap<String, Location> home = null;
				
				if(this.serverWorkers.getHomes().containsKey(uuid))
					 home = this.serverWorkers.getHomes().get(uuid);
				else
					home = new HashMap<String, Location>();
				
				home.put("home", loc);
				this.serverWorkers.getHomes().put(uuid, home);
				p.sendMessage(prefix + "§2Sikeresen elmentetted az otthonodat!");
				
				this.serverWorkers.saveHomes();
				
				return true;
			}
			
			if(args.length == 1) {
				String homeName = args[0];
				HashMap<String, Location> home = null;
				
				if(this.serverWorkers.getHomes().containsKey(uuid))
					 home = this.serverWorkers.getHomes().get(uuid);
				else
					home = new HashMap<String, Location>();
				
				home.put(homeName, loc);
				this.serverWorkers.getHomes().put(uuid, home);
				
				p.sendMessage(prefix + "§2Sikeresen elmentetted a(z) §e" + homeName + " §2nevû otthonodat!");
				
				this.serverWorkers.saveHomes();
				
				return true;
			}
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("homes")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			String prefix = "§aOtthon §f» ";
			UUID uuid = p.getUniqueId();
			
			if(!this.serverWorkers.getHomes().containsKey(uuid)) {
				p.sendMessage(prefix + "§2Jelenleg nincs egyetlen otthonod sem!");
				
				return true;
			}
			
			HashMap<String, Location> homeList = this.serverWorkers.getHomes().get(uuid);
			
			p.sendMessage(prefix + "§2Elérhetõ otthonaid:");
			
			homeList.keySet().forEach(home -> p.sendMessage("§e- §2" + home));
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("spawn")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			ConfigurationSection spawnSection = this.plugin.getConfig().getConfigurationSection("spawn");
			Location spawn = new Location(
					Bukkit.getWorld(spawnSection.getString("world")),
					spawnSection.getDouble("x"),
					spawnSection.getDouble("y"),
					spawnSection.getDouble("z"),
					Float.parseFloat(String.valueOf(spawnSection.get("pitch"))),
					Float.parseFloat(String.valueOf(spawnSection.get("yaw"))));
			
			if(p.getVehicle() != null) {
				if(p.getVehicle() instanceof Horse || p.getVehicle() instanceof Donkey || p.getVehicle() instanceof Mule) {
					Entity horse = p.getVehicle();
					
					p.leaveVehicle();
					horse.teleport(spawn);
					horse.addPassenger(p);
				}
			}
			
			p.teleport(spawn);
			p.sendMessage("§dA varázslat elrepített oda, ahová szeretnéd!");
			
			p.getLocation().setPitch(spawn.getPitch());
			p.getLocation().setYaw(spawn.getYaw());
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("delhome")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			UUID uuid = p.getUniqueId();
			ConfigurationSection homes = this.plugin.getConfig().getConfigurationSection("homes." + uuid.toString());
			
			if(args.length == 0) {
				p.sendMessage("§cKérlek, adj meg egy otthont!");
				
				return true;
			}
			
			if(args.length == 1) {
				String home = args[0];
				
				if(!homes.getKeys(false).contains(home)) {
					p.sendMessage("§cIsmeretlen otthon!");
					
					return true;
				}
				
				homes.set(home, null);
				
				this.serverWorkers.getHomes().get(p.getUniqueId()).remove(home);
				this.plugin.saveConfig();
				
				p.sendMessage("§aOtthon §f» §2Sikersen törölted a(z) §e" + home + " §2otthonodat!");
				
				return true;
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("tptoggle")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cA parancsot csak játékos használhatja!");
				
				return true;
			}
			
			Player p = (Player) sender;
			ConfigurationSection tpSection = this.plugin.getConfig().getConfigurationSection("tptoggle");
			
			if(!tpSection.contains(p.getUniqueId().toString())) {
				tpSection.set(p.getUniqueId().toString(), true);
				
				p.sendMessage("§e§lTP §7§l| §cDeaktiváltad §fa teleportálást!");
				
				this.plugin.saveConfig();
				
				return true;
			}
			
			boolean enabled = tpSection.getBoolean(p.getUniqueId().toString());
			
			if(!enabled) {
				tpSection.set(p.getUniqueId().toString(), true);
				
				p.sendMessage("§e§lTP §7§l| §cDeaktiváltad §fa teleportálást!");
				
				this.plugin.saveConfig();
				
				return true;
			}
			
			tpSection.set(p.getUniqueId().toString(), false);
			
			p.sendMessage("§e§lTP §7§l| §aAktiváltad §fa teleportálást!");
			
			this.plugin.saveConfig();
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("tp")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cEzt a parancsot csak játékos használhatja!");
				
				return true;
			}
			
			if(args.length == 0 || args.length > 1)
				return false;
			
			String target = args[0];
			
			if(Bukkit.getPlayer(target) == null) {
				sender.sendMessage("§cIsmeretlen játékos!");
				
				return true;
			}
			
			Player p = ((Player) sender);
			Player targeted = Bukkit.getPlayer(target);
			
			ConfigurationSection tpSection = this.plugin.getConfig().getConfigurationSection("tptoggle");
			
			if(tpSection.contains(targeted.getUniqueId().toString()) && tpSection.getBoolean(targeted.getUniqueId().toString()) == true) {
				p.sendMessage("§c" + targeted.getName() + " letiltotta, hogy ráteleportálhassanak.");
				
				return true;
			}
			
			p.teleport(targeted);
			
			return true;
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			
			if(cmd.getName().equalsIgnoreCase("home")) {
				if(args.length == 1) {
					ArrayList<String> homes = new ArrayList<String>();
					
					if(this.serverWorkers.getHomes().containsKey(p.getUniqueId()))
						this.serverWorkers.getHomes().get(p.getUniqueId()).keySet().forEach(home -> homes.add(home));
					
					return homes;
				}
			}
			
			if(cmd.getName().equalsIgnoreCase("delhome")) {
				if(args.length == 1) {
					ArrayList<String> homes = new ArrayList<String>();
					
					if(this.serverWorkers.getHomes().containsKey(p.getUniqueId()))
						this.serverWorkers.getHomes().get(p.getUniqueId()).keySet().forEach(home -> homes.add(home));
					
					return homes;
				}
			}
			
			if(cmd.getName().equalsIgnoreCase("tp")) {
				ArrayList<String> targets = new ArrayList<String>();
				
				Bukkit.getOnlinePlayers().forEach(target -> targets.add(target.getName()));
				
				return targets;
			}
			
			if(cmd.getName().equalsIgnoreCase("merrevan")) {
				ArrayList<String> targets = new ArrayList<String>();
				
				Bukkit.getOnlinePlayers().forEach(target -> targets.add(target.getName()));
				
				return targets;
			}
		}
		
		return null;
	}

}
