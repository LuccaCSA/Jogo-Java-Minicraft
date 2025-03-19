package minicraft.graphics;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.image.BufferedImage;

public class Cronometro {
    
    private Timer idleTimer;
    private SpriteSheet spriterelogio;
    private HashMap<String, BufferedImage[]> animations;
    private int frame = 0;
    private String state = "horario";  // Defina o estado da animação
    private long startTime; // Tempo inicial do cronômetro
    private final long dayDuration = 160000; // Duração de um ciclo completo do dia em milissegundos 

    public Cronometro() {
        spriterelogio = new SpriteSheet("minicraft/graphics/sprites/horario.png", 18, 18);  // Carregar o sprite do relógio
        animations = new HashMap<>();
        loadAnimations();
        startIdleAnimation();
    }

    private void loadAnimations() {
        // Aqui, o relógio terá duas imagens para animação, alterando a cada 30 segundos
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
                // Muda o frame de animação a cada 30 segundos (30000 milissegundos)
                frame = (frame + 1) % animations.get(state).length;
            }
        }, 0, 5000); // 0 delay inicial, repete a cada 5 segundos
    }

    public BufferedImage getCurrentFrame() {
        return animations.get(state)[frame];
    }

    public float getDayPhase() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        return (float) (elapsedTime % dayDuration) / dayDuration; // Retorna um valor entre 0 e 1
    }
}
