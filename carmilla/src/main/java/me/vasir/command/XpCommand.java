package me.vasir.command;

import me.vasir.manager.UserManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;

public class XpCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("xp")) return;

        // Hedef kullanıcıyı al
        OptionMapping userOption = event.getOption("kullanıcı");
        Member target = (userOption != null) ? userOption.getAsMember() : null;
        if (target == null) {
            event.reply("Lütfen geçerli bir kullanıcı belirtin!").setEphemeral(true).queue();
            return;
        }

        // XP miktarını al
        OptionMapping amountOption = event.getOption("miktar");
        int xp = (amountOption != null) ? amountOption.getAsInt() : -1;

        // XP miktarını 500 ile sınırlama
        if (xp <= 0) {
            event.reply("XP miktarı pozitif bir sayı olmalıdır!").setEphemeral(true).queue();
            return;
        }

        if (xp > 500) {
            xp = 500;  // XP miktarını 500 ile sınırla
        }

        // XP ekle
        new UserManager().addXp(target, xp);

        // Geri bildirim embed'i
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("XP Eklendi")
                .setDescription(target.getAsMention() + " kullanıcısına **" + xp + " XP** eklendi!")
                .setColor(new Color(97, 59, 102));

        event.replyEmbeds(embed.build()).queue();
    }
}
