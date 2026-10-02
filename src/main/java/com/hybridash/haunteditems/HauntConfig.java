package com.hybridash.haunteditems;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** config/haunteditems.properties */
public class HauntConfig {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("haunteditems.properties");

	/** 1 in this many chance, rolled once per second per player. 2400 = about once every 40 minutes. */
	public int oddsPerSecond = 2400;
	/** Allow the hauntings that move items around. Set to false if you only want sounds and whispers. */
	public boolean allowItemMoving = true;

	public static HauntConfig load() {
		HauntConfig c = new HauntConfig();
		Properties p = new Properties();
		if (Files.exists(FILE)) {
			try (Reader r = Files.newBufferedReader(FILE)) {
				p.load(r);
				c.oddsPerSecond = Math.max(1, Integer.parseInt(p.getProperty("oddsPerSecond", "2400").trim()));
				c.allowItemMoving = Boolean.parseBoolean(p.getProperty("allowItemMoving", "true").trim());
			} catch (Exception e) {
				HauntedItems.LOGGER.warn("Couldn't read {}, using defaults", FILE, e);
			}
		}
		c.save();
		return c;
	}

	private void save() {
		Properties p = new Properties();
		p.setProperty("oddsPerSecond", Integer.toString(oddsPerSecond));
		p.setProperty("allowItemMoving", Boolean.toString(allowItemMoving));
		try {
			Files.createDirectories(FILE.getParent());
			try (Writer w = Files.newBufferedWriter(FILE)) {
				p.store(w, "HauntedItems\n"
						+ "oddsPerSecond: 1 in this many chance each second (2400 = about every 40 min, 600 = every 10 min)\n"
						+ "allowItemMoving: false = only sounds and whispers, items never move");
			}
		} catch (IOException e) {
			HauntedItems.LOGGER.warn("Couldn't save {}", FILE, e);
		}
	}
}
