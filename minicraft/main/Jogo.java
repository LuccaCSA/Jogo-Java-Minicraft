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
        setTitle("Minicraft");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(1200, 1000));
        canvas.setFocusable(false);
        add(canvas);

        pack();
        setLocationRelativeTo(null);

        addKeyListener(this);

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    player.handleMousePress(e);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    System.out.println("Botão direito clicado na posição: " + e.getX() + ", " + e.getY());
                }
            }
        });

        setVisible(true);

        player = new Player(600, 500);
        mapa = new Mapa();
        camera = new Camera(600, 500);
        cronometro = new Cronometro();
        creeper = new Creeper(300, 300);
        slimeteste = new Slime(400, 400);
        zumbiteste = new Zumbi(600, 300);
        inimigos = new ArrayList<>();
        inimigos.add(creeper);
        inimigos.add(slimeteste);
        inimigos.add(zumbiteste);

        startGame();
    }

    private void startGame() {
        running = true;
        gameLoop();
    }

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

    private void updateGame(long gameTime) {
        player.update(inimigos);
        camera.update(player, mapa.getLargura(), mapa.getAltura(), canvas.getWidth(), canvas.getHeight());

        for (Inimigo inimigo : inimigos) {
            if (inimigo.estaVivo()) {
                inimigo.update(player);
                System.out.println("Updating " + inimigo.getClass().getSimpleName() + " at x=" + inimigo.getX() + ", y=" + inimigo.getY() + ", vivo=" + inimigo.estaVivo());
            }
        }
    }

    private void render(long gameTime) {
        SwingUtilities.invokeLater(() -> {
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

            for (Inimigo inimigo : inimigos) {
                if (inimigo.estaVivo()) {
                    inimigo.render(g, camera.getX(), camera.getY());
                    System.out.println("Rendering " + inimigo.getClass().getSimpleName() + " at x=" + inimigo.getX() + ", y=" + inimigo.getY());
                } else {
                    System.out.println("Skipping render of " + inimigo.getClass().getSimpleName() + " (not alive)");
                }
            }

            g.dispose();
            bs.show();
        });
    }

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
    }

    public static void main(String[] args) {
        new Jogo();
    }

    public Player getPlayer() { return player; }
    public ArrayList<Inimigo> getInimigos() { return inimigos; }
}