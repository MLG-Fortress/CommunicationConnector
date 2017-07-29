package to.us.mlgfort.communicationconnector;

import com.cnaude.purpleirc.Events.IRCMessageEvent;
import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import com.teej107.slack.MessageSentFromSlackEvent;
import com.teej107.slack.Slack;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 7/29/2017.
 *
 * @author RoboMWM
 */
public class CommunicationConnector extends JavaPlugin implements Listener
{
    private final String SERBUR_NAME = "Da_Serbur_Sez";
    ConsoleCommandSender SERBUR_SENDER;
    Slack slack;
    PurpleIRC purpleIRC;
    public void onEnable()
    {
        SERBUR_SENDER = getServer().getConsoleSender();
        PluginManager pm = getServer().getPluginManager();
        getServer().getPluginManager().registerEvents(this, this);
        slack = (Slack)pm.getPlugin("SlackIntegration");
        purpleIRC = (PurpleIRC)pm.getPlugin("PurpleIRC");
    }

    public void sendToApps(CommandSender sender, String message)
    {
        String name;
        boolean avatar = true;
        if (!(sender instanceof Player))
        {
            name = SERBUR_NAME;
            avatar = false;
        }
        else
        {
            name = ChatColor.stripColor(((Player)sender).getDisplayName());
        }

        slack.sendToSlack(name, ChatColor.stripColor(message), avatar);

        sendToIRC(name + ": " + message);
    }

    private void sendToIRC(String message)
    {
        for (PurpleBot bot : purpleIRC.ircBots.values())
            bot.asyncIRCMessage("#MLG", message);
    }

    public void sendToAppsAndServer(Apps sendingApp, String name, String message)
    {
        String appName = WordUtils.capitalize(sendingApp.toString().toLowerCase());
        String formattedMessage = ChatColor.GRAY + appName + "\u2759" + name + ": " + ChatColor.WHITE + message;

        if (sendingApp != Apps.SLACK)
            slack.sendToSlack(name, message, false);
        if (sendingApp != Apps.IRC)
            sendToIRC(name + ": " + message);

        getServer().broadcastMessage(formattedMessage);
    }

    //MC Listeners//

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event)
    {
        String joinMessage = event.getJoinMessage();
        if (joinMessage == null || joinMessage.isEmpty())
            joinMessage = event.getPlayer().getName() + " IZ BAK 4 MOAR MEINKRAFT!!!!!!1111111111!!!1";
        sendToApps(SERBUR_SENDER, joinMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        String quitMessage = event.getQuitMessage();
        if (quitMessage == null || quitMessage.isEmpty())
            quitMessage = event.getPlayer().getName() + " left us in loneliness :c";
        sendToApps(SERBUR_SENDER, quitMessage);
    }

    //TODO: deathmessages deathmessage messagedeath ded

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onAsyncChat(AsyncPlayerChatEvent event)
    {
        sendToApps(event.getPlayer(), event.getMessage());
    }

    //App Listeners//

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSlackMessageSent(MessageSentFromSlackEvent event)
    {
        sendToAppsAndServer(Apps.SLACK, event.getUsername(), event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onIRCChatReceivedEvent(IRCMessageEvent event)
    {
        String[] messageArray = event.getMessage().split(" ");
        String name = messageArray[0];
        messageArray[0] = "";
        String message = String.join(" ", messageArray);
        sendToAppsAndServer(Apps.IRC, name, message);
    }
}

enum Apps
{
    SLACK,
    IRC,
    DISCORD
}

