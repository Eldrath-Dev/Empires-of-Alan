package com.alan.empiresOfAlan.model;

import com.alan.empiresOfAlan.model.enums.ClaimFlag;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Claim {
    private final UUID id;
    private final String worldName;
    private final int x;
    private final int z;
    private UUID townId;
    private final Map<ClaimFlag, Boolean> flags;

    public Claim(UUID id, String worldName, int x, int z, UUID townId) {
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.z = z;
        this.townId = townId;
        this.flags = new HashMap<>();

        // Initialize with default values
        for (ClaimFlag flag : ClaimFlag.values()) {
            flags.put(flag, flag.getDefaultValue());
        }
    }

    public UUID getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public UUID getTownId() {
        return townId;
    }

    public void setTownId(UUID townId) {
        this.townId = townId;
    }

    public Map<ClaimFlag, Boolean> getFlags() {
        return new HashMap<>(flags); // Return a copy
    }

    public boolean getFlag(ClaimFlag flag) {
        return flags.getOrDefault(flag, flag.getDefaultValue());
    }

    public void setFlag(ClaimFlag flag, boolean value) {
        flags.put(flag, value);
    }

    /**
     * Get the Bukkit Chunk object for this claim
     *
     * @return The Chunk object, or null if the world doesn't exist
     */
    public Chunk getChunk() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return world.getChunkAt(x, z);
    }

    /**
     * Check if a chunk matches this claim
     *
     * @param chunk The chunk to check
     * @return true if the chunk matches this claim
     */
    public boolean matches(Chunk chunk) {
        return chunk.getWorld().getName().equals(worldName) &&
                chunk.getX() == x &&
                chunk.getZ() == z;
    }

    /**
     * Creates a unique key for this claim's location
     *
     * @return A string key in the format "worldName:x:z"
     */
    public String getLocationKey() {
        return worldName + ":" + x + ":" + z;
    }

    /**
     * Creates a unique key for a chunk's location
     *
     * @param chunk The chunk
     * @return A string key in the format "worldName:x:z"
     */
    public static String getLocationKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Claim other = (Claim) obj;
        return worldName.equals(other.worldName) && x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * worldName.hashCode() + x) + z;
    }
}