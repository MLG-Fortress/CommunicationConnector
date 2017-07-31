package to.us.mlgfort.communicationconnector;

import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Created on 7/31/2017.
 *
 * @author RoboMWM
 */
public class DumDiscord
{
    CommunicationConnector instance;

    DumDiscord(CommunicationConnector plugin)
    {
        instance = plugin;
    }

    //Discord
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onDiscordMessageReceived(DiscordGuildMessageReceivedEvent event)
    {
        instance.sendToApps(Apps.DISCORD, event.getAuthor().getName(), event.getMessage().getStrippedContent());
    }
}
