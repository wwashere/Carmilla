package me.vasir.handler;

import me.vasir.manager.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;

public class GuildHandler extends ListenerAdapter {

    private final ConfigManager configManager = new ConfigManager();

    // Mesaj geldiğinde, belirli bir kanalda botun mesajlarını siler
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getChannel().getId().equals(configManager.getConfig("regchannel"))) {
            if (!e.getAuthor().isBot()) {
                e.getMessage().delete().queue();
            }
        }
    }

    // Sunucuya yeni bir üye katıldığında, katılımı loglar
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        TextChannel logChannel = e.getGuild().getTextChannelById(configManager.getConfig("joinchannel"));
        if (logChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Kullanıcı Katıldı")
                .setDescription("Kullanıcı: " + e.getUser().getName() + " (" + e.getUser().getId() + ")")
                .setColor(Color.GREEN)
                .setThumbnail(e.getUser().getEffectiveAvatarUrl())
                .addField("Id", e.getUser().getId(), true)
                .setTimestamp(Instant.now());

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    // Sunucudan bir üye ayrıldığında, ayrılma durumunu loglar
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
        TextChannel logChannel = e.getGuild().getTextChannelById(configManager.getConfig("joinchannel"));
        if (logChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Kullanıcı Ayrıldı")
                .setDescription("Kullanıcı Adı: " + e.getUser().getAsTag())
                .setColor(Color.RED)
                .setThumbnail(e.getUser().getEffectiveAvatarUrl())
                .addField("Id", e.getUser().getId(), true)
                .setTimestamp(Instant.now());

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    // Sunucuya boost atıldığında, boost olayını loglar
    @Override
    public void onGuildMemberUpdateBoostTime(GuildMemberUpdateBoostTimeEvent event) {
        if (event.getOldTimeBoosted() == null && event.getNewTimeBoosted() != null) {
            Member member = event.getMember();
            ConfigManager configManager = new ConfigManager();
            TextChannel textChannel = event.getGuild().getTextChannelById(configManager.getConfig("chatchannel"));

            if (textChannel != null && textChannel.canTalk()) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("💎 Boost İçin Teşekkürler!");
                embed.setThumbnail(member.getEffectiveAvatarUrl());
                embed.setDescription(member.getAsMention() + " sunucuya bir boost bastı! Topluluğumuzu desteklediğin için teşekkürler 💜");
                embed.setColor(new Color(176, 72, 186)); // Mor ton
                embed.setTimestamp(Instant.now());

                textChannel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}
