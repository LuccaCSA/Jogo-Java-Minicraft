package minicraft.inimigos;

import java.awt.Graphics;
import minicraft.player.Player;

public abstract class Inimigo {
    protected int x, y;
    protected int vida;
    protected int velocidade;
    protected int raioDetecao;
    protected boolean vivo;
    protected final int distanciaMinima = 20;
    protected int larguraHitbox = 18;
    protected int alturaHitbox = 18;
    protected int danoAtaque = 10;
    protected float tempoAtaque = 0;
    protected float intervaloAtaque = 1.0f; // 1 segundo entre ataques
    protected int alcanceAtaque = 25;

    public Inimigo(int x, int y, int vida, int velocidade) {
        this.x = x;
        this.y = y;
        this.vida = vida;
        this.velocidade = velocidade;
        this.raioDetecao = 250;
        this.vivo = true;
    }

    public void update(Player player) {
        if (!vivo) return;

        float deltaTime = 0.0167f; // Aproximadamente 60 FPS
        double distancia = calcularDistancia(player.getCentroX(), player.getCentroY());

        if (distancia <= raioDetecao) {
            if (distancia > alcanceAtaque) {
                moverEmDirecao(player.getCentroX(), player.getCentroY());
            } else {
                atacar(player, deltaTime);
            }
        }

        tempoAtaque += deltaTime;
    }

    public abstract void render(Graphics g, int cameraX, int cameraY);

    public void tomarDano(int dano) {
        vida -= dano;
        if (vida <= 0) {
            vivo = false;
        }
    }

    protected void atacar(Player player, float deltaTime) {
        if (tempoAtaque >= intervaloAtaque && estaColidindoCom(player)) {
            player.tomarDano(danoAtaque);
            tempoAtaque = 0;
        }
    }

    protected double calcularDistancia(int targetX, int targetY) {
        return Math.sqrt(Math.pow(x - targetX, 2) + Math.pow(y - targetY, 2));
    }

    protected void moverEmDirecao(int targetX, int targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia <= distanciaMinima) return;

        double step = Math.min(velocidade, distancia - distanciaMinima);
        double dirX = dx / distancia;
        double dirY = dy / distancia;

        x += (int)(dirX * step);
        y += (int)(dirY * step);
    }

    public int getLarguraHitbox() {
        return larguraHitbox;
    }

    public int getAlturaHitbox() {
        return alturaHitbox;
    }

    public int getHitboxX() {
        return x + (larguraHitbox / 2);
    }

    public int getHitboxY() {
        return y + (alturaHitbox / 2);
    }

    public boolean estaColidindoCom(Player player) {
        int centroX = x + (larguraHitbox / 2);
        int centroY = y + (alturaHitbox / 2);

        return centroX < player.getX() + player.getLarguraHitbox() &&
               centroX + larguraHitbox > player.getX() &&
               centroY < player.getY() + player.getAlturaHitbox() &&
               centroY + alturaHitbox > player.getY();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean estaVivo() { return vivo; }
}