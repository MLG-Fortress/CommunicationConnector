package to.us.mlgfort.communicationconnector;

import com.cnaude.purpleirc.Events.IRCMessageEvent;
import com.teej107.slack.MessageSentFromSlackEvent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 7/29/2017.
 *
 * @author RoboMWM
 */
public class CommunicationConnector extends JavaPlugin implements Listener
{
    private final String SERBUR_NAME = "Da_Serbur_Sez";
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void sendToApps(String name, String message)
    {
    }

    public void sendToAppsAndServer(Apps sendingApp, String name, String message)
    {
        String appName = WordUtils.capitalize(sendingApp.toString().toLowerCase());

        getServer().broadcastMessage(ChatColor.GRAY + appName + "\u2759" + name + ": " + ChatColor.WHITE + message);
    }

    //MC Listeners//

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event)
    {
        String joinMessage = event.getPlayer().getName() + " IZ BAK 4 MOAR MEINKRAFT!!!!!!1111111111!!!1";
        sendToApps(SERBUR_NAME, joinMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        String quitMessage = event.getPlayer().getName() + " left us in loneliness :c";
        sendToApps(SERBUR_NAME, quitMessage);
    }

    //TODO: deathmessages deathmessage messagedeath ded

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onAsyncChat(AsyncPlayerChatEvent event)
    {
        sendToApps(ChatColor.stripColor(event.getPlayer().getDisplayName()), event.getMessage());
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

