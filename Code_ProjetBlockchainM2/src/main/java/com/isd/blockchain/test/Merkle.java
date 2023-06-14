package com.isd.blockchain.test;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Merkle {
	
	private List<String> merkleTree=new ArrayList<>();
	
    public  Merkle(List<String> txnLists) {
    	if(txnLists.size() % 2 == 1){
	        txnLists.add(txnLists.get(txnLists.size()-1));
	    }
    	this.merkleTree=new ArrayList<>(txnLists);
    	Collections.reverse(merkleTree);
        this.merkleTree = merkleTree(merkleTree,txnLists);
        Collections.reverse(merkleTree);
    }
    private List<String> merkleTree(List<String> merkleFinal,List<String> parentHashList){
	    //Return the Merkle Root
	    if(parentHashList.size() == 1){
	        return merkleFinal;
	    }
	    //ArrayList<String> parentHashList=new ArrayList<>();
	    ArrayList<String> hashList=new ArrayList<>(parentHashList);
	    parentHashList.clear();
	    
	    // If odd number of transactions , add the last transaction again
	    if(hashList.size() % 2 == 1){
	        hashList.add(hashList.get(hashList.size()-1));
	    }
	    
	    //Hash the leaf transaction pair to get parent transaction
	    for(int i=hashList.size()-1; i>0; i-=2){
	    	//String hashedString = hashList.get(i-1)+hashList.get(i);
	    	String hashedString = addHashes(hashList.get(i-1),hashList.get(i));
	        parentHashList.add(hashedString);merkleFinal.add(hashedString);
	        //System.out.println(hashedString);
	    }
	    
	    Collections.reverse(parentHashList);
	    return merkleTree(merkleFinal,parentHashList);
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
    public List<String> getPath(String transactionHash) {
        ArrayList<String> path = new ArrayList<>();
        if (!merkleTree.contains(transactionHash)) {
            return null; // Le hash de transaction n'est pas présent dans l'arbre
        }
        int index = merkleTree.indexOf(transactionHash);
        System.out.println("index:"+index);
        System.out.println("taille:"+merkleTree.size());
        while (index > 0) {
            if (index % 2 == 0) {//pair
                path.add(merkleTree.get(index - 1));
                index = (index/ 2)-1;
            } else {
                path.add(merkleTree.get(index + 1));
                index=(index-1)/2;
            }
        }
        return path;
    }
    

    
	public List<String> getMerkleTree() {
		return merkleTree;
	}
	
	public static void main(String[] args) {
    	ArrayList<String> t = new ArrayList<>();
        t.add("t1"); t.add("t2"); t.add("t3");t.add("t4");
        t.add("t5"); t.add("t6"); t.add("t7"); t.add("t8");
    	//t.add("a4a3be4f778");t.add("a4a3be4f779");t.add("a4a3be4f780");t.add("a4a3be4f781");
        Merkle m = new Merkle(t);
        //m.createMerkleTree(t);
        System.out.println(m.merkleTree);

        String transactionHash = "t3";
        String rootFounded=transactionHash;
        List<String> path = m.getPath(transactionHash);
        if (path == null) {
            System.out.println("Transaction hash not found in Merkle tree");
        } else {
            System.out.println("Path for transaction hash " + transactionHash + ":");
            for (String hash : path) {
                System.out.println(hash);
                rootFounded=m.addHashes(rootFounded,hash);
            }
        }
        System.out.println("rootFounded "+rootFounded);
        System.out.println("real root   "+m.getMerkleTree().get(0));
        System.out.println(rootFounded.equals(m.getMerkleTree().get(0)));
    }

}




