package com.isd.blockchain;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net. ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import com.isd.blockchain.Utxo.TransactionOutput;



public class ServerThread extends Thread {

	private ServerSocket serverSocket;
	private Set<ServerThreadThread> serverThreadThreads = new HashSet<ServerThreadThread> ();
	//private String username;
	private int portnumber;
	
	private Bloc blocGenesis;
	private LinkedList<Bloc> Branche = new LinkedList<Bloc>();
	private ArrayList<Transaction>poolTransaction=new ArrayList<>();
	
	public ServerThread(String portNumb/*,String username*/) throws IOException {
		this.serverSocket = new ServerSocket (Integer. valueOf(portNumb)) ;
		this.portnumber=Integer. valueOf(portNumb);
		//this.username=username;
		
		
        //Bloc(long id, long nonce, String hash, List<Transaction> transacations, List<TransactionOutput> utxos)
		this.blocGenesis = new Bloc(0, 0, "0000");
		this.blocGenesis.getUtxos().add(new TransactionOutput("komara", "",1000, "0000001"));
		this.blocGenesis.getUtxos().add(new TransactionOutput("samir","",1000, "0000001"));
		this.blocGenesis.getUtxos().add(new TransactionOutput("sylvain","", 1000, "0000001"));
		// Définir les comptes initiaux
		this.Branche.add(this.blocGenesis);
		
	}
	

	public void run() {
	
		try {
		
			while(true) {
				//acceptation de la nouvelle connexion
				Socket peerSocket=serverSocket.accept();
				ServerThreadThread serverThreadThread = new ServerThreadThread(peerSocket, this);
				System.out.println("nouvelle connexion acceptee");
				serverThreadThreads.add(serverThreadThread);
				
				serverThreadThread.start();
				
				//conversion du hashset en json
				JsonArrayBuilder serverThreadArrayBuilder = Json.createArrayBuilder();
				for (ServerThreadThread s : serverThreadThreads) {
				    JsonObjectBuilder serverThreadBuilder = Json.createObjectBuilder()
				    		.add("portnumber", s.getPortnumber())
				            .add("socket", s.getSocket().toString());
				    serverThreadArrayBuilder.add(serverThreadBuilder.build());
				}
				
				JsonArray serverThreadArray = serverThreadArrayBuilder.build();
				//ecriture dans le stringwriter
				StringWriter stringWriter = new StringWriter();
				JsonWriter jsonWriter = Json.createWriter(stringWriter);
			    jsonWriter.writeArray(serverThreadArray);
			    jsonWriter.close();
			    
			    String jsonString = stringWriter.toString();
			    //System.out.println(jsonString); // Afficher le contenu du tableau JSON sous forme de chaîne de caractères
				//envoi de la liste des peers sur le réseau
			    PrintWriter printWriter = new PrintWriter(peerSocket.getOutputStream(), true);
				printWriter.println(jsonString);
			}
		
		} catch (Exception e) { e.printStackTrace(); }
	
	}
	
	public void sendMessage (String message,Socket socket) {
	
		try { 
			for (ServerThreadThread serverThreadThread : serverThreadThreads) {
				if((socket==null) || (!serverThreadThread.getSocket().equals(socket))){
					serverThreadThread.getPrintWriter().println(message);
					//System.out.println("envoi "+message);
				}
			}
		
		} catch(Exception e) { e.printStackTrace(); }
	
	}
	
	
	
	public Set<ServerThreadThread> getServerThreadThreads() { return serverThreadThreads; }
	
	public void setServerThreadThreads(Set<ServerThreadThread> serverThreadThreads) {
		this.serverThreadThreads = serverThreadThreads;
	}

	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}


	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}


	public LinkedList<Bloc> getBranche() {
		return Branche;
	}


	public void setBranche(LinkedList<Bloc> branche) {
		Branche = branche;
	}


	public ArrayList<Transaction> getPoolTransaction() {
		return poolTransaction;
	}


	public void setPoolTransaction(ArrayList<Transaction> poolTransaction) {
		this.poolTransaction = poolTransaction;
	}


	public int getPortnumber() {
		return portnumber;
	}


	public void setPortnumber(int portnumber) {
		this.portnumber = portnumber;
	}
	
	
}
