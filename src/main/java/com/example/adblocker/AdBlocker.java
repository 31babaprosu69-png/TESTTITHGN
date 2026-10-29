package com.example.adblocker;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.Set;

/**
 * Server-side only. Blocks hosting advertisements:
 *  - message commands (tellraw, say, title, msg, ...) whose text contains a blocked word
 *    (also when wrapped in "execute ... run ...")
 *  - player chat messages containing a blocked word
 * Matching is case-insensitive.
 */
@Mod(AdBlocker.MODID)
public class AdBlocker {
    public static final String MODID = "adblocker";

    // Lowercase. "minestrator" also covers "MineStrator.com"; "myboxfree" covers "[MyBoxFree]".
    private static final String[] KEYWORDS = {"minestrator", "flexcore", "myboxfree", "smartbackup"};
    private static final int MIN_LENGTH = 8; // shortest keyword

    private static final Set<String> MESSAGE_COMMANDS =
            Set.of("tellraw", "say", "title", "msg", "tell", "w", "me", "teammsg", "tm");

    private static final Component WARNING =
            Component.literal("Your message was removed because it contains a blocked word.");

    public AdBlocker() {
        // Server-only mod: vanilla / mod-less clients can still join.
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                        (remote, isServer) -> true));

        MinecraftForge.EVENT_BUS.register(this);
    }

    private static boolean containsBlocked(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String keyword : KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    /** Returns the lowercase name of the command that actually runs (after any "execute ... run"). */
    private static String effectiveCommandName(String cmd) {
        int start = cmd.startsWith("/") ? 1 : 0;
        int end = cmd.indexOf(' ', start);
        String name = (end < 0 ? cmd.substring(start) : cmd.substring(start, end)).toLowerCase(Locale.ROOT);

        if (name.equals("execute")) {
            int run = cmd.lastIndexOf(" run ");
            if (run >= 0) {
                String rest = cmd.substring(run + 5).stripLeading();
                int sp = rest.indexOf(' ');
                name = (sp < 0 ? rest : rest.substring(0, sp)).toLowerCase(Locale.ROOT);
            }
        }
        if (name.startsWith("minecraft:")) name = name.substring(10);
        return name;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCommand(CommandEvent event) {
        String cmd = event.getParseResults().getReader().getString();
        // Cheap checks first: length, then keyword scan; command name is only parsed on a match.
        if (cmd.length() < MIN_LENGTH || !containsBlocked(cmd)) return;
        if (MESSAGE_COMMANDS.contains(effectiveCommandName(cmd))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ServerChatEvent event) {
        String text = event.getRawText();
        if (text.length() < MIN_LENGTH || !containsBlocked(text)) return;
        event.setCanceled(true);
        ServerPlayer player = event.getPlayer();
        if (player != null) player.sendSystemMessage(WARNING);
    }
}
