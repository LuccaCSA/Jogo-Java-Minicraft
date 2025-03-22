package minicraft.player;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import minicraft.graphics.SpriteSheet;
import minicraft.inimigos.Inimigo;

public class Player {
    private int x, y, speed = 4, frame;
    private int vida = 100;
    private boolean up, down, left, right, facingRight = true;
    private String state = "PARADO";
    private SpriteSheet spriteSheet;
    private HashMap<String, BufferedImage[]> animations;
    private Timer idleTimer;
    private final int larguraHitbox = 48;
    private final int alturaHitbox = 48;

    // Sistema de ataque
    private boolean attacking = false;
    private int attackFrame = 0;
    private long lastAttackTime = 0;
    private final int attackDamage = 15;
    private final int attackWidth = 70;
    private final int attackHeight = 100;
    private final int attackRange = -20;
    private ArrayList<AttackParticle> attackParticles = new ArrayList<>();

    // Controle de animação
    private int animationSpeed = 5;
    private int animationCounter = 0;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;

        this.spriteSheet = new SpriteSheet("minicraft/graphics/sprites/steve_sprites1.png", 18, 18);
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

        animations.put("ATACANDO", new BufferedImage[]{
            spriteSheet.getSprite(0, 54),
            spriteSheet.getSprite(18, 54),
            spriteSheet.getSprite(36, 54),
            spriteSheet.getSprite(54, 54),
            spriteSheet.getSprite(72, 54),
            spriteSheet.getSprite(90, 54),
            spriteSheet.getSprite(108, 54),
            spriteSheet.getSprite(126, 54)
        });
    }

    public void handleMousePress(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !attacking) {
            startAttack();
        }
    }

    private void startAttack() {
        state = "ATACANDO";
        attacking = true;
        attackFrame = 0;
        lastAttackTime = System.currentTimeMillis();
        createAttackParticles();
    }

    private void createAttackParticles() {
        int particleX = facingRight ? x + larguraHitbox + attackRange : x - attackWidth - attackRange;
        int particleY = y + (alturaHitbox / 2) - (attackHeight / 2);

        for (int i = 0; i < 3; i++) {
            attackParticles.add(new AttackParticle(
                particleX + (facingRight ? i * 20 : -i * 20),
                particleY + (int)(Math.random() * attackHeight),
                facingRight
            ));
        }
    }

    public void update(ArrayList<Inimigo> inimigos) {
        if (attacking) {
            updateAttack(inimigos);
            return;
        }

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

        if (state.equals("ANDANDO")) {
            animationCounter++;
            if (animationCounter >= animationSpeed) {
                frame = (frame + 1) % animations.get("ANDANDO").length;
                animationCounter = 0;
            }
        }
    }

    private void updateAttack(ArrayList<Inimigo> inimigos) {
        long currentTime = System.currentTimeMillis();
        attackFrame = (int)((currentTime - lastAttackTime) / 62);

        if (attackFrame >= animations.get("ATACANDO").length) {
            attacking = false;
            state = "PARADO";
            attackParticles.clear();
        } else {
            java.awt.Rectangle attackArea = getAttackArea();
            if (attackArea != null) {
                for (Inimigo inimigo : inimigos) {
                    if (inimigo.estaVivo() && attackArea.intersects(
                            inimigo.getHitboxX() - inimigo.getLarguraHitbox() / 2,
                            inimigo.getHitboxY() - inimigo.getAlturaHitbox() / 2,
                            inimigo.getLarguraHitbox(),
                            inimigo.getAlturaHitbox())) {
                        inimigo.tomarDano(attackDamage);
                    }
                }
            }
        }

        for (int i = attackParticles.size() - 1; i >= 0; i--) {
            if (!attackParticles.get(i).update()) {
                attackParticles.remove(i);
            }
        }
    }

    public void render(Graphics g, int cameraX, int cameraY) {
        BufferedImage sprite;
        if (attacking) {
            sprite = animations.get("ATACANDO")[attackFrame % animations.get("ATACANDO").length];
        } else {
            int maxFrame = animations.get(state).length - 1;
            if (frame > maxFrame) frame = maxFrame;
            sprite = animations.get(state)[frame];
        }

        if (!facingRight) {
            sprite = flipImage(sprite);
        }

        int spriteWidth = 48;
        int spriteHeight = 48;
        int offsetX = (larguraHitbox - spriteWidth) / 2;
        int offsetY = (alturaHitbox - spriteHeight) / 2;

        g.drawImage(sprite,
            x - cameraX + offsetX,
            y - cameraY + offsetY,
            spriteWidth, spriteHeight, null);

        for (AttackParticle particle : attackParticles) {
            particle.render(g, cameraX, cameraY);
        }

        java.awt.Rectangle attackArea = getAttackArea();
        if (attackArea != null) {
            g.setColor(java.awt.Color.RED);
            g.drawRect(attackArea.x - cameraX, attackArea.y - cameraY, attackArea.width, attackArea.height);
        }
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

    public java.awt.Rectangle getAttackArea() {
        if (!attacking || attackFrame < 2 || attackFrame > 5) return null;

        int attackX = facingRight ? x + larguraHitbox + attackRange : x - attackWidth - attackRange;
        int attackY = y + (alturaHitbox / 2) - (attackHeight / 2);

        return new java.awt.Rectangle(attackX, attackY, attackWidth, attackHeight);
    }

    private class AttackParticle {
        int x, y;
        int life = 10;
        boolean facingRight;
        BufferedImage sprite;

        AttackParticle(int x, int y, boolean facingRight) {
            this.x = x;
            this.y = y;
            this.facingRight = facingRight;
            this.sprite = spriteSheet.getSprite(144, 36);
        }

        boolean update() {
            life--;
            x += facingRight ? 3 : -3;
            return life > 0;
        }

        void render(Graphics g, int cameraX, int cameraY) {
            BufferedImage toDraw = facingRight ? sprite : flipImage(sprite);
            g.drawImage(toDraw, x - cameraX - 8, y - cameraY - 8, 16, 16, null);
        }
    }

    public void tomarDano(int dano) {
        vida -= dano;
        if (vida < 0) vida = 0;
        System.out.println("Jogador tomou " + dano + " de dano! Vida restante: " + vida);
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public int getVida() {
        return vida;
    }

    private void startIdleAnimation() {
        idleTimer = new Timer();
        idleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (state.equals("PARADO")) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        frame = (frame + 1) % animations.get("PARADO").length;
                    });
                }
            }
        }, 500, 500);
    }

    public void handleKeyPress(int keyCode, boolean pressed) {
        if (keyCode == KeyEvent.VK_W) up = pressed;
        if (keyCode == KeyEvent.VK_S) down = pressed;
        if (keyCode == KeyEvent.VK_A) left = pressed;
        if (keyCode == KeyEvent.VK_D) right = pressed;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getLarguraHitbox() { return larguraHitbox; }
    public int getAlturaHitbox() { return alturaHitbox; }
    public int getHitboxX() { return x; }
    public int getHitboxY() { return y; }
    public int getCentroX() { return x + (larguraHitbox / 2); }
    public int getCentroY() { return y + (alturaHitbox / 2); }
}