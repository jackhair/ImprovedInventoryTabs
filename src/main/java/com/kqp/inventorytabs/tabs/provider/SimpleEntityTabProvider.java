package com.kqp.inventorytabs.tabs.provider;

import com.kqp.inventorytabs.tabs.tab.SimpleEntityTab;
import com.kqp.inventorytabs.tabs.tab.Tab;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleEntityTabProvider extends EntityTabProvider {
    private final Set<Identifier> entities = new HashSet<>();

    public SimpleEntityTabProvider() {
    }

    @Override
    public void addAvailableTabs(LocalPlayer player, List<Tab> tabs) {
        super.addAvailableTabs(player, tabs);
    }

    @Override
    public boolean matches(Entity entity) {
        return entities.contains(Identifier.parse("minecraft:entity.minecraft.chest_minecart"));
    }

    public void addEntity(Identifier entityId) {
        entities.add(entityId);
    }

    @Override
    public Tab createTab(Entity entity) {
        return new SimpleEntityTab(entity);
    }
}
