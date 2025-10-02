package com.alan.empiresOfAlan.database.dao;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.model.Nation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class NationDAO {
    private final EmpiresOfAlan plugin;

    public NationDAO(EmpiresOfAlan plugin) {
        this.plugin = plugin;
    }

    public void saveNation(Nation nation) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT OR REPLACE INTO nations (id, name, capital_id, leader_id, balance, spawn_world, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, tax_rate, last_tax_collection, is_public) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setString(1, nation.getId().toString());
                stmt.setString(2, nation.getName());
                stmt.setString(3, nation.getCapitalId().toString());
                stmt.setString(4, nation.getLeaderId().toString());
                stmt.setDouble(5, nation.getBankAccount().getBalance());

                // Spawn location
                if (nation.hasSpawn()) {
                    Location spawn = nation.getSpawn();
                    stmt.setString(6, spawn.getWorld().getName());
                    stmt.setDouble(7, spawn.getX());
                    stmt.setDouble(8, spawn.getY());
                    stmt.setDouble(9, spawn.getZ());
                    stmt.setFloat(10, spawn.getYaw());
                    stmt.setFloat(11, spawn.getPitch());
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                    stmt.setNull(7, java.sql.Types.DOUBLE);
                    stmt.setNull(8, java.sql.Types.DOUBLE);
                    stmt.setNull(9, java.sql.Types.DOUBLE);
                    stmt.setNull(10, java.sql.Types.FLOAT);
                    stmt.setNull(11, java.sql.Types.FLOAT);
                }

                stmt.setDouble(12, nation.getTaxRate());
                stmt.setLong(13, nation.getLastTaxCollection());
                stmt.setBoolean(14, nation.isPublic());

                stmt.executeUpdate();

                // Save towns relationship
                saveNationTowns(nation);

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save nation: " + e.getMessage());
            }
        });
    }

    private void saveNationTowns(Nation nation) {
        try (Connection conn = plugin.getSQLiteManager().getConnection()) {
            // Clear existing towns
            try (PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM nation_towns WHERE nation_id = ?")) {
                clearStmt.setString(1, nation.getId().toString());
                clearStmt.executeUpdate();
            }

            // Insert towns
            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO nation_towns (nation_id, town_id) VALUES (?, ?)")) {
                for (UUID townId : nation.getTowns()) {
                    insertStmt.setString(1, nation.getId().toString());
                    insertStmt.setString(2, townId.toString());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save nation towns: " + e.getMessage());
        }
    }

    public Map<UUID, Nation> loadAllNations() {
        Map<UUID, Nation> nations = new HashMap<>();

        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM nations");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                UUID capitalId = UUID.fromString(rs.getString("capital_id"));
                UUID leaderId = UUID.fromString(rs.getString("leader_id"));
                Nation nation = new Nation(id, rs.getString("name"), capitalId, leaderId);

                // Set bank balance
                nation.getBankAccount().setBalance(rs.getDouble("balance"));

                // Set spawn
                String worldName = rs.getString("spawn_world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        Location spawn = new Location(world,
                                rs.getDouble("spawn_x"),
                                rs.getDouble("spawn_y"),
                                rs.getDouble("spawn_z"),
                                rs.getFloat("spawn_yaw"),
                                rs.getFloat("spawn_pitch"));
                        nation.setSpawn(spawn);
                    }
                }

                nation.setTaxRate(rs.getDouble("tax_rate"));
                nation.setLastTaxCollection(rs.getLong("last_tax_collection"));
                nation.setPublic(rs.getBoolean("is_public"));

                nations.put(id, nation);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load nations: " + e.getMessage());
        }

        // Load towns for each nation
        for (Nation nation : nations.values()) {
            loadNationTowns(nation);
        }

        return nations;
    }

    private void loadNationTowns(Nation nation) {
        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT town_id FROM nation_towns WHERE nation_id = ?")) {

            stmt.setString(1, nation.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    nation.addTown(UUID.fromString(rs.getString("town_id")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load nation towns: " + e.getMessage());
        }
    }

    public void deleteNation(UUID nationId) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection()) {
                // Delete nation towns
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM nation_towns WHERE nation_id = ?")) {
                    stmt.setString(1, nationId.toString());
                    stmt.executeUpdate();
                }

                // Delete nation
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM nations WHERE id = ?")) {
                    stmt.setString(1, nationId.toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete nation: " + e.getMessage());
            }
        });
    }
}