package com.example.stockassistant;



import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private ArrayList<Stock> stocklist;
    private MainActivity mainActivity;
    public StockAdapter(MainActivity mainActivity, ArrayList<Stock> stockArrayList){
        this.mainActivity = mainActivity;
        this.stocklist = stockArrayList;
    }

    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_entry, parent, false);

        itemView.setOnClickListener((View.OnClickListener) mainActivity);
        itemView.setOnLongClickListener((View.OnLongClickListener) mainActivity);

        return new StockViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock s = stocklist.get(position);

        if(s.getPriceChange() < 0){

            //Set all label colors to red if the net price change was greater than zero
            holder.stockName.setTextColor(Color.parseColor("red"));
            holder.ticker.setTextColor(Color.parseColor("red"));
            holder.price.setTextColor(Color.parseColor("red"));
            holder.priceChange.setTextColor(Color.parseColor("red"));
            holder.percentage.setTextColor(Color.parseColor("red"));
            holder.direction.setText("▼");
            holder.direction.setTextColor(Color.parseColor("red"));

        }
        else {

            //Set all label colors to green if the net price change was greater than zero

            holder.stockName.setTextColor(Color.parseColor("green"));
            holder.ticker.setTextColor(Color.parseColor("green"));
            holder.price.setTextColor(Color.parseColor("green"));
            holder.priceChange.setTextColor(Color.parseColor("green"));
            holder.percentage.setTextColor(Color.parseColor("green"));
            holder.direction.setText("▲");
            holder.direction.setTextColor(Color.parseColor("green"));
        }

        holder.ticker.setText(s.getTicker());
        holder.stockName.setText(s.getStockName());
        //Maybe change this later
        holder.percentage.setText("("+String.format(Locale.US, "%.2f",s.getPercentage())+"%)");
        holder.priceChange.setText(String.format(Locale.US,"%.2f",s.getPriceChange()));
        holder.price.setText(" $"+String.format(Locale.US,"%.2f",s.getPrice()));


    }

    @Override
    public int getItemCount() {
        return stocklist.size();
    }


}

