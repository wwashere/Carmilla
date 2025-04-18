package me.vasir.handler;

import me.vasir.command.*;
import me.vasir.manager.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;

public class JdaHandler extends ListenerAdapter {

    private final ConfigManager configManager;

    // ConfigManager'ı sınıf içinde tek bir kez oluşturuyoruz
    public JdaHandler() {
        this.configManager = new ConfigManager();
    }



    public void startBot() {
        JDABuilder builder = JDABuilder.createDefault(configManager.getConfig("token"));

        builder.enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
        );
        addEventListeners(builder);

        JDA jda = null;
        try {
            jda = builder.build().awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Guild guild = jda.getGuildById("1354155257151623281");

        if (guild != null) {
            jda.addEventListener(new LevelHandler(guild));
        } else {
            System.out.println("Guild bulunamadı!");
        }


        registerCommands(jda);



    }


    // Event listener'ları ekleyen metod
    private void addEventListeners(JDABuilder builder) {
        builder.addEventListeners(new RegisterCommand());
        builder.addEventListeners(new XpCommand());
        builder.addEventListeners(new ColorCommand());
        builder.addEventListeners(new PunishCommand());
        builder.addEventListeners(new GuildHandler());
        builder.addEventListeners(new ClearCommand());
        builder.addEventListeners(new LevelCommand());
    }

    // Komutları kaydeden metod
    private void registerCommands(JDA jda) {
        List<CommandData> commandData = new ArrayList<>();

        commandData.add(Commands.slash("kayıt", "Sunucuya kayıt ol")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND_IN_THREADS)));

        commandData.add(Commands.slash("temizle", "Belirtilen sayıda mesajı siler")
                .addOption(OptionType.INTEGER, "miktar", "Silinecek mesaj sayısı (1-50)", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)));

        commandData.add(Commands.slash("renk", "Rastgele bir renk al")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_CHANGE)));

        commandData.add(Commands.slash("seviye", "Seviyeni göster")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_CHANGE)));

        commandData.add(Commands.slash("xp", "Belirli bir kullanıcıya XP ekler")
                .addOption(OptionType.USER, "kullanıcı", "XP eklenecek kullanıcı", true)
                .addOption(OptionType.INTEGER, "miktar", "Eklenecek XP miktarı", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_THREADS)));

        commandData.add(Commands.slash("ceza", "Kullanıcıya ceza ver")
                .addOptions(new OptionData(OptionType.USER, "kullanıcı", "Cezalandırılacak kullanıcı", true))
                .addOptions(new OptionData(OptionType.STRING, "tür", "Ceza türü (timeout, ban, kick, warn)", true)
                        .addChoice("Timeout", "timeout")
                        .addChoice("Ban", "ban")
                        .addChoice("Kick", "kick")
                        .addChoice("Warn", "warn"))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE)));

        jda.updateCommands().addCommands(commandData).queue();
    }
}
