package com.alan.empiresOfAlan.database.dao;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.enums.ClaimFlag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClaimDAO {
    private final EmpiresOfAlan plugin;

    public ClaimDAO(EmpiresOfAlan plugin) {
        this.plugin = plugin;
    }

    public void saveClaim(Claim claim) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection()) {
                // Save claim
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO claims (id, world, x, z, town_id) VALUES (?, ?, ?, ?, ?)")) {

                    stmt.setString(1, claim.getId().toString());
                    stmt.setString(2, claim.getWorldName());
                    stmt.setInt(3, claim.getX());
                    stmt.setInt(4, claim.getZ());
                    stmt.setString(5, claim.getTownId().toString());

                    stmt.executeUpdate();
                }

                // Save claim flags
                saveClaimFlags(claim);

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save claim: " + e.getMessage());
            }
        });
    }

    private void saveClaimFlags(Claim claim) {
        try (Connection conn = plugin.getSQLiteManager().getConnection()) {
            // Clear existing flags
            try (PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM claim_flags WHERE claim_id = ?")) {
                clearStmt.setString(1, claim.getId().toString());
                clearStmt.executeUpdate();
            }

            // Insert flags
            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO claim_flags (claim_id, flag, value) VALUES (?, ?, ?)")) {
                for (Map.Entry<ClaimFlag, Boolean> entry : claim.getFlags().entrySet()) {
                    insertStmt.setString(1, claim.getId().toString());
                    insertStmt.setString(2, entry.getKey().getId());
                    insertStmt.setBoolean(3, entry.getValue());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save claim flags: " + e.getMessage());
        }
    }

    public Map<UUID, Claim> loadAllClaims() {
        Map<UUID, Claim> claims = new HashMap<>();

        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM claims");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                UUID townId = UUID.fromString(rs.getString("town_id"));
                Claim claim = new Claim(id, rs.getString("world"), rs.getInt("x"), rs.getInt("z"), townId);

                // Load flags
                loadClaimFlags(claim);

                claims.put(id, claim);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load claims: " + e.getMessage());
        }

        return claims;
    }

    private void loadClaimFlags(Claim claim) {
        try (Connection conn = plugin.getSQLiteManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT flag, value FROM claim_flags WHERE claim_id = ?")) {

            stmt.setString(1, claim.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String flagId = rs.getString("flag");
                    boolean value = rs.getBoolean("value");
                    ClaimFlag flag = ClaimFlag.getById(flagId);
                    if (flag != null) {
                        claim.setFlag(flag, value);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load claim flags: " + e.getMessage());
        }
    }

    public void deleteClaim(UUID claimId) {
        plugin.getAsyncExecutor().runAsync(() -> {
            try (Connection conn = plugin.getSQLiteManager().getConnection()) {
                // Delete claim flags
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM claim_flags WHERE claim_id = ?")) {
                    stmt.setString(1, claimId.toString());
                    stmt.executeUpdate();
                }

                // Delete claim
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM claims WHERE id = ?")) {
                    stmt.setString(1, claimId.toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete claim: " + e.getMessage());
            }
        });
    }
}