package com.example.portfoliop2p_api.node;

import com.example.portfoliop2p_api.data.Data;
import com.example.portfoliop2p_api.data.SHA256;

import android.app.Application;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class Node extends Application {
    public String id;

    ArrayList<String> nodesLeft;
    ArrayList<String> nodesRight;

    Dictionary DataStorage;
    public List<String> DataKeys;

    public String getId(){
        return id;
    }

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

        DataStorage = new Hashtable();
        this.DataKeys = new ArrayList<String>();


        //default value in storage FOR TESTING
        this.DataStorage.put(
                "9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08",
                "testing_data");

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


    public String AddData(String body) {

        //new hash obj
        SHA256 newHash = new SHA256();
        //hashes data
        String dataId = newHash.hash(body);

        //adds data to storage
        this.DataStorage.put(dataId,body);

        return dataId;
    }


    public String GetData(String dataId){
        try{
            String data = this.DataStorage.get(dataId).toString();

            //create new data object
            Data dataObj = new Data(dataId,data);

            //convert data to json
            String dataAsJson = dataObj.DataToJson();

            return dataAsJson;
        }catch (Exception e){
            return "Error: Requested data not found";

        }

    }


    public String DeleteData(String key){
        try{
            String data = this.DataStorage.remove(key).toString();
            return data;

        }catch (Exception e){
            return "Error: Requested data not found";

        }
    }

    public List<String> getDataStorageKeys(){
        if (!DataKeys.isEmpty()){
            return DataKeys;
        } else {
            List<String> error = new ArrayList<String>();
            error.add("Error: No keys found");
            return error;
        }
    }





}
