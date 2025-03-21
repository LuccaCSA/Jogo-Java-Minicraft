package minicraft.main;

import javax.swing.*;
import minicraft.graphics.Camera;
import minicraft.graphics.Cronometro;
import minicraft.inimigos.Creeper;
import minicraft.inimigos.Inimigo;
import minicraft.inimigos.Slime;
import minicraft.inimigos.Zumbi;
import minicraft.player.Player;
import minicraft.world.Mapa;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

public class Jogo extends JFrame implements KeyListener {
    private Canvas canvas;
    private boolean running = false;
    private Mapa mapa;
    private Player player;
    private Camera camera;
    private Cronometro cronometro;
    private Creeper creeper;
    private Slime slimeteste;
    private Zumbi zumbiteste;
    private ArrayList<Inimigo> inimigos;


    public Jogo() {
        // Configurações da janela
        setTitle("Minicraft");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Configura o Canvas
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(1200, 1000));
        canvas.setFocusable(false);
        add(canvas);

        // Centraliza a janela na tela
        pack();
        setLocationRelativeTo(null);

        // Adiciona o KeyListener para eventos de teclado
        addKeyListener(this);

        // Adiciona o MouseListener para eventos de mouse
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Botão esquerdo (M1)
                    player.handleMousePress(e); // Chama o método de ataque do Player
                } else if (e.getButton() == MouseEvent.BUTTON3) { // Botão direito (M3)
                    System.out.println("Botão direito clicado na posição: " + e.getX() + ", " + e.getY());
                    // Adicione aqui a lógica para o botão direito, se desejar
                }
            }
        });

        // Torna a janela visível
        setVisible(true);

        // Inicializa o jogador, o mapa e a câmera
        player = new Player(600, 500);
        mapa = new Mapa();
        camera = new Camera(600, 500);
        cronometro = new Cronometro();
        creeper = new Creeper(300, 300);
        slimeteste = new Slime(300, 300);
        zumbiteste = new Zumbi(600, 300);
        inimigos = new ArrayList<>();
        inimigos.add(zumbiteste);

        startGame();
    }

    // Método para iniciar o jogo
    private void startGame() {
        running = true;
        gameLoop();
    }

    // Loop principal do jogo
    private void gameLoop() {
        long gameStartTime = System.currentTimeMillis();

        while (running) {
            long currentTime = System.currentTimeMillis();
            long gameTime = currentTime - gameStartTime;

            updateGame(gameTime);
            render(gameTime);

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para atualizar a lógica do jogo
    private void updateGame(long gameTime) {
        player.update(inimigos);
        camera.update(player, mapa.getLargura(), mapa.getAltura(), canvas.getWidth(), canvas.getHeight());

        if (creeper.estaVivo()) {
            creeper.update(player);
        }

        if (slimeteste.estaVivo()) {
            slimeteste.update(player);
        }

        if (zumbiteste.estaVivo()) {
            zumbiteste.update(player);
        }
    }

    // Método para renderizar o jogo
    private void render(long gameTime) {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Color skyColor = cronometro.getSkyColor(gameTime);
        mapa.render(g, camera.getX(), camera.getY(), skyColor);

        player.render(g, camera.getX(), camera.getY());
        cronometro.render(g);

        if (creeper.estaVivo()) {
            creeper.render(g, camera.getX(), camera.getY());
        }

        if (slimeteste.estaVivo()) {
            slimeteste.render(g, camera.getX(), camera.getY());
        }

        if (zumbiteste.estaVivo()) {
            zumbiteste.render(g, camera.getX(), camera.getY());
        }

        for (Inimigo inimigo : inimigos) {
            inimigo.render(g, 0, 0);
        }

        g.dispose();
        bs.show();
    }

    // Métodos do KeyListener
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        player.handleKeyPress(keyCode, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        player.handleKeyPress(keyCode, false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Não usado no momento
    }

    // Método principal
    public static void main(String[] args) {
        new Jogo();
    }

    public Player getPlayer() { return player; }
    public ArrayList<Inimigo> getInimigos() { return inimigos; }
}