package com.example.portfoliop2p_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.portfoliop2p_api.node.Node;
import com.example.portfoliop2p_api.node.NodeSingleton;

import java.util.Enumeration;
import java.util.List;

public class DataSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    //Singleton
    NodeSingleton nodeSingleton = NodeSingleton.getInstance();
    private Node node;

    //extras
    String command;
    String serverIp;

    //buttons

    Button backButton;
    Button select;


    //dropdown
    AutoCompleteTextView autoCompleteTextView;

    ArrayAdapter<String> adapterItems;

    //key
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_selection);

        //getting the data from the IP Activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            command = extras.getString("command");
            serverIp = extras.getString("serverIp");
        }
        System.out.println("\n\n"+command+"\n\n");
        System.out.println("\n\n"+serverIp+"\n\n");

        // Singleton
        node = nodeSingleton.node;

        //get keys from data storage
        List<String> keys = node.getDataStorageKeys();

        //buttons
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        backButton = findViewById(R.id.backButton);
        select = findViewById(R.id.selectButton);


        //convert keys to resource list for widget (dropdown menu)
        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, keys);

        //set dropdown menu values
        autoCompleteTextView.setAdapter(adapterItems);

        //listeners
        backButton.setOnClickListener(this);
        select.setOnClickListener(this);

        //dropdown menu listener
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //get clicked key
                key = adapterView.getItemAtPosition(i).toString();

                //Give notification that key has been selected
                Toast.makeText(DataSelectionActivity.this, "Key: " + key, Toast.LENGTH_SHORT).show();
            }


        });


    }


    @Override
    public void onClick(View view) {
        if (view == backButton) {
            Intent myIntent = new Intent(this, CommandActivity.class);
            startActivity(myIntent);
        } else {

            if(view ==  select){

                Intent myIntent = new Intent(this, MainActivity.class);
                myIntent.putExtra("command", command);
                myIntent.putExtra("serverIp", serverIp);
                myIntent.putExtra("dataKey", key);
                startActivity(myIntent);

            }
        }

    }
}//DataSelectionActivity