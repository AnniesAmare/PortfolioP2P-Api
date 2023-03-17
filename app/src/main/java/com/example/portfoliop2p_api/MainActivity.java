package com.example.portfoliop2p_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // Variables
    String command;
    String serverIp;

    //UI
    private Button backButton;

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

        // Singleton
        node = nodeSingleton.node;

        //getting the data from the IP Activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            command = extras.getString("command");
            serverIp = extras.getString("serverIp");
        }
        System.out.println("\n\n"+command+"\n\n");
        System.out.println("\n\n"+serverIp+"\n\n");
    }


    @Override
    public void onClick(View view) {
        if (view == backButton) {
            Intent myIntent = new Intent(this, IpActivity.class);
            myIntent.putExtra("command", command);
            startActivity(myIntent);
        }

    }
}