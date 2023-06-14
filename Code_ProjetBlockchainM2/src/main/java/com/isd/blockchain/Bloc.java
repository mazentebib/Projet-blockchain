package com.isd.blockchain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.isd.blockchain.Utxo.TransactionOutput;
import com.isd.blockchain.test.Merkle;


public class Bloc {
	
	private long id;
	private long nonce;
	private String hash;
	private List<Transaction> transacations = new ArrayList<>();
	private Merkle merkle;
	private List<TransactionOutput> utxos = new ArrayList<>();
	
	
	public Bloc(long id, long nonce, String hash, List<Transaction> transacations, List<TransactionOutput> utxos) {
		super();
		this.id = id;
		this.nonce = nonce;
		this.hash = hash;
		this.transacations = transacations;
		this.utxos = utxos;
	}
	

	public Bloc(long id, long nonce, String hash) {
		super();
		this.id = id;
		this.nonce = nonce;
		this.hash = hash;
	}
	

	public Bloc() {
		super();
	}


	//Cree un nouveau bloc à partir d un objet json
	public Bloc(JsonObject jsonObject) {
        this.id = jsonObject.getJsonNumber("id").longValue();
        this.nonce = jsonObject.getJsonNumber("nonce").longValue();
        this.hash = jsonObject.getString("hash");

        // Récupérer les transactions sous forme d'un tableau JSON
        JsonArray jsonTransactions = jsonObject.getJsonArray("transactions");
        for (JsonValue jsonValue : jsonTransactions) {
            JsonObject jsonTransaction = (JsonObject) jsonValue;
            String source = jsonTransaction.getString("source");
            String destination = jsonTransaction.getString("destination");
            double montant = jsonTransaction.getJsonNumber("montant").doubleValue();
            Transaction transaction = new Transaction(source, destination, montant);
            this.transacations.add(transaction);
        }
        
        // Récupérer les utxos sous forme d'un tableau JSON
        JsonArray jsonUtxos= jsonObject.getJsonArray("utxos");
        for (JsonValue jsonValue : jsonUtxos) {
            JsonObject jsonUtxo = (JsonObject) jsonValue;
            String id = jsonUtxo.getString("id");
            String owner = jsonUtxo.getString("owner");
            String ownerAdress = jsonUtxo.getString("ownerAdress");
            double value = jsonUtxo.getJsonNumber("value").doubleValue();
            String idTransactionParent = jsonUtxo.getString("idTransactionParent");
            TransactionOutput transactionOutput = new TransactionOutput(owner,ownerAdress, value, idTransactionParent);
            this.utxos.add(transactionOutput);
        }

    }
	
	public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", this.id)
                .add("nonce", this.nonce)
                .add("hash", this.hash);

        // Convertir transactions en tableau JSON
        JsonArrayBuilder transactionsBuilder = Json.createArrayBuilder();
        if(this.transacations != null) {
	        for (Transaction transaction : this.transacations) {
	            transactionsBuilder.add(transaction.toJson());
	        }
        }
        builder.add("transactions", transactionsBuilder);
        
        // Convertir utxos en tableau JSON
        JsonArrayBuilder transactionsOutputBuilder = Json.createArrayBuilder();
        if(this.utxos != null) {
	        for (TransactionOutput transactionOutput: this.utxos) {
	        	transactionsOutputBuilder.add(transactionOutput.toJson());
	        }
        }
        builder.add("utxos", transactionsOutputBuilder);
        return builder.build();
    }
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
	@Override
	public String toString() {
		/*return "Bloc [id=" + id + ", nonce=" + nonce + ", hash=" + hash + ", transacations=" + transacations
				+ ", utxos=" + utxos + "]";*/
		String st = "---- Bloc ID#"+ this.id + " contient :\n";
		st+="hash:"+ this.hash+ "\n";
		st+="nonce:"+this.nonce+"\n";
		int i = 0;
        for (TransactionOutput troutput : utxos) {
            i++;
            st += "TransOutput #"+i+" : " + troutput.toString() + "\n";
        }
        i = 0;
        for (Transaction tr : transacations) {
            i++;
            st += "Transaction #"+i+" : " + tr.toString() + "\n";
        }
        st += "----- Fin Bloc ID#" + this.id + " -----\n";
        return st;
	}

	public List<Transaction> getTransacations() {
		return transacations;
	}

	public void setTransacations(ArrayList<Transaction> transacations) {
		this.transacations = transacations;
	}

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}


	public List<TransactionOutput> getUtxos() {
		return utxos;
	}


	public void setUtxos(List<TransactionOutput> utxos) {
		this.utxos = utxos;
	}
	

	public void setTransacations(List<Transaction> transacations) {
		this.transacations = transacations;
	}


	public Merkle getMerkle() {
		return merkle;
	}


	public void setMerkle(Merkle merkle) {
		this.merkle = merkle;
	}


	
	

}
