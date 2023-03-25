package com.example.portfoliop2p_api;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.portfoliop2p_api.node.Node;
import com.example.portfoliop2p_api.node.NodeSingleton;

import java.util.Enumeration;
import java.util.List;

public class DataSelectionActivity extends AppCompatActivity {

    //Singleton
    NodeSingleton nodeSingleton = NodeSingleton.getInstance();
    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_selection);

        // Singleton
        node = nodeSingleton.node;

        //get keys from data storage
        List<String> keys = node.getDataStorageKeys();

    }
}