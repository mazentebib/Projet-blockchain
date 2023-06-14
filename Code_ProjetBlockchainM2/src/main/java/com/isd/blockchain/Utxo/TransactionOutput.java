package com.isd.blockchain.Utxo;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class TransactionOutput {
	private String id; // L'ID de la sortie de transaction
    private String owner; // Le destinataire de la sortie de transaction
    private String ownerAdress="";//adresse du proprietaire
    private double value; // La valeur de la sortie de transaction
    private String idTransactionParent; // L'ID de la transaction parente

    public TransactionOutput(String owner, String ownerAdress, double value, String idTransactionParent) {
        this.owner = owner;
        this.value = value;
        this.idTransactionParent = idTransactionParent;
        this.id = UUID.randomUUID().toString(); // Générer un ID aléatoire pour la sortie de transaction
        this.ownerAdress=ownerAdress;
    }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", this.id)
                .add("owner", this.owner)
                .add("ownerAdress", this.ownerAdress)
                .add("value", this.value)
                .add("idTransactionParent", this.idTransactionParent);
        return builder.build();
    }
	/*// Vérifier si la sortie de transaction appartient au destinataire spécifié
    public boolean isMine(String owner) {
        return this.owner.equals(owner);
    }*/
    
    public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getOwner() {
		return owner;
	}


	public void setOwner(String owner) {
		this.owner = owner;
	}


	public double getValue() {
		return value;
	}


	public void setValue(double value) {
		this.value = value;
	}


	public String getIdTransactionParent() {
		return idTransactionParent;
	}


	public void setIdTransactionParent(String idTransactionParent) {
		this.idTransactionParent = idTransactionParent;
	}

	
	public String getOwnerAdress() {
		return ownerAdress;
	}

	public void setOwnerAdress(String ownerAdress) {
		this.ownerAdress = ownerAdress;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", owner=" + owner + ", value=" + value + ", idTransactionParent="
                + idTransactionParent + "]";
	}
	public String toStringLedger() {
        String st = "┃";
        for (int i = 0; i < 14 - owner.length(); i++) {
            st += " ";
        }
        st += owner + "┃";
        for (int i = 0; i < 14 - String.valueOf(value).length(); i++) {
            st += " ";
        }
        st += value + "┃";
        for (int i = 0; i < 14 - String.valueOf(ownerAdress).length(); i++) {
            st += " ";
        }
        st += ownerAdress ;
        return st;
    }


}
