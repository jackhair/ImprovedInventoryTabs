package com.kqp.inventorytabs.tabs.render;

import java.util.Locale;

import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;

/**
 * Where the tab lines sit relative to the container screen.
 */
public enum TabLayout implements SelectionListEntry.Translatable {
    /** Columns along the left and right edges, vertically centered on the screen. */
    VERTICAL,
    /** Rows above and below the container, following its edges. */
    HORIZONTAL;

    @Override
    public String getKey() {
        return "inventorytabs.layout." + name().toLowerCase(Locale.ROOT);
    }
}
