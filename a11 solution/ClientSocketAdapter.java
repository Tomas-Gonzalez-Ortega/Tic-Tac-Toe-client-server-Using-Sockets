import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.net.*;

public class ClientSocketAdapter implements Runnable {

	private final static Logger LOG = Logger.getLogger(ClientSocketAdapter.class.getName());

	private final ClientPresenter game;
	private final Player me;
	private final String host;
	private final int port;

	public ClientSocketAdapter(ClientPresenter game, Player me, int port, String host)
			throws IOException {
		this.game = game;
		this.me = me;
		this.host = host;
		this.port = port;
		LOG.info("I am " + me);
	}

	public void run() {
		LOG.info("Connecting to " + host + port);
		try (Socket s = new Socket(host, port);
			ObjectInputStream in = new ObjectInputStream(
				new BufferedInputStream(s.getInputStream()));
			ObjectOutputStream out = new ObjectOutputStream(
				(s.getOutputStream()));
		) {
			LOG.info("Connected to: " + s.getRemoteSocketAddress());
			new ClientStreamAdapterOut(out);
			new ClientStreamAdapterIn(in).call();
		} catch (IOException | ClassNotFoundException e) {
			LOG.severe("Exception handling socket: " + e.getMessage());
		} finally {
			game.playerQuit();
		};
	}

	private class ClientStreamAdapterIn implements Callable<Void> {
		private final ObjectInputStream in;

		public ClientStreamAdapterIn(ObjectInputStream in) {
			this.in = in;
		}

		public Void call() throws IOException, ClassNotFoundException {
			try {
				handleInputs();
			} finally {
				in.close();
			}
			return null;
		}

		private void handleInputs() throws IOException, ClassNotFoundException {
			for(;;) {
				TTTModel model = (TTTModel) in.readObject();
				game.update(model);
			}
		}
	}

	private class ClientStreamAdapterOut implements PropertyChangeListener {
		private final ObjectOutputStream out;
		private final ExecutorService exec;

		public ClientStreamAdapterOut(ObjectOutputStream out) {
			this.out = out;
			this.exec = Executors.newSingleThreadExecutor();
			game.addMoveListener(this);
		}

		public void propertyChange(PropertyChangeEvent pce) {
			int pos = (int) pce.getNewValue();
			exec.execute(() -> { sendModel(pos); });
		}
		private void sendModel(int pos) {
			try {
				out.writeObject(me);
				out.writeInt(pos);
				out.flush();
			} catch(IOException e) {
				LOG.warning(e.toString());
				exec.shutdown();
				try {
					out.close();
				} catch (IOException e2) {
					LOG.warning(e2.toString());
				}
			}
		}

	}
}
