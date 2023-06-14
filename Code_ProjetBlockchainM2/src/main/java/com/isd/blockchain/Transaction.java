package com.isd.blockchain;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.isd.blockchain.Utxo.TransactionInput;
import com.isd.blockchain.Utxo.TransactionOutput;



public class Transaction {
	
	private String Source;
	private String destination;
	private double montant;
	private List<TransactionInput> inputs=new ArrayList<>();
	private List<TransactionOutput> outputs=new ArrayList<>();
	
	private PublicKey publicKey;
	private byte[]sig;
	private String senderAddress;
	private String recipientAddress;
	private double timestamp;
	
	public Transaction(String source, String destination, double montant) {
		super();
		Source = source;
		this.destination = destination;
		this.montant = montant;
	}
	
	public Transaction(String source, String destination, double montant, PublicKey publicKey, byte[]sig, String senderAddress ,String recipientAddress,double timestamp) {
		super();
		Source = source;
		this.destination = destination;
		this.montant = montant;
		this.publicKey=publicKey;
		this.sig=sig;
		this.senderAddress=senderAddress;
		this.recipientAddress=recipientAddress;
		this.timestamp=timestamp;
	}
	
	 public JsonObject toJson() {
	        JsonObjectBuilder builder = Json.createObjectBuilder()
	                .add("source", this.Source)
	                .add("destination", this.destination)
	                .add("montant", this.montant);
	        return builder.build();
	 }
	 /**
	  * cherche les utxo de l'utilisateur source et les met comme transactionInput
	  * @param utxos
	  */
	 public void FindInputs(List<TransactionOutput> utxos) {
		 for (TransactionOutput transactionOutput : utxos) {
			 //verification du p2pkh aussi****************************************************************************************
			try {
				if(transactionOutput.getOwner().equals(this.Source) && P2pkh.verify(sig, publicKey, transactionOutput.getOwnerAdress(), Source, destination, montant)) {
					this.inputs.add(new TransactionInput(transactionOutput.getId(),transactionOutput));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	 }
	 
	 public boolean processTransaction(List<TransactionOutput> utxos) {
		 
		// Calculer la valeur d'entrée totale de la transaction
		 double montantTotalEntree=0;
		 for (TransactionInput input : this.inputs) {
			 montantTotalEntree+=input.getUTXO().getValue();
		 }
		// Vérifier que l'expéditeur dispose de suffisamment de fonds pour effectuer la transaction
		 if(montantTotalEntree<this.montant) {
			 return false;
		 }
		 // Calculer la valeur de sortie de la transaction
		 double valeurSortie=montantTotalEntree-this.montant;
		 // Mettre à jour les sorties de transaction pour les destinataires et les excédents
		 this.outputs.add(new TransactionOutput(this.destination, this.recipientAddress,montant, this.inputs.get(0).getIdTransactionOutput()));
		 if(valeurSortie>0) {//ajout de la transaction excedente
			 this.outputs.add(new TransactionOutput(this.Source, this.senderAddress,valeurSortie, this.inputs.get(0).getIdTransactionOutput()));
		 }
		// Ajouter les sorties de transaction non dépensées à la liste d'UTXOs
	    for (TransactionOutput output : this.outputs) {
	        utxos.add(output);
	    } 
	    // Supprimer les sorties de transaction dépensées de la liste d'UTXOs
	    for (TransactionInput input : this.inputs) {
	        utxos.remove(input.getUTXO());
	    }
	    
		return true;
	}
	 
	 
	@Override
	public String toString() {
		return "[Emetteur=" + Source + ", destinataire=" + destination + ", montant=" + montant + "]";
		//return "Transaction [Source=" + Source + ", destination=" + destination + ", montant=" + montant + "]";
	}

	public String getSource() {
		return Source;
	}

	public void setSource(String source) {
		Source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public double getMontant() {
		return montant;
	}

	public void setMontant(double montant) {
		this.montant = montant;
	}
	
	public double getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	public  String getSHA()
    {
		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder.append(this.Source).append(":").append(this.destination).append(":").append(this.montant).append(this.timestamp);
		String input=dataBuilder.toString();
        try {

            // Static getInstance method is called with hashing SHA
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // digest() method called
            // to calculate message digest of an input
            // and return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown"
                    + " for incorrect algorithm: " + e);

            return null;
        }
    }
}
