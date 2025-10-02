package com.alan.empiresOfAlan.model.enums;

public enum ClaimFlag {
    PVP("pvp", "Allow PvP combat in the claim", false),
    EXPLOSIONS("explosions", "Allow explosions in the claim", false),
    MOB_SPAWNING("mob-spawning", "Allow mob spawning in the claim", true),
    FIRE_SPREAD("fire-spread", "Allow fire to spread in the claim", false),
    BUILD("build", "Allow building by non-town members", false),
    INTERACT("interact", "Allow interactions by non-town members", false),
    PUBLIC_SPAWN("public-spawn", "Allow anyone to teleport to this claim", false);

    private final String id;
    private final String description;
    private final boolean defaultValue;

    ClaimFlag(String id, String description, boolean defaultValue) {
        this.id = id;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public static ClaimFlag getById(String id) {
        for (ClaimFlag flag : values()) {
            if (flag.id.equals(id)) {
                return flag;
            }
        }
        return null;
    }
}