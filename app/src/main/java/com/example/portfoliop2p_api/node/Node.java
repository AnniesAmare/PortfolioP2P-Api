package com.example.portfoliop2p_api.node;

import android.app.Application;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Node extends Application {
    public String id;

    ArrayList<String> nodesLeft;
    ArrayList<String> nodesRight;

    public ArrayList<String> GetPhonebookLeft(){
        return nodesLeft;
    }

    public ArrayList<String> GetPhonebookRight(){
        return nodesRight;
    }

    Node(WifiManager wifiManager) {
        super.onCreate();
        String id = getLocalIpAddress(wifiManager);
        this.id = id;

        ArrayList<String> nodesLeft = new ArrayList<>();
        nodesLeft.add(id);
        nodesLeft.add(id);
        nodesLeft.add(id);
        this.nodesLeft = nodesLeft;

        ArrayList<String> nodesRight = new ArrayList<>();
        nodesRight.add(id);
        nodesRight.add(id);
        nodesRight.add(id);
        this.nodesRight = nodesRight;

    }

    private String getLocalIpAddress(WifiManager wifiManager) {
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        String address;
        try {
            address = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return address;
    }

}
