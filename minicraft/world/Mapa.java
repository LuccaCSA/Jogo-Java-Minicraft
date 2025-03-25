package minicraft.world;

import java.io.File;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Color;


public class Mapa {
    private BufferedImage mapaImage;

    public Mapa() {
        try {
            mapaImage = ImageIO.read(new File("minicraft/graphics/sprites/mapatotal.png"));
            if (mapaImage == null) {
                System.out.println("Erro: a imagem do mapa não foi carregada!");
            } else {
                System.out.println("Mapa carregado! Dimensões: " + mapaImage.getWidth() + "x" + mapaImage.getHeight());
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar a imagem do mapa: ");
            e.printStackTrace();
        }
    }

    public int getLargura() {
        return (mapaImage != null) ? mapaImage.getWidth() : 0;
    }

    public int getAltura() {
        return (mapaImage != null) ? mapaImage.getHeight() : 0;
    }

    public void render(Graphics g, int cameraX, int cameraY, Color skyColor) {
        if (mapaImage != null) {
            g.drawImage(mapaImage, -cameraX, -cameraY, null);
            applyDayNightEffect(g, skyColor); // Aplica o efeito de iluminação
        }
    }

    private void applyDayNightEffect(Graphics g, Color skyColor) {
        g.setColor(skyColor); 
        g.fillRect(0, 0, mapaImage.getWidth(), mapaImage.getHeight());
    }
}
