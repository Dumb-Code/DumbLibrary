package net.dumbcode.dumblibrary.client;

public class MutVector2f {
    public float x;
    public float y;

    public MutVector2f() {
        this(0, 0);
    }

    public MutVector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
