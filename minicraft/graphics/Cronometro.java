package minicraft.graphics;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Cronometro {
    private Timer idleTimer;
    private SpriteSheet spriterelogio;
    private HashMap<String, BufferedImage[]> animations;
    private int frame = 0;
    private String state = "horario";
    private long startTime;
    private final long dayDuration = 160000; // 160 segundos (2 minutos e 40 segundos)
    private int x, y;
    private long lastFrameTime = System.currentTimeMillis(); // Tempo da última troca de frame

    public Cronometro() {
        this.x = 30; // Posição X fixa
        this.y = 30; // Posição Y fixa
        spriterelogio = new SpriteSheet("minicraft/graphics/sprites/horario.png", 18, 18);
        animations = new HashMap<>();
        startTime = System.currentTimeMillis(); // Inicia o tempo
        loadAnimations();
        startIdleAnimation();
    }

    private void loadAnimations() {
        animations.put("horario", new BufferedImage[]{
            spriterelogio.getSprite(0, 0),
            spriterelogio.getSprite(18, 0),
            spriterelogio.getSprite(36, 0),
            spriterelogio.getSprite(54, 0),
            spriterelogio.getSprite(72, 0),
            spriterelogio.getSprite(90, 0),
            spriterelogio.getSprite(108, 0),
            spriterelogio.getSprite(126, 0),
            spriterelogio.getSprite(0, 18),
            spriterelogio.getSprite(18, 18),
            spriterelogio.getSprite(36, 18),
            spriterelogio.getSprite(54, 18),
            spriterelogio.getSprite(72, 18),
            spriterelogio.getSprite(90, 18),
            spriterelogio.getSprite(108, 18),
            spriterelogio.getSprite(126, 18),
            spriterelogio.getSprite(0, 36),
            spriterelogio.getSprite(18, 36),
            spriterelogio.getSprite(36, 36),
            spriterelogio.getSprite(54, 36),
            spriterelogio.getSprite(72, 36),
            spriterelogio.getSprite(90, 36),
            spriterelogio.getSprite(108, 36),
            spriterelogio.getSprite(126, 36),
            spriterelogio.getSprite(0, 54),
            spriterelogio.getSprite(18, 54),
            spriterelogio.getSprite(36, 54),
            spriterelogio.getSprite(54, 54),
            spriterelogio.getSprite(72, 54),
            spriterelogio.getSprite(90, 54),
            spriterelogio.getSprite(108, 54),
            spriterelogio.getSprite(126, 54)
        });
    }

    private void startIdleAnimation() {
        idleTimer = new Timer();
        idleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                frame = (frame + 1) % animations.get(state).length;
            }
        }, 0, 5000); // Muda o frame a cada 5 segundos
    }

    public BufferedImage getCurrentFrame() {
        return animations.get(state)[frame];
    }

    public void update() {
        // Atualiza a animação (se necessário)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= 5000) { // 5000ms = 5 segundos
            frame = (frame + 1) % animations.get(state).length;
            lastFrameTime = currentTime;
        }
    }

    public void render(Graphics g) {
        BufferedImage currentFrame = getCurrentFrame();
        if (currentFrame != null) {
            // Desenha o cronômetro em uma posição fixa e aumenta o tamanho em 6 vezes
            g.drawImage(currentFrame, x, y, 48 * 3, 48 * 3, null);
        }
    }


    public float getDayPhase(long gameTime) {
        return (float) (gameTime % dayDuration) / dayDuration;
    }

    public String spawnMonstro;

    public Color getSkyColor(long gameTime) {
        float phase = getDayPhase(gameTime);
        float r, g, b, alpha;


        if (phase < 0.25f) { // Amanhecer (Escuro -> Claro)
            alpha = 0.7f * (1.0f - (phase / 0.25f));
            r = 0.0f;
            g = 0.0f;
            b = 0.2f;
        } 
        else if (phase < 0.5f) { // Meio-dia (Claro -> Sem efeito)
            alpha = 1.0f - ((phase - 0.25f) / 0.25f);
            r = 1.0f;
            g = 1.0f;
            b = 1.0f;
        }
        else if (phase < 0.75f) { // Entardecer (Sem efeito -> Laranja)
            alpha = 0.6f * ((phase - 0.5f) / 0.25f);
            r = 1.0f;
            g = 0.5f;
            b = 0.3f;
        }
        else { // Noite (Laranja -> Escuro)
            alpha = 0.8f * ((phase - 0.75f) / 0.25f);
            r = 0.2f;
            g = 0.1f;
            b = 0.4f;
            spawnMonstro = "sim";
        }

        return new Color(r, g, b, alpha); 
    }

}