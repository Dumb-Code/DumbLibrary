package net.dumbcode.dumblibrary.server.events;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

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

        private EnumAction action;

        public Action(ItemStack stack) {
            super(stack);
            this.action = EnumAction.NONE;
        }
    }
}
