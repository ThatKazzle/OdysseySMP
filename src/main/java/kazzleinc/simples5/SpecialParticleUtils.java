package kazzleinc.simples5;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class SpecialParticleUtils {
    SimpleS5 plugin;

    public SpecialParticleUtils(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    public void sendClientDustParticle(Player player, Location location, Particle.DustOptions dustOptions, int amount) {
        try {
            // Create a packet container for the particle effect
            PacketContainer packet = plugin.protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);

            // Set the particle type to DUST
            packet.getParticles().write(0, EnumWrappers.Particle.REDSTONE);

            // Set the particle location (XYZ)
            packet.getDoubles()
                    .write(0, location.getX())
                    .write(1, location.getY())
                    .write(2, location.getZ());

            // Set the dust options for color and size
            WrappedParticle<Particle.DustOptions> wrappedParticle = WrappedParticle.create(Particle.DUST, dustOptions);
            packet.getNewParticles().write(0, wrappedParticle);

            // Set the particle count
            packet.getIntegers().write(0, amount);

            // No offsets or speed needed for dust particles, but they are required fields in the packet
            packet.getFloat()
                    .write(0, 0f) // Offset X
                    .write(1, 0f) // Offset Y
                    .write(2, 0f) // Offset Z
                    .write(3, 1f); // Particle speed

            // Send the packet to the player
            plugin.protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
