package net.dumbcode.dumblibrary.server.dna;

import com.mojang.datafixers.util.Pair;
import lombok.NonNull;
import lombok.Value;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.horse.CoatColors;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum  EntityGeneticRegistry {
    INSTANCE;

    private final Map<EntityType<?>, List<Entry<?, ?>>> entityEntryList = new HashMap<>();
    private final Map<GeneticType<?, ?>, List<IsolatePart>> amountToIsolate = new HashMap<>();
    private final Map<EntityType<?>, Map<String, List<Integer>>> tintEntryList = new HashMap<>();
    private final Map<EntityType<?>, Pair<Function<?, String>, BiConsumer<?, String>>> variantData = new HashMap<>();

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
        registerTargetTint(EntityType.PANDA, 0xB2B2B2, 0x222222); //https://www.htmlcsscolor.com/hex/B2B2B2 https://www.htmlcsscolor.com/hex/222222

        //Turtle: boost immunity, Increase in health, Increase in underwater capability, decrease speed, and shift in skin tone to turtle green
        register(EntityType.TURTLE, GeneticTypes.IMMUNITY.get(), 1.25F);
        register(EntityType.TURTLE, GeneticTypes.HEALTH_MODIFIER.get(), 0.75F);
        register(EntityType.TURTLE, GeneticTypes.UNDERWATER_CAPACITY.get(), 1.25F);
        register(EntityType.TURTLE, GeneticTypes.SPEED_MODIFIER.get(), -0.5F);
        registerTargetTint(EntityType.TURTLE, 0xBFB37F, 0x28340A);//https://www.htmlcsscolor.com/hex/BFB37F https://www.htmlcsscolor.com/hex/28340A

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
        registerTargetTint(EntityType.FOX, FoxEntity.Type.RED.getName(), 0xB2B2B2, 0xE37C21); //https://www.htmlcsscolor.com/hex/B2B2B2 https://www.htmlcsscolor.com/hex/E37C21
        registerTargetTint(EntityType.FOX, FoxEntity.Type.SNOW.getName(), 0xB2B2B2, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerVariantGetter(EntityType.FOX, f -> f.getFoxType().getName(), (f, t) -> f.setFoxType(FoxEntity.Type.byName(t)));

        //Sheep: reduced intelligence
        register(EntityType.SHEEP, GeneticTypes.INTELLIGENCE.get(), -0.25F);
        registerTargetTint(EntityType.SHEEP, 0xB4947D, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B4947D https://www.htmlcsscolor.com/hex/B2B2B2

        //Cow
        registerTargetTint(EntityType.COW, 0x433626, 0xB1B1B1); //https://www.htmlcsscolor.com/hex/433626 https://www.htmlcsscolor.com/hex/B1B1B1

        //Pig: Increased immunity
        register(EntityType.PIG, GeneticTypes.IMMUNITY.get(), 0.25F);
        registerTargetTint(EntityType.PIG, 0xE9A3A2); //https://www.htmlcsscolor.com/hex/E9A3A2

        //Rabbit: Increased speed, reduced size, TODO: increase in chance of albinism
        register(EntityType.RABBIT, GeneticTypes.SPEED_MODIFIER.get(), 0.5F);
        register(EntityType.RABBIT, GeneticTypes.SIZE.get(), -0.75F);

        String[] rabbitTypes = new String[] { "white", "black", "black_and_white", "desert", "saltpepper", "brown" };
        registerTargetTint(EntityType.RABBIT, rabbitTypes[0], 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerTargetTint(EntityType.RABBIT, rabbitTypes[1], 0x131313); //https://www.htmlcsscolor.com/hex/131313
        registerTargetTint(EntityType.RABBIT, rabbitTypes[2], 0xB2B2B2, 0x131313); //https://www.htmlcsscolor.com/hex/B2B2B2 https://www.htmlcsscolor.com/hex/131313
        registerTargetTint(EntityType.RABBIT, rabbitTypes[3], 0xF9EAAF); //https://www.htmlcsscolor.com/hex/F9EAAF
        registerTargetTint(EntityType.RABBIT, rabbitTypes[4], 0x7F6D58); //https://www.htmlcsscolor.com/hex/7F6D58
        registerTargetTint(EntityType.RABBIT, rabbitTypes[5], 0x826F58); //https://www.htmlcsscolor.com/hex/826F58
        registerVariantGetter(EntityType.RABBIT, r -> {
            switch (r.getRabbitType()) {
                case 1:
                case 99: //Killer bunny
                    return rabbitTypes[0];
                case 2:
                    return rabbitTypes[1];
                case 3:
                    return rabbitTypes[2];
                case 4:
                    return rabbitTypes[3];
                case 5:
                    return rabbitTypes[4];
                default:
                    return rabbitTypes[5];
            }
        }, (r, s) -> r.setRabbitType(Arrays.asList(rabbitTypes).indexOf(s)));

        //Chicken: Reduced intelligence, shift in skin tone to white TODO: Small chance of clucking ????
        register(EntityType.CHICKEN, GeneticTypes.INTELLIGENCE.get(), -0.5F);
        registerTargetTint(EntityType.CHICKEN, 0xB2B2B2, 0xD40409); //https://www.htmlcsscolor.com/hex/B2B2B2 https://www.htmlcsscolor.com/hex/D40409

        //Parrot: Increased intelligence, Increased chances of taming,
        // TODO: Small chance of dinosaur being able to imitate sounds
        // TODO: Allergy to cocoa beans - when do dinos eat cocoa beans anyway????
        // TODO: delta skin when green
        String[] parrotTypes = new String[] { "red", "blue", "green", "light_blue", "gray" };
        register(EntityType.PARROT, GeneticTypes.INTELLIGENCE.get(), 0.25F);
        registerTargetTint(EntityType.PARROT, parrotTypes[0], 0xEB0100, 0xE8C100);//https://www.htmlcsscolor.com/hex/EB0100 https://www.htmlcsscolor.com/hex/E8C100
        registerTargetTint(EntityType.PARROT, parrotTypes[1], 0x112DEC, 0xE8C100);//https://www.htmlcsscolor.com/hex/112DEC https://www.htmlcsscolor.com/hex/E8C100
        registerTargetTint(EntityType.PARROT, parrotTypes[2], 0x9CDA00);//https://www.htmlcsscolor.com/hex/9CDA00
        registerTargetTint(EntityType.PARROT, parrotTypes[3], 0x12CCFD, 0xE8C100);//https://www.htmlcsscolor.com/hex/12CCFD  https://www.htmlcsscolor.com/hex/E8C100
        registerTargetTint(EntityType.PARROT, parrotTypes[4], 0xAFAFAF, 0xE8C100);//https://www.htmlcsscolor.com/hex/AFAFAF  https://www.htmlcsscolor.com/hex/E8C100
        registerVariantGetter(EntityType.PARROT, p -> {
            switch (p.getVariant()) {
                case 1: return parrotTypes[1];
                case 2: return parrotTypes[2];
                case 3: return parrotTypes[3];
                case 4: return parrotTypes[4];
                default: return parrotTypes[0];
            }
        }, (p, s) -> p.setVariant(Arrays.asList(parrotTypes).indexOf(s)));

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
        registerVariantGetter(EntityType.HORSE, h -> h.getVariant().name(), (h, s) -> h.setVariantAndMarkings(CoatColors.valueOf(s), h.getMarkings()));

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
        String[] catTypes = new String[] { "tabby", "black", "red", "siamese", "british_shorthair", "calico", "persian", "ragdoll", "white", "jellie", "all_black" };
        register(EntityType.CAT, GeneticTypes.INTELLIGENCE.get(), 0.5F);
        register(EntityType.CAT, GeneticTypes.TAMING_CHANCE.get(), 0.45F);
        register(EntityType.CAT, GeneticTypes.SIZE.get(), -0.6F);
        registerTargetTint(EntityType.CAT, catTypes[0], 0x856549, 0x736B60); //https://www.htmlcsscolor.com/hex/856549 https://www.htmlcsscolor.com/hex/736B60
        registerTargetTint(EntityType.CAT, catTypes[1], 0x1C1827, 0xB2B2B2); //https://www.htmlcsscolor.com/hex/1C1827 https://www.htmlcsscolor.com/hex/B2B2B2
        registerTargetTint(EntityType.CAT, catTypes[2], 0xF0B245, 0xD97720); //https://www.htmlcsscolor.com/hex/F0B245 https://www.htmlcsscolor.com/hex/D97720
        registerTargetTint(EntityType.CAT, catTypes[3], 0xF6E7D3, 0x5A473E); //https://www.htmlcsscolor.com/hex/F6E7D3 https://www.htmlcsscolor.com/hex/5A473E
        registerTargetTint(EntityType.CAT, catTypes[4], 0xBABABA); //https://www.htmlcsscolor.com/hex/BABABA
        registerTargetTint(EntityType.CAT, catTypes[5], 0xB2B2B2, 0xD89A3D); //https://www.htmlcsscolor.com/hex/B2B2B2 https://www.htmlcsscolor.com/hex/D89A3D
        registerTargetTint(EntityType.CAT, catTypes[6], 0xFCDCB0); //https://www.htmlcsscolor.com/hex/FCDCB0
        registerTargetTint(EntityType.CAT, catTypes[7], 0xB2B2B2, 0x544E49); //https://www.htmlcsscolor.com/hex/B2B2B2 https://www.htmlcsscolor.com/hex/544E49
        registerTargetTint(EntityType.CAT, catTypes[8], 0xB2B2B2); //https://www.htmlcsscolor.com/hex/B2B2B2
        registerTargetTint(EntityType.CAT, catTypes[9], 0xB2B2B2, 0x616161); //https://www.htmlcsscolor.com/hex/B2B2B2 https://www.htmlcsscolor.com/hex/616161
        registerTargetTint(EntityType.CAT, catTypes[10], 0x161623); //https://www.htmlcsscolor.com/hex/161623
        registerVariantGetter(EntityType.CAT, c -> {
            switch (c.getCatType()) {
                case 1: return catTypes[1];
                case 2: return catTypes[2];
                case 3: return catTypes[3];
                case 4: return catTypes[4];
                case 5: return catTypes[5];
                case 6: return catTypes[6];
                case 7: return catTypes[7];
                case 8: return catTypes[8];
                case 9: return catTypes[9];
                case 10: return catTypes[10];
                default: return catTypes[0];
            }
        }, (c, s) -> c.setCatType(Arrays.asList(catTypes).indexOf(s)));


        //Ocelot: Increase in speed, reduced chance of taming, decrease in size
        register(EntityType.OCELOT, GeneticTypes.SPEED_MODIFIER.get(), 0.5F);
        register(EntityType.OCELOT, GeneticTypes.TAMING_CHANCE.get(), -0.3F);
        register(EntityType.OCELOT, GeneticTypes.SIZE.get(), -0.6F);
        registerTargetTint(EntityType.OCELOT, 0xFDD976, 0x8C5329); //https://www.htmlcsscolor.com/hex/FDD976 https://www.htmlcsscolor.com/hex/8C5329

        //Mooshroom:
        //todo: Increased poison resistance ??
        registerTargetTint(EntityType.MOOSHROOM, MooshroomEntity.Type.BROWN.name(), 0xB68767, 0xB0B0B0); //https://www.htmlcsscolor.com/hex/B68767 https://www.htmlcsscolor.com/hex/B0B0B0
        registerTargetTint(EntityType.MOOSHROOM, MooshroomEntity.Type.RED.name(), 0xA41012, 0xB0B0B0); //https://www.htmlcsscolor.com/hex/A41012 https://www.htmlcsscolor.com/hex/B0B0B0
        registerVariantGetter(EntityType.MOOSHROOM, m -> m.getMushroomType().name(), (m, s) -> m.setMushroomType(MooshroomEntity.Type.valueOf(s)));

        //Fish: Increase in underwater capability, Decrease in health, size
        for (EntityType<?> type : new EntityType<?>[]{EntityType.SALMON, EntityType.COD, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH}) {
            register(type, GeneticTypes.UNDERWATER_CAPACITY.get(), 0.3F);
            register(type, GeneticTypes.HEALTH_MODIFIER.get(), -0.5F);
            register(type, GeneticTypes.SIZE.get(), -0.5F);
        }
        registerTargetTint(EntityType.SALMON, 0xA83A38, 0x4C6E52); //https://www.htmlcsscolor.com/hex/A83A38 https://www.htmlcsscolor.com/hex/4C6E52
        registerTargetTint(EntityType.COD, 0xAF9878, 0x775B49); //https://www.htmlcsscolor.com/hex/AF9878 https://www.htmlcsscolor.com/hex/775B49
        registerTargetTint(EntityType.PUFFERFISH, 0xC2B091, 0xE3970B); //https://www.htmlcsscolor.com/hex/C2B091 https://www.htmlcsscolor.com/hex/E3970B

        //Register all troipical fish colour
        for (DyeColor value : DyeColor.values()) {
            registerTargetTint(EntityType.TROPICAL_FISH, value.getName(), value.getColorValue());
        }
        registerVariantGetter(EntityType.TROPICAL_FISH, f -> DyeColor.byId((f.getVariant() & 0xFF0000) >> 16).getName(), (f, s) -> f.setVariant(DyeColor.byName(s, DyeColor.WHITE).getId() << 16));

        //Squid: Increase in health regen speed, Increase in underwater capability, Decrease in health
        register(EntityType.SQUID, GeneticTypes.HEALTH_REGEN_SPEED.get(), 0.25F);
        register(EntityType.SQUID, GeneticTypes.UNDERWATER_CAPACITY.get(), 0.5F);
        register(EntityType.SQUID, GeneticTypes.HEALTH_MODIFIER.get(), -0.3F);
        registerTargetTint(EntityType.SQUID, 0x132737, 0x536B7F); //https://www.htmlcsscolor.com/hex/132737 https://www.htmlcsscolor.com/hex/536B7F

        //Bee: Increase in speed
        // todo: Poison ability for carnivore bites
        register(EntityType.BEE, GeneticTypes.SPEED_MODIFIER.get(), 0.3F);
        register(EntityType.BEE, GeneticTypes.SIZE.get(), -0.5F);
        registerTargetTint(EntityType.BEE, 0xE6C15E, 0x5A3023); //https://www.htmlcsscolor.com/hex/E6C15E https://www.htmlcsscolor.com/hex/5A3023

        //Spider: Shift in day/night cycle
        register(EntityType.SPIDER, GeneticTypes.NOCTURNAL_CHANCE.get(), 0.75F);
        registerTargetTint(EntityType.SPIDER, 0x4E443C); //https://www.htmlcsscolor.com/hex/4E443C

        //Cave Spider:
        //- Reduced size
        // todo: poison ability for carnivore bites
        register(EntityType.CAVE_SPIDER, GeneticTypes.SIZE.get(), -0.5F);
        registerTargetTint(EntityType.CAVE_SPIDER, 0x153833); //https://www.htmlcsscolor.com/hex/153833

        //Axolotl: Faster health regeneration, Shift in skin tone/palette to Leucistic, Wild/Brown, Gold, Cyan, or Blue similar to parrots, Increased chances of taming, Increased aggression to aquatic
        //Goat: Increase in herd size for herbivores, Increase in intelligence, can eat grass - herbivores, Increase in attack, Increase in speed
        //Glowsquid: Increase in health regen speed, Increase in underwater capability, Decrease in health, Bioluminescence special gene: affects Parasaurolophus, other dinosaurs get glowing eyes if they don't already
    }


    public <S extends GeneticFactoryStorage<O>, O> S register(EntityType<?> entityType, GeneticType<S, O> type, O value) {
        return this.register(entityType, type, type.getStorage().get(), null, value);
    }

    public <S extends GeneticFactoryStorage<O>, O> S register(EntityType<?> entityType, GeneticType<S, O> type, String variant, O value) {
        return this.register(entityType, type, type.getStorage().get(), variant, value);
    }

    public <S extends GeneticFactoryStorage<O>, O> S register(EntityType<?> entityType, GeneticType<S, O> type, @NonNull S storage, @Nullable String variant, O value) {
        this.entityEntryList.computeIfAbsent(entityType, c -> new ArrayList<>()).add(new Entry<>(type, storage, variant, value));
        this.amountToIsolate.computeIfAbsent(type, t -> new ArrayList<>()).add(new IsolatePart(type, entityType, variant));
        return storage;
    }

    public boolean isRegistered(EntityType<?> type) {
        return this.entityEntryList.containsKey(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Entity> String getVariant(T entity) {
        Pair<Function<?, String>, BiConsumer<?, String>> pair =this.variantData.get(entity.getType());
        if(pair != null) {
            return ((Function<T, String>) pair.getFirst()).apply(entity);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T createFromType(EntityType<T> type, Level world, String variant) {
        T t = type.create(world);
        if(variant != null) {
            Pair<Function<?, String>, BiConsumer<?, String>> pair =this.variantData.get(type);
            if(pair != null) {
                ((BiConsumer<T, String>) pair.getSecond()).accept(t, variant);
            }
        }
        return t;
    }

    public List<Entry<?, ?>> gatherEntry(EntityType<?> entityType, @Nullable String variant) {
        List<Entry<?, ?>> list = new ArrayList<>();
        if(this.entityEntryList.containsKey(entityType)) {
            for (Entry<?, ?> entry : this.entityEntryList.get(entityType)) {
                if(variant == null || entry.getVariant() == null || entry.getVariant().equals(variant)) {
                    list.add(entry);
                }
            }
        }
        return list;
    }

    public List<IsolatePart> getEntriesToIsolate(GeneticType<?, ?> type) {
        return this.amountToIsolate.getOrDefault(type, Collections.emptyList());
    }

    public List<Integer> gatherTints(EntityType<?> entityType, @Nullable String variant) {
        if(this.tintEntryList.containsKey(entityType)) {
            Map<String, List<Integer>> tints = this.tintEntryList.get(entityType);
            if(tints.containsKey(variant)) {
                return tints.get(variant);
            }
        }
        return Collections.emptyList();
    }


    public void registerTargetTint(EntityType<?> entityType, Integer... colours) {
        registerTargetTint(entityType, null, colours);
    }

    public void registerTargetTint(EntityType<?> entityType, @Nullable String variant, Integer...colours) {
//        this.register(entityType, GeneticTypes.OVERALL_TINT.get(),
//            new GeneticTypeOverallTintStorage().setTintType(GeneticTypeOverallTintStorage.TintType.TARGET),
//            variant,
//            new GeneticTint(
//                new GeneticTint.Part(
//                    ((primary >> 16) & 0xFF) / 255F,
//                    ((primary >> 8) & 0xFF) / 255F,
//                    ((primary) & 0xFF) / 255F,
//                    1F
//                ),
//                new GeneticTint.Part(
//                    ((secondary >> 16) & 0xFF) / 255F,
//                    ((secondary >> 8) & 0xFF) / 255F,
//                    ((secondary) & 0xFF) / 255F,
//                    1F
//                ),
//                GeneticUtils.DEFAULT_COLOUR_IMPORTANCE
//            )
//        );
        Collections.addAll(this.tintEntryList
            .computeIfAbsent(entityType, t -> new HashMap<>())
            .computeIfAbsent(variant, v -> new ArrayList<>()), colours);
    }

    public <T extends Entity> void registerVariantGetter(EntityType<T> type, Function<T, String> getter, BiConsumer<T, String> setter) {
        this.variantData.put(type, Pair.of(getter, setter));
    }


    @Value
    public static class Entry<S extends GeneticFactoryStorage<O>, O> {
        GeneticType<S, O> type;
        S storage;
        @Nullable String variant;
        O value;

        public GeneticEntry<S, O> create(float modifier) {
            S storage = this.type.getStorage().get();
            //Ugly cloning method
            storage.deserialize(this.storage.serialize(new CompoundNBT()));
            return new GeneticEntry<>(this.type, storage).setModifier(this.type.getDataHandler().scale(this.value, modifier));
        }
    }

    @Value
    public static class IsolatePart {
        GeneticType<?, ?> type;
        EntityType<?> entityType;
        String variant;
    }
}
