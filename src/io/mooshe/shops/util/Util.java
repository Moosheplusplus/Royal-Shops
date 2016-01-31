package io.mooshe.shops.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

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
		try {
			if(player == null || sound == null)
				return;
			Sound s = getSound(sound);
			if(s == null)
				player.playSound(player.getLocation(),
						sound.toLowerCase(), 1.0f, pitch);
			else
				player.playSound(player.getLocation(), s, 1.0f, pitch);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String color(String msg) {
		return msg.replace('&', '\u00A7');
	}
	
}
