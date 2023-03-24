package com.example.portfoliop2p_api.node;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;

import com.example.portfoliop2p_api.http.HttpRequest;
import com.example.portfoliop2p_api.http.HttpResponse;
import com.example.portfoliop2p_api.data.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Permission;
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

                            case "getneighbors":
                                ArrayList<String> rightNeighbors = node.GetPhonebookRight();
                                ArrayList<String> leftNeighbors = node.GetPhonebookLeft();
                                String temp = "{rightNeighbors: [ " +
                                        "{id: " + leftNeighbors.get(0) +
                                        " IP: " + leftNeighbors.get(0) + "}," +
                                        "{id: " + leftNeighbors.get(1) +
                                        " IP: " + leftNeighbors.get(1) + "}," +
                                        "{id: " + leftNeighbors.get(2) +
                                        " IP: " + leftNeighbors.get(2) + "}" +
                                        "]" +
                                                "{leftNeighbors: [ " +
                                        "{id: " + rightNeighbors.get(0) +
                                        " IP: " + rightNeighbors.get(0) + "}," +
                                        "{id: " + rightNeighbors.get(1) +
                                        " IP: " + rightNeighbors.get(1) + "}," +
                                        "{id: " + rightNeighbors.get(2) +
                                        " IP: " + rightNeighbors.get(2) + "}]}";
                                httpResponse = new HttpResponse("HTTP", "200 OK", temp);

                                System.out.println(httpResponse);
                                break;

                            case "updatephonebook":
                                String input = httpRequest.Body.toLowerCase();

                                //node.nodesRight.set(0, T[0].split(":")[0]).replace("ip", "");
                                //node.nodesRight.set(1, T[1].split(":")[0]).replace("ip", "");
                                //node.nodesRight.set(2, T[2].split(":")[0]).replace("ip", "");

                                //node.nodesLeft.set(0, T[3].split(":")[0]).replace("ip", "");
                                //node.nodesRight.set(1, T[4].split(":")[0]).replace("ip", "");
                                //node.nodesRight.set(2, T[5].split(":")[0]).replace("ip", "");


                                /*

                                System.out.println("T" + T[0]);
                                System.out.println(T[1]);
                                System.out.println(T[2]);
                                System.out.println(T[3]);
                                System.out.println("T" + T[4]);


                                 */


                                System.out.println(node.nodesRight.get(0));
                                System.out.println(node.nodesRight.get(1));
                                System.out.println(node.nodesRight.get(2));

                                System.out.println(node.nodesLeft.get(0));
                                System.out.println(node.nodesLeft.get(1));
                                System.out.println(node.nodesLeft.get(2));

                                httpResponse = new HttpResponse("HTTP", "200 OK", "");
                                break;

                            case "adddata":

                                String Permission_error = "Error: no location";

                                if(httpRequest.Body != Permission_error) {
                                    //add data (which is the request body)
                                    String hash = node.AddData(httpRequest.Body);

                                    //send back key in body
                                    httpResponse = new HttpResponse("HTTP", "200 OK", "Data has been added. The key is: " + hash);

                                }else{

                                    httpResponse = new HttpResponse("HTTP", "400 Bad Request", Permission_error);

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
        String[] result;

        result = input.split(",");

        return result;
    }

    private void waitABit() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
