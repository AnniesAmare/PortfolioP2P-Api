package com.example.portfoliop2p_api.data;

import java.security.MessageDigest;

public class SHA256 {
    public SHA256() {

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            System.out.println("SHA256: error in constructor");
            System.out.println(e);
        }
    }

    MessageDigest digest;

    public String hash(String inputText){
        byte[] input = inputText.getBytes();
        byte[] hashValue = sha256Hash(input);
        String hashValueText = Hex.byteArrayToHexString(hashValue);
        //String blocks = Hex.hexStringToMultiLine(hashValueText,32);
        String blocks = Hex.hexStringToSingleLine(hashValueText);
        return blocks;
    }

    private byte[] sha256Hash(byte[] input) {
        byte[] hashValue = digest.digest(input);
        return hashValue;
    }
}
