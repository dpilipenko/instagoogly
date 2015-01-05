package instagoogly;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.net.util.Base64;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class CameraController {

	private static final String prefix = "data:image/jpeg;base64,";
	private static final String classifier = "/haarcascade_eye.xml";
	
	@MessageMapping("/cameraIn")
	@SendTo("/topic/cameraOut")
	public CameraOutMessage processCamera(CameraInMessage message) throws Exception {
		Mat mat = decodeFromString(message.getBase64Image());
		mat = findAndDrawEyes(mat);
		String newImage = encodeToString(mat);
		return new CameraOutMessage(newImage);
	}
	
	private Mat decodeFromString(String encodedString) throws IOException {
		// Base64 string to BufferedImage
		byte[] rawImageData = Base64.decodeBase64(encodedString.substring(prefix.length()));
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(rawImageData));
		
		// BufferedImage to OpenCV Mat
		byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, imageData);
		
		// TODO Figure out why imageData works but rawImageData does not...(?)
		return mat;
	}
	
	private Mat findAndDrawEyes(Mat mat) {
		CascadeClassifier eyeDetector = new CascadeClassifier(getClass().getResource(classifier).getPath());
		MatOfRect detections = new MatOfRect();
	    eyeDetector.detectMultiScale(mat, detections);
	    for (Rect rect : detections.toArray()) {
	    	Core.rectangle(mat, rect.tl(), rect.br(), new Scalar(0, 255, 255));
	    }
	    return mat;
	}
	
	private String encodeToString(Mat mat) throws IOException {
		// Correct colors from BGR to RGB
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
		
		// OpenCV Mat to BufferedImage
		byte[] rawImageData = new byte[mat.rows()*mat.cols()*(int)(mat.elemSize())];
		mat.get(0, 0, rawImageData);
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
		image.getRaster().setDataElements(0,0, mat.cols(), mat.rows(), rawImageData);
		
		// BufferedImage to byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		byte[] imageData = baos.toByteArray();
		
		// TODO Figure out why imageData works but rawImageData does not...(?)
		return prefix + Base64.encodeBase64String(imageData);
	}
}
