package to.us.mlgfort.communicationconnector;

import com.cnaude.purpleirc.Events.IRCMessageEvent;
import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import com.cnaude.purpleirc.ext.org.pircbotx.User;
import com.teej107.slack.MessageSentFromSlackEvent;
import com.teej107.slack.Slack;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
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
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 7/29/2017.
 *
 * @author RoboMWM
 */
public class CommunicationConnector extends JavaPlugin implements Listener
{
    private Slack slack;
    private PurpleIRC purpleIRC;
    private final Pattern removeLineBreaks = Pattern.compile("\n");
    private JavaPlugin hello;
    public void onEnable()
    {
        this.hello = this;
        PluginManager pm = getServer().getPluginManager();
        getServer().getPluginManager().registerEvents(this, this);
        slack = (Slack)pm.getPlugin("SlackIntegration");
        purpleIRC = (PurpleIRC)pm.getPlugin("PurpleIRC");
//        DumCord why = new DumCord(this);
//        try
//        {
//            DiscordSRV.api.subscribe(why);
//        }
//        catch (Throwable e)
//        {
//            e.printStackTrace();
//        }
//        new BukkitRunnable()
//        {
//            @Override
//            public void run()
//            {
//                sendToDiscord("MLG Fortress IZ BAK 4 MOAR MINECRAFT!!!111!! **IRC and Slack communication has been re-established!**");
//            }
//        }.runTaskLater(this, 100L);
    }

    private void sendToIRC(String msg, boolean dontPing)
    {
        new BukkitRunnable()
        {
            String message = msg;
            @Override
            public void run()
            {
                Iterator<PurpleBot> bots = purpleIRC.ircBots.values().iterator();
                while (bots.hasNext())
                {
                    if (dontPing)
                    {
                        PurpleBot bot = bots.next();
                        try
                        {
                            StringBuilder messageBuilder = new StringBuilder(message);
                            for (User user : bot.getBot().getUserBot().getChannels().first().getUsers())
                            {
                                if (message.toLowerCase().contains(user.getNick().toLowerCase()) && !message.toLowerCase().contains(user.getNick().toLowerCase() + ".com"))
                                {
                                    getLogger().info("matched " + user.getNick());
                                    Matcher matcher = Pattern.compile("(?i)\\b" + user.getNick() + "\\b").matcher(message);
                                    for (int i = 1; matcher.find(); i++)
                                    {
                                        messageBuilder.insert(matcher.start() + i, "\u200B");
                                        getLogger().info("replaced position " + matcher.start() + " with offset " + i);
                                    }
                                    message = messageBuilder.toString();
                                }
                            }
                        }
                        catch (Throwable rock)
                        {
                            continue;
                        }
                    }
                    break;
                }

                message = removeLineBreaks.matcher(message).replaceAll(" \u00B6 ");
                if (message.length() > 440)
                    message = message.substring(0, 440);
                final String finalMessage = message;
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        for (PurpleBot bot : purpleIRC.ircBots.values())
                            bot.asyncIRCMessage("#MLG", finalMessage);
                    }
                }.runTaskAsynchronously(hello);
            }
        }.runTaskAsynchronously(this);
    }

    private void sendToDiscord(String message)
    {
//        new BukkitRunnable()
//        {
//            @Override
//            public void run()
//            {
//                DiscordUtil.sendMessage(DiscordUtil.getTextChannelById("341448913200349203"), DiscordUtil.convertMentionsFromNames(message, DiscordSRV.getPlugin().getMainGuild()));
//            }
//        }.runTaskAsynchronously(this);
    }

    private void sendToSlack(CommandSender sender, String message)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                slack.sendToSlack(sender, message);
            }
        }.runTaskAsynchronously(this);
    }

    private void sendToSlack(String name, String message)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                slack.sendToSlack(name, message, false);
            }
        }.runTaskAsynchronously(this);
    }

    private void sendToMC(String message)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //unknown if getOnlinePlayers is thread-safe
                for (Player player : getServer().getOnlinePlayers())
                    player.sendMessage(message);
            }
        }.runTask(this);
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

        StringBuilder nameWithZeroWidthWhitespaceBuilder = new StringBuilder(name);
        String nameWithZeroWidthWhitespace = nameWithZeroWidthWhitespaceBuilder.insert(1, "\u200B").toString();
        //String nameWithZeroWidthWhitespace = name.substring(0, 1) + "\u200B" + name.substring(1);
        String prefix = "[" + appName + "] " + name;
        String prefixWithWhitespace = ChatColor.GRAY + "[" + appName + "] " + ChatColor.RESET + nameWithZeroWidthWhitespace;
        //String formattedMessage = ChatColor.GRAY + appName + "\u2759" + name + ": " + ChatColor.WHITE + message;
        //getServer().broadcastMessage(formattedMessage);

        if (sendingApp != Apps.IRC)
        {
            sendToIRC(prefixWithWhitespace + ": " + message, false);
            sendToMC(ChatColor.GRAY + appName + "❙" + ChatColor.RESET + name + ": " + message);
        }
        if (sendingApp != Apps.SLACK)
            sendToSlack(prefix, message);
        if (sendingApp != Apps.DUMCORD) //Must ensure DiscordSRV integration is disabled in PurpleIRC
            sendToDiscord("`" + prefixWithWhitespace + ":` " + message);
        this.getServer().getPluginManager().callEvent(new IncomingChatEvent(name, message));
    }

    private void mcToApps(Player player, String message)
    {
        sendToIRC(player.getDisplayName() + ": " + message, true);
        sendToSlack(player, message);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                StringBuilder nameWithZeroWidthWhitespaceBuilder = new StringBuilder(player.getDisplayName());
                String nameWithZeroWidthWhitespace = nameWithZeroWidthWhitespaceBuilder.insert(player.getDisplayName().length() - 3, "\u200B").toString();
                sendToDiscord("`" + nameWithZeroWidthWhitespace + ":` " + message);
            }
        }.runTaskAsynchronously(this);
    }

    public void sendToAllApps(String message)
    {
        sendToSlack("Serbur", message);
        sendToIRC(message, true);
        sendToDiscord(message);
    }

    public void sendToAllApps(String name, String message)
    {
        sendToIRC(name + ": " + message, true);
        sendToSlack(name, message);
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
        if (recentlyLeft.remove(event.getPlayer().getUniqueId()))
        {
            sendToIRC(ChatColor.GREEN + getWhitespacedName(event.getPlayer().getName()) + " rejoined", false);
            sendToSlack("They're bak!", "`" + getWhitespacedName(event.getPlayer().getName()) + " rejoined`");
            sendToDiscord("`" + getWhitespacedName(event.getPlayer().getName()) + " rejoined`");
        }

        if (event.getPlayer().hasPlayedBefore())
            return;

        sendToIRC(ChatColor.LIGHT_PURPLE + "A wild " + ChatColor.GREEN + event.getPlayer().getName() + ChatColor.LIGHT_PURPLE + " has appeared!", false);
        slack.sendToSlack("New player!", "*A wild `" + event.getPlayer().getName() + "` has appeared!*", false);
        sendToDiscord("**A wild `" + event.getPlayer().getName() + "` has appeared!**");
    }

    private Set<Player> kickedPlayers = new HashSet<>();
    private Set<Player> playerSentMessage = Collections.newSetFromMap(new ConcurrentHashMap<Player, Boolean>());
    private Set<UUID> recentlyLeft = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        if (kickedPlayers.remove(event.getPlayer()) || !playerSentMessage.remove(event.getPlayer()))
            return;
        recentlyLeft.add(event.getPlayer().getUniqueId());

        String quitMessage = event.getQuitMessage();
        if (quitMessage == null || quitMessage.isEmpty())
            quitMessage = "`" + getWhitespacedName(event.getPlayer().getName()) + " dc'd`";
        sendToIRC(ChatColor.DARK_GRAY + getWhitespacedName(event.getPlayer().getName()) + " dc'd", false);
        slack.sendToSlack("Somebody left", quitMessage, false);
        sendToDiscord(quitMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onKick(PlayerKickEvent event)
    {
        if (!playerSentMessage.remove(event.getPlayer()))
            return;
        sendToIRC(ChatColor.DARK_GRAY + event.getPlayer().getName() + " wuz kik'd cuz " + event.getReason(), true);
        String quitMessage = "`" + event.getPlayer().getName() + " wuz kik'd cuz " + event.getReason() + "`";
        sendToSlack("Somebody wuz kik'd", quitMessage);
        sendToDiscord(quitMessage);
        kickedPlayers.add(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onChat(AsyncPlayerChatEvent event)
    {
        playerSentMessage.add(event.getPlayer());
        mcToApps(event.getPlayer(), event.getMessage());
    }

    private String getWhitespacedName(String name)
    {
        StringBuilder nameWithZeroWidthWhitespaceBuilder = new StringBuilder(name);
        return nameWithZeroWidthWhitespaceBuilder.insert(1, "\u200B").toString();
    }
}

enum Apps
{
    SLACK,
    IRC,
    DUMCORD
}

