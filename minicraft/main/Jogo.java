package minicraft.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

public class Jogo extends JFrame implements KeyListener {
    private Canvas canvas; //area de desenho do jogo
    private boolean running = false; // Controla se o jogo esta em execução

    public Jogo() {
        // Configurações da janela
        setTitle("Minicraft");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Configura o Canvas
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(1000, 800)); // Tamanho da tela
        canvas.setFocusable(false); // O Canvas nao recebe foco de teclado
        add(canvas); // Adiciona o Canvas à janela

        // Centraliza a janela na tela
        pack();
        setLocationRelativeTo(null);

        // Adiciona o KeyListener para capturar eventos de teclado
        addKeyListener(this);

        // Torna a janela visível
        setVisible(true);

        // Inicia o jogo
        startGame();
    }

    // Método para iniciar o jogo
    private void startGame() {
        running = true;
        gameLoop();
    }

    // Loop principal do jogo
    private void gameLoop() {
        while (running) {
            update(); // Atualiza a lógica do jogo
            render(); // Renderiza o jogo

            try {
                Thread.sleep(16); // ~60 FPS (1000ms / 60 = ~16ms por frame)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para atualizar a lógica do jogo
    private void update() {
        // Aqui você atualiza a posição dos objetos, verifica colisões, etc.
    }

    // Método para renderizar o jogo
    private void render() {
        // Obtém o BufferStrategy do Canvas
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3); // Cria um BufferStrategy com 3 buffers
            return;
        }

        // Obtém o contexto gráfico para desenhar
        Graphics g = bs.getDrawGraphics();

        // Limpa a tela
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Aqui você desenha os objetos do jogo
        g.setColor(Color.WHITE);
        g.drawString("Minicraft - Desenvolvendo o jogo...", 20, 20);

        // Libera o contexto gráfico e exibe o buffer
        g.dispose();
        bs.show();
    }

    // Métodos do KeyListener
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("Tecla pressionada: " + KeyEvent.getKeyText(keyCode));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("Tecla liberada: " + KeyEvent.getKeyText(keyCode));
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