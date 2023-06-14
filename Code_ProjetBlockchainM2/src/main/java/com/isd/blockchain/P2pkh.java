package com.isd.blockchain;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Stack;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

public class P2pkh {
	
	public static boolean verify(byte[]sig,PublicKey publicKey,String ownerAdress,String sender, String destinataire, double montant) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException {
		if(ownerAdress.equals("")) {
			return true;
		}
		Stack<String> pile = new Stack<String>();
		String scriptSig =Base64.getEncoder().encodeToString(sig) + " " + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + " " ;
		String scriptPubKey = "OP_DUP OP_HASH160 " + ownerAdress + " OP_EQUALVERIFY OP_CHECKSIG";
		String[] commandes = (scriptSig + scriptPubKey).split(" ");
		//System.out.println("----------SCRIPT---------------------------------------------------------------------------------");
		for (String c : commandes){
			//System.out.println("|" + c + "|");
		}
		//System.out.println("-----------------------------------------------------------------------------------------------------");
		for (String c : commandes){
			//System.out.println(pile);
			//System.out.println("->>-"+c);
			
			if (pile.size() > 0 && pile.peek().equals("false")){
				break;
			}
			if (c.startsWith("OP")){
				exec_command(c, pile,sender,destinataire,montant);
			}
			else {
				pile.push(c);
			}
		}
		
		if (pile.size() == 1) {
			String val = pile.pop();
			if (val.equals("true")){
				return true;
			}
			return false;
		}
		return false;
	}
	
	private static void exec_command(String s, Stack<String> pile,String sender, String destinataire, double montant) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
		String val = "";
		String pubKey = "";
		String sig = "";
		byte[] pubKeyBytes = pubKey.getBytes();
		byte[] sigBytes = sig.getBytes();
		String hash = "";
		
		switch(s){
			case "OP_DUP":
				val = pile.peek();
				pile.push(val);
				break;
			case "OP_HASH256":
				val = pile.pop();
				
				hash = Base64.getEncoder().encodeToString(SHA256(Base64.getDecoder().decode(val)));
				pile.push(hash);
				break;
			case "OP_HASH160":
				val = pile.pop();
				byte[] valBytes = Base64.getDecoder().decode(val);
				//System.out.println("val"+val);
                X509EncodedKeySpec valSpec = new X509EncodedKeySpec(valBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey valPK = kf.generatePublic(valSpec);

				byte[] valPKBytes = valPK.getEncoded();
				byte[] sha256Hash = SHA256(valPKBytes);
				byte[] ripeMd160Hash = RIPEMD160(sha256Hash);
				hash = Hex.toHexString(ripeMd160Hash);
				pile.push(hash);
				break;
			case "OP_EQUALVERIFY":
				// EQUAL STEP
				String val1 = pile.pop();
				String val2 = pile.pop();
				
				if (val1.equals(val2)){
					pile.push("true");
				} else{
					pile.push("false");
				}
				
				// VERIFY STEP
				val = pile.pop();
				if (!val.equals("true")){
					pile.push("false");
				}
				break;
			case "OP_VERIFY":
				val = pile.pop();
				if (!val.equals("true")){
					pile.push("false");
				}
				break;
			case "OP_CHECKSIG":
				// CHECK SIG STEP
				pubKey = pile.pop();
				sig = pile.pop();
				
				pubKeyBytes = pubKey.getBytes();
				sigBytes =Base64.getDecoder().decode(sig); 

				String data = sender + destinataire + montant;
				
				pubKeyBytes = Base64.getDecoder().decode(pubKey);
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
                kf = KeyFactory.getInstance("RSA");
                PublicKey publicKey = kf.generatePublic(pubKeySpec);
				// Vérification de la signature avec la clé publique RSA
		        Signature verifier = Signature.getInstance("SHA256withRSA");
		        verifier.initVerify(publicKey);
		        verifier.update(data.getBytes());
		        boolean verified = verifier.verify(sigBytes);
				if(verified) {
					pile.push("true");
				}
				else {
					pile.push("false");
				}
				break;

			case "OP_CHECKSIGVERIFY":
				// CHECK SIG STEP
				pubKey = pile.pop();
				sig = pile.pop();
				
				pubKeyBytes = pubKey.getBytes();
				sigBytes = Base64.getDecoder().decode(sig); 

				 // Vérification de la signature avec la clé publique RSA
                data = sender + destinataire + montant;
				pubKeyBytes = Base64.getDecoder().decode(pubKey);
                pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
                kf = KeyFactory.getInstance("RSA");
                publicKey = kf.generatePublic(pubKeySpec);
				// Vérification de la signature avec la clé publique RSA
		        verifier = Signature.getInstance("SHA256withRSA");
		        verifier.initVerify(publicKey);
		        verifier.update(data.getBytes());
		        verified = verifier.verify(sigBytes);
				if(verified) {
					pile.push("true");
				}
				else {
					pile.push("false");
				}
				
				// VERIFY STEP
				val = pile.pop();
				if (!val.equals("true")){
					pile.push("false");
				}
				break;
				
				default:
				break;
				
		}
	}
	
	private static byte[] SHA256(byte[] data) {
        SHA256Digest digest = new SHA256Digest();
        digest.update(data, 0, data.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

    private static byte[] RIPEMD160(byte[] data) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(data, 0, data.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }
    
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Taille de clé recommandée
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] createSignature(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());

        return signature.sign();
    }

    public static boolean verifySignature(String data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());

        return signature.verify(signatureBytes);
    }
    public static void main(String[] args) throws Exception {
    	String sender = "Sam";
    	String destinataire = "Komara";
    	double montant = 30;
    	// Données à signer
        String data = sender + destinataire + montant;

      //----------------------TEST AVEC HASH160 ------------------//
    	KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        keyGen.initialize(2048, random);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        // Hash de la clé publique
        byte[] publicKeyBytes = publicKey.getEncoded();
        byte[] sha256Hash = SHA256(publicKeyBytes);
        byte[] ripeMd160Hash = RIPEMD160(sha256Hash);
        String address = Hex.toHexString(ripeMd160Hash);
        
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        
    	boolean transaction_valide = verify(signature.sign(), publicKey, address, sender, destinataire, montant);
    	System.out.println(transaction_valide);
    }
   
    
}
