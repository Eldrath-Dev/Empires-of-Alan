package com.alan.empiresOfAlan.model.enums;

public enum TownRole {
    OWNER(3, "Owner"),
    MAYOR(2, "Mayor"),
    KNIGHT(1, "Knight"),
    MEMBER(0, "Member");

    private final int level;
    private final String displayName;

    TownRole(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAtLeast(TownRole role) {
        return this.level >= role.level;
    }

    public static TownRole getByLevel(int level) {
        for (TownRole role : values()) {
            if (role.level == level) {
                return role;
            }
        }
        return MEMBER; // Default role
    }
}