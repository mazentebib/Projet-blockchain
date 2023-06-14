package com.isd.blockchain.wallet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;

public class WalletThread extends Thread{
private BufferedReader bufferedReader;
	
	public WalletThread (Socket socket) throws IOException {
		bufferedReader = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
	}
	
	public void run() {
	
		boolean flag = true;
		while (flag) {
		
			try {
				JsonReader jsonReader = Json.createReader(bufferedReader);
				JsonStructure jsonStructure = jsonReader.read();

				if (jsonStructure instanceof JsonObject) {//Le fichier JSON contient un objet JSON
				    JsonObject jsonObject =(JsonObject) jsonStructure;
				  //affichage dans la console
					if (jsonObject.containsKey("message"))
					System.out.println("Message:"+jsonObject.getString("message"));
					
					if (jsonObject.containsKey("socket"))
						System.out.println(jsonObject.getString("socket"));
					
					if (jsonObject.containsKey("TransactionHash")) {
						System.out.println("Le hash de votre Transaction:"+jsonObject.getString("TransactionHash"));
						System.out.println("Le Bloc associé à votre Transaction:"+jsonObject.getString("TransactionBlocId"));
					}
					if(jsonObject.containsKey("merkle")) {//la preuve de merkle de la transaction a ete envoyee
						String rootFounded=jsonObject.getString("transactionHash");
						JsonArray jsonArray= (JsonArray)jsonObject.getJsonArray("merkle");
						for (int i = 0; i < jsonArray.size(); i++) {
					        String hash = jsonArray.getString(i);
					        rootFounded=addHashes(rootFounded,hash);
					    }
						System.out.println("****************Verification en cours Preuve de Merkle****************");
						System.out.println("rootFounded "+rootFounded);
				        System.out.println("real root   "+jsonObject.getString("merkleroot"));
				        if(rootFounded.equals(jsonObject.getString("merkleroot"))) {
				        	System.out.println("Votre transaction est bien presente dans la blockchain");
				        	System.out.println("**********************************************************************");
				        }
				        else {
				        	System.out.println("Votre transaction n est pas presente dans la blockchain");
				        	System.out.println("**********************************************************************");
				        }
					}
					if (jsonObject.containsKey("ledger")) {
						System.out.println(jsonObject.getString("ledger"));
					}
					if (jsonObject.containsKey("listePeer")) {
						System.out.println(jsonObject.getString("listePeer"));
					}
				} 
				else if (jsonStructure instanceof JsonArray) {//Le fichier JSON contient un tableau JSON
					
				} 
				else {
				    System.out.println("Le fichier JSON ne contient ni objet ni tableau JSON.");
				}
				
			} 
			catch(Exception e) {
				flag = false;
				e.printStackTrace();
				interrupt();
			}
		}
	}
	
	 public String addHashes(String h1,String h2) {
	    	// Concatenate the two strings
	        String concat = h1+h2;

	        // Convert the concatenated string to a char array and sort it
	        char[] sortedChars = concat.toCharArray();
	        Arrays.sort(sortedChars);

	        // Convert the sorted char array back to a string
	        String sortedString = new String(sortedChars);

	        // Calculate the hash of the sorted string
	        return hash(sortedString);
	 }
	 
	 private String hash(String data) {
	        try {
	            MessageDigest md = MessageDigest.getInstance("SHA-256");
	            byte[] hash = md.digest(data.getBytes());
	            StringBuilder hexString = new StringBuilder();
	            for (byte b : hash) {
	                String hex = Integer.toHexString(0xff & b);
	                if (hex.length() == 1) {
	                    hexString.append('0');
	                }
	                hexString.append(hex);
	            }
	            return hexString.toString();
	        } catch (NoSuchAlgorithmException e) {
	            throw new RuntimeException(e);
	        }
	 }
}
