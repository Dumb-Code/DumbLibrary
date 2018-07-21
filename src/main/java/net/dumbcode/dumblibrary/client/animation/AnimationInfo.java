package net.dumbcode.dumblibrary.client.animation;

/**
 * Information about a certain {@link net.ilexiconn.llibrary.server.animation.Animation}
 */
public interface AnimationInfo {
    boolean shouldHold();
    boolean useInertia();
}
