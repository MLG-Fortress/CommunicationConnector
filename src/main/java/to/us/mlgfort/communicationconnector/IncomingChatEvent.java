package to.us.mlgfort.communicationconnector;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 12/29/2017.
 *
 * Chat incoming from an app to MC server
 *
 * @author RoboMWM
 */
public class IncomingChatEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private String name;
    private String message;

    public IncomingChatEvent(String name, String message)
    {
        this.name = name;
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public String getName()
    {
        return name;
    }
}
