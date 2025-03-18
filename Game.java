import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

// ======================== CLASSE PRINCIPAL ========================
public class Game extends JFrame implements KeyListener {
    private Canvas canvas;
    private Player player;
    private SpriteSheet spriteSheet;
    private BarraDeItens barraDeItens;
    private Camera camera; // Adiciona a câmera
    private Mapa mapa;
    private Cronometro cronometro;
    private final int mapWidth = 1600, mapHeight = 1200; // Tamanho do mapa

    public Game() {
        setTitle("Minicraft");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(1000, 800));
        canvas.setFocusable(false);

        add(canvas);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        addKeyListener(this);

        SpriteSheet jogadorsprite = new SpriteSheet("sprites/steve_sprites1.png", 16, 16);
        player = new Player(500, 400, jogadorsprite);
        SpriteSheet barrasprite = new SpriteSheet("sprites/hotbar1.png", 16, 16);
        barraDeItens = new BarraDeItens(barrasprite);

        camera = new Camera(0, 0); // Inicializa a câmera

        mapa = new Mapa("sprites/mapa.png"); // Certifique-se de usar o caminho correto

        cronometro = new Cronometro("sprites/horario.png");

        new Thread(this::gameLoop).start();
    }

    private void gameLoop() {
        while (true) {
            player.update();
            camera.update(player, mapWidth, mapHeight, 950, 800); // Atualiza a câmera

            render();
            try {
                Thread.sleep(80); // Controla a velocidade da animação
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
    
        Graphics g = bs.getDrawGraphics();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
        // Renderizar o mapa com base na posição da câmera
        mapa.render(g, camera.getX(), camera.getY());
    
        // Renderizar o jogador e outros elementos
        player.render(g, camera.getX(), camera.getY());
        barraDeItens.render(g);
    
        // Render do cronômetro (5x maior)
        BufferedImage currentFrame = cronometro.getCurrentFrame();
        int relogioWidth = currentFrame.getWidth() * 6;  // Aumenta a largura em 6 vezes
        int relogioHeight = currentFrame.getHeight() * 6; // Aumenta a altura em 6 vezes
        g.drawImage(currentFrame, 50, 50, relogioWidth, relogioHeight, null);
    
        // Aplicar sobreposição de cores conforme a fase do dia
        float dayPhase = cronometro.getDayPhase();
        applyDayPhaseTint(g, dayPhase);

        g.dispose();
        bs.show();
    }

    // Método para aplicar a sobreposição de cores conforme a fase do dia
    private void applyDayPhaseTint(Graphics g, float dayPhase) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
    
        if (dayPhase < 0.25f) {
            // Amanhecer: transição de escuro para claro
            float intensity = dayPhase / 0.25f;
            g.setColor(new Color(255, 255, 255, (int) (50 * intensity))); // Leve brilho
        } else if (dayPhase < 0.5f) {
            // Meio-dia: sem sobreposição
            g.setColor(new Color(0, 0, 0, 0));
        } else if (dayPhase < 0.75f) {
            // Entardecer: transição para alaranjado suave
            float intensity = (dayPhase - 0.5f) / 0.25f;
            g.setColor(new Color(255, 165, 0, (int) (50 * intensity))); // Alaranjado suave
        } else if(dayPhase<0.90){
            // Anoitecer: transição para escuro (mas não totalmente escuro)
            float intensity = (dayPhase - 0.75f) / 0.25f;
            g.setColor(new Color(0, 0, 0, (int) (100 * intensity))); // Escuro moderado
        } else{
            float intensity = (dayPhase - 0.6f) / 0.25f;
            g.setColor(new Color(0, 0, 0, (int) (50 * intensity))); // clareia antes do amanhecer
        }
    
        // Desenha a sobreposição sobre toda a tela
        g.fillRect(0, 0, width, height);
    }
    
        
    @Override
    public void keyPressed(KeyEvent e) {
    player.handleKeyPress(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
    player.handleKeyPress(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    public static void main(String[] args) {
        new Game();
    }
}

// ======================== CLASSE PLAYER ========================
class Player {
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

// ======================== CLASSE SPRITE SHEET ========================
class SpriteSheet {
    private BufferedImage sheet;
    private int spriteWidth, spriteHeight;

    public SpriteSheet(String path, int spriteWidth, int spriteHeight) {
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        try {
            sheet = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getSprite(int x, int y) {
        return sheet.getSubimage(x, y, spriteWidth, spriteHeight);
    }
}
//=====classe camera=====
class Camera {
    private int x, y;
    
    public Camera(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(Player player, int mapWidth, int mapHeight, int screenWidth, int screenHeight) {
        // Centraliza a câmera no jogador
        x = player.getX() - screenWidth / 2;
        y = player.getY() - screenHeight / 2;

        // Impede que a câmera mostre áreas fora do mapa
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > mapWidth - screenWidth) x = mapWidth - screenWidth;
        if (y > mapHeight - screenHeight) y = mapHeight - screenHeight;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}

//====classe mapa====
class Mapa {
    private BufferedImage mapaImage;

    public Mapa(String caminho) {
        try {
            mapaImage = ImageIO.read(new File(caminho));
            if (mapaImage == null) {
                System.out.println("Erro: a imagem do mapa não foi carregada!");
            } else {
                System.out.println("Mapa carregado! Dimensões: " + mapaImage.getWidth() + "x" + mapaImage.getHeight());
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar a imagem do mapa: " + caminho);
            e.printStackTrace();
        }
    }

    public void render(Graphics g, int cameraX, int cameraY) {
        if (mapaImage != null) {
            g.drawImage(mapaImage, -cameraX, -cameraY, null);
        } else {
            System.out.println("Erro: mapaImage é null na renderização!");
        }
    }
}

//====classe cronometro===

class Cronometro {
    private Timer idleTimer;
    private SpriteSheet spriterelogio;
    private HashMap<String, BufferedImage[]> animations;
    private int frame = 0;
    private String state = "horario";  // Defina o estado da animação
    private long startTime; // Tempo inicial do cronômetro
    private final long dayDuration = 160000; // Duração de um ciclo completo do dia em milissegundos 

    public Cronometro(String caminhoSprites) {
        spriterelogio = new SpriteSheet(caminhoSprites, 18, 18);  // Carregar o sprite do relógio
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


// ======================== CLASSE ITEM ========================
abstract class Item {
    protected String nome;
    protected BufferedImage sprite;
    
    public Item(String nome, BufferedImage sprite) {
        this.nome = nome;
        this.sprite = sprite;
    }
    
    public String getNome() { return nome; }
    public BufferedImage getSprite() { return sprite; }
}
// ======================== CLASSES DE ITENS ========================
class ItemVazio extends Item {
    public ItemVazio(BufferedImage sprite) { super("Mão Vazia", null); }
}

class ItemBloco extends Item {
    public ItemBloco(BufferedImage sprite) { super("Bloco", sprite); }
}

class ItemComida extends Item {
    public ItemComida(BufferedImage sprite) { super("Comida", sprite); }
}

class ItemEspada extends Item {
    public ItemEspada(BufferedImage sprite) { super("Espada", sprite); }
}

// ======================== CLASSE BARRA DE ITENS ========================
class BarraDeItens {
    private Item[] slots;
    private int slotSelecionado;


    public BarraDeItens(SpriteSheet barrasprite) {
        slots = new Item[6];
        slots[0] = new ItemVazio(barrasprite.getSprite(0, 0));
        slots[1] = new ItemEspada(barrasprite.getSprite(0, 18));
        slots[2] = new ItemComida(barrasprite.getSprite(0, 36));
        slots[3] = new ItemBloco(barrasprite.getSprite(0, 54));
        slots[4] = new ItemBloco(barrasprite.getSprite(0, 72));
        slots[5] = new ItemBloco(barrasprite.getSprite(0, 90));
        
        slotSelecionado = 0;
    }
    
    public void selecionarSlot(int slot) {
        if (slot >= 0 && slot < slots.length) {
            slotSelecionado = slot;
        }
    }
    
    public Item getItemAtual() {
        return slots[slotSelecionado];
    }
    
    public void render(Graphics g) {
        for (int i = 0; i < slots.length; i++) {
            int x = 100 + i * 50;
            int y = 500;
            g.setColor(i == slotSelecionado ? Color.WHITE : Color.GRAY);
            g.fillRect(x, y, 50, 50);
            
            if (slots[i].getSprite() != null) {
                g.drawImage(slots[i].getSprite(), x + 6, y + 6, 40, 40, null);
            }
        }
    }
}
