package minicraft.main;

import javax.swing.*;
import minicraft.graphics.Camera;
import minicraft.graphics.Cronometro;
import minicraft.inimigos.Creeper;
import minicraft.inimigos.Slime;
import minicraft.inimigos.Zumbi;
import minicraft.player.Player;
import minicraft.world.Mapa;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

public class Jogo extends JFrame implements KeyListener {
    private Canvas canvas; // área de desenho do jogo
    private boolean running = false; // Controla se o jogo está em execução
    private Mapa mapa;
    private Player player;
    private Camera camera;
    private Cronometro cronometro;
    private Creeper creeper;
    private Slime slimeteste;
    private Zumbi zumbiteste;

    public Jogo() {
        // Configurações da janela
        setTitle("Minicraft");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Configura o Canvas
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(1200, 1000)); // Tamanho da tela
        canvas.setFocusable(false); // O Canvas não recebe foco de teclado
        add(canvas); // Adiciona o Canvas à janela

        // Centraliza a janela na tela
        pack();
        setLocationRelativeTo(null);

        // Adiciona o KeyListener para capturar eventos de teclado
        addKeyListener(this);

        // Torna a janela visível
        setVisible(true);

        // Inicializa o jogador, o mapa e a câmera
        player = new Player(600, 500);
        mapa = new Mapa();
        camera = new Camera(600, 500); // Inicializa a câmera
        cronometro = new Cronometro();
        creeper = new Creeper(300, 300);
        slimeteste = new Slime(300, 300);
        zumbiteste = new Zumbi(600, 300);

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
        player.update();
        camera.update(player, mapa.getLargura(), mapa.getAltura(), canvas.getWidth(), canvas.getHeight());

        // Atualiza o Creeper
        if (creeper.estaVivo()) { // 
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
    
        // Limpa a tela
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
        // Obtém a cor do céu baseada no tempo
        Color skyColor = cronometro.getSkyColor(gameTime); 
    
        // Renderiza o mapa com o efeito de iluminação
        mapa.render(g, camera.getX(), camera.getY(), skyColor);
    
        // Renderiza o jogador e o cronômetro
        player.render(g, camera.getX(), camera.getY());
        cronometro.render(g);

        // Renderiza o Creeper (se estiver vivo)
        if (creeper.estaVivo()) { 
            creeper.render(g, camera.getX(), camera.getY());
        }

        if (slimeteste.estaVivo()) {
            slimeteste.render(g, camera.getX(), camera.getY());
        }

        if (zumbiteste.estaVivo()) {
            zumbiteste.render(g, camera.getX(), camera.getY());
        }
    
        g.dispose();
        bs.show();
    }

    // Métodos do KeyListener
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        player.handleKeyPress(keyCode, true); // Tecla pressionada
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        player.handleKeyPress(keyCode, false); // Tecla liberada
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Não usado no momento
    }

    // Método principal
    public static void main(String[] args) {
        // Cria uma instância do jogo
        new Jogo();
    }
}