package me.vasir.handler;

import me.vasir.manager.MonarchManager;
import me.vasir.manager.UserManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LevelHandler extends ListenerAdapter {

    private static final int XP = 10;
    private static final int EXTRA_VOICE_XP = 5;

    private final UserManager userManager = new UserManager();
    private final Set<Long> activeUsers = ConcurrentHashMap.newKeySet();

    private final Guild guild;

    public LevelHandler(Guild guild) {
        this.guild = guild;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::giveXpAndReset, 12, 12, TimeUnit.MINUTES);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!event.getGuild().getId().equals(guild.getId())) return;

        long userId = event.getAuthor().getIdLong();
        activeUsers.add(userId);
    }

    private void giveXpAndReset() {
        System.out.println("XP kontrolü yapılıyor, aktif kullanıcı sayısı: " + activeUsers.size());
        for (Member member : guild.getMembers()) {
            if (member.getUser().isBot()) continue;

            int totalXp = 0;
            long userId = member.getIdLong();

            if (activeUsers.contains(userId)) {
                totalXp += XP;
            }

            if (isUserInVoiceChannel(member)) {
                totalXp += EXTRA_VOICE_XP;
            }

            if (totalXp > 0) {
                userManager.addXp(member, totalXp);
            }
        }

        // Monarch kontrolü
        MonarchManager monarchManager = new MonarchManager(guild);
        monarchManager.updateMonarchIfNeeded();


        activeUsers.clear();
    }

    private boolean isUserInVoiceChannel(Member member) {
        return member.getVoiceState() != null && member.getVoiceState().inAudioChannel();
    }
}
