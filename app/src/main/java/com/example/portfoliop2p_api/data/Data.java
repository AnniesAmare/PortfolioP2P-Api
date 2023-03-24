package com.example.portfoliop2p_api.data;

import org.json.JSONException;
import org.json.JSONObject;


public class Data {
    public String ID;
    public String Value;


    public Data(String ID, String Value){
        this.ID = ID;
        this.Value = Value;
    }


    //json to data object
    public Data(String jsonString){
        try {
            JSONObject json = new JSONObject(jsonString);
            this.ID = json.getString("id");
            this.Value = json.getString("value");

        }catch (JSONException e){ //the error is thrown here
            System.out.println("Could not convert from: " + jsonString + " to Data");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    public String DataToJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", this.ID);
            json.put("value", this.Value);

        } catch (JSONException e) {
            System.out.println("Could not convert HttpRequest to json");
            return null;
        }
        return json.toString();
    }





}
