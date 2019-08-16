package net.dumbcode.dumblibrary.server;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBreaking;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class ItemComponentHandler {

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemComponent().setRegistryName("component_item").setTranslationKey("component_item")
        );
    }

    //Everything from this point is to replace the eating particles.
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(new ModelResourceLocation(DumbLibrary.MODEL_MISSING, "inventory"), new ItemComponentDummyModel(event.getModelManager().getMissingModel()));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            ArrayDeque<Particle>[][] fxLayers = ObfuscationReflectionHelper.getPrivateValue(ParticleManager.class, Minecraft.getMinecraft().effectRenderer, "field_78876_b", "fxLayers");
            fxLayers[1][1].removeIf(particle -> {
                if(particle instanceof ParticleBreaking) {
                    TextureAtlasSprite sprite = ObfuscationReflectionHelper.getPrivateValue(Particle.class, particle, "field_187241_h", "particleTexture");
                    return sprite instanceof ItemComponentDummyBreakingParticle;
                }
                return false;
            });
        }
    }

    private static class ItemComponentDummyModel extends BakedModelWrapper<IBakedModel> {

        private final TextureAtlasSprite sprite;

        private ItemComponentDummyModel(IBakedModel originalModel) {
            super(originalModel);
            this.sprite = new ItemComponentDummyBreakingParticle(originalModel.getParticleTexture());
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return this.sprite;
        }
    }

    private static class ItemComponentDummyBreakingParticle extends TextureAtlasSprite {
        private ItemComponentDummyBreakingParticle(TextureAtlasSprite from) {
            super("missingno");
            this.copyFrom(from);
        }
    }


}
