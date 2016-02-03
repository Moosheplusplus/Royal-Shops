package io.mooshe.shops.util;

import java.util.Random;

import org.bukkit.entity.Player;

public class CustomSound {

	public String sound;
	public Pitch pitch;
	
	public CustomSound(String sound) {
		this.sound = sound;
		pitch = new Pitch();
	}
	
	public void play(Player player) {
		Util.playSound(player, sound, pitch.get());
	}
	
	public class Pitch {
		
		public final float pitch;
		public final boolean random;
		
		public Pitch() {
			this.pitch = 0f;
			this.random = true;
		}
		
		public Pitch(float pitch) {
			this.pitch = pitch;
			this.random = false;
		}
		
		public float get() {
			if(random)
				return 0.8f + (new Random().nextFloat() * 0.2f);
			return pitch;
		}
	}
}
