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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 7/29/2017.
 *
 * @author RoboMWM
 */
public class CommunicationConnector extends JavaPlugin implements Listener
{
    Slack slack;
    PurpleIRC purpleIRC;
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        getServer().getPluginManager().registerEvents(this, this);
        slack = (Slack)pm.getPlugin("SlackIntegration");
        purpleIRC = (PurpleIRC)pm.getPlugin("PurpleIRC");
        DumCord why = new DumCord(this);
        try
        {
            DiscordSRV.api.subscribe(why);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                sendToDiscord("**MLG Fortress IZ BAK 4 MOAR MINECRAFT!!!111!11ONEELEVEN**");
            }
        }.runTaskLater(this, 100L);
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
        if (sendingApp != Apps.IRC)
            sendToIRC(prefixWithWhitespace + ": " + message);
        if (sendingApp != Apps.DUMCORD) //Must ensure DiscordSRV integration is disabled in PurpleIRC
            sendToDiscord("`" + prefix + ":` " + message);
        this.getServer().getPluginManager().callEvent(new IncomingChatEvent(name, message));
    }

    public void sendToAllApps(String message)
    {
        slack.sendToSlack("Serbur", message, false);
        sendToIRC(message);
        sendToDiscord(message);
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
        String name;
        boolean action = false;

        if (!messageArray[0].startsWith("\u00A77IRC\u2759\u00A7r")) //Something else...
            return;
        if (messageArray[1].equalsIgnoreCase("\u00A75*")) //action message
        {
            action = true;
            name = messageArray[2];
            messageArray[2] = "";
        }
        else
            name = messageArray[0].substring(8, messageArray[0].length() - 1); //removes prefix and suffix, since purpleirc formats messages before firing this event
        messageArray[0] = "";
        String message = String.join(" ", messageArray);
        if (action)
            message = "_" + message + "_"; //italicize action messages
        sendToApps(Apps.IRC, name, message);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player)
            return false;

        sendToAllApps(String.join(" ", args));

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event)
    {
        if (event.getPlayer().hasPlayedBefore())
            return;

        slack.sendToSlack("New player!", "*A wild `" + event.getPlayer().getName() + "` has appeared!*", false);
        sendToDiscord("**A wild `" + event.getPlayer().getName() + "` has appeared!**");
    }

    private Set<Player> kickedPlayers = new HashSet<>();
    private Set<Player> playerSentMessage = Collections.newSetFromMap(new ConcurrentHashMap<Player, Boolean>());

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        if (kickedPlayers.remove(event.getPlayer()) || !playerSentMessage.remove(event.getPlayer()))
            return;

        String quitMessage = event.getQuitMessage();
        if (quitMessage == null || quitMessage.isEmpty())
            quitMessage = "`" + event.getPlayer().getName() + "` _left_";
        slack.sendToSlack("Somebody left", quitMessage, false);
        sendToDiscord(quitMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onKick(PlayerKickEvent event)
    {
        if (!playerSentMessage.remove(event.getPlayer()))
            return;
        String quitMessage = "`" + event.getPlayer().getName() + "` _left bcuz " + event.getReason() + "_";
        slack.sendToSlack("Somebody wuz kik'd", quitMessage, false);
        sendToDiscord(quitMessage);
        kickedPlayers.add(event.getPlayer());
        playerSentMessage.remove(event.getPlayer()); //cleanup
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onChat(AsyncPlayerChatEvent event)
    {
        playerSentMessage.add(event.getPlayer());
    }

}

enum Apps
{
    SLACK,
    IRC,
    DUMCORD
}

