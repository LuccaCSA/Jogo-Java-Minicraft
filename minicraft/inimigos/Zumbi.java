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
        PARADO, ANDANDO, ATACANDO, DANO
    }

    private Estado estadoAtual;
    private Estado estadoAnterior;
    private HashMap<Estado, BufferedImage[]> animacoes;
    private HashMap<Estado, Float> duracaoFrames;
    private int frame;
    private float tempoAnimacao;
    private float tempoAtaque;
    private boolean atacando;
    private boolean danoAplicado;
    private final int danoAtaque = 10;
    private final float duracaoAtaque = 1.0f;
    private final float tempoDano = 1.0f;
    private final int alcanceAtaque = 25;

    private float knockbackX, knockbackY;
    private int direcao = 1;

    public Zumbi(int x, int y) {
        super(x, y, 20, 2);
        this.estadoAtual = Estado.PARADO;
        this.estadoAnterior = Estado.PARADO;
        this.tempoAnimacao = 0;
        this.tempoAtaque = 0;
        this.atacando = false;
        this.danoAplicado = false;
        carregarAnimacoes();
        configurarDuracoes();
    }

    private void carregarAnimacoes() {
        animacoes = new HashMap<>();
        try {
            SpriteSheet sheet = new SpriteSheet("minicraft/graphics/sprites/sprites_zumbi.png", 18, 18);

            animacoes.put(Estado.PARADO, new BufferedImage[]{
                sheet.getSprite(0, 0), sheet.getSprite(18, 0)
            });

            animacoes.put(Estado.ANDANDO, new BufferedImage[]{
                sheet.getSprite(0, 18), sheet.getSprite(18, 18),
                sheet.getSprite(36, 18), sheet.getSprite(54, 18)
            });

            animacoes.put(Estado.ATACANDO, new BufferedImage[]{
                sheet.getSprite(0, 36), sheet.getSprite(18, 36),
                sheet.getSprite(36, 36), sheet.getSprite(54, 36)
            });

            animacoes.put(Estado.DANO, new BufferedImage[]{
                sheet.getSprite(0, 54), sheet.getSprite(18, 54),
                sheet.getSprite(36, 54), sheet.getSprite(54, 54)
            });

        } catch (Exception e) {
            System.err.println("ERRO: Falha ao carregar sprites do Zumbi!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void configurarDuracoes() {
        duracaoFrames = new HashMap<>();
        duracaoFrames.put(Estado.PARADO, 0.5f);
        duracaoFrames.put(Estado.ANDANDO, 0.15f);
        duracaoFrames.put(Estado.ATACANDO, 0.25f);
        duracaoFrames.put(Estado.DANO, 0.25f);
    }

    @Override
    public void update(Player player) {
        if (!estaVivo()) return;

        float deltaTime = 0.0167f;

        if (knockbackX != 0 || knockbackY != 0) {
            aplicarKnockback(deltaTime);
            return;
        }

        double distancia = calcularDistancia(player.getX(), player.getY());

        if (distancia <= raioDetecao) {
            if (distancia > alcanceAtaque) {
                moverEmDirecao(player.getX(), player.getY());
                estadoAtual = Estado.ANDANDO;
            } else if (distancia <= alcanceAtaque && estadoAtual != Estado.ATACANDO) {
                iniciarAtaque();
            }
        } else {
            estadoAtual = Estado.PARADO;
        }

        if (estadoAtual == Estado.ATACANDO) {
            atualizarAtaque(player, deltaTime);
        }

        atualizarAnimacao(deltaTime);
    }

    private void aplicarKnockback(float deltaTime) {
        x += knockbackX * deltaTime * 50;
        y += knockbackY * deltaTime * 50;

        knockbackX *= (1 - deltaTime * 5);
        knockbackY *= (1 - deltaTime * 5);

        if (Math.abs(knockbackX) < 0.1f && Math.abs(knockbackY) < 0.1f) {
            knockbackX = 0;
            knockbackY = 0;
        }
    }

    private void iniciarAtaque() {
        estadoAtual = Estado.ATACANDO;
        frame = 0;
        tempoAnimacao = 0;
        tempoAtaque = 0;
        atacando = true;
        danoAplicado = false;
    }

    private void atualizarAtaque(Player player, float deltaTime) {
        tempoAtaque += deltaTime;
        tempoAnimacao += deltaTime;
    
        if (tempoAnimacao >= duracaoFrames.get(Estado.ATACANDO)) {
            frame = (frame + 1) % animacoes.get(Estado.ATACANDO).length;
            tempoAnimacao = 0;
    
            if (frame == animacoes.get(Estado.ATACANDO).length - 1 && !danoAplicado && estaColidindoCom(player)) {
                player.tomarDano(danoAtaque);
                danoAplicado = true;
            }
        }
    
        if (tempoAtaque >= duracaoAtaque) {
            estadoAtual = Estado.PARADO;
            tempoAtaque = 0;
            atacando = false;
            danoAplicado = false;
        }
    }

    private void atualizarAnimacao(float deltaTime) {
        tempoAnimacao += deltaTime;
        if (tempoAnimacao >= duracaoFrames.get(estadoAtual)) {
            frame = (frame + 1) % animacoes.get(estadoAtual).length;
            tempoAnimacao = 0;
        }
    }

    @Override
    public void tomarDano(int dano) {
        super.tomarDano(dano);
        if (estaVivo()) {
            estadoAnterior = estadoAtual;
            estadoAtual = Estado.DANO;
            frame = 0;
            tempoAnimacao = 0;

            double angulo = Math.atan2(y - getY(), x - getX());
            knockbackX = (float) Math.cos(angulo);
            knockbackY = (float) Math.sin(angulo);
            direcao = (knockbackX > 0) ? 1 : -1;
        }
    }

    @Override
    public void render(Graphics g, int cameraX, int cameraY) {
        BufferedImage[] frames = animacoes.get(estadoAtual);
        BufferedImage frameOriginal = frames[frame % frames.length];

        BufferedImage frameAtual = getSpriteDirecao(frameOriginal);

        g.drawImage(frameAtual, 
            x - cameraX - 16, 
            y - cameraY - 16, 
            48, 48, null);
    }

    private BufferedImage getSpriteDirecao(BufferedImage original) {
        if (direcao == 1) return original;
    
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
        double dx = targetX - x;
        double dy = targetY - y;
        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia <= distanciaMinima) return;

        double step = Math.min(velocidade, distancia - distanciaMinima);
        double dirX = dx / distancia;
        double dirY = dy / distancia;

        x += (int) (dirX * step);
        y += (int) (dirY * step);

        if (dirX > 0) {
            direcao = 1;
        } else if (dirX < 0) {
            direcao = -1;
        }
    }

    @Override
    public boolean estaColidindoCom(Player player) {
        int zEsq = x;
        int zDir = x + larguraHitbox;
        int zTopo = y;
        int zBase = y + alturaHitbox;

        int pEsq = player.getX() - (player.getLarguraHitbox() / 2);
        int pDir = pEsq + player.getLarguraHitbox();
        int pTopo = player.getY() - (player.getAlturaHitbox() / 2);
        int pBase = pTopo + player.getAlturaHitbox();

        return zEsq < pDir && 
               zDir > pEsq && 
               zTopo < pBase && 
               zBase > pTopo;
    }
}