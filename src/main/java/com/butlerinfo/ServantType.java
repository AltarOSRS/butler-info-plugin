package com.butlerinfo;

import lombok.Getter;
import net.runelite.api.NpcID;

import java.util.Optional;

public enum ServantType {
    RICK(NpcID.RICK, "Rick", 100),
    MAID(NpcID.MAID, "Maid", 50),
    COOK(NpcID.COOK, "Cook", 28),
    BUTLER(NpcID.BUTLER, "Butler", 20),
    DEMON_BUTLER(NpcID.DEMON_BUTLER, "Demon butler", 12);

    @Getter
    private final int npcId;

    @Getter
    private final String name;

    @Getter
    private final int ticks;

    ServantType(int npcId, String name, int ticks)
    {
        this.npcId = npcId;
        this.name = name;
        this.ticks = ticks;
    }

    public static Optional<ServantType> getByNpcId(int npcId)
    {
        for (ServantType type : ServantType.values()) {
            if (type.npcId == npcId) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
