package minicraft.core;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import minicraft.graphics.Camera;
import minicraft.interfaces.IDamageable;

public abstract class Entity implements IDamageable {
    protected int x, y;
    protected int width = 48;
    protected int height = 48;
    protected boolean alive = true;
    protected boolean solid = true;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(double delta, ArrayList<Entity> entities);
    public abstract void render(Graphics g, Camera camera);

    public void onCollision(Entity other) {}
    public boolean isSolid() { return solid; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    @Override
    public void takeDamage(int amount) {
    }

    @Override
    public boolean isAlive() { return alive; }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }
}