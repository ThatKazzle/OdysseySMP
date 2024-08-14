package kazzleinc.simples5;

import PowerClasses.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import commands.*;
import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import kazzleinc.simples5.ParticleUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Array;
import java.util.*;

public final class SimpleS5 extends JavaPlugin implements Listener {
    public boolean pvpEnabled = true;

    public ProtocolManager protocolManager;

    private SimpleS5 localPlugin = this;

    public NamespacedKey powerPotionKey = new NamespacedKey(this, "power");
    public odysseyCommands odysseyClass;

    public DoubleJumpListener vvfClass = new DoubleJumpListener(this);
    public CompleteCatalogue catalogueClass = new CompleteCatalogue(this);
    public HowDidWeGetHere hdwghClass = new HowDidWeGetHere(this);
    public MonstersHunted monstersClass = new MonstersHunted(this);
    public SniperDuel sniperDuelClass = new SniperDuel(this);
    public UneasyAlliance uneasyAllianceClass = new UneasyAlliance(this);
    public FeelsLikeHome feelsLikeHomeClass = new FeelsLikeHome(this);
    public WithOurPowersCombined wopcClass = new WithOurPowersCombined(this);
    public HiredHelp hiredHelpClass = new HiredHelp(this);
    public Beaconator beaconatorClass = new Beaconator(this);
    public BalancedDiet balancedDietClass = new BalancedDiet(this);
    public TheNextGeneration nextGenerationClass = new TheNextGeneration(this);

    public PowerStealerPlaceholder powerStealerPlaceholder = new PowerStealerPlaceholder(this);

    public final DisguiseProvider provider = DisguiseManager.getProvider();

    public boolean resetPlayerHealthAttribute = false;

    public BukkitTask secondsTask = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        odysseyClass = new odysseyCommands(this);
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(vvfClass, this);
        getServer().getPluginManager().registerEvents(catalogueClass, this);
        getServer().getPluginManager().registerEvents(odysseyClass, this);
        getServer().getPluginManager().registerEvents(hdwghClass, this);
        getServer().getPluginManager().registerEvents(monstersClass, this);
        getServer().getPluginManager().registerEvents(sniperDuelClass, this);
        getServer().getPluginManager().registerEvents(uneasyAllianceClass, this);
        getServer().getPluginManager().registerEvents(feelsLikeHomeClass, this);
        getServer().getPluginManager().registerEvents(wopcClass, this);
        getServer().getPluginManager().registerEvents(hiredHelpClass, this);

        getServer().getPluginManager().registerEvents(beaconatorClass, this);
        beaconatorClass.startTrackingPlayerStates();

        getServer().getPluginManager().registerEvents(balancedDietClass, this);
        getServer().getPluginManager().registerEvents(nextGenerationClass, this);

        protocolManager = ProtocolLibrary.getProtocolManager();
        uneasyAllianceClass.registerInvisListener();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("reloadconfig").setExecutor(new reloadConfigCommand(this));

        getCommand("odyssey").setExecutor(odysseyClass);
        getCommand("odyssey").setTabCompleter(odysseyClass);

        getCommand("resetCooldowns").setExecutor(new resetCooldownCommand(this));

        getCommand("power1").setExecutor(new powerOneCommand(this));
        getCommand("power2").setExecutor(new PowerTwoCommand(this));
        getCommand("toggle-pvp").setExecutor(new PvpToggleCommand(this));

        getServer().getMessenger().registerIncomingPluginChannel(this, "odysseyclientside:power_channel", new modPacketListener(this));
        //getServer().getMessenger().registerOutgoingPluginChannel(this, "odysseyclientside:power_channel_rec");

        //CustomWorldGenerator.createVoidWorld(this, "void_world");

        secondsTask = new BukkitRunnable() {
            @Override
            public void run() {
                //updating the cooldown display so it shows the cooldown
                updateCooldownDisplay();

                //fixing the bug with the catalogue not removing the power
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (getPlayerPowersList(player) != null && getPlayerPowersList(player).size() == 1) {
                        localPlugin.getConfig().set("players." + provider.getInfo(player).getName() + ".mode", 0);
                    }

                    if (localPlugin.getConfig().getInt("players." + provider.getInfo(player).getName() + ".mode", -1) == -1) {
                        localPlugin.getConfig().set("players." + provider.getInfo(player).getName() + ".mode", 0);
                    }

                    if (playerHasPower(player, "husbandry/complete_catalogue")) {
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24);
                    } else {
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);

        boolean allowEntities = getConfig().getBoolean("allow-entity-disguises");
        DisguiseManager.initialize(this, allowEntities);
        provider.allowOverrideChat(true);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player checkPlayer = event.getPlayer();

        AttributeInstance attackSpeed = checkPlayer.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        AttributeInstance scale = checkPlayer.getAttribute(Attribute.GENERIC_SCALE);
        AttributeInstance reach = checkPlayer.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE);

        //attribute thing, to make sure that the player goes back to normal attributes when they leave
        if (checkPlayer.isOnline()) {
            if (attackSpeed.getBaseValue() != 4.0) {
                attackSpeed.setBaseValue(4.0);
            }

            if (scale.getBaseValue() != 1.0) {
                scale.setBaseValue(1.0);
            }

            if (reach.getBaseValue() != 3.0) {
                reach.setBaseValue(3.0);
            }
        }
    }

    public void sendCustomPacket(Player player, String message) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        try {
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        byte[] data = byteArrayOutputStream.toByteArray();
        player.sendPluginMessage(this, "odysseyclientside:power_channel_rec", data);
    }

    @EventHandler
    public void onAdvancementMade(PlayerAdvancementDoneEvent event) {
        List<String> takenPowers = Arrays.asList("");
        Advancement advancement = event.getAdvancement();
        String advName = advancement.getKey().getKey();
        Player player = event.getPlayer();

        player.sendMessage(advName);
        player.sendMessage("is in check power status: " + checkPowerStatus().getOrDefault(advName, false));
        player.sendMessage(checkPowerStatus().toString());

        if (checkPowerStatus().getOrDefault(advName.split("/")[1], false)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.5f);
            player.sendMessage(ChatColor.RED + "You gained the advancement, but was not granted the power because someone else has it.");
        } else {
            for (String keys : getConfig().getConfigurationSection("defaults").getKeys(false)) {
                if (keys.equals(advName.split("/")[1])) {
                    grantAdvancementPower(advancement, player, playerIsAtPowerLimit(player));
                    saveConfig();
                }
            }
        }


    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        for (String keys : getConfig().getConfigurationSection("players." + provider.getInfo(player).getName() + ".powers").getKeys(false)) {

            if (keys.equals("ride_strider_in_overworld_lava")) {
                feelsLikeHomeClass.removeFireResistance(player);
            }

            if (getConfig().getBoolean("players." + provider.getInfo(player).getName() + ".powers." + keys)) {
                player.getWorld().dropItem(player.getLocation(), new PowerPotionItem(this, getAdvancementNameFormattedFromUnformattedString(keys), powerPotionKey).getItemStack());
                removePlayerAdvancement(player, keys);
                getConfig().set("players." + provider.getInfo(player).getName() + ".powers." + keys, false);
            }

        }
        saveConfig();
    }

    //I have to do this because I cant set an attribute on someone in the pause screen for some reason
    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        getServer().getScheduler().runTaskLater(this, () -> {
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        }, 5);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //Handling the clicking of the item if it is a power item
        //sendCustomPacket(player, "Sigma");
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            if (event.getItem() != null) {
                ItemStack item = event.getItem();
                ItemMeta meta = item.getItemMeta();

                String itemPowerKey = item.getItemMeta().getPersistentDataContainer().get(powerPotionKey, PersistentDataType.STRING);

                if (meta != null && item.getType() == Material.AMETHYST_SHARD && item.getItemMeta().getPersistentDataContainer().getKeys().contains(powerPotionKey) && event.getHand() == EquipmentSlot.HAND && !checkPowerStatus().getOrDefault(getAdvancementNameUnformattedFromFormattedString(itemPowerKey), false)) {
                    if (!playerIsAtPowerLimit(player)) {

                        grantAdvancementPower(grantAdvancement(player, getAdvancementKeyFromFormattedString(itemPowerKey)), player, false);

                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                    } else if (playerIsAtPowerLimit(player)) {
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.f, 1.f);

                        player.sendTitle(ChatColor.RED + "Unable to Equip!", ChatColor.RED + "You have 2 powers!", 10, 60, 10);
                    } else if (playerHasPower(player, itemPowerKey)) {
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.f, 1.f);

                            player.sendTitle(ChatColor.RED + "Unable to Equip!", ChatColor.RED + "You already have this power!", 10, 60, 10);
                    }
                } else if (checkPowerStatus().getOrDefault(getAdvancementNameUnformattedFromFormattedString(itemPowerKey), false)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.5f);
                    player.sendMessage(ChatColor.RED + "You were not granted the power because someone else has it.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player hitPlayer = (Player) event.getEntity();

            boolean blocked = event.getFinalDamage() == 0;

            if (!pvpEnabled) {
                event.setCancelled(true);
            }

//            if (hitPlayer.isBlocking()) {
//                if (damager.getInventory().getItemInMainHand().getType().equals(Material.MACE)) {
//                    hitPlayer.clearActiveItem();
//                    hitPlayer.setCooldown(Material.SHIELD, 20 * 5);
//                    hitPlayer.setShieldBlockingDelay(20 * 5);
//                    hitPlayer.playEffect(EntityEffect.SHIELD_BREAK);
//                    hitPlayer.getWorld().playSound(hitPlayer.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.f, 1.f);
//                }
//            }
        }
    }



    @EventHandler
    public void onPlayerPlaceBlockEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item != null && item.getType() == Material.STRUCTURE_VOID) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerOffhandEvent(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getMainHandItem();

        if (item != null && item.getType() == Material.STRUCTURE_VOID) {
            event.setCancelled(true);
        }
    }

    public ArrayList<String> getPlayerPowersList(Player player) {
        ArrayList<String> enabledKeys = new ArrayList<>();

        if (getConfig().getConfigurationSection("players." + provider.getInfo(player).getName() + ".powers") != null) {
            for (String keys : getConfig().getConfigurationSection("players." + provider.getInfo(player).getName() + ".powers").getKeys(false)) {
                String key = keys;
                Boolean value = getConfig().getBoolean("players." + provider.getInfo(player).getName() + ".powers." + key);

                if (value) {
                    enabledKeys.add(key);
                }
            }

            return enabledKeys;
        } else {
            return null;
        }
    }

    public String getAdvancementNameUnformatted(Advancement advancement) {
        return advancement.getKey().getKey();

    }

    public String getAdvancementNameFormattedFromAdvancement(Advancement advancement) {
        String[] arrayAdv = advancement.getKey().getKey().split("/");
        String underscored = arrayAdv[arrayAdv.length - 1];

        String spacedAdv = String.join(" ", underscored.split("_"));

        String titleCaseAdv = titleCaseString(spacedAdv);

        return titleCaseAdv;
    }

    public void removePlayerAdvancement(Player player, String advancementName) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + provider.getInfo(player).getName() + " only " + advancementName);
    }

    public String getAdvancementKeyFromFormattedString(String str) {
        return getConfig().getString("defaults." + String.join("_", str.split(" ")).toLowerCase());
    }

    public String getAdvancementNameUnformattedFromFormattedString(String str) {
        return String.join("_", str.split(" ")).toLowerCase();
    }

    /**
     * Takes in an unformatted advancement string, and returns a formatted one.
     * Example: <b>adventure/very_very_frightening</b> -> <b>Very Very Frightening</b>
     *
     * @param str the unformatted string
     *
     *
     * */
    public String getAdvancementNameFormattedFromUnformattedString(String str) {

        String[] noSlashString = str.split("/");
        String literalName = noSlashString[noSlashString.length - 1];
        String spacedString = String.join(" ", literalName.split("_"));
        return titleCaseString(spacedString);

    }

    public String titleCaseString(String input) {
        // Split the input string into words
        String[] words = input.split("\\s+");

        // Title case each word
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) { // Check if the word is not empty
                // Capitalize the first letter and append the rest of the word
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" "); // Add space between words
            }
        }

        // Remove the trailing space and return the result
        return titleCase.toString().trim();
    }

    public void grantAdvancementPower(Advancement advancement, Player player, Boolean isAtPowerLimit) {
        if (advancement.getDisplay().frame() != AdvancementDisplay.Frame.CHALLENGE) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.f);
        }
        String powerName;

        switch (getAdvancementNameFormattedFromAdvancement(advancement)) {
            case "All Effects":
                powerName = "How Did We Get Here?";
                break;
            case "Kill All Mobs":
                powerName = "Monsters Hunted";
                break;
            case "Froglights":
                powerName = "WOPC";
                break;
            case "Create Full Beacon":
                powerName = "Beaconator";
                break;
            case "Ride Strider In Overworld Lava":
                powerName = "Feels Like Home";
                break;
            case "Dragon Egg":
                powerName = "The Next Generation";
                break;
            default:
                powerName = getAdvancementNameFormattedFromAdvancement(advancement);
                break;
        }

        if (isAtPowerLimit) {
            player.sendTitle("New Power Collected!: " + powerName, ChatColor.RED + "But it has been dropped, you have 2 powers!");

            player.getWorld().dropItem(player.getLocation(), new PowerPotionItem(this, getAdvancementNameFormattedFromAdvancement(advancement), powerPotionKey).getItemStack());

        } else {
            player.sendTitle("New Power Collected!: " + powerName, "type " + ChatColor.GREEN + "\"/od powers\"" + ChatColor.RESET + " for details.");

            getConfig().set("players." + provider.getInfo(player).getName() + ".powers." + getAdvancementNameUnformatted(advancement), true);
        }

        saveConfig();
    }

    private void disableShieldIfPlayerInFront(Player player) {
        // Perform ray tracing to detect if a player is within 4.5 blocks in front
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        double maxDistance = 4.5;

        // Get a list of entities within a 4.5 block radius
        List<Entity> nearbyEntities = player.getNearbyEntities(maxDistance, maxDistance, maxDistance);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;
                // Calculate the vector from the player to the target
                Vector toTarget = targetPlayer.getEyeLocation().toVector().subtract(eyeLocation.toVector());
                // Check if the target is in front of the player within 4.5 blocks
                if (toTarget.normalize().dot(direction) > 0.95) {
                    // Target is in front of the player within the specified range
                    if (targetPlayer.getInventory().getItemInMainHand().getType() == Material.SHIELD || targetPlayer.getInventory().getItemInOffHand().getType() == Material.SHIELD) {
                        // Disable the shield by enacting a cooldown
                        targetPlayer.setCooldown(Material.SHIELD, 100); // 5 seconds cooldown (100 ticks)
                        player.sendMessage("You disabled the shield of " + provider.getInfo(targetPlayer).getName() + "!");
                        targetPlayer.sendMessage("Your shield was disabled by " + provider.getInfo(player).getName() + "!");
                    }
                }
            }
        }
    }

    private Map<String, Boolean> checkPowerStatus() {
        Map<String, Boolean> powerStatus = new HashMap<>();

        // Get the defaults section
        ConfigurationSection defaultsSection = getConfig().getConfigurationSection("defaults");
        if (defaultsSection == null) {
            return powerStatus; // Return empty map if "defaults" section is not found
        }

        // Get all default power keys
        Set<String> defaultPowerKeys = defaultsSection.getKeys(false);

        // Initialize all powers as "not taken"
        for (String defaultPowerKey : defaultPowerKeys) {
            powerStatus.put(defaultPowerKey, false);
        }

        // Get the "players" section
        ConfigurationSection playersSection = getConfig().getConfigurationSection("players");
        if (playersSection == null) {
            return powerStatus; // Return if "players" section is not found
        }

        // Loop through each player
        Set<String> playerKeys = playersSection.getKeys(false);
        for (String playerKey : playerKeys) {
            ConfigurationSection powersSection = playersSection.getConfigurationSection(playerKey + ".powers");
            if (powersSection != null) {
                // Loop through each power for the player
                for (String defaultPowerKey : defaultPowerKeys) {
                    String powerPath = defaultsSection.getString(defaultPowerKey);
                    if (powersSection.getBoolean(powerPath, false)) {
                        powerStatus.put(defaultPowerKey, true); // Mark as "taken" if any player has it set to true
                    }
                }
            }
        }

        return powerStatus;
    }

    public Advancement grantAdvancement(Player player, String key) {
        NamespacedKey advancementKey = NamespacedKey.minecraft(key);
        Advancement advancement = getServer().getAdvancement(advancementKey);

        if (advancement == null) {
            return null;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        for (String criteria : progress.getRemainingCriteria()) {
            progress.awardCriteria(criteria);
        }
        return advancement;
    }

    public boolean playerIsAtPowerLimit(Player player) {
        if (getConfig().getConfigurationSection("players." + provider.getInfo(player).getName() + ".powers") == null) {
            return false;
        } else {
            ConfigurationSection section = getConfig().getConfigurationSection("players." + provider.getInfo(player).getName() + ".powers");

            int powerCount = 0;

            for (Object values : section.getValues(false).values()) {
                if (Boolean.parseBoolean(values.toString())) {
                    powerCount += 1;

                }
            }
            return powerCount >= 2;
        }


    }

    public boolean playerAlreadyHasPower(Player player, String key) {
        if (getConfig().getConfigurationSection("players." + provider.getInfo(player).getName() + ".powers") == null) {
            return false;
        } else {
            ConfigurationSection section = getConfig().getConfigurationSection("players." + provider.getInfo(player).getName() + ".powers");

            int powerCount = 0;

            for (String values : section.getKeys(false)) {
                if (Objects.equals(values, key)) {
                    powerCount = 1;
                }
            }

            return powerCount == 1;
        }


    }

    public boolean playerHasPower(Player player, String powerKey) {
        return getConfig().getBoolean("players." + provider.getInfo(player).getName() + ".powers." + powerKey);
    }

    public void updateCooldownDisplay() {
        String cooldownMessage = "";
        for (Player player : getServer().getOnlinePlayers()) {
            if (localPlugin.playerIsAtPowerLimit(player) && getPlayerPowersList(player) != null) {
                ArrayList<String> powerList = getPlayerPowersList(player);
                int playerMode = (Integer) getConfig().get("players." + provider.getInfo(player).getName() + ".mode", -1);

                if (playerMode != -1 && powerList.get(playerMode) != null) {
                    cooldownMessage = switchOnPowers(player, powerList.get(playerMode));
                }

            } else if (getPlayerPowersList(player) != null && localPlugin.getPlayerPowersList(player).size() == 1) {
                ArrayList<String> powerList = getPlayerPowersList(player);
                if (localPlugin.getPlayerPowersList(player).get(0) != null) {
                    cooldownMessage = switchOnPowers(player, powerList.get(0));
                }

            } else {
                cooldownMessage = "";
            }

            player.sendActionBar(cooldownMessage);
        }
    }

    public static double roundDecimalNumber(double number, int decimalPlaces) {
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Map<String, Boolean> getAllPowers(FileConfiguration config) {
        Map<String, Boolean> powersMap = new HashMap<>();

        // Get the "players" section
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) {
            return powersMap; // Return empty map if "players" section is not found
        }

        // Get all player keys
        Set<String> playerKeys = playersSection.getKeys(false);
        for (String playerKey : playerKeys) {
            // Get the "powers" section for each player
            ConfigurationSection powersSection = playersSection.getConfigurationSection(playerKey + ".powers");
            if (powersSection != null) {
                // Get all power keys and their values
                Set<String> powerKeys = powersSection.getKeys(false);
                for (String powerKey : powerKeys) {
                    boolean value = powersSection.getBoolean(powerKey);
                    powersMap.put(playerKey + "/" + powerKey, value); // Add to map with player key as prefix
                }
            }
        }

        return powersMap;
    }

    /**
     * Gets all players within a specified radius from a given location.
     *
     * @param center The center location.
     * @param radius The radius to search within.
     * @return A list of players within the radius.
     */
    public static List<Player> getPlayersInRange(Location center, double radius) {
        List<Player> playersInRange = new ArrayList<>();
        double radiusSquared = radius * radius; // Use squared distance for efficiency

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(center.getWorld())) {
                // Compare squared distances to avoid expensive square root calculations
                if (center.distance(player.getLocation()) < radius) {

                    //player.sendMessage("distance: " + center.distance(player.getLocation()) + " radius: " + radius);

                    playersInRange.add(player);
                }
            }
        }

        return playersInRange;
    }

    public double mapValue(double value, double inMin, double inMax, double outMin, double outMax) {
        if (inMin == inMax) {
            throw new IllegalArgumentException("inMin and inMax cannot be the same value");
        }

        // Normalize the input value within the input range
        double normalizedValue = (value - inMin) / (inMax - inMin);

        // Scale the normalized value to the output range
        return outMin + (normalizedValue * (outMax - outMin));
    }

    public static Vector lerp(Vector a, Vector b, double t) {
        return a.clone().multiply(1 - t).add(b.clone().multiply(t));
    }

    public String switchOnPowers(Player player, String cooldownKey) {
        String cooldownMessage = "";
        switch (cooldownKey) {
            case "player/power_stolen":
                cooldownMessage = powerStealerPlaceholder.getCooldownString(player, wopcClass.playerGetsPowerBackTime, "");
                break;
            case "adventure/very_very_frightening":
                cooldownMessage = vvfClass.getCooldownString(player, vvfClass.cooldowns, "Dash: ");
                break;
            case "nether/all_effects":
                cooldownMessage = hdwghClass.getCooldownString(player, hdwghClass.rightClickedCooldowns, "Item Disable: ");
                break;
            case "husbandry/complete_catalogue":
                cooldownMessage = catalogueClass.getCooldownString(player, catalogueClass.cooldowns, "Rechargeable Totem: ");
                break;
            case "adventure/kill_all_mobs":
                cooldownMessage = monstersClass.getCooldownString(player, monstersClass.sphereCooldowns, "Domain Expansion: ");
                break;
            case "adventure/sniper_duel":
                cooldownMessage = sniperDuelClass.getCooldownString(player, sniperDuelClass.cooldowns, "Sniper Vision: ");
                break;
            case "nether/uneasy_alliance":
                cooldownMessage = uneasyAllianceClass.getCooldownString(player, uneasyAllianceClass.cooldowns, "Invisibility: ");
                break;
            case "husbandry/froglights":
                cooldownMessage = wopcClass.getCooldownString(player, wopcClass.frogCooldowns, "Frog Tongue: ");
                break;
            case "adventure/summon_iron_golem":
                cooldownMessage = hiredHelpClass.getCooldownString(player, hiredHelpClass.cooldowns, "Hired Help: ");
                break;
            case "nether/ride_strider_in_overworld_lava":
                cooldownMessage = feelsLikeHomeClass.getCooldownString(player, feelsLikeHomeClass.cooldowns, "Blazed: ");
                break;
            case "nether/create_full_beacon":
                cooldownMessage = beaconatorClass.getCooldownString(player, beaconatorClass.cooldowns, "Beaconator: ");
                break;
            case "husbandry/balanced_diet":
                cooldownMessage = balancedDietClass.getCooldownString(player, balancedDietClass.cooldowns, "Stored Energy: ");
                break;
            case "end/dragon_egg":
                cooldownMessage = nextGenerationClass.getCooldownString(player, nextGenerationClass.cooldowns, "Ground Pound: ");
                break;
        }

        return cooldownMessage;
    }
}
