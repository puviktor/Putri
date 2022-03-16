package eu.playerunion.putri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import eu.playerunion.putri.commands.DefaultCommands;
import eu.playerunion.putri.listeners.PlayerListener;
import eu.playerunion.putri.workers.ServerWorkers;

public class Main extends JavaPlugin {
	
	private static Main instance;
	
	public final Logger LOGGER = this.getServer().getLogger();
	
	private Plugin sorvival;
	
	public ServerWorkers serverWorkers;
	public DefaultCommands commandExecutor;
	
	public static Main getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		this.LOGGER.info("A plugin betöltése folyamatban van...");
		
		if(this.getServer().getPluginManager().getPlugin("Sorvival") != null)
			this.sorvival = this.getServer().getPluginManager().getPlugin("Sorvival");
		
		this.serverWorkers = new ServerWorkers();
		this.commandExecutor = new DefaultCommands();
		
		this.serverWorkers.setupMotd();
		
		// Régi adatok migrálása az új plugin mappájába.
		
		if(this.sorvival != null && this.sorvival.isEnabled()) {
			if(!this.getDataFolder().exists()) {
				this.getDataFolder().mkdirs();
				
				for(File f : this.sorvival.getDataFolder().listFiles())
					try {
						IOUtils.copy(new FileInputStream(f), new FileOutputStream(new File(this.getDataFolder(), f.getName())));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		
		this.getServerWorkers().loadHomes();
		
		this.getCommand("ping").setExecutor(this.commandExecutor);
		this.getCommand("home").setExecutor(this.commandExecutor);
		this.getCommand("sethome").setExecutor(this.commandExecutor);
		this.getCommand("delhome").setExecutor(this.commandExecutor);
		this.getCommand("tptoggle").setExecutor(this.commandExecutor);
		this.getCommand("spawn").setExecutor(this.commandExecutor);
		this.getCommand("back").setExecutor(this.commandExecutor);
		this.getCommand("tp").setExecutor(this.commandExecutor);
		
		this.LOGGER.info("A plugin sikeresen betöltött!");
	}
	
	public ServerWorkers getServerWorkers() {
		return this.serverWorkers;
	}

}
