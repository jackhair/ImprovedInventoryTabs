package com.kqp.inventorytabs.init;

import com.kqp.inventorytabs.interf.TabManagerContainer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Loader-independent client state. The key mapping is created here and
 * registered by each loader's entry point; the loaders also call
 * {@link #levelTick()} from their client tick events.
 */
public class InventoryTabsClient {
    public static final KeyMapping NEXT_TAB_KEY_BIND = new KeyMapping(
            "inventorytabs.key.next_tab", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, "key.categories.inventory");

    public static boolean serverDoSightCheckFlag = true;

    // Handle state of tab manager
    public static void levelTick() {
        Minecraft client = Minecraft.getInstance();

        if (client.screen != null) {
            TabManagerContainer tabManagerContainer = (TabManagerContainer) client;

            tabManagerContainer.getTabManager().update();
        }
    }

    private static final Map<String, Pattern> SCREEN_PATTERN_CACHE = new HashMap<>();

    /**
     * Screens that draw a panel of their own left of the container, painted
     * by the screen itself rather than added as widgets, so the placement
     * logic can't see it. Tabs on these screens use only the right column.
     * Built in rather than a config default so every existing install picks
     * it up. Entries follow the excludeScreens format; naming a mod's API
     * marker interface keeps working when its screen class moves packages
     * between versions, as Curios' did.
     */
    private static final List<String> LEFT_PANEL_SCREENS = List.of(
            // Curios: its slot panel sits left of the container, up to 158px wide
            "top.theillusivec4.curios.api.client.ICuriosScreen"
    );
    private static final Map<Class<?>, Boolean> LEFT_PANEL_CACHE = new HashMap<>();

    public static boolean screenSupported(Screen screen) {
        return (screen instanceof AbstractContainerScreen<?>) && !(screen instanceof CreativeModeInventoryScreen)
                && !isExcludedScreen(screen);
    }

    /**
     * Whether the config's excludeScreens list names this screen. Entries are
     * fully qualified class names where '*' matches anything, and a screen's
     * superclasses and interfaces count too, so one entry can cover a whole mod.
     */
    public static boolean isExcludedScreen(Screen screen) {
        List<String> patterns = InventoryTabs.getConfig().excludeScreens;
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        return matchesAny(screen.getClass(), patterns);
    }

    /** Whether this screen is one of {@link #LEFT_PANEL_SCREENS}, so gets no left tab column. */
    public static boolean hasLeftPanel(Screen screen) {
        return LEFT_PANEL_CACHE.computeIfAbsent(screen.getClass(), type -> matchesAny(type, LEFT_PANEL_SCREENS));
    }

    /**
     * Whether any pattern matches the class, one of its superclasses, or any
     * interface those implement (directly or through a super-interface).
     */
    private static boolean matchesAny(Class<?> screenType, List<String> patterns) {
        for (Class<?> type = screenType; type != null && type != Object.class; type = type.getSuperclass()) {
            if (matchesAny(type.getName(), patterns) || matchesAnyInterface(type, patterns)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAnyInterface(Class<?> type, List<String> patterns) {
        for (Class<?> iface : type.getInterfaces()) {
            if (matchesAny(iface.getName(), patterns) || matchesAnyInterface(iface, patterns)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAny(String className, List<String> patterns) {
        for (String pattern : patterns) {
            if (screenPattern(pattern).matcher(className).matches()) {
                return true;
            }
        }
        return false;
    }

    private static Pattern screenPattern(String pattern) {
        return SCREEN_PATTERN_CACHE.computeIfAbsent(pattern.trim(), key -> {
            StringBuilder regex = new StringBuilder();
            for (String literal : key.split("\\*", -1)) {
                if (!regex.isEmpty()) {
                    regex.append(".*");
                }
                regex.append(Pattern.quote(literal));
            }
            return Pattern.compile(regex.toString());
        });
    }
}
