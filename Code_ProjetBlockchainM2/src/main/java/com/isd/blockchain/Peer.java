package com.isd.blockchain;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;


public class Peer {
	//private String username;
	private int portnumber;
	private ServerThread serverThread;

	public Peer(/*String username,*/ int portnumber) {
		super();
		//this.username = username;
		this.portnumber = portnumber;
	}

	public static void main(String[] args) throws Exception {
		BufferedReader bufferedReader = new BufferedReader (new InputStreamReader(System.in));
		System.out.println("> Entrer un port pour ce peer:");
		String[] setupValues = bufferedReader.readLine().split(" ");
		//Peer peer=new Peer(setupValues[0],Integer.parseInt(setupValues[1]));
		Peer peer=new Peer(Integer.parseInt(setupValues[0]));
		peer.serverThread=new ServerThread(setupValues[0]);
		peer.serverThread.start();
		MiningThread miningThread = new MiningThread(peer.serverThread, 3);
		miningThread.start();
		peer.updateListenToPeers (bufferedReader, setupValues[0], peer.serverThread);
	
	}

	public void updateListenToPeers (BufferedReader bufferedReader, String username, ServerThread serverThread) throws Exception{
		
		System.out.println("> Entrer hostname:port#");
		System.out.println("> du Noeud auquel on veut se connecter(p pour passer):");
		String input = bufferedReader. readLine();
		String[] inputValues = input.split(" ");
		
		if (!input.equals("p")) for (int i = 0; i < inputValues.length; i++) {
			String[] address = inputValues[i].split(":");
			Socket socket = null;
			try {
			socket = new Socket(address[0], Integer.valueOf(address [1]));
			//Ajout de la nouvelle connexion a la liste de serveur
			ServerThreadThread serverThreadThread = new ServerThreadThread(socket, serverThread);
			serverThreadThread.setPortnumber(Integer.valueOf(address [1]));
			serverThread.getServerThreadThreads().add(serverThreadThread);
			serverThreadThread.start();
			
			//envoi du port du Peer
			StringWriter stringWriter = new StringWriter();
			Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
			.add("connexion", String.valueOf( this.portnumber))
			.build());
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
			printWriter.println(stringWriter.toString());
			
			} catch(Exception e) {
				if (socket != null) socket.close ();
				else System.out.println("Arguments incorrects passons au mode communication");
			}
		}
		communicate(bufferedReader, username, serverThread);
	}
	
	public void communicate(BufferedReader bufferedReader,String username,ServerThread serverThread) {
		
		try {
			System.out.println(">liste des Actions disponibles");
			System.out.println(">1 pour la liste des peers");
			System.out.println(">2 pour se connecter à un nouveau peer");
			System.out.println(">3 pour sortir du programme");
			boolean flag = true;
			while(flag) {
				String message = bufferedReader. readLine();
				if (message.equals("3")) {
					flag = false;
					break;
				} else if (message.equals("2")) {
					updateListenToPeers (bufferedReader, username, serverThread);
	
				}else if (message.startsWith("1")) {
					System.out.println("port:"+this.portnumber);
					for(ServerThreadThread s: this.serverThread.getServerThreadThreads()) {
						System.out.println("port"+s.getPortnumber());
					}
	
				}else {
					communicate(bufferedReader, username, serverThread);
					/*
					StringWriter stringWriter = new StringWriter();
					Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
					.add("Noeud", username)
					.add("message", message)
					.build());
					serverThread.sendMessage(stringWriter.toString(),null);*/
				}
			}
				System.exit (0);

			} catch (Exception e) {
				System.out.println("arguments incorrects");
				communicate(bufferedReader, username, serverThread);
			}
	}
}