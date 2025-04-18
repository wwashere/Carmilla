package me.vasir.manager;

import me.vasir.data.UserStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UserManager {

    private final DatabaseManager databaseManager = new DatabaseManager();

    public void addXp(Member member) {
        addXp(member, 0);
    }

    // Kullanıcıya belirli bir miktarda XP ekler
    public void addXp(Member member, int xp) {
        UserStats userStats = databaseManager.getUserStatsFromDatabase(member.getIdLong());

        if (userStats.getLevel() >= 6) return;

        Random random = new Random();
        int newXp = random.nextInt(6) + 6;
        int oldXp = userStats.getXp();

        userStats.setXp(oldXp + newXp + xp);
        databaseManager.updateUserStats(userStats);

        // Seviye atlama gereksinimleri
        int[] levelThresholds = {64, 384, 1432, 8246};  // Seviyeler için XP eşik değerleri

        for (int levelThreshold : levelThresholds) {
            if (oldXp < levelThreshold && userStats.getXp() >= levelThreshold) {
                rankUp(member);
            }
        }
    }

    // Config dosyasından rol ID'si alır ve bunu Guild'deki Role nesnesine dönüştürür
    public Role getRoleFromConfig(Guild guild, String roleKey) {
        ConfigManager configManager = new ConfigManager();
        String roleId = configManager.getConfig(roleKey);

        if (roleId != null) {
            return guild.getRoleById(roleId);
        }

        return null;
    }

    // Kullanıcının seviyesini kontrol eder ve gerekirse rütbe atlar
    public void rankUp(Member member) {
        long userId = member.getIdLong();
        UserStats userStats = databaseManager.findUserStatsByID(userId);

        if (userStats.getLevel() == 5) return;

        rankUpDatabase(userId, userStats.getLevel());
        updateRank(member);
        sendLevelUpMessage(member, userStats.getLevel());
    }

    // Kullanıcının seviyesine göre rolünü günceller
    public void updateRank(Member member) {
        Guild guild = member.getGuild();
        long userId = member.getIdLong();
        UserStats userStats = databaseManager.findUserStatsByID(userId);

        // Kullanıcının seviyesine göre yeni rolü alır
        Role rank = switch (userStats.getLevel()) {
            case 1 -> getRoleFromConfig(guild, "wanderer");
            case 2 -> getRoleFromConfig(guild, "hunter");
            case 3 -> getRoleFromConfig(guild, "knight");
            case 4 -> getRoleFromConfig(guild, "vanguard");
            case 5 -> getRoleFromConfig(guild, "overlord");
            default -> null;
        };

        // Eğer yeni rol geçerli bir rolse kullanıcıya ekler
        if (rank != null) {
            guild.addRoleToMember(member, rank).queue();
        }

        // Kullanıcının eski rolünü kaldırır
        List<String> rankRoleKeys = Arrays.asList("wanderer", "hunter", "knight", "vanguard", "overlord");
        for (String roleKey : rankRoleKeys) {
            Role role = getRoleFromConfig(guild, roleKey);
            if (role != null && member.getRoles().contains(role)) {
                guild.removeRoleFromMember(member, role).queue();
            }
        }
    }

    // Kullanıcının seviyesini veritabanında günceller
    private void rankUpDatabase(long userId, int level) {
        UserStats userStats = databaseManager.getUserStatsFromDatabase(userId);

        if (userStats.getLevel() == 5) return;

        userStats.setLevel(level + 1);
        databaseManager.updateUserStats(userStats);
    }

    // Kullanıcının rengini rastgele bir şekilde değiştirir
    public void changeUserColor(Member member) {
        Guild guild = member.getGuild();
        Random random = new Random();
        int color = random.nextInt(9);

        // Renk rolleri listesi
        List<String> colorRoleKeys = Arrays.asList("diamond", "ruby", "emerald", "amber", "sapphire", "topaz", "azurite", "amethyst", "malachite");

        // Kullanıcının sahip olduğu renk rollerini kaldırır
        for (String roleKey : colorRoleKeys) {
            Role role = getRoleFromConfig(guild, roleKey);
            if (role != null && member.getRoles().contains(role)) {
                guild.removeRoleFromMember(member, role).queue();
            }
        }

        // Yeni rastgele renk rolünü atar
        Role newRole = switch (color) {
            case 0 -> getRoleFromConfig(guild, "diamond");
            case 1 -> getRoleFromConfig(guild, "ruby");
            case 2 -> getRoleFromConfig(guild, "emerald");
            case 3 -> getRoleFromConfig(guild, "amber");
            case 4 -> getRoleFromConfig(guild, "sapphire");
            case 5 -> getRoleFromConfig(guild, "topaz");
            case 6 -> getRoleFromConfig(guild, "azurite");
            case 7 -> getRoleFromConfig(guild, "amethyst");
            case 8 -> getRoleFromConfig(guild, "malachite");
            default -> null;
        };

        if (newRole != null) {
            guild.addRoleToMember(member, newRole).queue();
        }
    }

    // Seviye atlama mesajını gönderir
    public void sendLevelUpMessage(Member member, int oldLevel) {
        ConfigManager configManager = new ConfigManager();
        int newLevel = oldLevel + 1;
        Guild guild = member.getGuild();
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(new Color(97, 59, 102));
        embed.setTitle("Tebrikler!");

        // Seviye isimleri
        String rankName = switch (newLevel) {
            case 1 -> "Wanderer";
            case 2 -> "Hunter";
            case 3 -> "Knight";
            case 4 -> "Vanguard";
            case 5 -> "Overlord";
            default -> "Bilinmeyen Rütbe";
        };

        embed.setDescription(
                member.getAsMention() + " rütbe atladı ve artık **" + rankName + "** rütbesinde!"
        );

        embed.setTimestamp(Instant.now());

        TextChannel levelUpChannel = guild.getTextChannelById(configManager.getConfig("chatchannel"));

        if (levelUpChannel == null) {
            System.out.println("Level up mesajı için kanal bulunamadı.");
        } else {
            levelUpChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
