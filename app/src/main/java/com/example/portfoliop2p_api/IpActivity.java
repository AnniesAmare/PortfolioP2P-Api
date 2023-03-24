package com.example.portfoliop2p_api;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import com.example.portfoliop2p_api.node.Node;
import com.example.portfoliop2p_api.node.NodeSingleton;

import java.util.ArrayList;

public class IpActivity extends AppCompatActivity implements View.OnClickListener {
    //variables
    String command;

    //UI
    private Button backButton;
    private Button leftButtonIP1;
    private Button leftButtonIP2;
    private Button leftButtonIP3;
    private Button rightButtonIP1;
    private Button rightButtonIP2;
    private Button rightButtonIP3;
    private Button manualSubmit;
    private EditText ipInputField;

    //Singleton
    NodeSingleton nodeSingleton = NodeSingleton.getInstance();
    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        // UI
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        //left buttons
        leftButtonIP1 = findViewById(R.id.leftButtonIP1);
        leftButtonIP2 = findViewById(R.id.leftButtonIP2);
        leftButtonIP3 = findViewById(R.id.leftButtonIP3);
        //listeners
        leftButtonIP1.setOnClickListener(this);
        leftButtonIP2.setOnClickListener(this);
        leftButtonIP3.setOnClickListener(this);

        //right buttons
        rightButtonIP1 = findViewById(R.id.rightButtonIP1);
        rightButtonIP2 = findViewById(R.id.rightButtonIP2);
        rightButtonIP3 = findViewById(R.id.rightButtonIP3);
        //listeners
        rightButtonIP1.setOnClickListener(this);
        rightButtonIP2.setOnClickListener(this);
        rightButtonIP3.setOnClickListener(this);


        ipInputField = findViewById(R.id.editTextIP);
        ipInputField.setHint("Submit IP-address");
        manualSubmit = findViewById(R.id.manualSubmit);
        manualSubmit.setOnClickListener(this);

        // Singleton
        node = nodeSingleton.node;

        //Setting the button texts
        ArrayList<String> leftNeighbors = node.GetPhonebookLeft();
        leftButtonIP1.setText(leftNeighbors.get(0));
        leftButtonIP2.setText(leftNeighbors.get(1));
        leftButtonIP3.setText(leftNeighbors.get(2));


        ArrayList<String> rightNeighbors = node.GetPhonebookRight();
        rightButtonIP1.setText(leftNeighbors.get(0));
        rightButtonIP2.setText(leftNeighbors.get(1));
        rightButtonIP3.setText(leftNeighbors.get(2));

        //getting the data from the Command Activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            command = extras.getString("command");
        }
        System.out.println("\n\n"+command+"\n\n");

    }

    @Override
    public void onClick(View view) {
        String serverIp = "";
        if (view == backButton) {
            Intent myIntent = new Intent(this, CommandActivity.class);
            startActivity(myIntent);
        } else {
            switch (view.getId()) {
                case R.id.manualSubmit:
                    serverIp = ipInputField.getText().toString();
                    break;
                case R.id.rightButtonIP1:
                    serverIp = rightButtonIP1.getText().toString();
                    break;
                case R.id.rightButtonIP2:
                    serverIp = rightButtonIP2.getText().toString();
                    break;
                case R.id.rightButtonIP3:
                    serverIp = rightButtonIP3.getText().toString();
                    break;
                case R.id.leftButtonIP1:
                    serverIp = leftButtonIP1.getText().toString();
                    break;
                case R.id.leftButtonIP2:
                    serverIp = leftButtonIP2.getText().toString();
                    break;
                case R.id.leftButtonIP3:
                    serverIp = leftButtonIP3.getText().toString();
                    break;
            }

            if(command.contains("addData")){
                Intent myIntent = new Intent(this, DataActivity.class);
                myIntent.putExtra("command", command);
                myIntent.putExtra("serverIp", serverIp);
                startActivity(myIntent);

            } else {
                Intent myIntent = new Intent(this, MainActivity.class);
                myIntent.putExtra("command", command);
                myIntent.putExtra("serverIp", serverIp);
                startActivity(myIntent);
            }


        }
    }
}