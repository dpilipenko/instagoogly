package instagoogly;

public class CameraOutMessage {
	String base64Image;
	public CameraOutMessage(String base64Image) {
		this.base64Image = base64Image;
	}
	public String getBase64Image() {
		return this.base64Image;
	}
}
