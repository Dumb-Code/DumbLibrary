package net.dumbcode.dumblibrary.server.dna;

import lombok.NonNull;
import lombok.Value;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeOverallTintStorage;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.horse.CoatColors;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public enum  EntityGeneticRegistry {
    INSTANCE;

    private final Map<EntityType<?>, List<Entry<?>>> entityEntryList = new HashMap<>();
    private final Map<EntityType<?>, Function<?, String>> variantGetters = new HashMap<>();

    EntityGeneticRegistry() {
        //Bat: Decrease size, cause mild reversal of day/night cycle
        register(EntityType.BAT, GeneticTypes.SIZE.get(), -0.25F);
        register(EntityType.BAT, GeneticTypes.NOCTURNAL_CHANCE.get(), 0.25F);
        registerTargetTint(EntityType.BAT, 0x1C1912); //https://www.htmlcsscolor.com/hex/1C1912

        //Polar Bear: Increase size, health
        register(EntityType.POLAR_BEAR, GeneticTypes.SIZE.get(), 0.5F);
        register(EntityType.POLAR_BEAR, GeneticTypes.SPEED_MODIFIER.get(), 0.5F);
        register(EntityType.POLAR_BEAR, GeneticTypes.HEALTH_MODIFIER.get(), 0.35F);
        registerTargetTint(EntityType.POLAR_BEAR, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2

        //Panda Bear: Increase reduce speed, and reduce reproductive capability
        register(EntityType.PANDA, GeneticTypes.SPEED_MODIFIER.get(), 0.35F);
        register(EntityType.PANDA, GeneticTypes.REPRODUCTIVE_CAPABILITY.get(), -0.5F);
        registerTargetTint(EntityType.PANDA, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2

        //Turtle: boost immunity, Increase in health, Increase in underwater capability, decrease speed, and shift in skin tone to turtle green
        register(EntityType.TURTLE, GeneticTypes.IMMUNITY.get(), 1.25F);
        register(EntityType.TURTLE, GeneticTypes.HEALTH_MODIFIER.get(), 0.75F);
        register(EntityType.TURTLE, GeneticTypes.UNDERWATER_CAPACITY.get(), 1.25F);
        register(EntityType.TURTLE, GeneticTypes.SPEED_MODIFIER.get(), -0.5F);
        registerTargetTint(EntityType.TURTLE, 0x28340A);//https://www.htmlcsscolor.com/hex/28340A

        //Wolf: Increase intelligence, Increased chances of taming, Increase in Speed
        register(EntityType.WOLF, GeneticTypes.INTELLIGENCE.get(), 0.75F);
        register(EntityType.WOLF, GeneticTypes.TAMING_CHANCE.get(), 1.75F);
        register(EntityType.WOLF, GeneticTypes.SPEED_MODIFIER.get(), 0.25F);
        registerTargetTint(EntityType.WOLF, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2

        //Hoglin: Increased heat/fire resistance, Increased Size, Decreased chances of taming
        register(EntityType.HOGLIN, GeneticTypes.HEAT_RESISTANCE.get(), 0.5F);
        register(EntityType.HOGLIN, GeneticTypes.SIZE.get(), 0.75F);
        register(EntityType.HOGLIN, GeneticTypes.TAMING_CHANCE.get(), -0.5F);
        registerTargetTint(EntityType.HOGLIN, 0x8B6046); //https://www.htmlcsscolor.com/hex/8B6046

        //Dolphin: Increase in underwater capability, Increase in Speed
        register(EntityType.DOLPHIN, GeneticTypes.UNDERWATER_CAPACITY.get(), 0.75F);
        register(EntityType.DOLPHIN, GeneticTypes.SPEED_MODIFIER.get(), 0.35F);
        registerTargetTint(EntityType.DOLPHIN, 0x73737D); //https://www.htmlcsscolor.com/hex/73737D

        //Fox: Increase in speed, Increase in Intelligence
        register(EntityType.FOX, GeneticTypes.SPEED_MODIFIER.get(), 0.7F);
        register(EntityType.FOX, GeneticTypes.INTELLIGENCE.get(), 0.3F);
        registerTargetTint(EntityType.FOX, FoxEntity.Type.RED.getName(), 0xE37C21); //https://www.htmlcsscolor.com/hex/E37C21
        registerTargetTint(EntityType.FOX, FoxEntity.Type.SNOW.getName(), 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerVariantGetter(EntityType.FOX, f -> f.getFoxType().getName());

        //Sheep: reduced intelligence
        register(EntityType.SHEEP, GeneticTypes.INTELLIGENCE.get(), -0.25F);
        registerTargetTint(EntityType.SHEEP, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2

        //Cow
        registerTargetTint(EntityType.COW, 0x433626); //https://www.htmlcsscolor.com/hex/433626

        //Pig: Increased immunity
        register(EntityType.PIG, GeneticTypes.IMMUNITY.get(), 0.25F);
        registerTargetTint(EntityType.PIG, 0xE9A3A2); //https://www.htmlcsscolor.com/hex/E9A3A2

        //Rabbit: Increased speed, reduced size, TODO: increase in chance of albinism
        register(EntityType.RABBIT, GeneticTypes.SPEED_MODIFIER.get(), 0.5F);
        register(EntityType.RABBIT, GeneticTypes.SIZE.get(), -0.75F);
        registerTargetTint(EntityType.RABBIT, "white", 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerTargetTint(EntityType.RABBIT, "black", 0x131313); //https://www.htmlcsscolor.com/hex/131313
        registerTargetTint(EntityType.RABBIT, "desert", 0xF9EAAF); //https://www.htmlcsscolor.com/hex/F9EAAF
        registerTargetTint(EntityType.RABBIT, "saltpepper", 0x7F6D58); //https://www.htmlcsscolor.com/hex/7F6D58
        registerTargetTint(EntityType.RABBIT, "brown", 0x826F58); //https://www.htmlcsscolor.com/hex/826F58
        registerVariantGetter(EntityType.RABBIT, r -> {
            switch (r.getRabbitType()) {
                case 1:
                case 99: //Killer bunny
                    return "white";
                case 2:
                case 3:
                    return "black";
                case 4:
                    return "desert";
                case 5:
                    return "saltpepper";
                default:
                    return "brown";
            }
        });

        //Chicken: Reduced intelligence, shift in skin tone to white TODO: Small chance of clucking ????
        register(EntityType.CHICKEN, GeneticTypes.INTELLIGENCE.get(), -0.5F);
        registerTargetTint(EntityType.CHICKEN, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2

        //Parrot: Increased intelligence, Increased chances of taming,
        // TODO: Small chance of dinosaur being able to imitate sounds
        // TODO: Allergy to cocoa beans - when do dinos eat cocoa beans anyway????
        // TODO: delta skin when green
        register(EntityType.PARROT, GeneticTypes.INTELLIGENCE.get(), 0.25F);
        registerTargetTint(EntityType.PARROT, "red", 0xEB0100);//https://www.htmlcsscolor.com/hex/EB0100
        registerTargetTint(EntityType.PARROT, "blue", 0x112DEC);//https://www.htmlcsscolor.com/hex/112DEC
        registerTargetTint(EntityType.PARROT, "green", 0x9CDA00);//https://www.htmlcsscolor.com/hex/9CDA00
        registerTargetTint(EntityType.PARROT, "light_blue", 0x12CCFD);//https://www.htmlcsscolor.com/hex/12CCFD
        registerTargetTint(EntityType.PARROT, "gray", 0xAFAFAF);//https://www.htmlcsscolor.com/hex/AFAFAF
        registerVariantGetter(EntityType.PARROT, p -> {
            switch (p.getVariant()) {
                case 1: return "blue";
                case 2: return "green";
                case 3: return "light_blue";
                case 4: return "gray";
                default: return "red";
            }
        });

        //Llama: Increase in chance of taming
        //TODO: Ability to carry/transport items
        //TODO: Small chance of spit ability(no venom)
        register(EntityType.LLAMA, GeneticTypes.TAMING_CHANCE.get(), 0.25F);
        registerTargetTint(EntityType.LLAMA, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2

        //Horse: Increase in speed, increase in chance of taming, Increase in jump strength
        register(EntityType.HORSE, GeneticTypes.TAMING_CHANCE.get(), 0.4F);
        register(EntityType.HORSE, GeneticTypes.JUMP_STRENGTH.get(), 0.3F);
        registerTargetTint(EntityType.HORSE, CoatColors.WHITE.name(), 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerTargetTint(EntityType.HORSE, CoatColors.CREAMY.name(), 0x926633); //https://www.htmlcsscolor.com/hex/926633
        registerTargetTint(EntityType.HORSE, CoatColors.CHESTNUT.name(), 0x8A461B); //https://www.htmlcsscolor.com/hex/8A461B
        registerTargetTint(EntityType.HORSE, CoatColors.BROWN.name(), 0x53250D); //https://www.htmlcsscolor.com/hex/53250D
        registerTargetTint(EntityType.HORSE, CoatColors.BLACK.name(), 0x24252D); //https://www.htmlcsscolor.com/hex/24252D
        registerTargetTint(EntityType.HORSE, CoatColors.GRAY.name(), 0x5F5F5F); //https://www.htmlcsscolor.com/hex/5F5F5F
        registerTargetTint(EntityType.HORSE, CoatColors.DARKBROWN.name(), 0x2F1A0F); //https://www.htmlcsscolor.com/hex/2F1A0F
        registerVariantGetter(EntityType.HORSE, h -> h.getVariant().name());

        //Mule: Decrease in speed, Increase in chance of taming, Increase in chance of infertility, Increase in Intelligence
        //todo: increase in transport capacity
        register(EntityType.MULE, GeneticTypes.SPEED_MODIFIER.get(), -0.2F);
        register(EntityType.MULE, GeneticTypes.TAMING_CHANCE.get(), 0.3F);
        register(EntityType.MULE, GeneticTypes.REPRODUCTIVE_CAPABILITY.get(), -0.3F);
        register(EntityType.MULE, GeneticTypes.INTELLIGENCE.get(), 0.2F);
        registerTargetTint(EntityType.MULE, 0x502C1A); //https://www.htmlcsscolor.com/hex/502C1A

        //Donkey: Decrease in speed, Increase in intelligence
        register(EntityType.DONKEY, GeneticTypes.SPEED_MODIFIER.get(), -0.2F);
        register(EntityType.DONKEY, GeneticTypes.INTELLIGENCE.get(), 0.25F);
        registerTargetTint(EntityType.DONKEY, 0x8A7666); //https://www.htmlcsscolor.com/hex/8A7666

        //Cat: Increase in intelligence, Increase in chance of taming, Shift of skin tone/pallete to a random cat skin, Decrease in size
        register(EntityType.CAT, GeneticTypes.INTELLIGENCE.get(), 0.5F);
        register(EntityType.CAT, GeneticTypes.TAMING_CHANCE.get(), 0.45F);
        register(EntityType.CAT, GeneticTypes.SIZE.get(), -0.6F);
        registerTargetTint(EntityType.CAT, "tabby", 0x856549); //https://www.htmlcsscolor.com/hex/856549
        registerTargetTint(EntityType.CAT, "black", 0x1C1827); //https://www.htmlcsscolor.com/hex/1C1827
        registerTargetTint(EntityType.CAT, "red", 0xF0B245); //https://www.htmlcsscolor.com/hex/F0B245
        registerTargetTint(EntityType.CAT, "siamese", 0xF6E7D3); //https://www.htmlcsscolor.com/hex/F6E7D3
        registerTargetTint(EntityType.CAT, "british_shorthair", 0xBABABA); //https://www.htmlcsscolor.com/hex/BABABA
        registerTargetTint(EntityType.CAT, "calico", 0xD89A3D); //https://www.htmlcsscolor.com/hex/D89A3D
        registerTargetTint(EntityType.CAT, "persian", 0xFCDCB0); //https://www.htmlcsscolor.com/hex/FCDCB0
        registerTargetTint(EntityType.CAT, "ragdoll", 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerTargetTint(EntityType.CAT, "white", 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerTargetTint(EntityType.CAT, "jellie", 0x616161); //https://www.htmlcsscolor.com/hex/616161
        registerTargetTint(EntityType.CAT, "all_black", 0x161623); //https://www.htmlcsscolor.com/hex/161623
        registerVariantGetter(EntityType.CAT, c -> {
            switch (c.getCatType()) {
                case 1: return "black";
                case 2: return "red";
                case 3: return "siamese";
                case 4: return "british_shorthair";
                case 5: return "calico";
                case 6: return "persian";
                case 7: return "ragdoll";
                case 8: return "white";
                case 9: return "jellie";
                case 10: return "all_black";
                default: return "tabby";
            }
        });


        //Ocelot: Increase in speed, reduced chance of taming, decrease in size
        register(EntityType.OCELOT, GeneticTypes.SPEED_MODIFIER.get(), 0.5F);
        register(EntityType.OCELOT, GeneticTypes.TAMING_CHANCE.get(), -0.3F);
        register(EntityType.OCELOT, GeneticTypes.SIZE.get(), -0.6F);
        registerTargetTint(EntityType.OCELOT, 0xFDD976); //https://www.htmlcsscolor.com/hex/FDD976

        //Mooshroom:
        //todo: Increased poison resistance ??
        registerTargetTint(EntityType.MOOSHROOM, MooshroomEntity.Type.BROWN.name(), 0xB68767); //https://www.htmlcsscolor.com/hex/B68767
        registerTargetTint(EntityType.MOOSHROOM, MooshroomEntity.Type.RED.name(), 0xA41012); //https://www.htmlcsscolor.com/hex/A41012
        registerVariantGetter(EntityType.MOOSHROOM, m -> m.getMushroomType().name());

        //Fish: Increase in underwater capability, Decrease in health, size
        for (EntityType<?> type : new EntityType<?>[]{EntityType.SALMON, EntityType.COD, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH}) {
            register(type, GeneticTypes.UNDERWATER_CAPACITY.get(), 0.3F);
            register(type, GeneticTypes.HEALTH_MODIFIER.get(), -0.5F);
            register(type, GeneticTypes.SIZE.get(), -0.5F);
        }
        registerTargetTint(EntityType.SALMON, 0xA83A38); //https://www.htmlcsscolor.com/hex/A83A38
        registerTargetTint(EntityType.COD, 0x775B49); //https://www.htmlcsscolor.com/hex/775B49
        registerTargetTint(EntityType.PUFFERFISH, 0xE3970B); //https://www.htmlcsscolor.com/hex/E3970B

        //Register all troipical fish colour
        for (DyeColor value : DyeColor.values()) {
            registerTargetTint(EntityType.TROPICAL_FISH, value.getName(), value.getColorValue());
        }
        registerVariantGetter(EntityType.TROPICAL_FISH, f -> DyeColor.byId((f.getVariant() & 0xFF0000) >> 16).getName());

        //Squid: Increase in health regen speed, Increase in underwater capability, Decrease in health
        register(EntityType.SQUID, GeneticTypes.HEALTH_REGEN_SPEED.get(), 0.25F);
        register(EntityType.SQUID, GeneticTypes.UNDERWATER_CAPACITY.get(), 0.5F);
        register(EntityType.SQUID, GeneticTypes.HEALTH_MODIFIER.get(), -0.3F);
        registerTargetTint(EntityType.SQUID, 0x536B7F); //https://www.htmlcsscolor.com/hex/536B7F

        //Bee: Increase in speed
        // todo: Poison ability for carnivore bites
        register(EntityType.BEE, GeneticTypes.SPEED_MODIFIER.get(), 0.3F);
        registerTargetTint(EntityType.BEE, 0xE6C15E); //https://www.htmlcsscolor.com/hex/E6C15E

        //Spider: Shift in day/night cycle
        register(EntityType.SPIDER, GeneticTypes.NOCTURNAL_CHANCE.get(), 0.75F);
        registerTargetTint(EntityType.SPIDER, 0x4E443C); //https://www.htmlcsscolor.com/hex/4E443C

        //Cave Spider:
        //- Reduced size
        // todo: poison ability for carnivore bites
        register(EntityType.CAVE_SPIDER, GeneticTypes.SIZE.get(), -0.5F);
        register(EntityType.CAVE_SPIDER, GeneticTypes.SIZE.get(), -0.5F);
        registerTargetTint(EntityType.CAVE_SPIDER, 0x153833); //https://www.htmlcsscolor.com/hex/153833

        //Axolotl: Faster health regeneration, Shift in skin tone/palette to Leucistic, Wild/Brown, Gold, Cyan, or Blue similar to parrots, Increased chances of taming, Increased aggression to aquatic
        //Goat: Increase in herd size for herbivores, Increase in intelligence, can eat grass - herbivores, Increase in attack, Increase in speed
        //Glowsquid: Increase in health regen speed, Increase in underwater capability, Decrease in health, Bioluminescence special gene: affects Parasaurolophus, other dinosaurs get glowing eyes if they don't already
    }


    public <S extends GeneticFactoryStorage> S register(EntityType<?> entityType, GeneticType<S> type, double value) {
        return this.register(entityType, type, type.getStorage().get(), null, value);
    }

    public <S extends GeneticFactoryStorage> S register(EntityType<?> entityType, GeneticType<S> type, String variant, double value) {
        return this.register(entityType, type, type.getStorage().get(), variant, value);
    }

    public <S extends GeneticFactoryStorage> S register(EntityType<?> entityType, GeneticType<S> type, @NonNull S storage, @Nullable String variant, double value) {
        this.entityEntryList.computeIfAbsent(entityType, c -> new ArrayList<>()).add(new Entry<>(type, storage, variant, value));
        return storage;
    }

    public boolean isRegistered(EntityType<?> type) {
        return this.entityEntryList.containsKey(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Entity> String getVariant(T entity) {
        Function<T, String> function = (Function<T, String>) this.variantGetters.get(entity.getType());
        if(function != null) {
            return function.apply(entity);
        }
        return null;
    }

    public List<Entry<?>> gatherEntry(EntityType<?> entityType, @Nullable String variant) {
        List<Entry<?>> list = new ArrayList<>();
        if(this.entityEntryList.containsKey(entityType)) {
            for (Entry<?> entry : this.entityEntryList.get(entityType)) {
                if(entry.getVariant() == null || entry.getVariant().equals(variant)) {
                    list.add(entry);
                }
            }
        }
        return list;
    }

    public void registerTargetTint(EntityType<?> entityType, int color) {
        registerTargetTint(entityType, null, color);
    }


    public void registerTargetTint(EntityType<?> entityType, @Nullable String variant, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        this.register(entityType, GeneticTypes.OVERALL_TINT.get(),
            new GeneticTypeOverallTintStorage().setTintType(GeneticTypeOverallTintStorage.TintType.TARGET),
            variant,
            GeneticUtils.encodeFloatColor(r/255F, g/255F, b/255F, 1F)
        );
    }

    public <T extends Entity> void registerVariantGetter(EntityType<T> type, Function<T, String> getter) {
        this.variantGetters.put(type, getter);
    }


    @Value
    public static class Entry<S extends GeneticFactoryStorage> {
        GeneticType<S> type;
        S storage;
        @Nullable String variant;
        double value;

        public GeneticEntry<S> create(float modifier) {
            S storage = this.type.getStorage().get();
            //Ugly cloning method
            storage.deserialize(this.storage.serialize(new CompoundNBT()));
            return new GeneticEntry<>(this.type, storage).setModifier(this.type.getDataHandler().scale(this.value, modifier));
        }
    }
}
