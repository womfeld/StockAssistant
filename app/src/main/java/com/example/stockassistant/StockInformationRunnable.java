package com.example.stockassistant;


import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class StockInformationRunnable implements Runnable {
    private MainActivity mainActivity;

    private final String prefixURL = "https://cloud.iexapis.com/stable/stock/";

    private final String myAPIKey = "pk_35aeb3dce8e94953aed3a9657e24e408";

    private static final String TAG = "StockLoaderRunnable";

    private final String ticker;


    //Given a stock ticker as the parameter, returns all other stock information
    //from API such as price, price change, and percentage
    public StockInformationRunnable(MainActivity mainActivity, String ticker) {
        this.mainActivity = mainActivity;
        this.ticker = ticker;
    }



    @Override
    public void run() {

        Uri.Builder buildURL = Uri.parse(prefixURL).buildUpon();

        buildURL.appendPath(ticker);
        buildURL.appendPath("quote");
        buildURL.appendQueryParameter("token", myAPIKey);
        String urlToUse = buildURL.build().toString();


        Log.d(TAG, "doInBackground: " + urlToUse);


        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            //Showed in class for establishing a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            //Just added
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                handleResults(null);
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            //Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            //System.out.println("null was returned");
            handleResults(null);
            return;

        }

        handleResults(sb.toString());

    }

    public void handleResults(final String jsonString) {

        final Stock s = parseJSON(jsonString);
        mainActivity.runOnUiThread(() -> mainActivity.updateStock(s));
    }


    private Stock parseJSON(String s) {
        Stock st = new Stock();
        try {
            JSONObject jObjMain = new JSONObject(s);

            String name = jObjMain.getString("companyName");
            String symbol = jObjMain.getString("symbol");
            Double price = jObjMain.getDouble("latestPrice");
            Double priceChange = jObjMain.getDouble("change");
            Double changePercentage = jObjMain.getDouble("changePercent");

            st.setStockName(name);
            st.setTicker(symbol);
            st.setPrice(price);
            st.setPriceChange(priceChange);
            st.setPercentage(changePercentage);
            return st;
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }


}

