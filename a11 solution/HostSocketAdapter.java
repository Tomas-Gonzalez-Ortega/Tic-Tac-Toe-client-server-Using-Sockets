import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.net.*;

public class HostSocketAdapter implements Runnable {

	private final static Logger LOG = Logger.getLogger(HostSocketAdapter.class.getName());

	private final ServerSocket listen;
	private final TTTPresenter game;
	private final Player me;

	public HostSocketAdapter(TTTPresenter game, Player me, int port) throws IOException {
		this(game, me, new ServerSocket(port));
	}

	private HostSocketAdapter(TTTPresenter game, Player me, ServerSocket ss) {
		this.listen = ss;
		this.game = game;
		this.me = me;
		LOG.info("I am " + me);
	}

	public void run() {
		LOG.info("Awaiting connection on: " + listen.getLocalSocketAddress());

		try (Socket s = listen.accept();
			ObjectOutputStream out = new ObjectOutputStream(
				(s.getOutputStream()));
			ObjectInputStream in = new ObjectInputStream(
				new BufferedInputStream(s.getInputStream()));
		) {
			LOG.info("Received connection from: " + s.getRemoteSocketAddress());

			new HostStreamAdapterOut(out); // asynch
			new HostStreamAdapterIn(in).call(); // blocking
		} catch (IOException | ClassNotFoundException e) {
			LOG.severe("Exception handling socket: " + e.getMessage());
		} finally {
			game.playerQuit();
		}
	}

	private class HostStreamAdapterIn implements Callable<Void> {
		private final ObjectInputStream in;

		public HostStreamAdapterIn(ObjectInputStream in) {
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
				Player mover = (Player) in.readObject();
				int pos = in.readInt();
				if(mover == me) {
					game.move(mover, pos);
				} else {
					LOG.warning("Received move for wrong player!");
				}
			}
		}
	}

	private class HostStreamAdapterOut implements PropertyChangeListener {
		private final ObjectOutputStream out;
		private final ExecutorService exec;

		public HostStreamAdapterOut(ObjectOutputStream out) {
			this.out = out;
			this.exec = Executors.newSingleThreadExecutor();
			game.addModelListener(this);
		}

		public void propertyChange(PropertyChangeEvent pce) {
			TTTModel model = (TTTModel) pce.getNewValue();
			exec.execute(() -> { sendModel(model); });
		}
		private void sendModel(TTTModel model) {
			LOG.info("Sending model update.");
			try {
				out.writeObject(model);
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
