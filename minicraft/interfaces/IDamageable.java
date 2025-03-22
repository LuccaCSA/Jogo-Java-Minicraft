package minicraft.interfaces;

public interface IDamageable {
    void takeDamage(int amount);
    boolean isAlive();
    int getX();
    int getY();
    int getWidth();
    int getHeight();
}