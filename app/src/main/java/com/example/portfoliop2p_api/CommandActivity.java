package com.example.portfoliop2p_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.portfoliop2p_api.node.Node;
import com.example.portfoliop2p_api.node.NodeSingleton;

public class CommandActivity extends AppCompatActivity implements View.OnClickListener{
    //UI-elements
    private TextView thisIp;
    private Button newNeighbor;
    private Button getID;
    private Button getPhonebook;
    private Button getData;
    private Button addData;
    private Button deleteData;

    //Singleton
    NodeSingleton nodeSingleton = NodeSingleton.getInstance();
    private Node node;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        //UI
        thisIp = findViewById(R.id.textField);

        newNeighbor = findViewById(R.id.newNeighbor);
        getID = findViewById(R.id.getId);
        getPhonebook = findViewById(R.id.getPhonebook);
        getData = findViewById(R.id.getData);
        addData = findViewById(R.id.addData);
        deleteData = findViewById(R.id.deleteData);

        // Adding the on-click listener
        newNeighbor.setOnClickListener(this);
        getID.setOnClickListener(this);
        getPhonebook.setOnClickListener(this);
        getData.setOnClickListener(this);
        addData.setOnClickListener(this);
        deleteData.setOnClickListener(this);

        //Singleton
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (nodeSingleton.node == null) {
            nodeSingleton.startNode(wifiManager);
        }
        node = nodeSingleton.node;
        thisIp.setText("This IP: \n"+ node.id);

    }

    @Override
    public void onClick(View view) {
        String command = "";

        if(view == newNeighbor || view == getID || view == getPhonebook) {
            if (view == newNeighbor) {
                command = "newNeighbor";
            }
            if (view == getID) {
                command = "getID";
            }
            if (view == getPhonebook) {
                command = "getPhonebook";
            }

            Intent myIntent = new Intent(this, IpActivity.class);
            myIntent.putExtra("command", command);
            startActivity(myIntent);

        }else {

            if (view == getData) {
                command = "getData";
            }
            if (view == addData) {
                command = "addData";
            }
            if (view == deleteData) {
                command = "deleteData";
            }


            Intent myIntent = new Intent(this, DataActivity.class);
            myIntent.putExtra("command", command);
            startActivity(myIntent);

        }

    }

}