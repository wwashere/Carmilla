package me.vasir.manager;

import me.vasir.data.UserStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MonarchManager {

    private final Guild guild;
    private final ConfigManager configManager;
    private final String monarchRoleId;
    private final String overlordRoleId;

    public MonarchManager(Guild guild) {
        this.guild = guild;
        this.configManager = new ConfigManager();
        this.monarchRoleId = configManager.getConfig("monarch");
        this.overlordRoleId = configManager.getConfig("overlord");
    }

    public void updateMonarchIfNeeded() {
        String lastUpdateStr = configManager.getConfig("monarchupdate");

        // Debug log: Config'ten alınan son güncelleme tarihi
        if (lastUpdateStr == null) {
            System.out.println("Monarch güncelleme tarihi config'ten alınamadı.");
        } else {
            System.out.println("Son Güncelleme Tarihi (Config'ten alınan): " + lastUpdateStr);
        }

        LocalDateTime lastUpdate = LocalDateTime.MIN;
        try {
            if (lastUpdateStr != null) {
                lastUpdate = LocalDateTime.parse(lastUpdateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
            }
        } catch (Exception e) {
            System.out.println("Tarih parse işlemi hatalı: " + e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();

        // Debug log: Şu anki tarih
        System.out.println("Şu Anki Tarih: " + now);

        // 7 gün geçti mi kontrolü
        long daysBetween = Duration.between(lastUpdate, now).toDays();
        System.out.println("Son güncelleme ile şu an arasındaki gün farkı: " + daysBetween);

        if (daysBetween >= 7) {
            System.out.println("7 gün geçmiş, Monarch güncelleniyor...");
            updateMonarch();

            // Tarihi güncelle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            String formattedDate = now.format(formatter);
            configManager.setConfig("monarchupdate", formattedDate);
        } else {
            System.out.println("7 günden önce bir güncelleme yapılmış.");
        }
    }



    public void updateMonarch() {
        Role monarchRole = guild.getRoleById(monarchRoleId);
        Role overlordRole = guild.getRoleById(overlordRoleId);
        if (monarchRole == null || overlordRole == null) return;

        // Overlord’ları bul
        List<Member> overlords = guild.getMembers().stream()
                .filter(m -> m.getRoles().contains(overlordRole))
                .toList();

        if (overlords.isEmpty()) return;

        Member newMonarch = null;
        int highestXp = -1;

        DatabaseManager databaseManager = new DatabaseManager();

        for (Member member : overlords) {
            UserStats stats = databaseManager.getUserStatsFromDatabase(member.getIdLong());
            if (stats != null && stats.getXp() > highestXp) {
                highestXp = stats.getXp();
                newMonarch = member;
            }
        }

        if (newMonarch != null) {
            // Eski Monarch'lardan rolü kaldır
            for (Member m : guild.getMembersWithRoles(monarchRole)) {
                guild.removeRoleFromMember(m, monarchRole).queue();
            }

            // Yeni Monarch’a rol ver
            guild.addRoleToMember(newMonarch, monarchRole).queue();

            // Duyuru gönder
            String announcementChannelId = configManager.getConfig("chatchannel");
            TextChannel channel = guild.getTextChannelById(announcementChannelId);

            if (channel != null) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("👑 Yeni Monarch Seçildi!")
                        .setDescription(newMonarch.getAsMention() + " artık sunucunun yeni **Monarch**'ı oldu!\n\nTebrikler! 🎉")
                        .setThumbnail(newMonarch.getUser().getEffectiveAvatarUrl())
                        .setColor(new Color(218, 165, 32))
                        .setFooter("Yeni yarış başladı!", guild.getJDA().getSelfUser().getEffectiveAvatarUrl());

                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }

        // Tüm Overlord'ların XP'sini sıfırla
        for (Member member : overlords) {
            UserStats stats = databaseManager.getUserStatsFromDatabase(member.getIdLong());
            if (stats != null) {
                stats.setXp(0); // XP sıfırla
                databaseManager.updateUserStats(stats); // Veritabanına kaydet
            }
        }
    }
}
