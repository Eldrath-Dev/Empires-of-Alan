package com.alan.empiresOfAlan.model;

import com.alan.empiresOfAlan.model.enums.NationRole;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Resident {
    private final UUID uuid;
    private String name;
    private UUID townId;
    private TownRole townRole;
    private UUID nationId;
    private NationRole nationRole;
    private long lastOnline;
    private boolean townChat;
    private boolean nationChat;

    public Resident(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.townRole = TownRole.MEMBER;
        this.nationRole = NationRole.MEMBER;
        this.lastOnline = System.currentTimeMillis();
        this.townChat = false;
        this.nationChat = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getTownId() {
        return townId;
    }

    public void setTownId(UUID townId) {
        this.townId = townId;
    }

    public TownRole getTownRole() {
        return townRole;
    }

    public void setTownRole(TownRole townRole) {
        this.townRole = townRole;
    }

    public UUID getNationId() {
        return nationId;
    }

    public void setNationId(UUID nationId) {
        this.nationId = nationId;
    }

    public NationRole getNationRole() {
        return nationRole;
    }

    public void setNationRole(NationRole nationRole) {
        this.nationRole = nationRole;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean isInTownChat() {
        return townChat;
    }

    public void setTownChat(boolean townChat) {
        this.townChat = townChat;
    }

    public boolean isInNationChat() {
        return nationChat;
    }

    public void setNationChat(boolean nationChat) {
        this.nationChat = nationChat;
    }

    public boolean hasTown() {
        return townId != null;
    }

    public boolean hasNation() {
        return nationId != null;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void leaveTown() {
        this.townId = null;
        this.townRole = TownRole.MEMBER;
        this.townChat = false;
    }

    public void leaveNation() {
        this.nationId = null;
        this.nationRole = NationRole.MEMBER;
        this.nationChat = false;
    }

    public boolean hasTownPermission(TownRole requiredRole) {
        return hasTown() && townRole.isAtLeast(requiredRole);
    }

    public boolean hasNationPermission(NationRole requiredRole) {
        return hasNation() && nationRole.isAtLeast(requiredRole);
    }
}