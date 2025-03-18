package minicraft.player;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import minicraft.graphics.SpriteSheet;
//asda

public class Player {
    private int x, y, speed = 7, frame;
    private boolean up, down, left, right, facingRight = true;
    private String state = "PARADO";
    private SpriteSheet spriteSheet;
    private HashMap<String, BufferedImage[]> animations;
    private Timer idleTimer;

    public Player(int x, int y, SpriteSheet spriteSheet) {
        this.x = x;
        this.y = y;
        this.spriteSheet = spriteSheet;
        this.animations = new HashMap<>();

        loadAnimations();
        startIdleAnimation();
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
        /*
        animação de soco
        * animations.put("SOCANDO", new BufferedImage[]{
                    spriteSheet.getSprite(0, 36),
                    spriteSheet.getSprite(18, 36),
                    spriteSheet.getSprite(36, 36),
                    spriteSheet.getSprite(54, 36),
                    spriteSheet.getSprite(72, 36),
                    spriteSheet.getSprite(90, 36),
                });
        */
    }

    private void startIdleAnimation() {
        idleTimer = new Timer();
        idleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (state.equals("PARADO")) {
                    frame = (frame + 1) % animations.get("PARADO").length;
                }
            }
        }, 800, 800); // Alterna a cada 1 segundo
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
        } else {
            frame = (frame + 1) % animations.get(state).length;
        }
    
        // Adicionando uma segurança extra para evitar acessar um índice inválido
        if (frame >= animations.get(state).length) {
            frame = 0;
        }
    }
    

    public void render(Graphics g, int cameraX, int cameraY) {
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