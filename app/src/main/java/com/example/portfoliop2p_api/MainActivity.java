package com.example.portfoliop2p_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.portfoliop2p_api.http.HttpRequest;
import com.example.portfoliop2p_api.http.HttpResponse;
import com.example.portfoliop2p_api.node.Node;
import com.example.portfoliop2p_api.node.NodeObserver;
import com.example.portfoliop2p_api.node.NodeSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NodeObserver {
    // Variables
    String command;
    String serverIp;
    String location;
    String dataKey;

    //UI
    private Button backButton;
    private Button submitCommand;
    private TextView serverInfoTv, clientInfoTv;

    // Logging/status messages
    private String serverinfo = "SERVER LOG:";
    private String clientinfo = "CLIENT LOG: ";


    //Singleton
    NodeSingleton nodeSingleton = NodeSingleton.getInstance();
    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        submitCommand = findViewById(R.id.addLocationDataButton);
        submitCommand.setOnClickListener(this);

        serverInfoTv = findViewById(R.id.serveroutput);
        clientInfoTv = findViewById(R.id.clientoutput);


        // Singleton
        node = nodeSingleton.node;
        nodeSingleton.addObserver(this);

        //getting the data from the IP Activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            command = extras.getString("command");
            serverIp = extras.getString("serverIp");
            location = extras.getString("location");
            dataKey = extras.getString("dataKey");
        }
        System.out.println("\n\n"+command+"\n\n");
        System.out.println("\n\n"+serverIp+"\n\n");
        System.out.println("\n\n"+location+"\n\n");
        System.out.println("\n\n"+dataKey+"\n\n");
    }


    @Override
    public void onClick(View view) {
        if (view == backButton) {
            Intent myIntent = new Intent(this, IpActivity.class);
            myIntent.putExtra("command", command);
            startActivity(myIntent);
        }
        if (view == submitCommand){
            submitCommand.setText("Resend "+command+" to "+ serverIp);
            Thread clientThread = new Thread(new MyClientThread());
            clientThread.start();
        } else if (view.getId() == R.id.addLocationDataButton){
            Thread clientThread = new Thread(new MyClientThread());
            clientThread.start();
        }
    }

    @Override
    public void update(String info) {
        sUpdate(info);
    }

    class MyClientThread implements Runnable {
        @Override
        public void run() {
            try {
                cUpdate("CLIENT: starting client socket ");
                Socket connectionToServer = new Socket(serverIp, 4444);
                cUpdate("CLIENT: client connected ");

                DataInputStream inClientStream = new DataInputStream(connectionToServer.getInputStream());
                DataOutputStream outClientStream = new DataOutputStream(connectionToServer.getOutputStream());

                String serverResponse;

                //default value for clientRequest
                String clientRequest = command;

                //Constructing the command
                if (!command.isEmpty()){
                    HttpRequest httpRequest;
                    switch (command.toLowerCase()){
                            case "getid":
                                httpRequest = new HttpRequest("HTTP", "GET", "getID");
                                clientRequest = httpRequest.GetJsonString();
                                break;

                            case "getphonebook":
                                httpRequest = new HttpRequest("HTTP", "GET", "getPhonebook");
                                clientRequest = httpRequest.GetJsonString();
                                break;

                            case "updatephonebook":
                                ArrayList<String> rightNeighbors = node.GetPhonebookRight();
                                ArrayList<String> leftNeighbors = node.GetPhonebookLeft();
                                JSONObject json = new JSONObject();

                                try{
                                    JSONArray rightJsonArray = new JSONArray();
                                    JSONArray leftJsonArray = new JSONArray();

                                    //filling the json arrays
                                    for ( String leftNeighbor : leftNeighbors ) {
                                        leftJsonArray.put(leftNeighbor);
                                    }
                                    for ( String rightNeighbor : rightNeighbors ) {
                                        rightJsonArray.put(rightNeighbor);
                                    }

                                    //putting the arrays into our json object
                                    json.put("rightNeighbors", rightJsonArray);
                                    json.put("leftNeighbors", leftJsonArray);

                                }catch (JSONException e) {
                                    System.out.println("Could not convert Right or Left neighbors to json");
                                }
                                String output = json.toString();

                                httpRequest = new HttpRequest("HTTP", "GET", "updatePhonebook", output);

                                clientRequest = httpRequest.GetJsonString();
                                break;

                            case "adddata":

                                if(!location.isEmpty()) {
                                    httpRequest = new HttpRequest("HTTP", "POST", "addData", location);
                                    clientRequest = httpRequest.GetJsonString();
                                }else{
                                    httpRequest = new HttpRequest("HTTP", "POST", "addData", "testing_data");
                                    clientRequest = httpRequest.GetJsonString();
                                }

                                break;

                            case "getdata":
                                //the request-body contains the key for the data
                                httpRequest = new HttpRequest("HTTP", "GET", "getData",
                                        "9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08");
                                clientRequest = httpRequest.GetJsonString();
                                break;

                            case "deletedata":
                                //the request-body contains the key for the data
                                httpRequest = new HttpRequest("HTTP", "DEL", "deleteData",
                                        "9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08");
                                clientRequest = httpRequest.GetJsonString();
                                break;

                            default:
                                throw new IllegalStateException("Unexpected value: " + command.toLowerCase());
                    }
                }

                outClientStream.writeUTF(clientRequest);
                outClientStream.flush();
                cUpdate("I said:      " + clientRequest);
                serverResponse = inClientStream.readUTF();
                cUpdate("Server says: " + serverResponse);

                waitABit();
                connectionToServer.shutdownInput();
                cUpdate("CLIENT: closed inputstream");
                connectionToServer.shutdownOutput();
                cUpdate("CLIENT: closed outputstream");
                connectionToServer.close();
                cUpdate("CLIENT: closed socket");


            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }//run()
    } //class MyClientThread

    //Wait by setting the thread to sleep for 1,5 seconds
    private void waitABit() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sUpdate(String message) {
        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverinfo = message + "\n" + serverinfo;
                serverInfoTv.setText(serverinfo);
            }
        });

    }

    //Client update TextView
    private void cUpdate(String message) {
        System.out.println(message);

        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clientinfo = message + "\n" + clientinfo;
                clientInfoTv.setText(clientinfo);
            }
        });
    }
}