package com.example.stockassistant;

import android.net.Uri;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class StockNameRunnable implements Runnable {



    private MainActivity mainActivity;
    private static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";

    public StockNameRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }



    @Override
    public void run() {
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
        } catch (Exception e) {
            handleResults(null);
            return;
        }

        handleResults(sb.toString());

    }

    public void handleResults(final String s) {

        HashMap<String,String> symbolNameList = parseJSON(s);
        mainActivity.runOnUiThread(() -> mainActivity.updateData(symbolNameList));


    }


    private HashMap<String,String> parseJSON(String s) {

        HashMap<String,String> nameSymbolMap = new HashMap<>();
        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jSymbolName = (JSONObject) jObjMain.get(i);
                String symbol = jSymbolName.getString("symbol");
                String name = jSymbolName.getString("name");
                nameSymbolMap.put(symbol, name);
            }
            return nameSymbolMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

