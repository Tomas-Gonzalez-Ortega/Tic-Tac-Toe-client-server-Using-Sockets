import java.awt.Dimension;
import java.io.IOException;
import javax.swing.JFrame;

public class TTT {
	public static void main(String[] args) throws IOException {
		switch (args.length) {
			case 0:
				guigui();
				break;
			case 1:
				server(Integer.parseInt(args[0]));
				break;
			case 2:
				client(Integer.parseInt(args[0]), args[1]);
				break;
			default:
				System.err.println(" Usage: java TTT [port [hostname]]\n" +
				                   " local: java TTT <no args>\n" +
			                       "server: java TTT port#\n" +
						           "client: java TTT port# hostname\n");
				System.exit(1);
		}
	}

	private static void guigui() {
		HostPresenter p = new HostPresenter();
		for(Player player : Player.values()) {
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setContentPane(new TTTComponent(p, player));
			f.setMinimumSize(new Dimension(400,400));
			f.setVisible(true);
		}
	}

	private static void server(int port) throws IOException {
		HostPresenter p = new HostPresenter();
		HostSocketAdapter sa = new HostSocketAdapter(p, Player.X, port);
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new TTTComponent(p, Player.O));
		f.setMinimumSize(new Dimension(400,400));
		f.setVisible(true);
		sa.run();
	}

	private static void client(int port, String host) throws IOException {
		ClientPresenter p = new ClientPresenter();
		ClientSocketAdapter sa = new ClientSocketAdapter(p, Player.X, port, host);
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new TTTComponent(p, Player.X));
		f.setMinimumSize(new Dimension(400,400));
		f.setVisible(true);
		sa.run();
	}
}
