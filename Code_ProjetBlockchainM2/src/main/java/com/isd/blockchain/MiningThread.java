package com.isd.blockchain;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.json.Json;
import javax.json.JsonObject;

import com.isd.blockchain.Utxo.TransactionOutput;
import com.isd.blockchain.test.Merkle;



public class MiningThread extends Thread{

	private ServerThread serverThread;
	private int threshold;
	private int difficulty=5;
	private int previousHash;
	private long previousId;
	private long nonce;
	private String data;

	public MiningThread(ServerThread serverThread, int threshold) {
		super();
		this.serverThread = serverThread;
		this.threshold=threshold;
	}
	
	public void run() {
        while (true) {
            if (this.serverThread.getPoolTransaction().size() >= this.threshold) {
                synchronized(this.serverThread.getPoolTransaction()) {
                	String previousHash=this.serverThread.getBranche().get(0).getHash();
                	this.previousId=this.serverThread.getBranche().get(0).getId();
                	
                	//Verification de la liste des transactions et creation des nouveaux utxos
                	Bloc bloc= new Bloc();bloc.setUtxos(new ArrayList<TransactionOutput>(this.serverThread.getBranche().get(0).getUtxos()));
                	List<String> transactionHashList=new ArrayList<>();
                	for (Transaction transaction : this.serverThread.getPoolTransaction()) {
                		transaction.FindInputs(bloc.getUtxos());//rechercher les inputs de la transaction et remplir la liste
                		if(transaction.processTransaction(bloc.getUtxos())) {
                			bloc.getTransacations().add(transaction);
                			transactionHashList.add(transaction.getSHA());
                		}
                	}
                	//ne pas miner quand aucune transaction n'est valide
                	if(transactionHashList.size()==0) {
                		System.out.println("aucune transaction n a ete validee");
                		this.serverThread.getPoolTransaction().clear();
                		continue;
                	}
                	//création de l arbre de merkle du bloc
                	bloc.setMerkle(new Merkle(transactionHashList));
                    // Parcourir la liste transactionList et la convertir en une chaîne de caractères
                    StringBuilder dataBuilder = new StringBuilder();
                    for (Transaction transaction : bloc.getTransacations()) {
                        String source = transaction.getSource();
                        String destination = transaction.getDestination();
                        double montant = transaction.getMontant();
                        dataBuilder.append(source).append(":").append(destination).append(":").append(montant).append(":").append(transaction.getTimestamp()).append(",");
                    }
                    // Retirer la virgule finale de la chaîne de données
                    if (dataBuilder.length() > 0) {
                        dataBuilder.setLength(dataBuilder.length() - 1);
                    }
                    this.data = dataBuilder.toString();
                    
                    // Code de minage
                    String target = new String(new char[this.difficulty]).replace('\0', '0'); // la cible est une chaîne de zéros de longueur égale à la difficulté
                    String hash =calculateHash();
                    System.out.println("mining en cours");
                    while (!hash.substring(0, difficulty).equals(target)) { // tant que le hash ne satisfait pas la cible	
                        this.nonce = new Random().nextLong(); // générer une valeur aléatoire pour le nonce
                        hash = calculateHash(); // calculer le hash en utilisant le nonce actuel
                        
                       if(this.serverThread.getBranche().get(0).getId() != this.previousId) {//arreter le mining car la branche a change
                        	System.out.println("minining en cours arrête");
                        	break;
                        }
                    }
                    //Bloc bloc=new Bloc(this.previousId+1,this.nonce, hash, null, new ArrayList<>(this.serverThread.getPoolTransaction()));
                    bloc.setId(this.previousId+1);
                    bloc.setNonce(this.nonce);
                    bloc.setHash(hash);
                    if(this.serverThread.getBranche().get(0).getId() == this.previousId) {//ajouter le bloc uniquement si la branche n'a pas deja changee
                    	System.out.println("la branche n a pas change "+this.previousId);
                    	this.serverThread.getBranche().addFirst(bloc);
                    	 System.out.println(this.serverThread.getBranche().get(0).toString());
                    	//gossip du bloc sur le réseau
                    	System.out.println("gossip du bloc");
                        JsonObject blocJson= bloc.toJson();
                        StringWriter stringWriter = new StringWriter();
    					Json.createWriter(stringWriter).writeObject(blocJson);
    					this.serverThread.sendMessage(stringWriter.toString(),null);
                    }
                    else {
                    	System.out.println("branche a change previous="+this.previousId);
                    }
					
                   // System.out.println(this.serverThread.getBranche().get(0).toString());
                    // Effacer la liste après le minage
                	this.serverThread.getPoolTransaction().clear();
                }
            } 
            else {
                try {
                    Thread.sleep(1000); // Attendre 1 seconde avant de revérifier
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	private String calculateHash() {
		
        String dataToHash = this.previousHash + Long.toString(nonce) + this.data;
        String calculatedHash = "";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(dataToHash.getBytes());

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            calculatedHash = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return calculatedHash;
    }
}
