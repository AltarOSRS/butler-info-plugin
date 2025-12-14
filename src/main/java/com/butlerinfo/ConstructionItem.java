package com.butlerinfo;

import lombok.Getter;
import java.util.Optional;
public enum ConstructionItem
{
    PLANK("Wooden plank", 960),
    OAK_PLANK("Oak plank", 8778),
    TEAK_PLANK("Teak plank", 8780),
    MAHOGANY_PLANK("Mahogany plank", 8782),
    SOFT_CLAY("Soft clay", 1761),
    LIMESTONE_BRICK("Limestone brick", 3420),
    BOLT_OF_CLOTH("Cloth", 8790),
    MAGIC_STONE("Magic housing stone", 8788),
    MARRENTILL("Marrentill", 251);
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