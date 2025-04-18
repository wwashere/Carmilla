package me.vasir.command;

import me.vasir.manager.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.time.Instant;

public class ClearCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("temizle")) return;

        OptionMapping amountOption = event.getOption("miktar");
        if (amountOption == null) {
            event.reply("Silinecek mesaj sayısı belirtilmedi.").setEphemeral(true).queue();
            return;
        }

        int amount = amountOption.getAsInt();
        if (amount < 1 || amount > 50) {
            event.reply("1 ile 50 arasında bir sayı girmelisin.").setEphemeral(true).queue();
            return;
        }

        TextChannel channel = event.getChannel().asTextChannel();

        // Mesajları getirip sil
        channel.getHistory().retrievePast(amount).queue(messages -> {
            channel.purgeMessages(messages);
            event.reply(messages.size() + " mesaj silindi.").setEphemeral(true).queue();

            // Log kanalı ID'si alınır
            ConfigManager configManager = new ConfigManager();
            String logChannelId = configManager.getConfig("logchannel");

            // Guild ve log kanalı kontrolü
            if (logChannelId == null || event.getGuild() == null) return;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
            if (logChannel == null || !logChannel.canTalk()) return;

            // Log embed'i oluşturulur
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Mesajlar Silindi")
                    .setDescription(event.getUser().getAsMention() + " tarafından " + messages.size() + " mesaj silindi.")
                    .setColor(new Color(255, 85, 85))
                    .setTimestamp(Instant.now());

            logChannel.sendMessageEmbeds(embed.build()).queue();
        });
    }
}
