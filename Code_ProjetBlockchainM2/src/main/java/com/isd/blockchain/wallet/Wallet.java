package com.isd.blockchain.wallet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

import javax.json.Json;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

public class Wallet {
	private String username;
	private int portnumber;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private String address;
	private Socket socket;

	public Wallet(String username, int portnumber) {
		super();
		this.username = username;
		this.portnumber = portnumber;
		//initialisation des cles publique et prive
		try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(2048, random);

            KeyPair pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
		// Hash de la clé publique
        byte[] publicKeyBytes = publicKey.getEncoded();
        byte[] sha256Hash = SHA256(publicKeyBytes);
        byte[] ripeMd160Hash = RIPEMD160(sha256Hash);
        this.address = Hex.toHexString(ripeMd160Hash);
        System.out.println("Address: " + address);
		
	}
	
	private byte[] signData(String sender,String destinataire,double montant) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(this.privateKey);
        String data = sender + destinataire + montant;
        signature.update(data.getBytes());
        return signature.sign();
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
	 // Fonction utilitaire pour convertir un tableau de bytes en une chaîne hexadécimale
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
	public static void main(String[] args) throws Exception {
		BufferedReader bufferedReader = new BufferedReader (new InputStreamReader(System.in));
		System.out.println("> Entrer le nom d utilisateur et le port ce wallet:");
		System.out.println("> exemple komara 5050");
		String[] setupValues = bufferedReader.readLine().split(" ");
		Wallet wallet=new Wallet(setupValues[0],Integer.parseInt(setupValues[1]));
		//System.out.println(wallet.publicKey);
		wallet.updateListenToPeers (bufferedReader, setupValues[0]);
		
	}

	public void updateListenToPeers (BufferedReader bufferedReader, String username) throws Exception{
		
		System.out.println(">Connecter vous a un Peer hostname:port#");
		System.out.println(">exemple localhost:4040");
		String input = bufferedReader. readLine();
		String[] inputValues = input.split(" ");
		
		this.socket = null;
		if (!input.equals("s")) for (int i = 0; i < inputValues.length; i++) {
			String[] address = inputValues[i].split(":");
			
			try {
			this.socket = new Socket(address[0], Integer.valueOf(address [1]));
			WalletThread walletThread= new WalletThread(this.socket);
			walletThread.start();
			
			} catch(Exception e) {
				if (this.socket != null) {
					this.socket.close ();
					updateListenToPeers(bufferedReader, username);
				}
				else {
					System.out.println("Mauvais arguments entrés ou port deja utilise");
					updateListenToPeers(bufferedReader, username);
				}
			}
		}
		communicate(bufferedReader, username, this.socket);
	}
	
	public void communicate(BufferedReader bufferedReader,String username,Socket socket) {
		
		try {
			System.out.println("> 1 Pour avoir le ledger");
			System.out.println("> 2 Pour avoir  la liste des peers");
			System.out.println("> 3 Pour sortir du programme");
			System.out.println("> 4 Changer le peer auquel on est connecte");
			System.out.println("> 5 Afficher l adresse du wallet");
			System.out.println("Pour envoyer une transaction:transaction destinataire adresseDestinataire montant");
			System.out.println("Pour demander une preuve de Merkle:merkle bloc transactionHash");
			boolean flag = true;
			while(flag) {
				String message = bufferedReader. readLine();
				if (message.equals("3")) {
					flag = false;
					break;
				}else if (message.equals("1")) {//demande de ledger
					StringWriter stringWriter = new StringWriter();
					Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
					.add("ledger", "ledger")
					.build());
					PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
					printWriter.println(stringWriter.toString());
	
				}else if (message.equals("2")) {//demande de liste de peer
					StringWriter stringWriter = new StringWriter();
					Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
					.add("listePeer", "listePeer")
					.build());
					PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
					printWriter.println(stringWriter.toString());
	
				}else if (message.equals("4")) {
					updateListenToPeers (bufferedReader, username);
	
				}else if (message.equals("5")) {
					System.out.println("Adresse Wallet:"+this.address);
	
				}else {
					if(message.startsWith("transaction")) {
						String[] mots = message.split(" ");
						if(mots.length!=4 ) {
							System.out.println("Mauvais arguments");
							communicate(bufferedReader, username, socket);
						}
						try {
							Double.parseDouble(mots[3]);
						} catch (Exception e) {
							System.out.println("Mauvais arguments");
							communicate(bufferedReader, username, socket);
						}
						StringWriter stringWriter = new StringWriter();
						byte[] sig=signData(username, mots[1],Double.parseDouble(mots[3]));
						Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
						.add("username", username)
						.add("destinataire", mots[1])
						.add("transaction","transaction")
						.add("montant",mots[3])
						.add("publicKey", Base64.getEncoder().encodeToString(this.publicKey.getEncoded()))
						.add("sig",Base64.getEncoder().encodeToString(sig) )
						.add("senderAddress", this.address)
						.add("recipientAddress", mots[2])
						.add("timestamp",Double.toString(Instant.now().getEpochSecond()))
						.add("NoeudDest", "")
						.build());
						PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
						printWriter.println(stringWriter.toString());
					}
					if(message.startsWith("merkle")) {
						String[] mots = message.split(" ");
						if(mots.length!=3 ) {
							System.out.println("Mauvais arguments");
							communicate(bufferedReader, username, socket);
						}
						try {
							Long.parseLong(mots[1]);
						} catch (Exception e) {
							System.out.println("Mauvais arguments");
							communicate(bufferedReader, username, socket);
						}
						StringWriter stringWriter = new StringWriter();
						Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
								.add("merkel", "merkel")
								.add("blocId", mots[1])
								.add("transactionHash",mots[2])
								.build());
						PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
						printWriter.println(stringWriter.toString());
					}
				}
			}
				System.exit (0);

			} catch (Exception e) {communicate(bufferedReader, username, socket);}
	}

}
