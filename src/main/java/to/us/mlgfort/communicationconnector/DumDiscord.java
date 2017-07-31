package to.us.mlgfort.communicationconnector;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;

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
    @Subscribe
    private void onDiscordMessageReceived(DiscordGuildMessageReceivedEvent event)
    {
        instance.sendToApps(Apps.DISCORD, event.getAuthor().getName(), event.getMessage().getStrippedContent());
    }
}