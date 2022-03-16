package eu.playerunion.putri.listeners;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import eu.playerunion.putri.Main;
import eu.playerunion.putri.workers.ServerWorkers;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerListener implements Listener {
	
	private Main plugin = Main.getInstance();
	
	private ServerWorkers serverWorkers = this.plugin.getServerWorkers();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5.0F, 0.1F);
		
		if(!p.hasPlayedBefore()) {
			// Random teleport helye.
			
			Bukkit.broadcastMessage("§f» §aJött egy §e" + p.getName() + " §anevű újonc.");
		}
		
		e.setJoinMessage("§e" + p.getName() + " megérkezett közénk!");
		
		p.sendMessage("§b» §fKöszöntelek a szerveren!");
		p.sendMessage("§b» §fJelenleg online játékosok: §e" + Bukkit.getOnlinePlayers().size());
		p.sendMessage("§b» §fLegutóbbi látogatásod: §e" + this.plugin.getConfig().getString("jatekos." + p.getName() + ".lastOnline"));
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
		String date = simpleDateFormat.format(new Date());
		
		this.plugin.getConfig().set("jatekos." + p.getName() + ".lastOnline", date);
		
		this.plugin.saveConfig();
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		
		e.setQuitMessage("§e" + p.getName() + " elhagyta a szervert!");
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
		String date = simpleDateFormat.format(new Date());
		
		this.plugin.getConfig().set("jatekos." + p.getName() + ".lastOnline", date);
		
		this.plugin.saveConfig();
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		
		this.serverWorkers.saveLocation(p);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		
		this.serverWorkers.saveLocation(p);
		
		p.sendMessage("§c§lMEGHALTÁL! §fA halálod helyére való visszatéréshez használd a §e/back §fparancsot!");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEntityEvent e) {
		Entity ent = e.getRightClicked();
		
		if(ent instanceof Tameable) {
			if(((Tameable) ent).getOwner() != null) {
				AnimalTamer owner = ((Tameable) ent).getOwner();
				Player p = e.getPlayer();
				
				p.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ent.getType().name() + " gazdája: " + owner.getName()));
				
				if(ent instanceof Horse || ent instanceof Donkey || ent instanceof Mule) {
					if(!p.getUniqueId().toString().equals(owner.getUniqueId().toString())) {
						e.setCancelled(true);
						
						p.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c§l✘ §7§l| §fEz a(z) " + ent.getType().name().toLowerCase().replaceAll("_", "") + " " + owner.getName() + " tulajdona!"));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		ItemStack stack = p.getInventory().getItemInMainHand();
		
		if(e.getBlock().getType() == Material.SPAWNER) {
			if(p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR) {
				
				Material[] pickaxes = {Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE};
				
				for(Material m : pickaxes){
					if(stack.getType() == m){
						if(stack.containsEnchantment(Enchantment.SILK_TOUCH)) {
							CreatureSpawner spawner = (CreatureSpawner) e.getBlock().getState();
							String creatureName = spawner.getSpawnedType().name();
							
							stack = new ItemStack(Material.SPAWNER, 1);
							ItemMeta meta = stack.getItemMeta();
							
							meta.setDisplayName("§5" + creatureName + " idézõ");
							stack.setItemMeta(meta);
							
							if(p.getInventory().firstEmpty() == -1)
								e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), stack);
							else
								p.getInventory().addItem(stack);
							
							p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a+1 §5" + creatureName + " idézõ"));
						}
						
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		ItemStack stack = e.getItemInHand();
		
		if(stack != null && stack.getType() != Material.AIR) {
			if(stack.getType() == Material.SPAWNER) {
				if(stack.hasItemMeta()) {
					ItemMeta meta = stack.getItemMeta();
					
					if(meta.hasDisplayName()) {
						String name = meta.getDisplayName();
						if(name.startsWith("§5")) {
							EntityType type = Enum.valueOf(EntityType.class, name.split(" ")[0].replaceAll("§5", ""));
							CreatureSpawner spawner = (CreatureSpawner) e.getBlock().getState();
							
							spawner.setSpawnedType(type);
							spawner.update();
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onGameModeChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		
		if(e.getNewGameMode().equals(GameMode.SPECTATOR)) {
			PlayerInventory inv = p.getInventory();
			
			if(!inv.contains(Material.DIAMOND)) {
				p.sendTitle("§cEhehehe :(", "§cSajnálom, de erre nincs elég pénzed :c (10 dia)");
				
				e.setCancelled(true);
			}
			
			for(int i = 0; i < inv.getSize(); i++) {
				ItemStack s = inv.getItem(i);
				
				if(s == null)
					continue;
				
				if(s.getType() == Material.DIAMOND) {
					if(s.getAmount() >= 10) {
						s.setAmount(s.getAmount() - 10);
						
						inv.setItem(i, s);
						
						p.sendMessage("§9Viktor §f» §7Nem örülök, hogy csalsz, de így, hogy van pénzed, hajrá! Levontam a 10 diát.");
						
						break;
					}
					
					p.sendTitle("§cEhehehe :(", "§cSajnálom, de erre nincs elég pénzed :c (10 dia)");
					
					e.setCancelled(true);
					
					break;
				}
			} 
		} 
		
		if(e.getNewGameMode().equals(GameMode.CREATIVE)) {
			PlayerInventory inv = p.getInventory();
			
			if(!inv.contains(Material.DIAMOND)) {
				p.sendTitle("§cEhehehe :(", "§cSajnálom, de erre nincs elég pénzed :c (35 dia)");
				
				e.setCancelled(true);
			}
			
			for(int i = 0; i < inv.getSize(); i++) {
				ItemStack s = inv.getItem(i);
				
				if(s == null)
					continue;
				
				if(s.getType() == Material.DIAMOND) {
					if(s.getAmount() >= 35) {
						s.setAmount(s.getAmount() - 35);
						
						inv.setItem(i, s);
						
						p.sendMessage("§9Viktor §f» §7Nem örülök, hogy csalsz, de így, hogy van pénzed, hajrá! Levontam a 35 diát.");
						
						break;
					}
					
					p.sendTitle("§cEhehehe :(", "§cSajnálom, de erre nincs elég pénzed :c (35 dia)");
					
					e.setCancelled(true);
					break;
				}
			}
		}
		
	}

}
