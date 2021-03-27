package net.dumbcode.dumblibrary.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Queue;

public class ItemComponentHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            Queue<Particle> queue = Minecraft.getInstance().particleEngine.particles.get(IParticleRenderType.TERRAIN_SHEET);
            if(queue != null && !queue.isEmpty()) {
                queue.removeIf(p -> {
                    if(p instanceof BreakingParticle) {
                        TextureAtlasSprite sprite = ((BreakingParticle) p).sprite;
                        System.out.println(sprite);
                    }
                    return false;
                });
            }
        }
    }

}
