package com.example.portfoliop2p_api.node;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;

import com.example.portfoliop2p_api.http.HttpRequest;
import com.example.portfoliop2p_api.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class NodeSingleton {
    public ArrayList<NodeObserver> nodeObservers = new ArrayList<>();
    private static NodeSingleton instance;
    public Node node;
    public Thread server;

    public static NodeSingleton getInstance(){
        if (instance == null){
            instance = new NodeSingleton();
            instance.startServer();
        }
        return instance;
    }

    public void startNode(WifiManager wifiManager){
        this.node = new Node(wifiManager);
    }

    public void notifyNode(String info){
        for (NodeObserver nObserver : nodeObservers){
            nObserver.update(info);
        }
    }

    public void addObserver(NodeObserver newNodeObserver){
        nodeObservers.add(newNodeObserver);
    }
    public void removeObserver(NodeObserver oldNodeObserver){
        nodeObservers.add(oldNodeObserver);
    }

    private void startServer(){
        Thread serverThread = new Thread(new MyServerThread());
        this.server = serverThread;
        this.server.start();
    }
    public void stopServer(){
        serverCarryOn = false;
    }

    private boolean serverCarryOn = true;
    private int clientNumber = 0;
    public class MyServerThread implements Runnable {
        @SuppressLint("SuspiciousIndentation")
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(4444);

                //Always be ready for next client
                while (true) {
                    notifyNode("SERVER: start listening..");
                    Socket clientSocket = serverSocket.accept();
                    notifyNode("SERVER connection accepted");
                    clientNumber++;
                    new RemoteClient(clientSocket, clientNumber).start();

                }//while listening for clients

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }//run
    }//ServerThread

    class RemoteClient extends Thread {
        private final Socket client;
        private int number;
        private boolean clientCarryOn = true;

        public RemoteClient(Socket clientSocket, int number) {
            this.client = clientSocket;
            this.number = number;
        }
        public void run() {
            try {
                DataInputStream inNodeStream = new DataInputStream(client.getInputStream());
                DataOutputStream outNodeStream = new DataOutputStream(client.getOutputStream());

                String request;
                String response;

                //default response
                HttpResponse defaultHttpResponse = new HttpResponse("HTTP", "404 Not Found");
                response = defaultHttpResponse.GetJsonString();

                while (clientCarryOn) {
                    request = (String) inNodeStream.readUTF();

                    notifyNode("Client says: " + request);
                    //notify(X)

                    System.out.println("client to server " + request);

                    //Converting request to a HttpRequest object
                    HttpRequest httpRequest = new HttpRequest(request);
                    //If the request is successfully read:
                    if (!httpRequest.Path.isEmpty()) {
                        HttpResponse httpResponse;
                        //defining the response based on the path
                        switch(httpRequest.Path.toLowerCase()){
                            case "getid":
                                httpResponse = new HttpResponse("HTTP", "200 OK", node.getId());
                                break;

                            case "getphonebook":
                                ArrayList<String> rightNeighbors = node.GetPhonebookRight();
                                ArrayList<String> leftNeighbors = node.GetPhonebookLeft();
                                JSONObject json = new JSONObject();

                                try{
                                    json.put("rightNeighbors", rightNeighbors);
                                    json.put("leftNeighbors", leftNeighbors);

                                }catch (JSONException e) {
                                    System.out.println("Could not convert body to json");
                                }
                                String output = json.toString();

                                httpResponse = new HttpResponse("HTTP", "200 OK", output);

                                System.out.println(httpResponse);
                                break;

                            case "updatephonebook":
                                String input = httpRequest.Body.toLowerCase();

                                String right ="";
                                String left ="";

                                try{
                                    JSONObject json_input = new JSONObject(input);
                                    right = json_input.get("rightneighbors").toString();
                                    left = json_input.get("leftneighbors").toString();
                                }catch (Exception e){
                                    System.out.println("Could not convert " + input + " to json");
                                }

                                right = right.replace("[","");
                                right = right.replace("]","");
                                right = right.replace(" ","");
                                left = left.replace("[","");
                                left = left.replace("]","");
                                left = left.replace(" ","");

                                node.nodesRight.set(0, right.split(",")[0]);
                                node.nodesRight.set(1, right.split(",")[1]);
                                node.nodesRight.set(2, right.split(",")[2]);

                                node.nodesLeft.set(0, left.split(",")[0]);
                                node.nodesLeft.set(1, left.split(",")[1]);
                                node.nodesLeft.set(2, left.split(",")[2]);


                                System.out.println(node.nodesRight.get(0));
                                System.out.println(node.nodesRight.get(1));
                                System.out.println(node.nodesRight.get(2));

                                System.out.println(node.nodesLeft.get(0));
                                System.out.println(node.nodesLeft.get(1));
                                System.out.println(node.nodesLeft.get(2));

                                httpResponse = new HttpResponse("HTTP", "200 OK", "");
                                break;

                            case "adddata":

                                if(!httpRequest.Body.isEmpty()) {

                                    if (httpRequest.Body.contains("|")) {
                                        String hash = node.AddData(httpRequest.Body);
                                        //send back key in body
                                        httpResponse = new HttpResponse("HTTP", "200 OK", "Location data has been added. The key is: " + hash);
                                    }else{
                                        String hash = node.AddData(httpRequest.Body);
                                        //send back key in body
                                        httpResponse = new HttpResponse("HTTP", "200 OK", "Data has been added. The key is: " + hash);

                                    }

                                }else {
                                    httpResponse = new HttpResponse("HTTP", "400 Bad Request");
                                }


                                break;

                            case "getdata":
                                //gets data based on key
                                String data = node.GetData(httpRequest.Body);

                                if(data == "Error: Requested data not found"){
                                    httpResponse = new HttpResponse("HTTP", "400 Bad Request", data);

                                }else {
                                    //sends data back to client
                                    httpResponse = new HttpResponse("HTTP", "200 OK", data);
                                }

                                break;

                            case "deletedata":
                                //deletes data based on key
                                String del_data = node.DeleteData(httpRequest.Body);


                                if(del_data == "Error: Requested data not found"){
                                    httpResponse = new HttpResponse("HTTP", "400 Bad Request", del_data);

                                }else {
                                    //sends data back to client
                                    httpResponse = new HttpResponse("HTTP", "200 OK", "The data: " + del_data + ", has been deleted");
                                }

                                break;

                            default:
                                System.out.println("Does not recognize path: " + httpRequest.Path.toLowerCase());
                                httpResponse = new HttpResponse("HTTP", "400 Bad Request");
                        }

                        response = httpResponse.GetJsonString();
                    }

                    notifyNode(response);
                    outNodeStream.writeUTF(response);
                    outNodeStream.flush();
                    waitABit();
                    clientCarryOn = false;
                }
                //Closing everything down
                client.close();
                notifyNode("SERVER: Remote client " + number + " socket closed");
                inNodeStream.close();
                notifyNode("SERVER: Remote client " + number + " inputstream closed");
                outNodeStream.close();
                notifyNode("SERVER: Remote client  " + number + "outputstream closed");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

    }

    private String[] breakUpBody(String input){
        /*
        String[] temp;
        String[] result = new String[6];

        temp = input.split(" ");
        result.add(temp[4]);
        */
        return null;
    }

    private void waitABit() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
