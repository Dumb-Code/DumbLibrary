package net.dumbcode.dumblibrary.server.utils;

import net.minecraftforge.client.event.InputEvent;

import java.util.HashSet;
import java.util.Set;

public class MouseUtils {

    private static final Set<Integer> PRESSED = new HashSet<>();

    public static void onMouseEvent(InputEvent.RawMouseEvent event) {
        boolean pressed = event.getAction() == 1;
        if(pressed) {
            PRESSED.add(event.getButton());
        } else {
            PRESSED.remove(event.getButton());
        }
    }

    public static boolean isLeftPressed() {
        return isPressed(0);
    }

    public static boolean isMiddlePressed() {
        return isPressed(2);
    }

    public static boolean isRightPressed() {
        return isPressed(1);
    }

    public static boolean isPressed(int button) {
        return PRESSED.contains(button);
    }

}
