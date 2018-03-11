package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import model.Automobile;

public class DefaultSocketClient extends Thread implements SocketClientInterface, SocketClientConstants {
	private BufferedReader strIn;
	private PrintWriter strOut;
	private ObjectOutputStream objOut;
	private ObjectInputStream objIn;
	private Socket sock;
	private String strHost;
	private int iPort;
	private boolean handshaked = false;

	public DefaultSocketClient(String strHost, int iPort) {
		setPort(iPort);
		setHost(strHost);
	}

	public Socket getSocket() {
		return sock;
	}

	public void run() {
		if (openConnection()) {
			handleSession();
			closeSession();
		}
	}

	public boolean openConnection() {
		try {
			if (sock == null)
				sock = new Socket(strHost, iPort);

		} catch (IOException e) {
			if (DEBUG)
				System.err.println("Unable to connect to " + strHost);
			return false;
		}

		try {
			strOut = new PrintWriter(sock.getOutputStream(), true);
			strIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			objOut = new ObjectOutputStream(sock.getOutputStream());
			objIn = new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
			if (DEBUG)
				System.err.println("Unable to obtain stream to/from " + strHost);
			return false;
		}
		return true;
	}

	public void handleSession() {
		boolean waiting_for_input = false;
		int menu_option = -1;
		String strInput = "";
		ClientHelper ch = new ClientHelper();
		if (DEBUG)
			System.out.println("Handling session with " + strHost + ":" + iPort);
		try {
			// strOut.println("Hi!");
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String fromServer;
			String fromUser;
			while ((fromServer = strIn.readLine()) != null) {
				System.out.println("Server: " + fromServer);
//				if (fromServer.equals("What can we do for you?")) {
//					waiting_for_input = true;
//					menu_option = -1;
//				} else if (fromServer.equals("Auto sucessfully added!")) {
//					// ch.displayMainMenu();
//					waiting_for_input = true;
//					menu_option = -1;
//				} else if (fromServer.equals("Error: Auto cannot be added!")) {
//					// ch.displayMainMenu();
//					waiting_for_input = true;
//					menu_option = -1;
//				} else 
				if (fromServer.equals("Bye!")) break;
				else {
					waiting_for_input = true;
					menu_option = -1;
				}
				// ch.processResponse(fromServer);
				while (waiting_for_input) {
					if (menu_option == -1) {
						ch.displayMainMenu();
						fromUser = stdIn.readLine();
						if (fromUser.equals("1")) {
							sendOutput(fromUser);
							menu_option = 1;
						} else if (fromUser.equals("2")) {
							sendOutput(fromUser);
							menu_option = 2;
						} else if (fromUser.equals("0")) {
							sendOutput(fromUser);
							waiting_for_input = false;
//							closeSession();
						}	
					}
					else if (menu_option == 1) {
						System.out.println("Please enter file name (with .prop) to upload or 0 to go back to main menu.");
						fromUser = stdIn.readLine();
						if (fromUser.equals("0"))
							menu_option = -1;
						else {
							CarModelOptionsIO io = new CarModelOptionsIO();
							io.sendAutoFromPropFile("src/AutoDataFiles/" + fromUser, objOut);
							menu_option = -1;
							waiting_for_input = false;
						}
					}
					else if (menu_option == 2) {
//						System.out.println("Please enter Auto ID to configure or 0 to go back to main menu.");
//						fromUser = stdIn.readLine();
//						if (fromUser.equals("0"))
//							menu_option = -1;
//						else 
						{
							SelectCarOption sc = new SelectCarOption();
							try {
								
								String[] aList = (String[]) objIn.readObject();
								sc.displayAutoList(aList);
								fromUser = stdIn.readLine();
								int configure_option = Integer.parseInt(fromUser);
								while (configure_option == 0 || configure_option > aList.length)
								{
									System.out.println("Please select an Auto!");
								}
								sendOutput(aList[configure_option-1]);
								sc.displayAutoInfo((Automobile) objIn.readObject());
							} catch (ClassNotFoundException e) {
								if (DEBUG)
									System.out.println("Error: Cannot open Auto List!");
							}
							catch (IOException e) {
								if (DEBUG)
									System.out.println("Error: Cannot receive Auto List!");
							}
							menu_option = -1;
							waiting_for_input = false;
						}
					}
				}

				// fromUser = stdIn.readLine();
				// if (fromUser != null) {
				// if (fromUser.equals("0")) closeSession();
				// System.out.println("Client: " + fromUser);
				//
				//// strOut.println(ch.processRequest(fromUser));
				// }
			}
		} catch (IOException e) {
			if (DEBUG)
				System.out.println("Error: Handling session with " + strHost + ":" + iPort);
		}
	}

	public void sendOutput(String strOutput) {
		strOut.println(strOutput);
	}

	public void handleInput(String strInput) {
		// System.out.println(strInput);
	}

	public void closeSession() {
		try {
			sock.close();
			strIn.close();
			strOut.close();
			objIn.close();
			objOut.close();
		} catch (IOException e) {
			if (DEBUG)
				System.out.println("Error: Closing socket to " + strHost);
		}
	}

	public void setSocket(Socket s) {
		sock = s;
	}

	public void setHost(String strHost) {
		this.strHost = strHost;
	}

	public void setPort(int iPort) {
		this.iPort = iPort;
	}

}
