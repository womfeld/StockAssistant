package com.example.stockassistant;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "";
    private final ArrayList<Stock> stockList = new ArrayList<>();
    private SwipeRefreshLayout swiper;
    private RecyclerView recyclerView;
    private StockAdapter nAdapter;
    public View v;
    private StockDatabase stockDatabase;
    private HashMap<String, String> symbolToStockMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        nAdapter = new StockAdapter(this, stockList);

        recyclerView.setAdapter(nAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        //swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        swiper.setOnRefreshListener(() -> {

            if(!doNetworkCheck()){

                swiper.setRefreshing(false);
                networkErrorDialog("refresh");

            }
            else {

                doRefresh();
            }
        });
        stockDatabase = new StockDatabase(this);


        //The runnable that retrieves all of the stock names and symbols is executed in onCreate()
        //What this does is, as soon as the app is launched, it will go to the stock-name API and populate
        //the hashmap with all of the stock information (done in the stockNameRunnable.  The key/value pair is the name and ticker



        StockNameRunnable runnOne = new StockNameRunnable(this);
        new Thread(runnOne).start();




        ArrayList<Stock> tempStocks = stockDatabase.loadStocks();

        //This inidicates if there was no network connnection made, in which case we must
        //display error dialog and load all of the stocks previously in the application onto the
        //application
        if(!doNetworkCheck()){
            networkErrorDialog("");

            //Loops through stocks previously loaded into database and adds all of them
            for(int i=0; i<tempStocks.size(); i++){
                stockList.add(tempStocks.get(i));
            }

            //Sorts the stocks by alphabetical order using compareTo method
            Collections.sort(stockList, (lhs, rhs) -> lhs.getTicker().compareTo(rhs.getTicker()));

            //Once list is sorted, we must notify the adapter of data that was altered
            nAdapter.notifyDataSetChanged();

        }
        //If there was a network connection, then we must start the runnable that retrieves the
        //stock data (i.e. Price, price change, percentage) from the stock API for every stock in our stockList
        else {
            for(int i=0; i<tempStocks.size(); i++){

                String symbol = tempStocks.get(i).getTicker();
                StockInformationRunnable runn = new StockInformationRunnable(this, symbol);
                new Thread(runn).start();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_stock, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        stockDatabase.shutDown();
        super.onDestroy();
    }


    //Play around with changing this
    @Override
    protected void onResume() {
        stockList.size();
        super.onResume();
        nAdapter.notifyDataSetChanged();
    }



    @Override
    public void onClick(View view) {

        int pos = recyclerView.getChildLayoutPosition(view);

        String marketPlace = "http://www.marketwatch.com/investing/stock/";
        String symbol = stockList.get(pos).getTicker();

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(marketPlace + symbol));
        startActivity(i);


    }



    @Override
    public boolean onLongClick(View view) {

        int pos = recyclerView.getChildLayoutPosition(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_delete_24);

        //Sets builder title and caption
        TextView tv = view.findViewById(R.id.symbol);
        String symbol = tv.getText().toString();
        builder.setTitle("Duplicate Stock");
        builder.setMessage("Delete Stock Symbol "+symbol+"?");


        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stockDatabase.deleteStock(stockList.get(pos).getTicker());
                stockList.remove(pos);
                nAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }



    private boolean doNetworkCheck() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (cm == null) {
            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (netInfo != null ) {
            return true;
        } else {
            return false;
        }
    }




    //Test the connection stuff
    //Account for if there is no text entered (fix the toast)
    //Also, maybe change saveStock

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!doNetworkCheck()){
            networkErrorDialog("add");
            return false;
        }
        else{
            switch (item.getItemId()) {
                case R.id.add_stock:

                    //Extracts what the user types in the dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    final EditText et = new EditText(this);
                    et.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_CHARACTERS );
                    et.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(et);


                    //Add stock for the name/ticker that was searched
                    builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            if (!doNetworkCheck()) {

                                networkErrorDialog("add");

                            }

                            //Passed the network check
                            else {


                                if (et.getText().toString().isEmpty()) {
                                    Toast.makeText(MainActivity.this, "Ticker not found", Toast.LENGTH_SHORT).show();
                                }

                                //A ticker that was not empty/null was entered
                                else {


                                    //All possible stocks that may match the stock we are searching
                                    //These stocks are stored in array possibleStocks, which is an
                                    //array of strings in the form "TICKER - NAME"
                                    ArrayList<String> possibleStocks = searchHashMap(et.getText().toString());


                                    //If there were no stocks found, then show the noStocksFound dialog
                                    if (possibleStocks.size() == 0) {

                                        noStockFoundDialog(et.getText().toString());

                                    }

                                    //If there is only one possible stock, then we can save that result right away
                                    else if (possibleStocks.size() == 1) {
                                        if (isDuplicate(possibleStocks.get(0))) {
                                            duplicateDialog(et.getText().toString());
                                        } else {
                                            //Save the stock at (possibleStocks.get(0));
                                            int dash = possibleStocks.get(0).indexOf("-");
                                            String newSymbol = (possibleStocks.get(0)).substring(0, dash - 1);

                                            StockInformationRunnable runnM = new StockInformationRunnable(MainActivity.this, newSymbol);
                                            new Thread(runnM).start();

                                            Stock selectedStock = new Stock(newSymbol, symbolToStockMap.get(newSymbol));
                                            stockDatabase.addStock(selectedStock);
                                        }
                                    }

                                    else {


                                        //Now, implement a dialog that allows us to select a list of items and select one of the items
                                        AlertDialog.Builder displayStocks = new AlertDialog.Builder(MainActivity.this);

                                        //A list must be passed in to setItems method, so convert arrayList into a list
                                        //of strings
                                        final String[] stocks = new String[possibleStocks.size()];
                                        for (int i = 0; i < possibleStocks.size(); i++) {
                                            stocks[i] = possibleStocks.get(i);

                                        }

                                        displayStocks.setItems(stocks, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (isDuplicate(stocks[which])) {
                                                    //Show the duplicate dialog
                                                    duplicateDialog(et.getText().toString());
                                                }
                                                else {

                                                    //If there are no duplicates, then save the stock to the database

                                                    int d = stocks[which].indexOf("-");

                                                    //newTicker is ticker we are about to add to the stockList/stocks and our database
                                                    String newTicker = stocks[which].substring(0, d - 1);

                                                    StockInformationRunnable runnM = new StockInformationRunnable(MainActivity.this, newTicker);
                                                    new Thread(runnM).start();

                                                    Stock nStock = new Stock(newTicker, symbolToStockMap.get(newTicker));
                                                    stockDatabase.addStock(nStock);


                                                }
                                            }
                                        });

                                        displayStocks.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });


                                        //Displays the stock options dialog

                                        AlertDialog options = displayStocks.create();

                                        options.show();


                                    }

                                }

                            }

                        }
                    });


                    //Cancel the search
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                        }

                    });

                    builder.setMessage("Please enter a Stock Symbol:");
                    builder.setTitle("Stock Selection");

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    }


    //Remember, the updateData function is called once the stockNameRunnable finishes
    //populating the hashmap.  updateData is called in the stockNameRunnable, which passes
    //in the hashmap with all the stock names/tickers.
    //We then set the hashmap in mainActivity equal to this populated hashmap.
    public void updateData (HashMap < String, String > tickerNameMap){
        if(tickerNameMap!=null && !tickerNameMap.isEmpty()) {


            this.symbolToStockMap = tickerNameMap;
        }
        else{
            Toast.makeText(this, "Problem loading Stock Name and Ticker", Toast.LENGTH_SHORT).show();
        }

        //Wanted to see the value of the hashmap

        /*
        Iterator<String> itr = tickerNameMap.keySet().iterator();
        while (itr.hasNext()) {
            System.out.println("Entry:");
            System.out.println(itr.next());
        }
        */

    }


    //Returns all necessary information for any stock and adds to the stockList/recyclerView in the
    //correct order.  updateStocks is called in the runnable stockInformationLoader, which given a stock
    //ticker as the parameter, returns all other stock information from API such as price, price change, and percentage
    public void updateStock(Stock newStock) {

        if(newStock != null){
            int idx;
            if((idx = stockList.indexOf(newStock)) > -1){
                stockList.remove(idx);
            }
            stockList.add(newStock);


            Collections.sort(stockList, (lhs, rhs) -> lhs.getTicker().compareTo(rhs.getTicker()));

            nAdapter.notifyDataSetChanged();
        }

    }


    //Returns all possible options for stocks given the name/ticker that was searched in the add dialog.
    //If any stock contains the name/ticker as a substring, then that stock is a possible option
    //Returned as an array of strings in the form "TICKER - NAME"
    private ArrayList<String> searchHashMap(String text) {

        ArrayList<String> possibleStocks = new ArrayList<>();

        if(symbolToStockMap != null && !symbolToStockMap.isEmpty()) {

            Iterator<String> it = symbolToStockMap.keySet().iterator();
            while (it.hasNext()) {
                String symbol = it.next();
                String name = symbolToStockMap.get(symbol);

                //System.out.println("Hashmap entry: " + name);

                if (symbol.toUpperCase().contains(text.toUpperCase())) {
                    possibleStocks.add(symbol + " - " + name);
                } else if (name.toUpperCase().contains(text.toUpperCase())) {
                    possibleStocks.add(symbol + " - " + name);
                }
            }
        }
        return possibleStocks;
    }



    //Play around with changing this one
    private boolean isDuplicate(String tStock) {

        int d = tStock.indexOf("-");
        String symbol = tStock.substring(0, d - 1);


        for(int i = 0; i< stockList.size(); i++){
            if(symbol.equals(stockList.get(i).getTicker())){
                return true;
            }
        }

        return false;
    }


    public void duplicateDialog(String symbol){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_report_problem_24);
        builder.setTitle("Duplicate Stock");
        builder.setMessage("Stock Symbol " + symbol + " is already displayed");
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public void noStockFoundDialog(String symbol){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Data for stock symbol");
        builder.setTitle("Symbol Not Found: " + symbol);
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public void networkErrorDialog(String text){

        String caption;
        if(text.equals("add")) {
            caption = "Stocks Cannot Be Added Without A Network Connection";
        }
        else if(text.equals("refresh")){
            caption = "Stocks Cannot Be Updated Without A Network Connection";
        }
        else{
            caption = "Please Check Your Network and try again";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage(caption);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void doRefresh() {

        stockList.clear();
        nAdapter.notifyDataSetChanged();
        Log.d(TAG, "doRefresh: " + stockList.toString());

        swiper.setRefreshing(false);

        ArrayList<Stock> tempStocks = stockDatabase.loadStocks();

        for(int i=0; i<tempStocks.size(); i++){
            String symbol = tempStocks.get(i).getTicker();

            StockInformationRunnable runnThree = new StockInformationRunnable(this, symbol);
            new Thread(runnThree).start();
        }
    }


}
