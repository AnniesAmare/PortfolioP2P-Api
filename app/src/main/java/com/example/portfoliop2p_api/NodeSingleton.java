package com.example.portfoliop2p_api;

import android.net.wifi.WifiManager;

public class NodeSingleton {
    private static NodeSingleton instance;
    Node node;

    public static NodeSingleton getInstance(){
        if (instance == null){
            instance = new NodeSingleton();
            //instance.startServer();
        }
        return instance;
    }

    public void startNode(WifiManager wifiManager){
        this.node = new Node(wifiManager);
    }

/*
    private void waitABit() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
 */

}
