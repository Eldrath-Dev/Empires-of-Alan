package com.alan.empiresOfAlan.model.enums;

public enum NationRole {
    KING(3, "King"),
    OFFICER(2, "Officer"),
    KNIGHT(1, "Knight"),
    MEMBER(0, "Member");

    private final int level;
    private final String displayName;

    NationRole(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAtLeast(NationRole role) {
        return this.level >= role.level;
    }

    public static NationRole getByLevel(int level) {
        for (NationRole role : values()) {
            if (role.level == level) {
                return role;
            }
        }
        return MEMBER; // Default role
    }
}