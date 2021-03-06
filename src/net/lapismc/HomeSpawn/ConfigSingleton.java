package net.lapismc.HomeSpawn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigSingleton {
	private static ConfigSingleton instance = null;
	private JavaPlugin plugin = null;
	private Logger logger = null;
	private Map<String, FileConfiguration> playerConfigs = new HashMap<String, FileConfiguration>();

	public File PlayerDataDir = null;
	public FileConfiguration Plugin = null;
	public FileConfiguration Spawn = null;
	public FileConfiguration GlobalHomes = null;
	public FileConfiguration Messages = null;
	public FileConfiguration Update = null;
	public FileConfiguration Passwords = null;

	protected ConfigSingleton(JavaPlugin plugin) {
		this.plugin = plugin;
		logger = plugin.getLogger();
		createPlayerData();
		createPlugin();
		createSpawn();
		createGlobalHomes();
		createUpdate();
		createMessages();
		createPasswords();
	}

	public static ConfigSingleton getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new ConfigSingleton(plugin);
		}
		return instance;
	}

	public static void Reset() {
		instance = null;
	}

	public void savePlugin() {
		plugin.saveConfig();
	}

	private void createPlugin() {
		Plugin = plugin.getConfig();
	}

	public void savePlayerConfig(Player player) {
		if (playerConfigs.containsKey(player.getUniqueId().toString())) {
			File file = new File(plugin.getDataFolder() + File.separator + "PlayerData" + File.separator
					+ player.getUniqueId().toString() + ".yml");
			try {
				playerConfigs.get(player.getUniqueId().toString()).save(file);
			} catch (IOException e) {
				logger.severe("Couldn't save player file!");
				e.printStackTrace();
			}
		}
	}

	public FileConfiguration getPlayerConfig(Player player) {
		if (playerConfigs.containsKey(player.getUniqueId().toString())) {
			return playerConfigs.get(player.getUniqueId().toString());
		} else {
			File file = new File(plugin.getDataFolder() + File.separator + "PlayerData" + File.separator
					+ player.getUniqueId().toString() + ".yml");
			FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);

			if (!file.exists()) {
				logger.info("Creating Player(" + player.getUniqueId().toString() + ") configuration file!");
				try {
					file.createNewFile();
				} catch (IOException e) {
					logger.severe("Couldn't create player file!");
					e.printStackTrace();
				}
			}

			boolean changedSomething = false;
			if (!playerConfig.contains("Name")) {
				playerConfig.createSection("Name");
				playerConfig.set("Name", player.getName());
				changedSomething = true;
			}
			if (!playerConfig.contains("UUID")) {
				playerConfig.createSection("UUID");
				playerConfig.set("UUID", player.getUniqueId().toString());
				changedSomething = true;
			}
			if (!playerConfig.contains("Numb")) {
				playerConfig.createSection("Numb");
				playerConfig.set("Numb", 0);
				changedSomething = true;
			}
			if (!playerConfig.contains("List")) {
				playerConfig.createSection("List");
				changedSomething = true;
			}
			// ----------------------------------------------------------------
			// Until next dash line used for migrating old data. Remove after
			// some releases
			if (playerConfig.contains(player.getName() + ".Numb")) {
				playerConfig.set("Numb", playerConfig.getInt(player.getName() + ".Numb"));
				changedSomething = true;
			}
			if (playerConfig.contains(player.getName() + ".list")) {
				playerConfig.set("List", playerConfig.getStringList(player.getName() + ".list"));
				changedSomething = true;
			}
			List<String> list = playerConfig.getStringList("List");
			List<String> lowerList = new ArrayList<String>();
			for (String item : list)
			{
				if (!item.toLowerCase().equals(item))
				{
					if (!playerConfig.contains(item+".x")) {
						playerConfig.createSection(item.toLowerCase()+".x");
						playerConfig.set(item.toLowerCase()+".x", playerConfig.getInt(item + ".x"));
					}
					if (!playerConfig.contains(item+".y")) {
						playerConfig.createSection(item.toLowerCase()+".y");
						playerConfig.set(item.toLowerCase()+".y", playerConfig.getInt(item + ".y"));
					}
					if (!playerConfig.contains(item+".z")) {
						playerConfig.createSection(item.toLowerCase()+".z");
						playerConfig.set(item.toLowerCase()+".z", playerConfig.getInt(item + ".z"));
					}
					if (!playerConfig.contains(item+".world")) {
						playerConfig.createSection(item.toLowerCase()+".world");
						playerConfig.set(item.toLowerCase()+".world", playerConfig.getString(item + ".world"));
					}
					if (!playerConfig.contains(item+".Yaw")) {
						playerConfig.createSection(item.toLowerCase()+".Yaw");
						playerConfig.set(item.toLowerCase()+".Yaw", playerConfig.getDouble(item + ".Yaw"));
					}
					if (!playerConfig.contains(item+".Pitch")) {
						playerConfig.createSection(item.toLowerCase()+".Pitch");
						playerConfig.set(item.toLowerCase()+".Pitch", playerConfig.getDouble(item + ".Pitch"));
					}
					changedSomething = true;
				}
				lowerList.add(item.toLowerCase());
			}
			if (changedSomething)
			{
				list = lowerList;
				playerConfig.set("List", lowerList);
			}
			if (list.contains("home")) {
				if (playerConfig.contains(player.getDisplayName() + ".x")
						&& playerConfig.contains(player.getDisplayName() + ".y")
						&& playerConfig.contains(player.getDisplayName() + ".z")
						&& playerConfig.contains(player.getDisplayName() + ".world")
						&& playerConfig.contains(player.getDisplayName() + ".Yaw")
						&& playerConfig.contains(player.getDisplayName() + ".Pitch")) {
					if (!playerConfig.contains("home.x")) {
						playerConfig.createSection("home.x");
						playerConfig.set("home.x", playerConfig.getInt(player.getDisplayName() + ".x"));
						changedSomething = true;
					}
					if (!playerConfig.contains("home.y")) {
						playerConfig.createSection("home.y");
						playerConfig.set("home.y", playerConfig.getInt(player.getDisplayName() + ".y"));
						changedSomething = true;
					}
					if (!playerConfig.contains("home.z")) {
						playerConfig.createSection("home.z");
						playerConfig.set("home.z", playerConfig.getInt(player.getDisplayName() + ".z"));
						changedSomething = true;
					}
					if (!playerConfig.contains("home.world")) {
						playerConfig.createSection("home.world");
						playerConfig.set("home.world", playerConfig.getString(player.getDisplayName() + ".world"));
						changedSomething = true;
					}
					if (!playerConfig.contains("home.Yaw")) {
						playerConfig.createSection("home.Yaw");
						playerConfig.set("home.Yaw", playerConfig.getDouble(player.getDisplayName() + ".Yaw"));
						changedSomething = true;
					}
					if (!playerConfig.contains("home.Pitch")) {
						playerConfig.createSection("home.Pitch");
						playerConfig.set("home.Pitch", playerConfig.getDouble(player.getDisplayName() + ".Pitch"));
						changedSomething = true;
					}
				}
			}
			// ----------------------------------------------------------------
			if (!playerConfig.getString("Name").equals(player.getName())) {
				playerConfig.set("Name", player.getName());
				changedSomething = true;
			}

			if (changedSomething) {
				try {
					playerConfig.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			playerConfigs.put(player.getUniqueId().toString(), playerConfig);

			return playerConfig;
		}
	}

	private void createPlayerData() {
		PlayerDataDir = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "PlayerData");
		if (!PlayerDataDir.exists()) {
			logger.info("Creating PlayerData Directory!");
			try {
				boolean done = PlayerDataDir.mkdir();
				if (!done)
					throw new IOException("Can't create directory " + PlayerDataDir.getAbsolutePath());
			} catch (Exception e) {
				logger.severe("Couldn't create PlayerData directory!");
				e.printStackTrace();
			}
		}
	}

	public void saveSpawn() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Spawn.yml");
		try {
			Spawn.save(file);
		} catch (IOException e) {
			logger.severe("Couldn't save Spawn file!");
			e.printStackTrace();
		}
	}

	private void createSpawn() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Spawn.yml");
		Spawn = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("Creating Spawn configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("Couldn't create spawn file!");
				e.printStackTrace();
			}
		}
		// Better to check independent if file exists or not. that way we can
		// also migrate old files to newer version.
		String[] sections = new String[] { "spawn.X", "spawn.Y", "spawn.Z", "spawn.World", "spawn.Yaw", "spawn.Pitch",
				"spawnnew.X", "spawnnew.Y", "spawnnew.Z", "spawnnew.World", "spawnnew.Yaw", "spawnnew.Pitch" };
		boolean changedSomething = false;
		for (int s = 0; s < sections.length; s++) {
			if (!Spawn.contains(sections[s])) {
				Spawn.createSection(sections[s]);
				changedSomething = true;
			}
		}
		if (changedSomething) {
			try {
				Spawn.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveGlobalHomes() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "GlobalHomes.yml");
		try {
			GlobalHomes.save(file);
		} catch (IOException e) {
			logger.severe("Couldn't save GlobalHomes file!");
			e.printStackTrace();
		}
	}

	private void createGlobalHomes() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "GlobalHomes.yml");
		GlobalHomes = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("Creating GlobalHomes configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("Couldn't create GlobalHomes file!");
				e.printStackTrace();
			}
		}
		boolean changedSomething = false;
		if (!GlobalHomes.contains("List")) {
			GlobalHomes.createSection("List");
			changedSomething = true;
		}
		if (changedSomething) {
			try {
				GlobalHomes.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveUpdate() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Update.yml");
		try {
			Update.save(file);
		} catch (IOException e) {
			logger.severe("Couldn't save Update file!");
			e.printStackTrace();
		}
	}

	private void createUpdate() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Update.yml");
		Update = YamlConfiguration.loadConfiguration(file);
		boolean changedSomething = false;
		if (!Update.contains("Avail")) {
			Update.createSection("Avail");
			Update.set("Avail", "false");
			changedSomething = true;
		}
		if (!file.exists()) {
			logger.info("Creating Update configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("Couldn't create Update file!");
				e.printStackTrace();
			}
		}
		if (changedSomething) {
			try {
				Update.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveMessages() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Messages.yml");
		try {
			Messages.save(file);
		} catch (IOException e) {
			logger.severe("Couldn't save Messages file!");
			e.printStackTrace();
		}
	}

	private void createMessages() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Messages.yml");
		Messages = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("Creating Messages configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("Couldn't create messages file!");
				e.printStackTrace();
			}
		}
		// Better to check independent if file exists or not. that way we can
		// also migrate old files to newer version.
		String[] sections = new String[] { "Home.HomeSet", "Home.SentHome", "Home.NoHomeSet", "Home.NotVip",
				"Home.HomeRemoved", "Home.LimitReached", "Spawn.NotSet", "Spawn.SpawnSet", "Spawn.SpawnNewSet",
				"Spawn.SentToSpawn", "Spawn.Removed", "Wait", "Error.Permission", "Error.Args+", "Error.Args-",
				"Error.Args", "Home.NotFound", "Home.Current", "Home.Reserved" };
		boolean changedSomething = false;
		for (int s = 0; s < sections.length; s++) {
			if (!Messages.contains(sections[s])) {
				Messages.createSection(sections[s]);
				changedSomething = true;
			}
		}

		String[][] messages = new String[][] { { "Home.HomeSet", "Home Set, You Can Now Use /home [homeName]" },
				{ "Home.SentHome", "Welcome Home" }, { "Home.NoHomeSet", "You First Need To Set a Home With /sethome" },
				{ "Home.HomeRemoved", "Home Removed" },
				{ "Home.NotVip", "Sorry but you are not a VIP. You can not use multiple homes." },
				{ "Home.LimitReached",
						"Sorry But You have Reached The Max Limit Of Homes, please Use /delhome To Remove A Home" },
				{ "Spawn.NotSet", "You First Need To Set a Spawn With /setspawn" },
				{ "Spawn.SpawnSet", "Spawn Set, You Can Now Use /spawn" },
				{ "Spawn.SpawnNewSet", "Spawn New Set, All New Players Will Be Sent To This Location" },
				{ "Spawn.SentToSpawn", "Welcome To Spawn" }, { "Spawn.Removed", "Spawn Removed!" },
				{ "Wait",
						"You Must Wait {time} Seconds Before You Can Be Teleported, If You Move Or Get Hit By Another Player Your Teleport Will Be Canceled" },
				{ "Error.Permission", "You don't have permission to do that!" },
				{ "Error.Args+", "Too Much Infomation!" }, { "Error.Args-", "Not Enough Infomation" },
				{ "Error.Args", "Too Little or Too Much Infomation" },
				{ "Home.NotFound", "A home with this name does not exist!" },
				{ "Home.Current", "Your Current Homes Are:" }, { "Home.Reserved", "That's a reserved home name!" } };
		// If a message is null or empty, we set the default value
		for (int s = 0; s < messages.length; s++) {
			if (Messages.get(messages[s][0]) != null && !Messages.getString(messages[s][0]).isEmpty()) {
				Messages.set(messages[s][0], messages[s][1]);
				changedSomething = true;
			}
		}

		if (changedSomething) {
			try {
				Messages.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void savePasswords() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "PlayerData" + File.separator
				+ "Passwords.yml");
		try {
			Passwords.save(file);
		} catch (IOException e) {
			logger.severe("Couldn't save Passwords file!");
			e.printStackTrace();
		}
	}

	private void createPasswords() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "PlayerData" + File.separator
				+ "Passwords.yml");
		Passwords = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("Creating Passwords configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("Couldn't create passwords file!");
				e.printStackTrace();
			}
		}
	}

}
