package com.alan.empiresOfAlan.database.dao;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TownDAO {
    private final EmpiresOfAlan plugin;

    public TownDAO(EmpiresOfAlan plugin) {
        this.plugin = plugin;
    }

    public void saveTown(Town town) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT OR REPLACE INTO towns (id, name, owner_id, balance, nation_id, spawn_world, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, tax_rate, last_tax_collection, is_public) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setString(1, town.getId().toString());
                stmt.setString(2, town.getName());
                stmt.setString(3, town.getOwnerId().toString());
                stmt.setDouble(4, town.getBankAccount().getBalance());
                stmt.setString(5, town.getNationId() != null ? town.getNationId().toString() : null);

                // Spawn location
                if (town.hasSpawn()) {
                    Location spawn = town.getSpawn();
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

                stmt.setDouble(12, town.getTaxRate());
                stmt.setLong(13, town.getLastTaxCollection());
                stmt.setBoolean(14, town.isPublic());

                stmt.executeUpdate();

                // Save residents relationship
                saveTownResidents(town);
                // Save claims relationship
                saveTownClaims(town);

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save town: " + e.getMessage());
            }
        });
    }

    private void saveTownResidents(Town town) {
        try (Connection conn = plugin.getSQLiteManager().getConnection()) {
            // Clear existing residents
            try (PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM town_residents WHERE town_id = ?")) {
                clearStmt.setString(1, town.getId().toString());
                clearStmt.executeUpdate();
            }

            // Insert residents
            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO town_residents (town_id, resident_id) VALUES (?, ?)")) {
                for (UUID residentId : town.getResidents()) {
                    insertStmt.setString(1, town.getId().toString());
                    insertStmt.setString(2, residentId.toString());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save town residents: " + e.getMessage());
        }
    }

    private void saveTownClaims(Town town) {
        try (Connection conn = plugin.getSQLiteManager().getConnection()) {
            // Clear existing claims
            try (PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM town_claims WHERE town_id = ?")) {
                clearStmt.setString(1, town.getId().toString());
                clearStmt.executeUpdate();
            }

            // Insert claims
            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO town_claims (town_id, claim_id) VALUES (?, ?)")) {
                for (UUID claimId : town.getClaims()) {
                    insertStmt.setString(1, town.getId().toString());
                    insertStmt.setString(2, claimId.toString());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save town claims: " + e.getMessage());
        }
    }

    public Map<UUID, Town> loadAllTowns() {
        Map<UUID, Town> towns = new HashMap<>();

        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM towns");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                UUID ownerId = UUID.fromString(rs.getString("owner_id"));
                Town town = new Town(id, rs.getString("name"), ownerId);

                // Set bank balance
                town.getBankAccount().setBalance(rs.getDouble("balance"));

                // Set nation
                String nationIdStr = rs.getString("nation_id");
                if (nationIdStr != null) {
                    town.setNationId(UUID.fromString(nationIdStr));
                }

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
                        town.setSpawn(spawn);
                    }
                }

                town.setTaxRate(rs.getDouble("tax_rate"));
                town.setLastTaxCollection(rs.getLong("last_tax_collection"));
                town.setPublic(rs.getBoolean("is_public"));

                towns.put(id, town);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load towns: " + e.getMessage());
        }

        // Load residents and claims for each town
        for (Town town : towns.values()) {
            loadTownResidents(town);
            loadTownClaims(town);
        }

        return towns;
    }

    private void loadTownResidents(Town town) {
        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT resident_id FROM town_residents WHERE town_id = ?")) {

            stmt.setString(1, town.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    town.addResident(UUID.fromString(rs.getString("resident_id")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load town residents: " + e.getMessage());
        }
    }

    private void loadTownClaims(Town town) {
        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT claim_id FROM town_claims WHERE town_id = ?")) {

            stmt.setString(1, town.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    town.addClaim(UUID.fromString(rs.getString("claim_id")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load town claims: " + e.getMessage());
        }
    }

    public void deleteTown(UUID townId) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection()) {
                // Delete town residents
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM town_residents WHERE town_id = ?")) {
                    stmt.setString(1, townId.toString());
                    stmt.executeUpdate();
                }

                // Delete town claims
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM town_claims WHERE town_id = ?")) {
                    stmt.setString(1, townId.toString());
                    stmt.executeUpdate();
                }

                // Delete town
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM towns WHERE id = ?")) {
                    stmt.setString(1, townId.toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete town: " + e.getMessage());
            }
        });
    }
}