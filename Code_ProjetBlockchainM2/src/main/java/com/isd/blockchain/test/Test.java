package com.isd.blockchain.test;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.Arrays;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi.SHA256;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;




public class Test {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException, SignatureException {

    	// Générer une paire de clés RSA
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        PrivateKey privateKey=keyPair.getPrivate();
        // Récupérer la clé publique
        PublicKey publicKey = keyPair.getPublic();

        // Hash de la clé publique
        byte[] publicKeyBytes = publicKey.getEncoded();
        byte[] sha256Hash = SHA256(publicKeyBytes);
        byte[] ripeMd160Hash = RIPEMD160(sha256Hash);
        String address = Hex.toHexString(ripeMd160Hash);
        System.out.println("Address: " + address);
        
        // Message à signer
        String message = "Hello, world!";
        
        // Signature du message avec la clé privée RSA
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        byte[] signatureBytes = signature.sign();
        
        // Affichage de la signature
        System.out.println("Signature : " + bytesToHex(signatureBytes));
        
        // Vérification de la signature avec la clé publique RSA
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(message.getBytes());
        boolean verified = verifier.verify(signatureBytes);
        
        // Affichage du résultat de la vérification
        System.out.println("Signature verified : " + verified);
    }

    // Fonction utilitaire pour convertir un tableau de bytes en une chaîne hexadécimale
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
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
}
