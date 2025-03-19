package minicraft.player;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import minicraft.graphics.SpriteSheet;

public class Player {
    private int x, y, speed = 4, frame;
    private boolean up, down, left, right, facingRight = true;
    private String state = "PARADO";
    private SpriteSheet spriteSheet;
    private HashMap<String, BufferedImage[]> animations;
    private Timer idleTimer;

    // Controle da animação de movimento
    private int animationSpeed = 5; // Ajuste este valor para mudar a velocidade (quanto maior, mais lento)
    private int animationCounter = 0;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        
        this.spriteSheet = new SpriteSheet("minicraft/graphics/sprites/steve_sprites1.png", 18, 18);
        this.animations = new HashMap<>();
        
        loadAnimations();
        startIdleAnimation(); // Inicia o Timer para a animação idle
    }

    private void loadAnimations() {
        animations.put("PARADO", new BufferedImage[]{
            spriteSheet.getSprite(0, 0),
            spriteSheet.getSprite(18, 0),
        });

        animations.put("ANDANDO", new BufferedImage[]{
            spriteSheet.getSprite(0, 18),
            spriteSheet.getSprite(18, 18),
            spriteSheet.getSprite(36, 18),
            spriteSheet.getSprite(54, 18),
        });
    }

    private void startIdleAnimation() {
        idleTimer = new Timer();
        idleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (state.equals("PARADO")) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        // Avança o frame e reinicia ao chegar no final
                        frame = (frame + 1) % animations.get("PARADO").length;
                    });
                }
            }
        }, 500, 500); // Intervalo de 500ms (0.5 segundos)
    }

    public void update() {
        boolean moving = false;
    
        if (left) {
            x -= speed;
            facingRight = false;
            state = "ANDANDO";
            moving = true;
        }
        if (right) {
            x += speed;
            facingRight = true;
            state = "ANDANDO";
            moving = true;
        }
        if (up) {
            y -= speed;
            state = "ANDANDO";
            moving = true;
        }
        if (down) {
            y += speed;
            state = "ANDANDO";
            moving = true;
        }
    
        if (!moving) {
            state = "PARADO";
        }
    
        // Atualiza animação de movimento
        if (state.equals("ANDANDO")) {
            animationCounter++;
            if (animationCounter >= animationSpeed) {
                frame = (frame + 1) % animations.get("ANDANDO").length;
                animationCounter = 0;
            }
        }
    }

    public void render(Graphics g, int cameraX, int cameraY) {
        // Garante que o frame não ultrapasse o tamanho do array
        int maxFrame = animations.get(state).length - 1;
        if (frame > maxFrame) {
            frame = maxFrame;
        }
    
        BufferedImage sprite = animations.get(state)[frame];
        
        if (!facingRight) {
            sprite = flipImage(sprite);
        }
        
        g.drawImage(sprite, x - cameraX, y - cameraY, 48, 48, null);
    }


    private BufferedImage flipImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(image, 0, 0, w, h, w, 0, 0, h, null);
        g2d.dispose();
        return flipped;
    }

    public void handleKeyPress(int keyCode, boolean pressed) {
        if (keyCode == KeyEvent.VK_W) up = pressed;
        if (keyCode == KeyEvent.VK_S) down = pressed;
        if (keyCode == KeyEvent.VK_A) left = pressed;
        if (keyCode == KeyEvent.VK_D) right = pressed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}