package me.vasir.command;

import me.vasir.manager.UserManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ColorCommand extends ListenerAdapter {

    private final HashMap<Long, Long> cooldowns = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("renk")) return;
        if (event.getMember() == null) return;

        long userId = event.getUser().getIdLong();
        long currentTime = System.currentTimeMillis();
        long cooldownTime = 60_000; // 1 dakika

        // Cooldown kontrolü
        if (cooldowns.containsKey(userId) && currentTime - cooldowns.get(userId) < cooldownTime) {
            event.reply("Bu komutu tekrar kullanmak için biraz bekleyin!").setEphemeral(true).queue();
            return;
        }

        cooldowns.put(userId, currentTime);

        // Kullanıcının rengini değiştir
        UserManager userManager = new UserManager();
        userManager.changeUserColor(event.getMember());
        event.reply("Renginiz değiştirildi!").setEphemeral(true).queue();

        // Cooldown'u 1 dakika sonra kaldır
        scheduler.schedule(() -> cooldowns.remove(userId), cooldownTime, TimeUnit.MILLISECONDS);
    }
}
