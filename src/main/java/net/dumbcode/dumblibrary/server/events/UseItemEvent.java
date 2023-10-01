package net.dumbcode.dumblibrary.server.events;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraftforge.eventbus.api.Event;

@Getter
public class UseItemEvent extends Event {
    private final ItemStack stack;

    public UseItemEvent(ItemStack stack) {
        this.stack = stack;
    }

    @Getter
    @Setter
    public static class Duration extends UseItemEvent {

        private int duration;

        public Duration(ItemStack stack) {
            super(stack);
            this.duration = 0;
        }
    }

    @Getter
    @Setter
    public static class Action extends UseItemEvent {

        private UseAction action;

        public Action(ItemStack stack) {
            super(stack);
            this.action = UseAction.NONE;
        }
    }
}
