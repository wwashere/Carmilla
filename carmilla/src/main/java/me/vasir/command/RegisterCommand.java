package me.vasir.command;

import me.vasir.data.UserStats;
import me.vasir.manager.DatabaseManager;
import me.vasir.manager.UserManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RegisterCommand extends ListenerAdapter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DatabaseManager databaseManager = new DatabaseManager();
    private final UserManager userManager = new UserManager();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("kayıt")) return;

        Member member = event.getMember();
        if (member == null) return;

        // 2 saniye gecikmeli işlem (veritabanı kontrolü + kullanıcı ayarları)
        scheduler.schedule(() -> {
            UserStats stats = databaseManager.findUserStatsByID(member.getIdLong());

            if (stats == null) {
                stats = new UserStats(member.getIdLong(), 0, 0);
                databaseManager.createUserStats(stats);
                userManager.changeUserColor(member);
                userManager.rankUp(member);
            } else {
                userManager.updateRank(member);
            }
        }, 2, TimeUnit.SECONDS);

        event.replyEmbeds(createEmbed(member).build()).queue();
    }

    private EmbedBuilder createEmbed(Member member) {
        String formattedDate = member.getTimeCreated()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss", Locale.forLanguageTag("tr")));

        return new EmbedBuilder()
                .setTitle("✅ Kayıt Başarılı!")
                .setColor(new Color(97, 59, 102))
                .setThumbnail(member.getEffectiveAvatarUrl())
                .setFooter("Profil Bilgileri", member.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .setDescription("🎉 Sunucuya hoş geldin, **" + member.getEffectiveName() + "**!")
                .addField("👤 Kullanıcı Adı", member.getEffectiveName(), true)
                .addField("📅 Hesap Açılma Tarihi", formattedDate, true);
    }
}
