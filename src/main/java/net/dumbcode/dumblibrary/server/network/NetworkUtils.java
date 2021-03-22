package net.dumbcode.dumblibrary.server.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class NetworkUtils {
    public static PlayerEntity getPlayer(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        return context.getDirection().getReceptionSide().isClient() ? getClientPlayer() : context.getSender();
    }

    private static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
