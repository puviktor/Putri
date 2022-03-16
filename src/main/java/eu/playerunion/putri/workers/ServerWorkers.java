package eu.playerunion.putri.workers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import eu.playerunion.putri.Main;

public class ServerWorkers {
	
	private Main plugin = Main.getInstance();
	
	private HashMap<UUID, HashMap<String, Location>> homes = new HashMap<UUID, HashMap<String, Location>>();
	private HashMap<UUID, Location> previousLocations = new HashMap<UUID, Location>();
	
	public void setupMotd() {
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(PacketAdapter.params(
						this.plugin, PacketType.Status.Server.SERVER_INFO)
						.serverSide()
						.gamePhase(GamePhase.BOTH)
						.listenerPriority(ListenerPriority.HIGHEST)
						.optionAsync()) {
					
					@Override
					public void onPacketSending(PacketEvent event) {
						WrappedServerPing ping = event.getPacket().getServerPings().getValues().get(0);
						String motd = "§6§lPlayer§f§lUnion §7§l- §e§lPrivát Survival §71.18.1\n§7Belépést kérj Viktortól Discordon!";
						
						ping.setMotD(motd);
						
						event.getPacket().getServerPings().getValues().set(0, ping);
					}
				});
	}
	
	public void loadHomes() {
		if(this.plugin.getConfig().contains("homes")) {
			ConfigurationSection confSec = this.plugin.getConfig().getConfigurationSection("homes");
			
			confSec.getKeys(false).forEach(s -> this.loadHome(UUID.fromString(s)));
		}
	}
	
	public void loadHome(UUID uuid) {
	    ConfigurationSection homes = this.plugin.getConfig().getConfigurationSection("homes." + uuid.toString());
	    
	    if (homes != null) {
	    	HashMap<String, Location> homeLocation = new HashMap<String, Location>();
	    	
	    	for (String homeName : homes.getKeys(false)) {
	    		ConfigurationSection home = homes.getConfigurationSection(homeName);
	    		String world = home.getString("world", null);
	    		double x = home.getInt("x") + 0.5D;
	    		double y = home.getInt("y");
	    		double z = home.getInt("z") + 0.5D;
	    		float pitch = Float.parseFloat(String.valueOf(home.get("pitch")));
	    		float yaw = Float.parseFloat(String.valueOf(home.get("yaw")));
	    		
	    		Location loc = new Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
	    		
	    		if (world != null && x != Integer.MIN_VALUE && y != Integer.MIN_VALUE && z != Integer.MIN_VALUE) {
	    			homeLocation.put(homeName.toLowerCase(), loc);
	    			
	    			continue;
	    		} 
	    	}
	    
	    	this.homes.put(uuid, homeLocation);
	    }
	}
	
	public void saveHome(UUID uuid, String name, Location loc) {
		ConfigurationSection confSec = this.plugin.getConfig().getConfigurationSection("homes." + uuid.toString() + "." + name);
		
		if(confSec == null)
			confSec = this.plugin.getConfig().createSection("homes." + uuid + "." + name);
		
		confSec.set("world", loc.getWorld().getName());
		confSec.set("x", loc.getBlockX());
		confSec.set("y", loc.getBlockY());
		confSec.set("z", loc.getBlockZ());
		confSec.set("pitch", loc.getPitch());
		confSec.set("yaw", loc.getYaw());
		
		this.plugin.saveConfig();
	}
	
	public void saveHomes() {
		for(UUID uuid : this.homes.keySet()) {
			HashMap<String, Location> home = this.homes.get(uuid);
			
			for(String s : home.keySet())
				this.saveHome(uuid, s, home.get(s));
		}
	}
	
	public void saveLocation(Player p) {
		if(!this.previousLocations.containsKey(p.getUniqueId()))
			this.previousLocations.put(p.getUniqueId(), p.getLocation());
		
		this.previousLocations.replace(p.getUniqueId(), p.getLocation());
	}
	
	public Location getSavedLocation(Player p) {
		return this.previousLocations.containsKey(p.getUniqueId()) ? this.previousLocations.get(p.getUniqueId()) : null;
	}
	
	public HashMap<UUID, HashMap<String, Location>> getHomes() {
		return this.homes;
	}

}
