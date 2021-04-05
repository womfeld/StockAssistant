package com.example.stockassistant;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {


    TextView ticker;
    TextView stockName;
    TextView price;
    TextView priceChange;
    TextView percentage;
    TextView direction;

    public StockViewHolder(View itemView){
        super(itemView);
        stockName = itemView.findViewById(R.id.nameOfStock);
        ticker = itemView.findViewById(R.id.symbol);
        price = itemView.findViewById(R.id.stockPrice);
        priceChange = itemView.findViewById(R.id.totalPriceChange);
        percentage = itemView.findViewById(R.id.changePercentage);
        direction = itemView.findViewById(R.id.stockDirection);
    }

}
