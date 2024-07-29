package kazzleinc.simples5;

import PowerClasses.*;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import commands.*;
import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class SimpleS5 extends JavaPlugin implements Listener {

    public ProtocolManager protocolManager;

    private SimpleS5 localPlugin = this;

    public NamespacedKey powerPotionKey = new NamespacedKey(this, "power");
    public odysseyCommands odysseyClass = new odysseyCommands(this);

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

    public boolean resetPlayerHealthAttribute = false;

    public BukkitTask secondsTask = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
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
        getServer().getPluginManager().registerEvents(balancedDietClass, this);

        protocolManager = ProtocolLibrary.getProtocolManager();
        uneasyAllianceClass.registerInvisListener();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("reloadconfig").setExecutor(new reloadConfigCommand(this));

        getCommand("odyssey").setExecutor(odysseyClass);
        getCommand("odyssey").setTabCompleter(odysseyClass);

        getCommand("resetCooldowns").setExecutor(new resetCooldownCommand(this));

        getCommand("power1").setExecutor(new powerOneCommand(this));
        getCommand("power2").setExecutor(new PowerTwoCommand(this));

        getServer().getMessenger().registerIncomingPluginChannel(this, "odysseyclientside:power_channel", new modPacketListener(this));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "odysseyclientside:power_channel_rec");

        CustomWorldGenerator.createVoidWorld(this, "void_world");

        secondsTask = new BukkitRunnable() {
            @Override
            public void run() {
                //updating the cooldown display so it shows the cooldown
                updateCooldownDisplay();

                //fixing the bug with the catalogue not removing the power
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (getPlayerPowersList(player) != null) {
                        if (!getPlayerPowersList(player).contains("husbandry/complete_catalogue")) {
                            catalogueClass.removeCataloguePower(player);
                        }

                        if (!getPlayerPowersList(player).contains("nether/ride_strider_in_overworld_lava")) {
                            feelsLikeHomeClass.removeFireResistance(player);
                        }
                    }


                    if (getPlayerPowersList(player) != null && getPlayerPowersList(player).size() == 1) {
                        localPlugin.getConfig().set("players." + player.getName() + ".mode", 0);
                    }

                    if (localPlugin.getConfig().getInt("players." + player.getName() + ".mode", -1) == -1) {
                        localPlugin.getConfig().set("players." + player.getName() + ".mode", 0);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);

        final int[] rotation = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                rotation[0] += 4;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    //ParticleUtils.createParticleDodecahedron(player.getEyeLocation(), 4, 30, Particle.DUST, Color.PURPLE);
                }
            }
        }.runTaskTimer(this, 0, 0);

        //fixes the config thing so that things can work without deleting the config
        if (getConfig().getConfigurationSection("defaults") != Objects.requireNonNull(getConfig().getDefaults()).getConfigurationSection("defaults")) {
            for (String keys : getConfig().getConfigurationSection("defaults").getKeys(false)) {
                if (!getConfig().getDefaults().isSet(keys)) {
                    getConfig().set(keys, getConfig().getDefaults().get("defaults.") + keys);
                    saveConfig();
                }
            }
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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

        Advancement advancement = event.getAdvancement();

        String advName = advancement.getKey().getKey();

        Player player = event.getPlayer();

        for (String keys : getConfig().getConfigurationSection("defaults").getKeys(false)) {
            if (keys.equals(advName.split("/")[1])) {
                grantAdvancementPower(advancement, player, playerIsAtPowerLimit(player));
                saveConfig();
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        for (String keys : getConfig().getConfigurationSection("players." + player.getName() + ".powers").getKeys(false)) {
            if (keys.equals("complete_catalogue")) {
                catalogueClass.removeCataloguePower(player);
                resetPlayerHealthAttribute = true;
            }

            if (keys.equals("ride_strider_in_overworld_lava")) {
                feelsLikeHomeClass.removeFireResistance(player);
            }

            if (getConfig().getBoolean("players." + player.getName() + ".powers." + keys)) {
                player.getWorld().dropItem(player.getLocation(), new PowerPotionItem(this, getAdvancementNameFormattedFromUnformattedString(keys), powerPotionKey).getItemStack());
                removePlayerAdvancement(player, keys);
                getConfig().set("players." + player.getName() + ".powers." + keys, false);
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

                if (meta != null && item.getType() == Material.AMETHYST_SHARD && item.getItemMeta().getPersistentDataContainer().getKeys().contains(powerPotionKey) && event.getHand() == EquipmentSlot.HAND) {
                    if (!playerIsAtPowerLimit(player)) {

                        grantAdvancementPower(grantAdvancement(player, getAdvancementKeyFromFormattedString(itemPowerKey)), player, false);

                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                    } else if (playerIsAtPowerLimit(player)) {
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.f, 1.f);

                        player.sendTitle(ChatColor.RED + "Unable to Equip!", ChatColor.RED + "You have 2 powers!", 10, 60, 10);
                    } else if (playerAlreadyHasPower(player, itemPowerKey)) {
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.f, 1.f);

                            player.sendTitle(ChatColor.RED + "Unable to Equip!", ChatColor.RED + "You already have this power!", 10, 60, 10);
                    }
                }
            }
        }
    }

    @EventHandler
    public void  onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player hitPlayer = (Player) event.getEntity();

            boolean blocked = event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) > 0;

            if (blocked && hitPlayer.isBlocking()) {
                if (damager.getInventory().getItemInMainHand().equals(Material.MACE)) {
                    hitPlayer.setCooldown(Material.SHIELD, 20*5);
                    hitPlayer.clearActiveItem();
                    hitPlayer.playEffect(EntityEffect.SHIELD_BREAK);
                }
            }
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

        if (getConfig().getConfigurationSection("players." + player.getName() + ".powers") != null) {
            for (String keys : getConfig().getConfigurationSection("players." + player.getName() + ".powers").getKeys(false)) {
                String key = keys;
                Boolean value = getConfig().getBoolean("players." + player.getName() + ".powers." + key);

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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + player.getName() + " only " + advancementName);
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

        if (isAtPowerLimit) {
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
                case "Balanced Diet":
                    powerName = "Balanced Diet";
                default:
                    powerName = getAdvancementNameFormattedFromAdvancement(advancement);
                    break;
            }

            player.sendTitle("New Power Collected!: " + powerName, ChatColor.RED + "But it has been dropped, you have 2 powers!");

            player.getWorld().dropItem(player.getLocation(), new PowerPotionItem(this, getAdvancementNameFormattedFromAdvancement(advancement), powerPotionKey).getItemStack());

        } else {
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
                case "Balanced Diet":
                    powerName = "Balanced Diet";
                default:
                    powerName = getAdvancementNameFormattedFromAdvancement(advancement);
                    break;
            }

            player.sendTitle("New Power Collected!: " + powerName, "type " + ChatColor.GREEN + "\"/od powers\" for details.");

            getConfig().set("players." + player.getName() + ".powers." + getAdvancementNameUnformatted(advancement), true);
            if (advancement.getKey().getKey().equals("husbandry/complete_catalogue")) {
                catalogueClass.giveCataloguePower(player);
            }
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
                        player.sendMessage("You disabled the shield of " + targetPlayer.getName() + "!");
                        targetPlayer.sendMessage("Your shield was disabled by " + player.getName() + "!");
                    }
                }
            }
        }
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
        if (getConfig().getConfigurationSection("players." + player.getName() + ".powers") == null) {
            return false;
        } else {
            ConfigurationSection section = getConfig().getConfigurationSection("players." + player.getName() + ".powers");

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
        if (getConfig().getConfigurationSection("players." + player.getName() + ".powers") == null) {
            return false;
        } else {
            ConfigurationSection section = getConfig().getConfigurationSection("players." + player.getName() + ".powers");

            int powerCount = 0;

            for (String values : section.getKeys(false)) {
                if (Objects.equals(values, key)) {
                    powerCount = 1;
                }
            }

            return powerCount == 1;
        }


    }

    public void updateCooldownDisplay() {
        String cooldownMessage = "";
        for (Player player : getServer().getOnlinePlayers()) {

            if (localPlugin.playerIsAtPowerLimit(player) && getPlayerPowersList(player) != null) {

                ArrayList<String> powerList = getPlayerPowersList(player);

                int playerMode = (Integer) getConfig().get("players." + player.getName() + ".mode", -1);

                if (playerMode != -1 && powerList.get(playerMode) != null) {
                    switch (powerList.get(playerMode)) {
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
                            cooldownMessage = wopcClass.getCooldownString(player, wopcClass.cooldowns, "Odyssey Stealer: ");
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
                    }
                }
            } else if (getPlayerPowersList(player) != null && localPlugin.getPlayerPowersList(player).size() == 1) {
                ArrayList<String> powerList = localPlugin.getPlayerPowersList(player);

                if (localPlugin.getPlayerPowersList(player).get(0) != null) {
                    switch (localPlugin.getPlayerPowersList(player).get(0)) {
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
                            cooldownMessage = wopcClass.getCooldownString(player, wopcClass.cooldowns, "Odyssey Stealer: ");
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
                    }
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


    /**
     * Modifies all HashMap<UUID, Long> fields in subclasses of the specified superclass
     * by setting the value for the given UUID to 0L.

     * @param playerUUID The UUID of the player to set to 0L.
     */
    public void modifyHashMapsInSubclasses(UUID playerUUID) {
        // Use Reflections to find all subclasses
        Reflections reflections = new Reflections("PowerClasses", new SubTypesScanner(false));
        Set<Class<? extends ParentPowerClass>> subTypes = reflections.getSubTypesOf(ParentPowerClass.class);

        // Loop through each subclass
        for (Class<?> subclass : subTypes) {
            this.getServer().getLogger().info("Inspecting class: " + subclass.getName());

            // Get all fields declared in the subclass
            Field[] fields = subclass.getDeclaredFields();
            for (Field field : fields) {
                // Check if the field is of type HashMap<UUID, Long>
                if (field.getType().equals(HashMap.class)) {
                    // Check the generic types of the HashMap
                    if (isUUIDLongHashMap(field)) {
                        field.setAccessible(true);
                        try {
                            // Get the value of the field from an instance of the subclass
                            // Assuming static fields or accessible instance of subclass is needed
                            HashMap<UUID, Long> hashMap = (HashMap<UUID, Long>) field.get(null);
                            if (hashMap != null) {
                                hashMap.put(playerUUID, 0L);
                                this.getServer().getLogger().info("Modified field: " + field.getName());
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the field is a HashMap<UUID, Long>.
     *
     * @param field The field to check.
     * @return True if the field is a HashMap<UUID, Long>, false otherwise.
     */
    private static boolean isUUIDLongHashMap(Field field) {
        // Check the generic type of the HashMap using reflection
        if (field.getGenericType().toString().equals("java.util.HashMap<java.util.UUID, java.lang.Long>")) {
            return true;
        }
        return false;
    }
}
