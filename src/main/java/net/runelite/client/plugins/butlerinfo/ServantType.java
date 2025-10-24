package net.runelite.client.plugins.butlerinfo;

import lombok.Getter;
import java.util.Optional;
public enum ServantType {
    // Includes payment amount for each servant
    RICK(221, "Rick", 100, 500),
    MAID(223, "Maid", 50, 1000),
    COOK(225, "Cook", 28, 3000),
    BUTLER(227, "Butler", 20, 5000),
    DEMON_BUTLER(229, "Demon butler", 12, 10000);
    @Getter
    private final int npcId;

    @Getter
    private final String name;
    @Getter
    private final int ticks;

    // Field for payment amount
    @Getter
    private final int paymentAmount;
    ServantType(int npcId, String name, int ticks, int paymentAmount)
    {
        this.npcId = npcId;
        this.name = name;
        this.ticks = ticks;
        this.paymentAmount = paymentAmount;
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