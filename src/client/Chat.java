package client;

import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Chat extends Application {
	int WIDTH = 500, HEIGHT = 500;

	private String user = "Calvin: ";
	private String user2 = "Paul: ";

	private TextArea ta = new TextArea();
	
	private Button bt = new Button("Send");
	
	private boolean isServer = false;
	
	
	private NetworkConnection connection = isServer ? createServer() : createClient();
	
	@Override
	public void init() throws Exception {
		connection.startConnection();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		ta.setPrefHeight(450);
		
		BorderPane borderPane = new BorderPane();

		TextField tf = new TextField();

		HBox tp = new HBox();

		HBox bp = new HBox();
		
		bt.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	if (!tf.getText().isEmpty()) {
					String message;
					if (isServer) {
						message = user + tf.getText();
					}
					else {
						message = user2 + tf.getText();
					}
					ta.appendText(message + "\n");
					tf.clear();
					
					try {
						connection.send(message);
					}
					catch (Exception er) {
						ta.appendText("Cannot send message\n");
					}
				}
		    }
		});
		
		tf.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER  && !tf.getText().isEmpty()) {
				String message;
				if (isServer) {
					message = user + tf.getText();
				}
				else {
					message = user2 + tf.getText();
				}
				ta.appendText(message + "\n");
				tf.clear();
				
				try {
					connection.send(message);
				}
				catch (Exception er) {
					ta.appendText("Cannot send message\n");
				}
			}
		});
		tf.setPrefWidth(430);

		bp.getChildren().addAll(tf, bt);
		tp.getChildren().addAll(ta);

		borderPane.setTop(tp);
		borderPane.setBottom(bp);  

		borderPane.setTop(tp);
		borderPane.setBottom(bp);  
		Scene scene = new Scene(borderPane, WIDTH, HEIGHT);
		
		if (isServer) {
			primaryStage.setTitle("Calvin's Chat Client"); // Set the stage title
		}
		else {
			primaryStage.setTitle("Paul's Chat Client");
		}
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
	}
	
	@Override
	public 	void stop() throws Exception {
		connection.closeConnection();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	private Server createServer() {
		return new Server(53000, data -> {
			// Used to run background thread. JavaFX runs on UI thread
			Platform.runLater(() -> {
				ta.appendText(data.toString() + "\n");
			});
		});
	}
	
	private Client createClient() {
		return new Client("127.0.0.1", 53000, data -> {
			// Used to run background thread. JavaFX runs on UI thread
			Platform.runLater(() -> {
				ta.appendText(data.toString() + "\n");
			});
		});
	}

}
