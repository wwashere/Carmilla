package me.vasir.command;

import me.vasir.data.UserStats;
import me.vasir.manager.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;

public class LevelCommand extends ListenerAdapter {

    private final DatabaseManager databaseManager = new DatabaseManager();

    // XP eşikleri
    private final int[] levelThresholds = {64, 384, 1432, 8246};

    // Seviye isimleri
    private final String[] levelNames = {
            "Wanderer", // 1. Seviye
            "Hunter",   // 2. Seviye
            "Knight",   // 3. Seviye
            "Vanguard", // 4. Seviye
            "Overlord"  // 5. Seviye
    };

    // Cooldown takibi
    private final HashMap<Long, Long> cooldowns = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("seviye")) return;

        long userId = event.getUser().getIdLong();
        long now = System.currentTimeMillis();

        // Cooldown kontrolü
        if (cooldowns.containsKey(userId)) {
            long lastUsed = cooldowns.get(userId);
            // 10 dakika (milisaniye cinsinden)
            long COOLDOWN_TIME = 10 * 60 * 1000;
            if (now - lastUsed < COOLDOWN_TIME) {
                long remainingTime = (COOLDOWN_TIME - (now - lastUsed)) / 1000;
                long minutes = remainingTime / 60;
                long seconds = remainingTime % 60;
                event.reply("Bu komutu tekrar kullanabilmen için **" + minutes + " dakika " + seconds + " saniye** beklemelisin!").setEphemeral(true).queue();
                return;
            }
        }

        UserStats userStats = databaseManager.getUserStatsFromDatabase(userId);

        if (userStats == null) {
            event.reply("Kayıt bulunamadı!").setEphemeral(true).queue();
            return;
        }

        int currentLevel = userStats.getLevel();
        int currentXp = userStats.getXp();

        // Max seviyeye ulaştı mı?
        if (currentLevel >= 5) {
            EmbedBuilder maxEmbed = new EmbedBuilder()
                    .setTitle("📊 Seviye Bilgisi")
                    .setColor(new Color(97, 59, 102))
                    .setThumbnail(event.getUser().getEffectiveAvatarUrl())
                    .addField("👤 Kullanıcı", event.getUser().getAsMention(), false)
                    .addField("🆙 Seviye", levelNames[4] + " (Maksimum)", true)
                    .addField("✨ XP", String.valueOf(currentXp), true)
                    .addField("🥳 Durum", "Maksimum seviyeye ulaştın!", false)
                    .setFooter("Carmilla Bot", event.getJDA().getSelfUser().getEffectiveAvatarUrl());

            event.replyEmbeds(maxEmbed.build()).queue();

            cooldowns.put(userId, now); // Cooldown kaydet
            return;
        }

        int nextLevelXp = levelThresholds[currentLevel - 1];
        int xpToNextLevel = nextLevelXp - currentXp;

        // XP barı hazırlama
        String xpBar = generateXpBar(currentXp, nextLevelXp);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 Seviye Bilgisi")
                .setColor(new Color(97, 59, 102))
                .setThumbnail(event.getUser().getEffectiveAvatarUrl())
                .addField("👤 Kullanıcı", event.getUser().getAsMention(), false)
                .addField("🆙 Seviye", levelNames[currentLevel - 1], true)
                .addField("✨ XP", currentXp + " / " + nextLevelXp, true)
                .addField("📈 Kalan XP", xpToNextLevel + " XP", true)
                .addField("⏳ Seviye Durumu", xpBar, false)
                .setFooter("Carmilla Bot", event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        event.replyEmbeds(embed.build()).queue();

        cooldowns.put(userId, now); // Cooldown kaydet
    }

    // XP çubuğu oluşturucu
    private String generateXpBar(int currentXp, int nextLevelXp) {
        int barLength = 20;
        int filledLength = (int) ((double) currentXp / nextLevelXp * barLength);
        String filled = "█".repeat(filledLength);
        String empty = "░".repeat(barLength - filledLength);
        return filled + empty;
    }
}
