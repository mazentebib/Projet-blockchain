package com.isd.blockchain.Utxo;

public class TransactionInput {
	private String idTransactionOutput; // L'ID de la sortie de transaction non dépensée
    private TransactionOutput UTXO; // La sortie de transaction non dépensée

    public TransactionInput(String idTransactionOutput) {
        this.idTransactionOutput = idTransactionOutput;
    }
    
	public TransactionInput(String idTransactionOutput, TransactionOutput uTXO) {
		super();
		this.idTransactionOutput = idTransactionOutput;
		UTXO = uTXO;
	}

	public String getIdTransactionOutput() {
		return idTransactionOutput;
	}

	public void setIdTransactionOutput(String idTransactionOutput) {
		this.idTransactionOutput = idTransactionOutput;
	}

	public TransactionOutput getUTXO() {
		return UTXO;
	}

	public void setUTXO(TransactionOutput uTXO) {
		UTXO = uTXO;
	}

    

}
