package minicraft.inimigos;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import minicraft.graphics.SpriteSheet;
import minicraft.player.Player;

public class Zumbi extends Inimigo {
    private enum Estado {
        PARADO, ANDANDO, ATACANDO, RECEBENDO_DANO
    }

    private Estado estadoAtual;
    private HashMap<Estado, BufferedImage[]> animacoes;
    private HashMap<Estado, Float> duracaoFrames;
    private int frame;
    private float tempoAnimacao;
    private float tempoAtaque;
    private boolean atacando;
    private final int danoAtaque = 10; // Dano causado pelo ataque
    private final float duracaoAtaque = 0.75f; // Duração do ataque em segundos

    // Tamanho aumentado em 3x (18 * 3 = 54)
    private static final int ESCALA = 3;
    private static final int TAMANHO_ORIGINAL = 18;
    private static final int TAMANHO_APOS_ESCALA = TAMANHO_ORIGINAL * ESCALA;

    // Direção do Zumbi (1 para direita, -1 para esquerda)
    private int direcao = 1;

    public Zumbi(int x, int y) {
        super(x, y, 50, 3); // Vida: 50, Velocidade: 3
        // Ajusta a posição para centralizar a hitbox
        this.larguraHitbox = TAMANHO_APOS_ESCALA;
        this.alturaHitbox = TAMANHO_APOS_ESCALA;
        this.estadoAtual = Estado.PARADO;
        this.tempoAnimacao = 0;
        this.tempoAtaque = 0;
        this.atacando = false;
        carregarAnimacoes();
        configurarDuracoes();
    }

    private void carregarAnimacoes() {
        animacoes = new HashMap<>();
        try {
            SpriteSheet sheet = new SpriteSheet("minicraft/graphics/sprites/sprites_zumbi.png", TAMANHO_ORIGINAL, TAMANHO_ORIGINAL);

            // Animação parado
            animacoes.put(Estado.PARADO, new BufferedImage[]{
                escalarSprite(sheet.getSprite(0, 0)),
                escalarSprite(sheet.getSprite(18, 0))
            });

            // Animação andando
            animacoes.put(Estado.ANDANDO, new BufferedImage[]{
                escalarSprite(sheet.getSprite(0, 18)),
                escalarSprite(sheet.getSprite(18, 18)),
                escalarSprite(sheet.getSprite(36, 18)),
                escalarSprite(sheet.getSprite(54, 18))
            });

            // Animação atacando
            animacoes.put(Estado.ATACANDO, new BufferedImage[]{
                escalarSprite(sheet.getSprite(0, 36)),
                escalarSprite(sheet.getSprite(18, 36)),
                escalarSprite(sheet.getSprite(36, 36)),
                escalarSprite(sheet.getSprite(54, 36)),
                escalarSprite(sheet.getSprite(72, 36)),
                escalarSprite(sheet.getSprite(90, 36))
            });

            // Animação recebendo dano
            animacoes.put(Estado.RECEBENDO_DANO, new BufferedImage[]{
                escalarSprite(sheet.getSprite(0, 54)),
                escalarSprite(sheet.getSprite(18, 54)),
                escalarSprite(sheet.getSprite(36, 54)),
                escalarSprite(sheet.getSprite(54, 54))
            });

        } catch (Exception e) {
            System.err.println("ERRO: Falha ao carregar sprites do Zumbi!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private BufferedImage escalarSprite(BufferedImage original) {
        BufferedImage scaled = new BufferedImage(
            TAMANHO_APOS_ESCALA, 
            TAMANHO_APOS_ESCALA, 
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = scaled.getGraphics();
        g.drawImage(original, 0, 0, TAMANHO_APOS_ESCALA, TAMANHO_APOS_ESCALA, null);
        g.dispose();
        return scaled;
    }

    private void configurarDuracoes() {
        duracaoFrames = new HashMap<>();
        duracaoFrames.put(Estado.PARADO, 0.5f); // 0.5s por frame
        duracaoFrames.put(Estado.ANDANDO, 0.15f); // 0.15s por frame
        duracaoFrames.put(Estado.ATACANDO, 0.125f); // 0.125s por frame (6 frames em 0.75s)
        duracaoFrames.put(Estado.RECEBENDO_DANO, 0.25f); // 0.25s por frame
    }

    @Override
    public void update(Player player) {
        if (!estaVivo()) return;

        float deltaTime = 0.0167f; // Supondo 60 FPS
        tempoAnimacao += deltaTime;

        // Calcula a posição do centro do Zumbi e do Player
        int centroZumbiX = x + (larguraHitbox / 2);
        int centroZumbiY = y + (alturaHitbox / 2);
        int centroPlayerX = player.getX() + (player.getLarguraHitbox() / 2);
        int centroPlayerY = player.getY() + (player.getAlturaHitbox() / 2);

        // Calcula a distância entre o Zumbi e o Player
        double distancia = calcularDistancia(centroPlayerX, centroPlayerY);

        if (estadoAtual == Estado.RECEBENDO_DANO) {
            atualizarDano(deltaTime);
            return;
        }

        if (estadoAtual == Estado.ATACANDO) {
            atualizarAtaque(player, deltaTime);
            return;
        }

        // Se o Zumbi está dentro do raio de detecção, ele se aproxima do jogador
        if (distancia <= raioDetecao) {
            if (distancia > distanciaMinima) {
                moverEmDirecao(centroPlayerX, centroPlayerY);
                estadoAtual = Estado.ANDANDO;
            } else {
                iniciarAtaque();
            }
        } else {
            estadoAtual = Estado.PARADO;
        }

        atualizarAnimacao();
    }

    private void iniciarAtaque() {
        estadoAtual = Estado.ATACANDO;
        tempoAtaque = 0;
        frame = 0;
        atacando = true;
    }

    private void atualizarAtaque(Player player, float deltaTime) {
        tempoAtaque += deltaTime;
        tempoAnimacao += deltaTime;

        // Causa dano no início do ataque
        if (atacando && estaColidindoCom(player)) {
            player.tomarDano(danoAtaque);
            atacando = false; // Evita dano contínuo
        }

        // Atualiza animação de ataque
        if (tempoAnimacao >= duracaoFrames.get(Estado.ATACANDO)) { // 0.125s por frame
            frame = (frame + 1) % animacoes.get(Estado.ATACANDO).length;
            tempoAnimacao = 0;
        }

        // Finaliza o ataque após 0.75 segundos
        if (tempoAtaque >= duracaoAtaque) { // 0.75s
            estadoAtual = Estado.PARADO;
            tempoAtaque = 0;
        }
    }

    private void atualizarDano(float deltaTime) {
        tempoAnimacao += deltaTime;
        if (tempoAnimacao >= duracaoFrames.get(Estado.RECEBENDO_DANO)) {
            frame = (frame + 1) % animacoes.get(Estado.RECEBENDO_DANO).length;
            tempoAnimacao = 0;

            // Volta ao estado parado após a animação de dano
            if (frame == animacoes.get(Estado.RECEBENDO_DANO).length - 1) {
                estadoAtual = Estado.PARADO;
            }
        }
    }

    private void atualizarAnimacao() {
        if (tempoAnimacao >= duracaoFrames.get(estadoAtual)) {
            frame = (frame + 1) % animacoes.get(estadoAtual).length;
            tempoAnimacao = 0;
        }
    }

    @Override
    public void tomarDano(int dano) {
        super.tomarDano(dano);
        if (estaVivo()) {
            estadoAtual = Estado.RECEBENDO_DANO;
            frame = 0;
            tempoAnimacao = 0;
        }
    }

    @Override
    public void render(Graphics g, int cameraX, int cameraY) {
        if (!estaVivo()) return;

        BufferedImage[] frames = animacoes.get(estadoAtual);
        BufferedImage frameOriginal = frames[frame % frames.length];

        // Aplica flip horizontal se o Zumbi estiver virado para a esquerda
        BufferedImage frameAtual = (direcao == -1) ? flipSprite(frameOriginal) : frameOriginal;

        // Renderiza centralizado na hitbox
        int renderX = x - cameraX ;
        int renderY = y - cameraY ;

        g.drawImage(frameAtual, renderX, renderY, null);
    }

    private BufferedImage flipSprite(BufferedImage original) {
        BufferedImage flipped = new BufferedImage(
            original.getWidth(), 
            original.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-original.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        op.filter(original, flipped);
        return flipped;
    }

    @Override
    protected void moverEmDirecao(int targetX, int targetY) {
        // Calcula a direção (horizontal e vertical)
        double dx = targetX - (x + larguraHitbox / 2);
        double dy = targetY - (y + alturaHitbox / 2);
        double distancia = Math.sqrt(dx * dx + dy * dy);

        // Define a direção do Zumbi (usando a diferença de posição para decidir para onde ele se move)
        direcao = (dx > 0) ? 1 : -1; // Para movimento horizontal

        // Se estiver dentro da distância mínima, não se move
        if (distancia <= distanciaMinima) return;

        double dirX = dx / distancia;
        double dirY = dy / distancia;

        double step = Math.min(velocidade, distancia - distanciaMinima);

        // Move o Zumbi na direção calculada
        x += (int) (dirX * step);
        y += (int) (dirY * step);
    }

    @Override
    public boolean estaColidindoCom(Player player) {
        // Colisão baseada em retângulos (simples e eficaz)
        return x < player.getX() + player.getLarguraHitbox() &&
               x + larguraHitbox > player.getX() &&
               y < player.getY() + player.getAlturaHitbox() &&
               y + alturaHitbox > player.getY();
    }
}