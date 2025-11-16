package tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageInfo {
    public static void main(String[] args) throws Exception {
        String[] files = new String[]{
            "UI\\Home.png",
            "UI\\Notes.png",
            "UI\\Notes (1).png",
            "UI\\Schedule Planner.png",
            "UI\\Schedule Planner (1).png",
            "UI\\Notifications.png",
            "UI\\Notifications (1).png",
            "UI\\Notifications (2).png",
            "assets\\back-to-school-doodle-pattern-vector-45436119 1.png",
            "assets\\SS logo 1.png"
        };
        for (String f : files) {
            File file = new File(f);
            if (!file.exists()) {
                System.out.println(f + " -> NOT FOUND");
                continue;
            }
            BufferedImage img = ImageIO.read(file);
            System.out.println(f + " -> " + img.getWidth() + "x" + img.getHeight());
        }
    }
}

