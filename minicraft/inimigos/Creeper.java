package minicraft.inimigos;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import minicraft.graphics.SpriteSheet;
import minicraft.player.Player;

public class Creeper extends Inimigo {
    private enum Estado {
        PARADO, ANDANDO, EXPLODINDO, DANO, EXPLOSAO_FINAL
    }

    private Estado estadoAtual;
    private Estado estadoAnterior;
    private HashMap<Estado, BufferedImage[]> animacoes;
    private HashMap<Estado, Float> duracaoFrames;
    private int frame;
    private float tempoAnimacao;
    private float tempoExplosao;
    private boolean explodiu;
    
    private float knockbackX, knockbackY;
    private final int danoExplosao = 20;
    private final int distanciaExplosao = 35;
    private final int raioExplosao = 50;
    
    private int direcao = 1;
    private int explosaoX, explosaoY;

    public Creeper(int x, int y) {
        super(x, y, 50, 3);
        this.estadoAtual = Estado.PARADO;
        this.estadoAnterior = Estado.PARADO;
        this.tempoAnimacao = 0;
        this.tempoExplosao = 0;
        this.explodiu = false;
        carregarAnimacoes();
        configurarDuracoes();
    }

    private void carregarAnimacoes() {
        animacoes = new HashMap<>();
        try {
            SpriteSheet sheet = new SpriteSheet("minicraft/graphics/sprites/sprites_creeper.png", 18, 18);
            
            // Parado
            animacoes.put(Estado.PARADO, new BufferedImage[]{
                sheet.getSprite(0, 0), sheet.getSprite(18, 0)
            });

            // Andando
            animacoes.put(Estado.ANDANDO, new BufferedImage[]{
                sheet.getSprite(0, 18), sheet.getSprite(18, 18), 
                sheet.getSprite(36, 18), sheet.getSprite(54, 18)
            });

            // Explodindo (animação principal)
            animacoes.put(Estado.EXPLODINDO, new BufferedImage[]{
                sheet.getSprite(0, 36), sheet.getSprite(18, 36),
                sheet.getSprite(36, 36), sheet.getSprite(54, 36),
                sheet.getSprite(72, 36), sheet.getSprite(90, 36)
            });

            // Explosão final (residual)
            animacoes.put(Estado.EXPLOSAO_FINAL, new BufferedImage[]{
                sheet.getSprite(0, 72), sheet.getSprite(18, 72),
                sheet.getSprite(36, 72), sheet.getSprite(54, 72)
            });

            // Dano
            animacoes.put(Estado.DANO, new BufferedImage[]{
                sheet.getSprite(0, 54), sheet.getSprite(18, 54),
                sheet.getSprite(36, 54), sheet.getSprite(54, 54)
            });

        } catch (Exception e) {
            System.err.println("ERRO: Falha ao carregar sprites do Creeper!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void configurarDuracoes() {
        duracaoFrames = new HashMap<>();
        duracaoFrames.put(Estado.PARADO, 0.5f);        // 0.5s por frame
        duracaoFrames.put(Estado.ANDANDO, 0.15f);      // 0.15s por frame
        duracaoFrames.put(Estado.EXPLODINDO, 0.1667f); // ~0.1667s por frame (1s total)
        duracaoFrames.put(Estado.EXPLOSAO_FINAL, 0.25f);// 0.25s por frame (1s total)
        duracaoFrames.put(Estado.DANO, 0.25f);         // 0.25s por frame
    }

    @Override
    public void update(Player player) {
        if (explodiu) return;

        // Atualiza explosão final mesmo após "morte"
        if (!estaVivo() && estadoAtual == Estado.EXPLOSAO_FINAL) {
            atualizarExplosaoFinal();
            return;
        }

        float deltaTime = 0.0167f;
        double distancia = calcularDistancia(player.getX(), player.getY());

        if (knockbackX != 0 || knockbackY != 0) {
            aplicarKnockback(deltaTime);
            return;
        }

        switch (estadoAtual) {
            case DANO:
                atualizarDano(deltaTime);
                break;
                
            case EXPLODINDO:
                atualizarExplosao(player, deltaTime, distancia);
                break;
                
            default:
                atualizarMovimento(player, deltaTime, distancia);
                break;
        }
    }

    private void atualizarExplosaoFinal() {
        tempoAnimacao += 0.0167f;
        if (tempoAnimacao >= duracaoFrames.get(Estado.EXPLOSAO_FINAL)) {
            frame = (frame + 1) % animacoes.get(Estado.EXPLOSAO_FINAL).length;
            tempoAnimacao = 0;
            
            if(frame >= animacoes.get(Estado.EXPLOSAO_FINAL).length - 1) {
                explodiu = true;
            }
        }
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

    private void atualizarDano(float deltaTime) {
        tempoAnimacao += deltaTime;
        if (tempoAnimacao >= duracaoFrames.get(Estado.DANO)) {
            frame = (frame + 1) % animacoes.get(Estado.DANO).length;
            tempoAnimacao = 0;
            
            if (frame >= animacoes.get(Estado.DANO).length - 1) {
                estadoAtual = estadoAnterior;
                frame = 0;
            }
        }
    }

    private void atualizarExplosao(Player player, float deltaTime, double distancia) {
        // Verifica se jogador saiu do alcance
        if (distancia > distanciaExplosao) {
            cancelarExplosao();
            return;
        }

        tempoExplosao += deltaTime;
        tempoAnimacao += deltaTime;

        // Atualiza frame da explosão
        if (tempoAnimacao >= duracaoFrames.get(Estado.EXPLODINDO)) {
            frame = (frame + 1) % animacoes.get(Estado.EXPLODINDO).length;
            tempoAnimacao = 0;
        }

        // Finaliza explosão após 1 segundo
        if (tempoExplosao >= 1.0f) {
            finalizarExplosao(player);
        }
    }

    private void cancelarExplosao() {
        estadoAtual = Estado.ANDANDO;
        tempoExplosao = 0;
        frame = 0;
        tempoAnimacao = 0;
    }

    private void finalizarExplosao(Player player) {
        // Aplica dano
        if (calcularDistancia(player.getX(), player.getY()) <= raioExplosao) {
            player.tomarDano(danoExplosao);
        }
        
        // Prepara explosão residual
        explosaoX = x;
        explosaoY = y;
        estadoAtual = Estado.EXPLOSAO_FINAL;
        vivo = false;
        frame = 0;
        tempoAnimacao = 0;
    }

    private void atualizarMovimento(Player player, float deltaTime, double distancia) {
        if (distancia <= raioDetecao) {
            // Atualiza direção
            double dx = player.getX() - x;
            if(dx != 0) direcao = (dx > 0) ? 1 : -1;

            if (distancia > distanciaMinima) {
                moverEmDirecao(player.getX(), player.getY());
                estadoAtual = Estado.ANDANDO;
            }
            
            // Inicia explosão ao chegar perto
            if (distancia <= distanciaExplosao) {
                iniciarExplosao();
            }
        } else {
            estadoAtual = Estado.PARADO;
        }
        atualizarAnimacao(deltaTime);
    }

    private void iniciarExplosao() {
        estadoAtual = Estado.EXPLODINDO;
        frame = 0;
        tempoAnimacao = 0;
        tempoExplosao = 0;
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
            
            // Calcula knockback
            double angulo = Math.atan2(y - getY(), x - getX());
            knockbackX = (float) Math.cos(angulo);
            knockbackY = (float) Math.sin(angulo);
            direcao = (knockbackX > 0) ? 1 : -1;
        }
    }

    private BufferedImage getSpriteDirecao(BufferedImage original) {
        if(direcao == 1) return original;
        
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
    public void render(Graphics g, int cameraX, int cameraY) {
        if (explodiu) return;
        
        BufferedImage[] frames = animacoes.get(estadoAtual);
        BufferedImage frameOriginal = frames[frame % frames.length];
        
        // Define posição de renderização
        int renderX = estadoAtual == Estado.EXPLOSAO_FINAL ? explosaoX : x;
        int renderY = estadoAtual == Estado.EXPLOSAO_FINAL ? explosaoY : y;

        BufferedImage frameAtual = getSpriteDirecao(frameOriginal);
        
        // Ajuste de posição para flip
        int drawX = renderX - cameraX - 16;
        if(direcao == -1) drawX -= 2;
        
        g.drawImage(frameAtual, 
            drawX,
            renderY - cameraY - 16,
            48, 48, null);
    }

    @Override
    protected void moverEmDirecao(int targetX, int targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if(distance <= distanciaMinima) return;
        
        double step = Math.min(velocidade, distance - distanciaMinima);
        double dirX = dx / distance;
        double dirY = dy / distance;
        
        x += (int)(dirX * step);
        y += (int)(dirY * step);
    }
}