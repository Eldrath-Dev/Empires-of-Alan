package com.alan.empiresOfAlan.database.dao;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.enums.NationRole;
import com.alan.empiresOfAlan.model.enums.TownRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResidentDAO {
    private final EmpiresOfAlan plugin;

    public ResidentDAO(EmpiresOfAlan plugin) {
        this.plugin = plugin;
    }

    public void saveResident(Resident resident) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT OR REPLACE INTO residents (uuid, name, town_id, town_role, nation_id, nation_role, last_online) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setString(1, resident.getUuid().toString());
                stmt.setString(2, resident.getName());
                stmt.setString(3, resident.getTownId() != null ? resident.getTownId().toString() : null);
                stmt.setInt(4, resident.getTownRole().getLevel());
                stmt.setString(5, resident.getNationId() != null ? resident.getNationId().toString() : null);
                stmt.setInt(6, resident.getNationRole().getLevel());
                stmt.setLong(7, resident.getLastOnline());

                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save resident: " + e.getMessage());
            }
        });
    }

    public Map<UUID, Resident> loadAllResidents() {
        Map<UUID, Resident> residents = new HashMap<>();

        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM residents");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                Resident resident = new Resident(uuid, rs.getString("name"));

                String townIdStr = rs.getString("town_id");
                if (townIdStr != null) {
                    resident.setTownId(UUID.fromString(townIdStr));
                }

                resident.setTownRole(TownRole.getByLevel(rs.getInt("town_role")));

                String nationIdStr = rs.getString("nation_id");
                if (nationIdStr != null) {
                    resident.setNationId(UUID.fromString(nationIdStr));
                }

                resident.setNationRole(NationRole.getByLevel(rs.getInt("nation_role")));
                resident.setLastOnline(rs.getLong("last_online"));

                residents.put(uuid, resident);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load residents: " + e.getMessage());
        }

        return residents;
    }

    public void deleteResident(UUID uuid) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM residents WHERE uuid = ?")) {

                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete resident: " + e.getMessage());
            }
        });
    }
}