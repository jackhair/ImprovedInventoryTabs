package com.kqp.inventorytabs.tabs.render;

import com.kqp.inventorytabs.tabs.tab.Tab;

/**
 * Data class that describes how a tab should be rendered.
 */
public class TabRenderInfo {
    public Tab tabReference;
    /**
     * 0 for a normal tab; -1/+1 when this slot is a page-back/page-forward
     * arrow instead of a tab (tabReference is null then).
     */
    public int pageArrow;
    public int index;
    public int x, y;
    public int texW, texH;
    public int texU, texV;
    public int itemX, itemY;
}
