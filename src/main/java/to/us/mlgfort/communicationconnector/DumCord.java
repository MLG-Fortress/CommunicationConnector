package to.us.mlgfort.communicationconnector;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;

/**
 * Created on 7/31/2017.
 *
 * @author RoboMWM
 */
public class DumCord
{
    CommunicationConnector instance;

    DumCord(CommunicationConnector plugin)
    {
        instance = plugin;
    }

    //Discord
    @Subscribe
    public void onDiscordMessageReceived(DiscordGuildMessageReceivedEvent event)
    {
        instance.sendToApps(Apps.DUMCORD, event.getMember().getEffectiveName(), event.getMessage().getContent());
    }
}
