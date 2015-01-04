package instagoogly;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.net.util.Base64;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class CameraController {

	@MessageMapping("/cameraIn")
	@SendTo("/topic/cameraOut")
	public CameraOutMessage processCamera(CameraInMessage message) throws Exception {
		String base64Image = message.getBase64Image();
		
		// 1. Get encoding prefix
		String encodingPrefix = "base64,";
		int contentStartIndex = base64Image.indexOf(encodingPrefix) + encodingPrefix.length();
		String prefix = base64Image.substring(0, contentStartIndex); 
		
		// 2. Decode image
		byte[] imageData = Base64.decodeBase64(base64Image.substring(base64Image.indexOf(prefix) + prefix.length()));
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
		
		// 3. Process image
		BufferedImage invertedImage = invertColor(image);
		
		// 4. Encode image
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(invertedImage, "jpg", baos);
		byte[] imageInByte = baos.toByteArray();
		String encoding = prefix + Base64.encodeBase64String(imageInByte);
		
		return new CameraOutMessage(encoding);
	}

	private BufferedImage invertColor(BufferedImage image) {
		for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgba = image.getRGB(x, y);
                Color col = new Color(rgba, true);
                col = new Color(255 - col.getRed(),
                                255 - col.getGreen(),
                                255 - col.getBlue());
                image.setRGB(x, y, col.getRGB());
            }
        }
		return image;
	}
	
}
