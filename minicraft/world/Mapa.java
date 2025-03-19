package minicraft.world;

import java.io.File;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Mapa {
    private BufferedImage mapaImage;

    public Mapa() {
        try {
            mapaImage = ImageIO.read(new File("minicraft/graphics/sprites/mapa.png"));
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

    public void render(Graphics g, int cameraX, int cameraY) {
        if (mapaImage != null) {
            g.drawImage(mapaImage, -cameraX, -cameraY, null);
        } else {
            System.out.println("Erro: mapaImage é null na renderização!");
        }
    }
}
