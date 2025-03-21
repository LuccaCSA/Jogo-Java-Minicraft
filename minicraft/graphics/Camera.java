package minicraft.graphics;

import minicraft.player.Player;

public  class Camera {
    private int x, y;
    
    public Camera(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(Player player, int mapWidth, int mapHeight, int screenWidth, int screenHeight) {
        // Centraliza a câmera no jogador
        x = player.getX() - (screenWidth / 2) + (player.getLarguraHitbox() / 2);
        y = player.getY() - (screenHeight / 2) + (player.getAlturaHitbox() / 2);

        // Impede que a câmera mostre áreas fora do mapa
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > mapWidth - screenWidth) x = mapWidth - screenWidth;
        if (y > mapHeight - screenHeight) y = mapHeight - screenHeight;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}