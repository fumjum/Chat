package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class NetworkConnection {
	// Function to receive message from other-end
	private Consumer<Serializable> onReceiveCallback;

	private ConnectionThread cThread = new ConnectionThread();


	// Serialized object to be sent over network
	public NetworkConnection(Consumer<Serializable> onReceiveCallback) {
		this.onReceiveCallback = onReceiveCallback;

		// Set thread to Daemon Thread
		cThread.setDaemon(true);
	}

	// Start the network connection
	public void startConnection() throws Exception {
		cThread.start();
	}

	// Send data to other-end
	public void send(Serializable data) throws Exception {
		cThread.out.writeObject(data);
	}

	// Close network connection
	public void closeConnection() throws Exception {
		cThread.socket.close();
	}

	// Used to differentiate client from server and vis-versa
	protected abstract boolean isServer();

	// Get ip from server for client
	protected abstract String getIp();

	// Get port from server for client
	protected abstract int getPort();

	// Background thread used for asynchorus read and write
	private class ConnectionThread extends Thread {

		// Need access to these objects to close them
		private Socket socket;
		private ObjectOutputStream out;

		@Override
		public void run() {
			try (ServerSocket server = isServer() ? new ServerSocket(getPort()) : null;
				 Socket socket = isServer() ? server.accept() : new Socket(getIp(), getPort());
				 // Used for sending objects to other-end
				 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				 // Used for receiving objects from other-end
				 ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
				
				this.socket = socket;
				this.out = out;
				// Makes connection faster
				socket.setTcpNoDelay(true);

				while (true) {
					// Data from other-end
					Serializable data = (Serializable) in.readObject();

					// Pass in data to function
					onReceiveCallback.accept(data);
				}
			}
			catch(Exception e) {
				System.out.println("Exception occurred");
				onReceiveCallback.accept("Connection aborted.");
			}
		}
	}
}
