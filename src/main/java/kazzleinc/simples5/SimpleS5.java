package kazzleinc.simples5;

import PowerClasses.*;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import commands.*;
import dev.iiahmed.disguise.*;
import fr.skytasul.glowingentities.GlowingEntities;
import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class SimpleS5 extends JavaPlugin implements Listener {
    public boolean pvpEnabled = true;

    public ProtocolManager protocolManager;

    public NamespacedKey powerPotionKey = new NamespacedKey(this, "power");
    public odysseyCommands odysseyClass;

    public GlowingEntities glowingEntitiesClass;

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
    public Bullseye bullseyeClass = new Bullseye(this);
    public EventPowerOne eventPowerOneClass = new EventPowerOne(this);
    public CharliPower charliPowerClass = new CharliPower(this);
    public QuakPower quakPowerClass = new QuakPower(this);
    public PowerStealerPlaceholder powerStealerPlaceholder = new PowerStealerPlaceholder(this);
    public FortniteOdyssey fortniteOdysseyClass = new FortniteOdyssey(this);
    public TestingScrollWheelPower scrollWheelTestClass = new TestingScrollWheelPower(this);

    public final DisguiseProvider provider = DisguiseManager.getProvider();

    /** Player data (powers, slot, claimed advancements). Use this, never config.yml, for player state. */
    public PowerStore powerStore;

    /** Every power, its key, display name and action-bar cooldown. Filled by registerPowers(). */
    public final PowerRegistry powers = new PowerRegistry();

    public BukkitTask secondsTask = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Player data lives in data.yml (keyed by UUID). On first start after updating, this moves
        // the old name-keyed data out of config.yml, after backing config.yml up.
        powerStore = new PowerStore(this);
        powerStore.load();
        powerStore.migrateFromConfig();
        powerStore.startAutoFlush();
        for (Player online : Bukkit.getOnlinePlayers()) powerStore.touchName(online);

        odysseyClass = new odysseyCommands(this);
        LongerPotsClass longerPotsClass = new LongerPotsClass(this);
        // Plugin startup logic
        registerPowers();

        // Every power that listens for events is registered once, straight from the registry.
        Set<ParentPowerClass> listening = Collections.newSetFromMap(new IdentityHashMap<>());
        for (PowerRegistry.PowerDefinition def : powers.all()) {
            if (def.power() instanceof Listener listener && listening.add(def.power())) {
                getServer().getPluginManager().registerEvents(listener, this);
            }
        }
        getServer().getPluginManager().registerEvents(odysseyClass, this);

        catalogueClass.startVisualHits();
        beaconatorClass.startTrackingPlayerStates();

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
        getCommand("scatter").setExecutor(new playerScatterCommand(this));

        getServer().getMessenger().registerIncomingPluginChannel(this, "odysseyclientside:power_channel", new modPacketListener(this));

        secondsTask = new BukkitRunnable() {
            @Override
            public void run() {
                //updating the cooldown display so it shows the cooldown
                updateCooldownDisplay();

                //fixing the bug with the catalogue not removing the power
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (powerStore.getEnabledPowerCount(player) == 1) {
                        powerStore.setMode(player, 0);
                    }

                    if (!powerStore.hasMode(player)) {
                        powerStore.setMode(player, 0);
                    }

                    feelsLikeHomeClass.refreshFireResistance(player);

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

        glowingEntitiesClass = new GlowingEntities(this);
    }

    @Override
    public void onDisable() {
        // Bukkit cancels this plugin's tasks after onDisable, so anything a power changed for a
        // limited time (ice spheres, boxes, golems, invisibility, Juggernaut size) is undone here.
        if (secondsTask != null) secondsTask.cancel();

        Set<ParentPowerClass> shutDown = Collections.newSetFromMap(new IdentityHashMap<>());
        for (PowerRegistry.PowerDefinition def : powers.all()) {
            if (!shutDown.add(def.power())) continue;
            try {
                def.power().onShutdown();
            } catch (RuntimeException e) {
                getLogger().log(java.util.logging.Level.WARNING, "Cleanup failed for " + def.key(), e);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPowerAttributes(player);
        }

        if (glowingEntitiesClass != null) glowingEntitiesClass.disable();

        // Write any unsaved player data before the server stops.
        if (powerStore != null) powerStore.shutdown();
    }

    /**
     * Puts back the attributes Juggernaut (Balanced Diet) changes: attack speed, size and reach.
     * They are saved with the player, so a crash or restart mid-power would otherwise keep them.
     */
    public static void resetPowerAttributes(Player player) {
        setBaseIfChanged(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED), 4.0);
        setBaseIfChanged(player.getAttribute(Attribute.GENERIC_SCALE), 1.0);
        setBaseIfChanged(player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE), 3.0);
    }

    private static void setBaseIfChanged(AttributeInstance attribute, double value) {
        if (attribute != null && attribute.getBaseValue() != value) {
            attribute.setBaseValue(value);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        resetPowerAttributes(player);
        balancedDietClass.doesSiphon.remove(player.getUniqueId());
    }

    @EventHandler
    public void onAdvancementMade(PlayerAdvancementDoneEvent event) {
        List<String> takenPowers = Arrays.asList("");
        Advancement advancement = event.getAdvancement();
        String advName = advancement.getKey().getKey();
        Player player = event.getPlayer();


        if (powerStore.getPowerTakenStatus().getOrDefault(advName.split("/")[1], false)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.5f);
            player.sendMessage(ChatColor.RED + "You gained the advancement, but was not granted the power because someone else has it.");
        } else if (powerStore.isCapped(advName.split("/")[1])) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.5f);
            player.sendMessage("Nice try, but this power has already been redeemed.");
        } else {
            for (String keys : getConfig().getConfigurationSection("defaults").getKeys(false)) {
                if (keys.equals(advName.split("/")[1]) && !advName.equals("adventure/bullseye")) {
                    grantAdvancementPower(advancement, player, playerIsAtPowerLimit(player));
                    powerStore.setCapped(advName.split("/")[1], true);
                    break;
                }
            }
        }


    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (!powerStore.hasPowersSection(player)) return;

        for (String keys : new ArrayList<>(powerStore.getPowerKeys(player))) {

            if (keys.equals("nether/ride_strider_in_overworld_lava")) {
                feelsLikeHomeClass.removeFireResistance(player);
            }

            if (powerStore.hasPower(player, keys)) {
                player.getWorld().dropItem(player.getLocation(), new PowerPotionItem(this, getAdvancementNameFormattedFromUnformattedString(keys), powerPotionKey).getItemStack());
                removePlayerAdvancement(player, keys);
                powerStore.setPower(player, keys, false);
            }

        }
    }

    //I have to do this because I cant set an attribute on someone in the pause screen for some reason
    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        getServer().getScheduler().runTaskLater(this, () -> {
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        }, 5);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        powerStore.touchName(player);

        // Fallback for the quit handler, which doesn't run if the server stopped or crashed.
        resetPowerAttributes(player);

        // join-aliases in config.yml: show another name in this player's join message.
        // If the server's config.yml has no join-aliases yet, the list built into the jar is used.
        // Once it has one (even {}), only that list counts, so it can be emptied to turn this off.
        String aliasPath = "join-aliases." + player.getName();
        String alias = getConfig().isSet("join-aliases") ? getConfig().getString(aliasPath, null) : getConfig().getString(aliasPath);
        if (alias != null && !alias.isBlank()) {
            event.setJoinMessage(ChatColor.YELLOW + alias + " joined the server");
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //Handling the clicking of the item if it is a power item
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            if (event.getItem() != null) {
                ItemStack item = event.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;

                String itemPowerKey = meta.getPersistentDataContainer().get(powerPotionKey, PersistentDataType.STRING);

                // Checked first: a power you already hold also counts as "taken", so this used to
                // say "someone else has it", and the "already have" branch below could never run.
                String itemAdvancementKey = itemPowerKey == null ? null : getAdvancementKeyFromFormattedString(itemPowerKey);
                if (itemAdvancementKey != null && item.getType() == Material.AMETHYST_SHARD && event.getHand() == EquipmentSlot.HAND && powerStore.hasPower(player, itemAdvancementKey)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.f, 1.f);
                    player.sendTitle(ChatColor.RED + "Unable to Equip!", ChatColor.RED + "You already have this power!", 10, 60, 10);
                    return;
                }

                if (meta != null && item.getType() == Material.AMETHYST_SHARD && item.getItemMeta().getPersistentDataContainer().getKeys().contains(powerPotionKey) && event.getHand() == EquipmentSlot.HAND && !powerStore.getPowerTakenStatus().getOrDefault(getAdvancementNameUnformattedFromFormattedString(itemPowerKey), false)) {
                    if (!playerIsAtPowerLimit(player)) {

                        powerStore.setCapped(getAdvancementKeyFromFormattedString(itemPowerKey), true);

                        switch (getAdvancementKeyFromFormattedString(itemPowerKey)) {
                            case "adventure/bullseye" -> {

                                powerStore.setPower(player, "adventure/bullseye", true);

                                World world = player.getWorld();
                                new BukkitRunnable() {
                                    int i = 0;

                                    @Override
                                    public void run() {
                                        if (i < 3) {
                                            world.strikeLightningEffect(player.getLocation());
                                            i++;
                                        } else {
                                            this.cancel();
                                        }
                                    }
                                }.runTaskTimer(this, 0, 10);

                                player.getWorld().setStorm(true);
                            }
                            case "events/charlis_odyssey" -> {
                                powerStore.setPower(player, "events/charlis_odyssey", true);

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 1.f);
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "Charli4K's Odyssey Equipped...");
                            }
                            case "events/quaks_odyssey" -> {
                                powerStore.setPower(player, "events/quaks_odyssey", true);

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 1.f);
                                player.sendMessage(ChatColor.YELLOW + "QuakX's Odyssey Equipped...");
                            }
                            case "events/fortnite_odyssey" -> {
                                powerStore.setPower(player, "events/fortnite_odyssey", true);

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 1.f);
                            }
                            default -> {
                                    grantAdvancementPower(grantAdvancement(player, getAdvancementKeyFromFormattedString(itemPowerKey)), player, false);
                            }
                        }

                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.f, 1.f);

                        player.sendTitle(ChatColor.RED + "Unable to Equip!", ChatColor.RED + "You have 2 powers!", 10, 60, 10);
                    }
                } else if (itemPowerKey != null && powerStore.getPowerTakenStatus().getOrDefault(getAdvancementNameUnformattedFromFormattedString(itemPowerKey), false)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.5f);
                    player.sendMessage(ChatColor.RED + "You were not granted the power because someone else has it.");
                }
            }
        }
    }

    /** True if the stack is a dropped power item. Safe for items with no meta. */
    public boolean isPowerItem(ItemStack stack) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(powerPotionKey);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item) {
            Item item = (Item) event.getEntity();

            if (isPowerItem(item.getItemStack())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Item) {
            Item item = (Item) event.getEntity();

            if (isPowerItem(item.getItemStack()) && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
        }

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();

            if (!pvpEnabled && !damager.isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType().equals(Material.MACE) && !powerStore.isMaceCrafted()) {
            event.getWhoClicked().sendMessage(ChatColor.GREEN + "You were the first person to get the Mace, and no one can craft it anymore.");
            powerStore.setMaceCrafted(true);
        }

    }

    @EventHandler
    public void prepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult().getType().equals(Material.MACE) && powerStore.isMaceCrafted()) {
            event.getInventory().setResult((ItemStack) null);
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

    @EventHandler
    public void preventRenaming(PrepareAnvilEvent event) {
        ItemStack original = event.getInventory().getFirstItem();
        if (original == null || original.getItemMeta().getDisplayName() == null || event.getResult() == null) return;
        String displayName = original.getItemMeta().getDisplayName();
        if (original.getPersistentDataContainer().has(powerPotionKey)) {
            ItemMeta resultMeta = event.getResult().getItemMeta();
            resultMeta.setDisplayName(displayName);
            event.getResult().setItemMeta(resultMeta);
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) throws ReflectiveOperationException {
        Player player = event.getPlayer();

        if (event.getItemDrop().getItemStack().getType() == Material.STRUCTURE_VOID) {
            event.setCancelled(true);
        }

        if (event.getItemDrop().getPersistentDataContainer().has(powerPotionKey)) {
            glowingEntitiesClass.setGlowing(event.getItemDrop(), player, ChatColor.GOLD);
        }
    }


    /** Enabled power keys for the player, or null if they have no powers entry. */
    public ArrayList<String> getPlayerPowersList(Player player) {
        return powerStore.getEnabledPowers(player);
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
        String powerName = powers.displayName(getAdvancementNameUnformatted(advancement));

        if (isAtPowerLimit) {
            player.sendTitle("New Power Collected!: " + powerName, ChatColor.RED + "But it has been dropped, you have 2 powers!");

            player.getWorld().dropItem(player.getLocation(), new PowerPotionItem(this, getAdvancementNameFormattedFromAdvancement(advancement), powerPotionKey).getItemStack());

        } else {
            player.sendTitle("New Power Collected!: " + powerName, "type " + ChatColor.GREEN + "\"/od powers\"" + ChatColor.RESET + " for details.");

            powerStore.setPower(player, getAdvancementNameUnformatted(advancement), true);
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
        return powerStore.getEnabledPowerCount(player) >= 2;
    }

    public boolean playerHasPower(Player player, String powerKey) {
        return powerStore.hasPower(player, powerKey);
    }

    public void updateCooldownDisplay() {
        for (Player player : getServer().getOnlinePlayers()) {
            String cooldownMessage = "";
            ArrayList<String> powerList = getPlayerPowersList(player);

            // selectedSlot resets a stale or corrupt slot, which would otherwise throw every second.
            int slot = selectedSlot(player, powerList);
            if (slot >= 0) {
                cooldownMessage = switchOnPowers(player, powerList.get(slot));
            }

            player.sendActionBar(cooldownMessage);
        }
    }

    public static double roundDecimalNumber(double number, int decimalPlaces) {
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

    /**
     * The one list of powers. Keys, cooldown maps and labels are the ones the old switchOnPowers /
     * PowerTwoCommand switches used; display names keep the old renames (e.g. All Effects ->
     * "How Did We Get Here?"), and every other power keeps its title-cased advancement name.
     */
    public void registerPowers() {
        powers.register("adventure/very_very_frightening", "Very Very Frightening", vvfClass, "Dash: ", () -> vvfClass.cooldowns);
        powers.register("nether/all_effects", "How Did We Get Here?", hdwghClass, "Item Disable: ", () -> hdwghClass.rightClickedCooldowns);
        powers.register("husbandry/complete_catalogue", "Complete Catalogue", catalogueClass, "Aural Barrage: ", () -> catalogueClass.auralBarrageCooldowns);
        powers.register("adventure/kill_all_mobs", "Monsters Hunted", monstersClass, "Domain Expansion: ", () -> monstersClass.sphereCooldowns);
        powers.register("adventure/sniper_duel", "Sniper Duel", sniperDuelClass, "Sniper Vision: ", () -> sniperDuelClass.cooldowns);
        powers.register("nether/uneasy_alliance", "Uneasy Alliance", uneasyAllianceClass, "Invisibility: ", () -> uneasyAllianceClass.cooldowns);
        powers.register("husbandry/froglights", "WOPC", wopcClass, "Tornado: ", () -> wopcClass.tornadoCooldowns);
        powers.register("adventure/summon_iron_golem", "Hired Help", hiredHelpClass, "Hired Help: ", () -> hiredHelpClass.cooldowns);
        powers.register("nether/ride_strider_in_overworld_lava", "Feels Like Home", feelsLikeHomeClass, "Blazed: ", () -> feelsLikeHomeClass.cooldowns);
        powers.register("nether/create_full_beacon", "Beaconator", beaconatorClass, "Beaconator: ", () -> beaconatorClass.cooldowns);
        powers.register("husbandry/balanced_diet", "Balanced Diet", balancedDietClass, "Stored Energy: ", () -> balancedDietClass.cooldowns);
        powers.register("end/dragon_egg", "The Next Generation", nextGenerationClass, "Ground Pound: ", () -> nextGenerationClass.cooldowns);
        powers.register("adventure/bullseye", "Bullseye", bullseyeClass, "Sonic Boom: ", () -> bullseyeClass.sonicBoomCooldowns);
        powers.register("events/event_power_one", "Event Power One", eventPowerOneClass, "Aural Barrage: ", () -> eventPowerOneClass.auralBarrageCooldowns);
        powers.register("events/charlis_odyssey", "Charlis Odyssey", charliPowerClass, "Dash: ", () -> charliPowerClass.cooldowns);
        powers.register("events/quaks_odyssey", "Quaks Odyssey", quakPowerClass, "Double Jump: ", () -> quakPowerClass.cooldowns);
        powers.register("events/fortnite_odyssey", "Fortnite Odyssey", fortniteOdysseyClass, "Box Up: ", () -> fortniteOdysseyClass.fullBoxCooldowns);
        // Work-in-progress scroll-wheel power: only loaded when enable-test-power is true in config.yml.
        if (getConfig().getBoolean("enable-test-power", false)) {
            powers.register("adventure/trim_with_all_exclusive_armor_patterns", "Trim With All Exclusive Armor Patterns", scrollWheelTestClass, "Not using this rn lmao: ", () -> scrollWheelTestClass.cooldowns);
        }
        // Placeholder slot a WOPC victim holds while their power is stolen; shows when it comes back.
        powers.register("player/power_stolen", "Power Stolen", powerStealerPlaceholder, "", () -> wopcClass.playerGetsPowerBackTime);
    }

    /**
     * Index into the player's enabled powers for their selected slot, reset to 0 if it is out of
     * range. Returns -1 if they have no enabled powers. Slots are numbered 0 and 1 everywhere.
     */
    public int selectedSlot(Player player, List<String> enabled) {
        if (enabled == null || enabled.isEmpty()) return -1;
        int mode = powerStore.getMode(player);
        if (mode < 0 || mode >= enabled.size()) {
            mode = 0;
            powerStore.setMode(player, 0);
        }
        return mode;
    }

    /**
     * Switches between the player's two powers (/power1 and the client mod's power1 key).
     * Returns the key of the power now selected, or null if they have none.
     */
    public String cyclePowerSlot(Player player) {
        ArrayList<String> enabled = powerStore.getEnabledPowers(player);
        if (enabled == null) return null;

        int mode = powerStore.getMode(player);
        if (enabled.size() == 2) {
            if (mode == 0) {
                powerStore.setMode(player, 1);
            } else if (mode == 1) {
                powerStore.setMode(player, 0);
            }
        } else if (enabled.size() < 2) {
            powerStore.setMode(player, 0);
        }

        updateCooldownDisplay();

        int slot = selectedSlot(player, enabled);
        return slot < 0 ? null : enabled.get(slot);
    }

    /** Uses the player's selected power (/power2 and the client mod's power2 key). */
    public void usePower(Player player) {
        ArrayList<String> enabled = powerStore.getEnabledPowers(player);
        int slot = selectedSlot(player, enabled);
        if (slot < 0) {
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.1f);
            player.sendActionBar(ChatColor.RED + "You don't have any powers.");
            return;
        }

        PowerRegistry.PowerDefinition def = powers.get(enabled.get(slot));
        if (def == null) {
            getLogger().warning("Power Use was called on player [" + player.getName() + "] for '" + enabled.get(slot) + "', but no such power is registered.");
            return;
        }

        def.power().action(player);
        updateCooldownDisplay();
    }

    /** Action-bar cooldown text for one of the player's powers. */
    public String switchOnPowers(Player player, String cooldownKey) {
        PowerRegistry.PowerDefinition def = powers.get(cooldownKey);
        return def == null ? "" : def.cooldownString(player);
    }
}
