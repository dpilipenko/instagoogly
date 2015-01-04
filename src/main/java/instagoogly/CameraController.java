package instagoogly;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
		String prefix = getPrefix(message);

		byte[] imageData = decodeImage(prefix, message.getBase64Image());
		byte[] newImageData = processImage(imageData);
		String newImage = encodeImage(prefix, newImageData);
		
		return new CameraOutMessage(newImage);
	}
	
	private String getPrefix(CameraInMessage message) {
		String encodingPrefix = "base64,";
		int contentStartIndex = message.getBase64Image().indexOf(encodingPrefix) + encodingPrefix.length();
		String prefix = message.getBase64Image().substring(0, contentStartIndex);
		return prefix;
	}
	
	private byte[] decodeImage(String prefix, String base64Image) {
		return Base64.decodeBase64(base64Image.substring(base64Image.indexOf(prefix) + prefix.length()));
	}
	
	private byte[] processImage(byte[] imageData) throws IOException {
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		return baos.toByteArray();
	}
	
	private String encodeImage(String prefix, byte[] imageData) {
		return prefix + Base64.encodeBase64String(imageData);
	}
}
