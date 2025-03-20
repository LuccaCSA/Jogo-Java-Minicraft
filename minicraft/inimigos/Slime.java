package minicraft.inimigos;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import minicraft.graphics.SpriteSheet;
import minicraft.player.Player;

public class Slime extends Inimigo {
    private enum Estado {
        PARADO, PULANDO, DANO, COOLDOWN
    }

    private Estado estadoAtual;
    private HashMap<Estado, BufferedImage[]> animacoes;
    private HashMap<Estado, Float> duracaoFrames;
    private int frame;
    private float tempoAnimacao;
    private float tempoEstado;
    private float tempoDanoContinuo;
    
    private float knockbackX, knockbackY;
    private final int distanciaPulo = 200;
    private final int danoContato = 5;
    private boolean noAr;
    private float alturaPulo;
    private float velocidadeHorizontal;
    private int direcaoPulo;
    private float initialY; // Nova variável para guardar a posição inicial do pulo


    public Slime(int x, int y) {
        super(x - (18 * 3), y - (18 * 3), 30, 0); // Centraliza o spawn nas coordenadas originais
        this.larguraHitbox = 18 * 6;
        this.alturaHitbox = 18 * 6;
        this.estadoAtual = Estado.PARADO;
        this.tempoAnimacao = 0;
        this.tempoEstado = 0;
        this.noAr = false;
        this.alturaPulo = 0;
        carregarAnimacoes();
        configurarDuracoes();
    }

    private void carregarAnimacoes() {
        animacoes = new HashMap<>();
        try {
            SpriteSheet sheet = new SpriteSheet("minicraft/graphics/sprites/sprites_slime.png", 18, 18);
            
            BufferedImage[] parado = {
                sheet.getSprite(0, 0),
                sheet.getSprite(18, 0)
            };
            animacoes.put(Estado.PARADO, parado);
            animacoes.put(Estado.COOLDOWN, parado);

            animacoes.put(Estado.PULANDO, new BufferedImage[]{
                sheet.getSprite(0, 18), sheet.getSprite(18, 18),
                sheet.getSprite(36, 18), sheet.getSprite(54, 18),
                sheet.getSprite(72, 18), sheet.getSprite(90, 18),
                sheet.getSprite(108, 18), sheet.getSprite(126, 18)
            });

            animacoes.put(Estado.DANO, new BufferedImage[]{
                sheet.getSprite(0, 36), sheet.getSprite(18, 36),
                sheet.getSprite(36, 36), sheet.getSprite(54, 36)
            });

        } catch (Exception e) {
            System.err.println("ERRO: Falha ao carregar sprites do Slime!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void configurarDuracoes() {
        duracaoFrames = new HashMap<>();
        duracaoFrames.put(Estado.PARADO, 0.5f);
        duracaoFrames.put(Estado.PULANDO, 0.125f);
        duracaoFrames.put(Estado.DANO, 0.25f);
        duracaoFrames.put(Estado.COOLDOWN, 1.0f);
    }

    @Override
    public boolean estaColidindoCom(Player player) {
        // Hitbox do Slime (baseada no centro)
        int slimeCenterX = x + (larguraHitbox / 2);
        int slimeCenterY = y + (alturaHitbox / 2);
        int slimeRadius = 54; // 108/2
        
        // Hitbox do Player (ajuste conforme seu código)
        int playerCenterX = player.getX() + (player.getLarguraHitbox() / 2);
        int playerCenterY = player.getY() + (player.getAlturaHitbox() / 2);
        int playerRadius = player.getLarguraHitbox() / 2;
        
        // Distância entre os centros
        double distancia = Math.sqrt(Math.pow(slimeCenterX - playerCenterX, 2) + 
                        Math.pow(slimeCenterY - playerCenterY, 2));
        
        return distancia < (slimeRadius + playerRadius);
    }
    
    @Override
    public void update(Player player) {
        if (!estaVivo()) return;

        float deltaTime = 0.0167f;
        tempoEstado += deltaTime;
        tempoAnimacao += deltaTime;
        tempoDanoContinuo += deltaTime;

        if (estadoAtual == Estado.DANO) {
            atualizarDano(deltaTime);
        } else {
            comportamentoNormal(player, deltaTime);
        }

        atualizarPulo();
        atualizarAnimacao();
        aplicarDanoContinuo(player);
    }

    private void comportamentoNormal(Player player, float deltaTime) {
        double distancia = calcularDistancia(player.getX(), player.getY());
        
        if (distancia <= raioDetecao) {
            if (estadoAtual == Estado.PARADO && tempoEstado >= 1.0f) {
                iniciarPulo(player);
            }
            
            if (estadoAtual == Estado.COOLDOWN && tempoEstado >= 1.0f) {
                estadoAtual = Estado.PARADO;
                tempoEstado = 0;
            }
        }
    }

    private void iniciarPulo(Player player) {
        estadoAtual = Estado.PULANDO;
        tempoEstado = 0;
        frame = 0;
        noAr = true;
        initialY = y; // Guarda a posição Y inicial antes do pulo
        alturaPulo = -15f; // Valor negativo para impulso para cima
        
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        
        if(distancia > 0) {
            direcaoPulo = (int) Math.signum(dx);
            velocidadeHorizontal = (float) (dx / distancia) * distanciaPulo * 0.0167f;
        }
    }

    private void atualizarPulo() {
        if (noAr) {
            alturaPulo += 0.8f;
            y += alturaPulo;
            
            // Verifica se a BASE da hitbox tocou o chão
            if (y + alturaHitbox >= initialY + alturaHitbox) {
                y = (int) initialY;
                alturaPulo = 0;
                noAr = false;
                estadoAtual = Estado.COOLDOWN;
                tempoEstado = 0;
            }
            
            x += velocidadeHorizontal;
        }
    }

    private void atualizarDano(float deltaTime) {
        x += knockbackX * deltaTime * 40;
        y += knockbackY * deltaTime * 40;
        
        knockbackX *= 0.9f;
        knockbackY *= 0.9f;

        if (tempoAnimacao >= duracaoFrames.get(Estado.DANO)) {
            frame = (frame + 1) % animacoes.get(Estado.DANO).length;
            tempoAnimacao = 0;
            
            if (frame == animacoes.get(Estado.DANO).length - 1) {
                estadoAtual = Estado.PARADO;
                tempoEstado = 0;
            }
        }
    }

    private void atualizarAnimacao() {
        if (tempoAnimacao >= duracaoFrames.get(estadoAtual)) {
            frame = (frame + 1) % animacoes.get(estadoAtual).length;
            tempoAnimacao = 0;
        }
    }

    private void aplicarDanoContinuo(Player player) {
        if (estaColidindoCom(player) && tempoDanoContinuo >= 1.0f) {
            player.tomarDano(danoContato);
            tempoDanoContinuo = 0;
        }
    }

    @Override
    public void tomarDano(int dano) {
        super.tomarDano(dano);
        if (estaVivo()) {
            estadoAtual = Estado.DANO;
            frame = 0;
            tempoAnimacao = 0;
            
            double angulo = Math.atan2(y - getY(), x - getX());
            knockbackX = (float) Math.cos(angulo) * 2;
            knockbackY = (float) Math.sin(angulo) * 2;
        }
    }

    @Override
    public void render(Graphics g, int cameraX, int cameraY) {
        if (!estaVivo()) return;
        
        BufferedImage[] frames = animacoes.get(estadoAtual);
        if (frames == null || frames.length == 0) return;
        
        BufferedImage frameAtual = frames[frame % frames.length];
        
        // Alinhamento perfeito hitbox/sprite (sem offsets)
        int renderX = x - cameraX;
        int renderY = y - cameraY;
        
        g.drawImage(frameAtual, 
            renderX,
            renderY,
            larguraHitbox, 
            alturaHitbox, 
            null);
    }
}