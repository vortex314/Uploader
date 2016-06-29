package be.limero.programmer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;

import be.limero.util.Bytes;
import be.limero.util.Cbor;
import be.limero.util.Slip;

public class Programmer {
	private static final Logger log = Logger.getLogger(Programmer.class
			.getName());

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		//		LogFormatter.Init();
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
		log.info("Programmer started ");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Programmer window = new Programmer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	byte[] send() {
		Cbor cbor = new Cbor(1000);
		cbor.add(new Bytes(Stm32Protocol.Get()));
		log.info(" cbor = "+cbor.toHex());
		Bytes slip=Slip.encode(cbor);
		log.info(" slip = "+slip.toHex());
		return slip.bytes();
	}

	void sendMsg() {
		Socket socket = null;

		try {
			socket = new Socket("192.168.0.229", 23);
			log.info(" socket : " + socket);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			os.write(send());
			os.flush();
//			Thread.sleep(2000);

			os.close();
			is.close();

			socket.close();
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception occured", e);
			System.exit(1);
		}

	}

	/**
	 * Create the application.
	 */
	public Programmer() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendMsg();
			}
		});
		btnReset.setBounds(12, 12, 117, 25);
		frame.getContentPane().add(btnReset);

		JButton btnFlash = new JButton("Flash");
		btnFlash.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnFlash.setBounds(141, 12, 117, 25);
		frame.getContentPane().add(btnFlash);
	}
}
