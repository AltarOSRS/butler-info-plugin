package com.butlerinfo;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemID;

import java.util.Optional;

public enum ConstructionItem
{
    PLANK("Wooden plank", ItemID.PLANK),
    OAK_PLANK("Oak plank", ItemID.OAK_PLANK),
    TEAK_PLANK("Teak plank", ItemID.TEAK_PLANK),
    MAHOGANY_PLANK("Mahogany plank", ItemID.MAHOGANY_PLANK),
    SOFT_CLAY("Soft clay", ItemID.SOFT_CLAY),
    LIMESTONE_BRICK("Limestone brick", ItemID.LIMESTONE_BRICK),
    STEEL_BAR("Steel bar", ItemID.STEEL_BAR),
    BOLT_OF_CLOTH("Cloth", ItemID.BOLT_OF_CLOTH),
    GOLD_LEAF("Gold leaf", ItemID.GOLD_LEAF),
    MARBLE_BLOCK("Marble block", ItemID.MARBLE_BLOCK),
    MAGIC_STONE("Magic housing stone", ItemID.MAGIC_STONE);

    @Getter
    private final String name;

    @Getter
    private final int itemId;

    ConstructionItem(String name, int itemId)
    {

        this.name = name;
        this.itemId = itemId;
    }

    public static Optional<ConstructionItem> getByName(String name)
    {
        for (ConstructionItem item : ConstructionItem.values()) {
            if (item.getName().equals(name)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}
