package minicraft.inimigos;

import java.awt.Graphics;
import minicraft.player.Player;

public abstract class Inimigo {
    protected int x, y;
    protected int vida;
    protected int velocidade;
    protected int raioDetecao;
    protected boolean vivo;
    protected final int distanciaMinima = 15; // Nova constante
    protected int larguraHitbox = 18; // Valores padrão
    protected int alturaHitbox = 18;

    public Inimigo(int x, int y, int vida, int velocidade) {
        this.x = x;
        this.y = y;
        this.vida = vida;
        this.velocidade = velocidade;
        this.raioDetecao = 250;
        this.vivo = true;
    }

    public void update(Player player) {
        if(!vivo) return;
        
        double distancia = calcularDistancia(player.getX(), player.getY());
        
        if(distancia <= raioDetecao) {
            moverEmDirecao(player.getX(), player.getY());
        }
    }

    public abstract void render(Graphics g, int cameraX, int cameraY);

    public void tomarDano(int dano) {
        vida -= dano;
        if(vida <= 0) {
            vivo = false;
        }
    }

    protected double calcularDistancia(int targetX, int targetY) {
        return Math.sqrt(Math.pow(x - targetX, 2) + Math.pow(y - targetY, 2));
    }

    protected void moverEmDirecao(int targetX, int targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        
        if(distancia <= distanciaMinima) return;
        
        double step = Math.min(velocidade, distancia - distanciaMinima);
        double dirX = dx / distancia;
        double dirY = dy / distancia;
        
        x += (int)(dirX * step);
        y += (int)(dirY * step);
    }

    // Getters para a hitbox
    public int getLarguraHitbox() {
        return larguraHitbox;
    }

    public int getAlturaHitbox() {
        return alturaHitbox;
    }

    public boolean estaColidindoCom(Player player) {
        return x < player.getX() + player.getLarguraHitbox() &&
               x + larguraHitbox > player.getX() &&
               y < player.getY() + player.getAlturaHitbox() &&
               y + alturaHitbox > player.getY();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean estaVivo() { return vivo; }
}