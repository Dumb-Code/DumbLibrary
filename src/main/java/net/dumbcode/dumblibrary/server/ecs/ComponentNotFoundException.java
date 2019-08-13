package net.dumbcode.dumblibrary.server.ecs;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;

/**
 * {@code ComponentNotFoundException} is the exception thrown when a component is not found on a component
 * @author Wyn Price
 */
@Getter
public class ComponentNotFoundException extends RuntimeException {

    /**
     * The component access that the {@link #type} could not be found on.
     */
    private final transient ComponentAccess componentAccess;

    /**
     * The type that could not be found on the {@link #componentAccess}.
     */
    private final transient EntityComponentType type;

    public ComponentNotFoundException(ComponentAccess componentAccess, EntityComponentType type) {
        super("Component '" + type.getIdentifier() + "' is not present");
        this.componentAccess = componentAccess;
        this.type = type;
    }
}
