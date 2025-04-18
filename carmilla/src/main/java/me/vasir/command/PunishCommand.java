package me.vasir.command;

import me.vasir.manager.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class PunishCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ceza")) return;

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Bu komut sadece bir sunucuda kullanılabilir!").setEphemeral(true).queue();
            return;
        }

        Member executor = event.getMember();
        if (executor == null) {
            event.reply("Komutu kimin kullandığını belirleyemedim!").setEphemeral(true).queue();
            return;
        }

        OptionMapping targetOption = event.getOption("kullanıcı");
        Member target = (targetOption != null) ? targetOption.getAsMember() : null;
        if (target == null) {
            event.reply("Geçerli bir kullanıcı belirtmelisin!").setEphemeral(true).queue();
            return;
        }

        OptionMapping typeOption = event.getOption("tür");
        String type = (typeOption != null) ? typeOption.getAsString().trim() : "";
        if (type.isEmpty()) {
            event.reply("Geçerli bir ceza türü belirtmelisin! (`timeout`, `ban`, `kick`, `warn`)").setEphemeral(true).queue();
            return;
        }

        ConfigManager configManager = new ConfigManager();
        String logChannelId = configManager.getConfig("logchannel");
        if (logChannelId == null) {
            event.reply("Log kanalı yapılandırılmamış! Lütfen yöneticilere bildirin.").setEphemeral(true).queue();
            return;
        }

        TextChannel logChannel = guild.getTextChannelById(logChannelId);
        if (logChannel == null) {
            event.reply("Belirtilen log kanalı bulunamadı!").setEphemeral(true).queue();
            return;
        }

        // Log embed oluştur
        EmbedBuilder logEmbed = new EmbedBuilder()
                .setTitle("Ceza Uygulandı")
                .setColor(Color.ORANGE)
                .setThumbnail(target.getUser().getEffectiveAvatarUrl())
                .addField("Yetkili", executor.getAsMention(), true)
                .addField("Cezalı Kullanıcı", target.getAsMention(), true)
                .addField("Ceza Türü", type, true)
                .setFooter("Ceza Tarihi")
                .setTimestamp(event.getTimeCreated());

        // Ceza işlemini uygula
        switch (type.toLowerCase()) {
            case "timeout":
                target.timeoutFor(Duration.ofMinutes(60)).queue();
                event.reply(target.getAsMention() + " kullanıcısı **60 dakika** susturuldu!").queue();
                break;

            case "ban":
                target.ban(0, TimeUnit.DAYS).queue();
                event.reply(target.getAsMention() + " sunucudan **banlandı!**").queue();
                break;

            case "kick":
                target.kick().queue();
                event.reply(target.getAsMention() + " sunucudan **atıldı!**").queue();
                break;

            case "warn":
                target.timeoutFor(Duration.ofMinutes(10)).queue();
                event.reply(target.getAsMention() + " uyarıldı!").queue();
                break;

            default:
                event.reply("Geçerli bir ceza türü gir! (`timeout`, `ban`, `kick`, `warn`)").setEphemeral(true).queue();
                return;
        }

        // Log mesajını gönder
        logChannel.sendMessageEmbeds(logEmbed.build()).queue();
    }
}
