import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class StreamHandler extends Thread {
	ObjectInputStream ois;
	ObjectOutputStream oos;
	int id;
	Socket socket;

	public ObjectInputStream getIStream(Socket peer) {
		// get streams on separate threads
		try {
			ois = new ObjectInputStream(peer.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ois;
	}

	public ObjectOutputStream getOStream(Socket peer) {
		// get streams on separate threads
		try {
			oos = new ObjectOutputStream(peer.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return oos;
	}
}