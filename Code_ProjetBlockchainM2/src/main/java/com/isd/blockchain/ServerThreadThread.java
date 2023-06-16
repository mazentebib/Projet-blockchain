package com.isd.blockchain;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import com.isd.blockchain.Utxo.TransactionOutput;

public class ServerThreadThread extends Thread {

	private ServerThread serverThread;
	private Socket socket;
	private PrintWriter printWriter;
	private String peerName;
	private int portnumber;
	
	public ServerThreadThread(Socket socket, ServerThread serverThread) {
	
		this.serverThread = serverThread;
		this.socket = socket;
	}
	
	public void run() {
	
		try {
			
			BufferedReader bufferedReader = new BufferedReader (new InputStreamReader(this.socket.getInputStream ()));
			this.printWriter = new PrintWriter(socket.getOutputStream(), true);
			while(true) {
				JsonReader jsonReader = Json.createReader(bufferedReader);
				JsonStructure jsonStructure = jsonReader.read();

				if (jsonStructure instanceof JsonObject) {//Le fichier JSON contient un objet JSON
				    JsonObject jsonObject =(JsonObject) jsonStructure;
				    //affichage dans la console du message Node
					if (jsonObject.containsKey("Noeud")) {
						System.out.println("["+jsonObject.getString("username")+"]:"+jsonObject.getString("message"));
						//transfert du message
						if(!jsonObject.toString().contains(Integer.toString(serverThread.getPortnumber()))) {//broadcast du message uniquement si il ne contient pas mon nom
							serverThread.sendMessage(jsonObject.toString(),socket) ;
						}
					}
					if (jsonObject.containsKey("transaction")) {//Le fichier contient une transaction envoyee par un wallet
						System.out.println("transaction entrante");
						
						//récuperation de la cle publique
						byte[] publicKeyBytes = Base64.getDecoder().decode(jsonObject.getString("publicKey"));
						X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
						KeyFactory keyFactory = KeyFactory.getInstance("RSA");
						PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
						//System.out.println("publicKey "+publicKey);
						
						//recuperation de la signature
						byte[] sig =Base64.getDecoder().decode(jsonObject.getString("sig"));
						
						//ajout de la transaction dans le pool des transactions
						Transaction t= new Transaction(jsonObject.getString("username"), 
													   jsonObject.getString("destinataire"),
													   Double.parseDouble(jsonObject.getString("montant")),
													   publicKey,
													   sig,
													   jsonObject.getString("senderAddress"),
													   jsonObject.getString("recipientAddress"),
													   Double.parseDouble(jsonObject.getString("timestamp"))
													   );
						this.serverThread.getPoolTransaction().add(t);//ajout de la transaction au pool de transaction
						//gossip du message si et selement si NoeuDest est vide
						if(jsonObject.getString("NoeudDest").equals("")) {
							//conversion du string en jsonValue
							StringWriter stringWriter = new StringWriter();
							Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
									.add("username", jsonObject.getString("username"))
									.add("destinataire", jsonObject.getString("destinataire"))
									.add("transaction","transaction")
									.add("montant",jsonObject.getString("montant"))
									.add("NoeudDest",Integer.toString(this.serverThread.getPortnumber()))
									.add("publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded()))
									.add("sig",Base64.getEncoder().encodeToString(sig) )
									.add("senderAddress", jsonObject.getString("senderAddress"))
									.add("recipientAddress", jsonObject.getString("recipientAddress"))
									.add("timestamp", jsonObject.getString("timestamp"))
									.build());
							//envoi
							serverThread.sendMessage(stringWriter.toString(),socket) ;
							
							//envoi du hash de la transaction et du bloc au wallet
							StringWriter stringWriter1 = new StringWriter();
							Json.createWriter(stringWriter1).writeObject(Json.createObjectBuilder()
									.add("TransactionHash", t.getSHA())
									.add("TransactionBlocId",Long.toString(this.serverThread.getBranche().get(0).getId()+1) )
									.build());
							PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
							printWriter.println(stringWriter1.toString());
						}
					}
					if(jsonObject.containsKey("blocs")) {//Une copie de la blockchain a ete envoyee
						// Récupérer le tableau JSON des blocs
						System.out.println("blockchain remplacee");
						JsonArray blocsJson = jsonObject.getJsonArray("blocs");

						// Créer une liste pour les blocs
						LinkedList<Bloc> Branche = new LinkedList<>();

						// Parcourir les blocs du tableau JSON
						for (JsonObject blocJson : blocsJson.getValuesAs(JsonObject.class)) {
						    // Créer un nouveau bloc à partir de l'objet JSON
						    Bloc bloc = new Bloc(blocJson);

						    // Ajouter le bloc à la liste
						    Branche.add(bloc);
						}
						//remplacer toute notre branche actuelle
						this.serverThread.setBranche(Branche);
						
					}
					if(jsonObject.containsKey("id")) {//Un bloc a ete envoye
						System.out.println("un bloc a ete envoye");
						Bloc bloc = new Bloc(jsonObject);
						
						if(bloc.getId() == this.serverThread.getBranche().get(0).getId()+1) {//k=n+1
							
							//ajout du bloc car il a une plus grande preuve de travail
							System.out.println("ajout du bloc car il a une plus grande preuve de travail");
							this.serverThread.getBranche().addFirst(bloc);
							System.out.println(this.serverThread.getBranche().get(0).toString());
							
							//effacer le pool det transaction
							this.serverThread.getPoolTransaction().clear();
							/*synchronized(this.serverThread.getPoolTransaction()) {
								
							}*/
						}
						if(bloc.getId() > this.serverThread.getBranche().get(0).getId()) {//k>n
							//demander la copie de la blockchain
							System.out.println("demande de copie envoyee");
							StringWriter stringWriter = new StringWriter();
							Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
							.add("CopieBlockchain", "CopieBlockchain")
							.build());
							PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
							printWriter.println(stringWriter.toString());
							
							//effacer le pool det transaction
							this.serverThread.getPoolTransaction().clear();
						}
						
					}
					if(jsonObject.containsKey("CopieBlockchain")) {//une demande de copie de la blockchain a ete envoyee
						// Créer un constructeur d'objet JSON pour la liste de blocs
						JsonObjectBuilder brancheBuilder = Json.createObjectBuilder();
						
						// Créer un constructeur de tableau JSON pour les blocs
						JsonArrayBuilder blocsBuilder = Json.createArrayBuilder();

						// Ajouter chaque bloc à l'objet JSON
						for (Bloc bloc : this.serverThread.getBranche()) {
						    // Convertir le bloc en objet JSON
						    JsonObject blocJson = bloc.toJson();

						    // Ajouter l'objet JSON du bloc au tableau
						    blocsBuilder.add(blocJson);
						}

						// Ajouter le tableau JSON des blocs à l'objet JSON de la branche
						brancheBuilder.add("blocs", blocsBuilder);

						// Créer l'objet JSON final pour la branche
						JsonObject brancheJson = brancheBuilder.build();
						
						//envoi de la copie
						StringWriter stringWriter = new StringWriter();
						Json.createWriter(stringWriter).writeObject(brancheJson);
						PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
						printWriter.println(stringWriter.toString());
					}
					if(jsonObject.containsKey("merkel")) {
						System.out.println("verif merkle");
						for (Bloc bloc : serverThread.getBranche()){
							if(bloc.getId()==Long.parseLong(jsonObject.getString("blocId"))) {//le bloc a ete trouve
								List<String>path=bloc.getMerkle().getPath(jsonObject.getString("transactionHash"));
								if(path==null) {//la transaction n existe pas
									System.out.println("la transaction n'existe pas");
									StringWriter stringWriter = new StringWriter();
									Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
							        		.add("message", "la transaction "+jsonObject.getString("transactionHash")+" n'existe pas")
							        		.build());
									PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
									printWriter.println(stringWriter.toString());
								}
								else {//la transaction existe envoi de la preuve
									JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
							        for (String hash : path) {
							            jsonArrayBuilder.add(hash);
							        }
							        JsonArray jsonArray = jsonArrayBuilder.build();
							        StringWriter stringWriter = new StringWriter();
							        Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
							        		.add("merkle", jsonArray)
							        		.add("merkleroot", bloc.getMerkle().getMerkleTree().get(0))
							        		.add("transactionHash", jsonObject.getString("transactionHash"))
							        		.build());
							        PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
									printWriter.println(stringWriter.toString());
							        
								}
							}
						}
					}
					if (jsonObject.containsKey("socket"))
						System.out.println(jsonObject.getString("socket"));
					
					if (jsonObject.containsKey("connexion")) {
						System.out.println("PORTNUMBER RECU " + jsonObject.getString("connexion"));
						this.portnumber = Integer.parseInt(jsonObject.getString("connexion"));
					}
					if (jsonObject.containsKey("ledger")) {//demande de ledger envoyee livre de compte
						String st = "---- Bloc ID#"+ this.serverThread.getBranche().get(0).getId() + " contient :\n";
				        int i = 0;
				        st += "OWNER             MONTANT            ADRESSE \n";
				        st += "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓\n";
				        for (TransactionOutput troutput :this.serverThread.getBranche().get(0).getUtxos()) {
				            i++;
				            st += troutput.toStringLedger() + "\n";
				        }
				        st += "┡━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┩\n";
				        st += "----- Fin Bloc ID#" + this.serverThread.getBranche().get(0).getId() + " -----\n";
				        StringWriter stringWriter = new StringWriter();
				        Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
				        		.add("ledger", st)
				        		.build());
				        PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
						printWriter.println(stringWriter.toString());
					}
					if (jsonObject.containsKey("listePeer")) {
						String liste="Liste des Peers:"+this.serverThread.getServerSocket().getLocalPort()+" ";
						for(ServerThreadThread s: this.serverThread.getServerThreadThreads()) {
							if(s.getPortnumber()!=0) {
								liste+=s.getPortnumber()+" ";
							}
						}
						StringWriter stringWriter = new StringWriter();
				        Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
				        		.add("listePeer", liste)
				        		.build());
				        PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
						printWriter.println(stringWriter.toString());
					}
				} 
				else if (jsonStructure instanceof JsonArray) {//Le fichier JSON contient un tableau JSON
					JsonArray jsonArray= (JsonArray)jsonStructure;
				    for (int i = 0; i < jsonArray.size(); i++) {
				        String socket = jsonArray.getJsonObject(i).getString("socket");
				        int port = jsonArray.getJsonObject(i).getInt("portnumber");
				        //System.out.println("Peer " + i + ": " + socket + " Port " + port);
				        System.out.println("Phase de maillage");
				        
				        //connexion aux autres peers
				        if(port!=0) {
				        	boolean exist=false;
					        for(ServerThreadThread serverThreadThread:this.serverThread.getServerThreadThreads()) {
					        	if(port==serverThreadThread.getPortnumber()) {
					        		exist=true;
					        	}
					        }
					        if(!exist) {
					        	Socket newSocket = null;
					        	try {
					        		System.out.println("envoi de connexion au port:"+port);
					        		newSocket = new Socket("localhost", port);
									//Ajout de la nouvelle connexion a la liste de serveur
									ServerThreadThread serverThreadThread = new ServerThreadThread(newSocket, this.serverThread);
									serverThreadThread.setPortnumber(port);
									serverThread.getServerThreadThreads().add(serverThreadThread);
									serverThreadThread.start();
									
									//envoi du port du Peer
									StringWriter stringWriter = new StringWriter();
									Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
									.add("connexion", String.valueOf( this.serverThread.getServerSocket().getLocalPort()))
									.build());
									PrintWriter printWriter = new PrintWriter(newSocket.getOutputStream(), true);
									printWriter.println(stringWriter.toString());
								} catch (Exception e) {
									if (newSocket != null) newSocket.close ();
									else System.out.println("Mauvais port trouvé dans la liste:"+port);
								}
					        	
					        }
				        }
				        
				    }
				} 
				else {
				    System.out.println("Le fichier JSON ne contient ni objet ni tableau JSON.");
				}
			}
			
		} catch (Exception e) { 
			System.out.println("Exception:cette connexion a ete supprimee"+this.portnumber);
			serverThread.getServerThreadThreads().remove(this); 
			e.printStackTrace();
			
		}
	
	}
	
	public PrintWriter getPrintWriter() { return printWriter; }
	
	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public int getPortnumber() {
		return portnumber;
	}

	public void setPortnumber(int portnumber) {
		this.portnumber = portnumber;
	}
	
}