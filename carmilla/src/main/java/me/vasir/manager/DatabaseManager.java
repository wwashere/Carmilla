package me.vasir.manager;

import me.vasir.data.HikariCP;
import me.vasir.data.UserStats;

import java.sql.*;

public class DatabaseManager {

    // Veritabanında kullanıcı tablosunu oluşturur, eğer tablonun zaten var olup olmadığını kontrol eder
    public void setDatabaseTable() {
        String userTableSQL = "CREATE TABLE IF NOT EXISTS user("
                + "id BIGINT PRIMARY KEY, "
                + "level INT, "
                + "xp INT)";

        try (Connection conn = HikariCP.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(userTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Database table oluşturulurken hata oluştu!", e);
        }
    }

    // Yeni bir kullanıcı verisi oluşturur
    public void createUserStats(UserStats userStats) {
        String sql = "INSERT INTO user(id, level, xp) VALUES (?, ?, ?) ";

        try (Connection conn = HikariCP.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setLong(1, userStats.getId());
            statement.setInt(2, userStats.getLevel());
            statement.setInt(3, userStats.getXp());

            statement.executeUpdate(); // Veriyi ekler
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcı verileri eklenirken/güncellenirken hata oluştu!", e);
        }
    }

    // Kullanıcı ID'si ile veritabanındaki kullanıcı bilgilerini getirir
    public UserStats findUserStatsByID(long id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = HikariCP.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new UserStats(
                        resultSet.getLong("id"),
                        resultSet.getInt("level"),
                        resultSet.getInt("xp")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcı bulunurken hata oluştu!", e);
        }
        return null;
    }

    // Kullanıcı verilerini günceller
    public void updateUserStats(UserStats playerStats) {
        String sql = "UPDATE user SET level = ?, xp = ? WHERE id = ?";

        try (Connection conn = HikariCP.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, playerStats.getLevel());
            statement.setInt(2, playerStats.getXp());
            statement.setLong(3, playerStats.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcı güncellenirken hata oluştu!", e);
        }
    }

    // Veritabanından kullanıcı verilerini alır, eğer kullanıcı yoksa yeni bir kullanıcı oluşturur
    public UserStats getUserStatsFromDatabase(long userId) {
        UserStats userStats = findUserStatsByID(userId);

        if (userStats == null) {
            userStats = new UserStats(userId, 0, 1);
            createUserStats(userStats);
        }

        return userStats;
    }
}
