package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModOp;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.storages.RandomUUIDStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticFieldModifierStorage extends RandomUUIDStorage {

    private ModOp operation = ModOp.MULTIPLY_BASE_THEN_ADD;
    private float modifier = 1F;

    @Override
    public CompoundNBT serialize(CompoundNBT nbt) {
        nbt.putInt("Operation", this.operation.ordinal());
        nbt.putFloat("Modifier", this.modifier);
        return super.serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        this.operation = ModOp.values()[nbt.getInt("Operation") % ModOp.values().length];
        this.modifier = nbt.getFloat("Modifier");
        super.deserialize(nbt);
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        json.addProperty("operation", this.operation.ordinal());
        json.addProperty("modifier", this.modifier);
        return super.serialize(json);
    }

    @Override
    public void deserialize(JsonObject json) {
        this.operation = ModOp.values()[JSONUtils.getAsInt(json, "operation", 0) % ModOp.values().length];
        this.modifier = JSONUtils.getAsFloat(json, "modifier", 1F);
        super.deserialize(json);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack stack, EntityGeneticRegistry.Entry<?> entry, int x, int y, int height, int width) {
        int color = -1;
        float value = entry.getValue()*this.modifier;
        if(this.operation == ModOp.MULTIPLY) {
            if(value > 1) {
                color = 0xFF00FF00;
            } else if(value < 1) {
                color = 0xFFFF0000;
            }
        } else {
            if(value > 0) {
                color = 0xFF00FF00;
            } else if(value < 0) {
                color = 0xFFFF0000;
            }
        }
        Minecraft.getInstance().font.draw(stack, entry.getType().getTranslationComponent(), x+2, y+4, color);
    }
}
