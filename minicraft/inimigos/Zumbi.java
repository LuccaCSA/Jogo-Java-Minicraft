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
    private HashMap<Estado, BufferedImage[]> animacoes;
    private HashMap<Estado, Float> duracaoFrames;
    private int frame;
    private float tempoAnimacao;
    private int direcao = 1;

    public Zumbi(int x, int y) {
        super(x, y, 30, 2);
        this.estadoAtual = Estado.PARADO;
        this.tempoAnimacao = 0;
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
                sheet.getSprite(36, 36), sheet.getSprite(54, 36),
                sheet.getSprite(72, 36), sheet.getSprite(90, 36)

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
        double distancia = calcularDistancia(player.getCentroX(), player.getCentroY());

        if (distancia <= raioDetecao) {
            if (distancia > alcanceAtaque) {
                moverEmDirecao(player.getCentroX(), player.getCentroY());
                estadoAtual = Estado.ANDANDO;
            } else {
                estadoAtual = Estado.ATACANDO;
                atacar(player, deltaTime);
            }
        } else {
            estadoAtual = Estado.PARADO;
        }

        atualizarAnimacao(deltaTime);
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
            estadoAtual = Estado.DANO;
            frame = 0;
            tempoAnimacao = 0;
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
        super.moverEmDirecao(targetX, targetY);
        double dx = targetX - x;
        direcao = dx > 0 ? 1 : -1;
    }
}