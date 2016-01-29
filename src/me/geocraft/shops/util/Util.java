package me.geocraft.shops.util;

import java.io.*;

import org.bukkit.Sound;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Util {

	/**
	 * Returns the Sound corresponding to the name, or returns
	 * {@code null} if the sound does not exist. See {@link Sound} for
	 * reference.
	 * 
	 * @param name The enum constant as a string.
	 */
	public static Sound getSound(String name) {
		Sound sound;
		try {
			sound = Sound.valueOf(name.toUpperCase());
		} catch(Exception e) {
			sound = null;
		}
		return sound;
	}
	
	public static void playSound(Player player, String sound,
			float pitch) {
		if(player == null || sound == null)
			return;
		Sound s = getSound(sound);
		if(s == null)
			player.playSound(player.getLocation(),
					sound.toLowerCase(), 1.0f, pitch);
		else
			player.playSound(player.getLocation(), s, 1.0f, pitch);
	}
	
	/**
	 * Automatically loads the default YAML config into the plugin directory 
	 * packed with the plugin.
	 * 
	 */
	public static void loadYamlConfig(JavaPlugin plugin, String res) {
		File f = new File(plugin.getDataFolder(), res);
		// TODO don't be lazy and add a non-deprecated method of loading this
		@SuppressWarnings("deprecation")
		FileConfiguration data = YamlConfiguration.loadConfiguration(
				plugin.getResource(res));
		try {
			f.createNewFile();
			data.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		plugin.getConfig().addDefaults(data);
	}
	
	public static String color(String msg) {
		return msg.replace('&', '\u00A7');
	}
	
}
