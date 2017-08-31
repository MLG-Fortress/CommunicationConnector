package to.us.mlgfort.communicationconnector;

import com.cnaude.purpleirc.Events.IRCMessageEvent;
import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import com.teej107.slack.MessageSentFromSlackEvent;
import com.teej107.slack.Slack;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 7/29/2017.
 *
 * @author RoboMWM
 */
public class CommunicationConnector extends JavaPlugin implements Listener
{
    private final String SERBUR_NAME = "Da_Serbur_Sez";
    Slack slack;
    PurpleIRC purpleIRC;
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        getServer().getPluginManager().registerEvents(this, this);
        slack = (Slack)pm.getPlugin("SlackIntegration");
        purpleIRC = (PurpleIRC)pm.getPlugin("PurpleIRC");
        DumDiscord why = new DumDiscord(this);
        try
        {
            DiscordSRV.api.subscribe(why);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void sendToIRC(final String message)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (PurpleBot bot : purpleIRC.ircBots.values())
                    bot.asyncIRCMessage("#MLG", message);
            }
        }.runTaskAsynchronously(this);
    }

    private void sendToDiscord(final String message)
    {
        DiscordUtil.sendMessage(DiscordUtil.getTextChannelById("341448913200349203"), message);
    }

    public void sendToApps(Apps sendingApp, String name, String message)
    {
        String appName = sendingApp.toString();
        switch(sendingApp)
        {
            case IRC:
                break;
            default:
                appName = WordUtils.capitalize(sendingApp.toString().toLowerCase());
        }

        String nameWithZeroWidthWhitespace = name.substring(0, 1) + "\u200B" + name.substring(1);
        String prefix = "[" + appName + "] " + name;
        String prefixWithWhitespace = "[" + appName + "] " + nameWithZeroWidthWhitespace;
        //String formattedMessage = ChatColor.GRAY + appName + "\u2759" + name + ": " + ChatColor.WHITE + message;
        //getServer().broadcastMessage(formattedMessage);

        if (sendingApp != Apps.SLACK)
            slack.sendToSlack(prefix, message, false);
        if (sendingApp != Apps.IRC && sendingApp != Apps.DISCORD) //PurpleIRC already integrates with DiscordSRV
        {
            sendToIRC(prefixWithWhitespace + ": " + message);
            sendToDiscord(prefix + ": " + message);
        }
    }

    public void sendToAllApps(String name, String message)
    {
        slack.sendToSlack(name, message, false);
        sendToIRC(name + ": " + message);
        sendToDiscord(name + ": " + message);
    }

    //MC Listeners//
    //I might be better off not attempting to implement this since literally every plugin implements this anyways...
    //@Deprecated
    //private void sendToApps(CommandSender sender, String message)
    {
//        String name = sender.getName();
//        boolean avatar = true;
//        if (!(sender instanceof Player))
//        {
//            name = SERBUR_NAME;
//            avatar = false;
//        }
//
//        slack.sendToSlack(name, ChatColor.stripColor(message), avatar);

        //sendToIRC(name + ": " + message);
    }

//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    private void onJoin(PlayerJoinEvent event)
//    {
//        String joinMessage = event.getJoinMessage();
//        if (joinMessage == null || joinMessage.isEmpty())
//            joinMessage = event.getPlayer().getName() + " IZ BAK 4 MOAR MEINKRAFT!!!!!!1111111111!!!1";
//        sendToApps(SERBUR_SENDER, joinMessage);
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    private void onQuit(PlayerQuitEvent event)
//    {
//        String quitMessage = event.getQuitMessage();
//        if (quitMessage == null || quitMessage.isEmpty())
//            quitMessage = event.getPlayer().getName() + " left us in loneliness :c";
//        sendToApps(SERBUR_SENDER, quitMessage);
//    }

    //TODO: deathmessages deathmessage messagedeath ded

//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    private void onAsyncChat(AsyncPlayerChatEvent event)
//    {
//        sendToApps(event.getPlayer(), event.getMessage());
//    }

    //App Listeners//

    //Slack
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSlackMessageSent(MessageSentFromSlackEvent event)
    {
        sendToApps(Apps.SLACK, event.getUsername(), event.getMessage());
    }

    //IRC
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onIRCChatReceivedEvent(IRCMessageEvent event)
    {
        String[] messageArray = event.getMessage().split(" ");
        String name = "";

        if (!messageArray[0].startsWith("\u00A77IRC\u2759\u00A7r"))
            return;
        if (messageArray[1].equalsIgnoreCase("\u00A75*"))
        {
            name = messageArray[2];
            messageArray[2] = "";
        }
        else
            name = messageArray[0].substring(8, messageArray[0].length() - 1); //removes prefix and suffix, since purpleirc formats messages before sending this event
        messageArray[0] = "";
        String message = String.join(" ", messageArray);
        sendToApps(Apps.IRC, name, message);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player)
            return false;

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            message.append(args[i]);

        sendToAllApps(args[0], message.toString());

        return true;
    }

}

enum Apps
{
    SLACK,
    IRC,
    DISCORD
}

