package com.example.stockassistant;

import java.io.Serializable;

public class Stock implements Serializable {
    private String ticker;
    private String stockName;
    private Double price;
    private Double priceChange;
    private Double percentage;


    public Stock(String ticker,String name, Double price, Double changeInPrice,Double percentage){
        this.ticker = ticker;
        this.stockName = name;
        this.price = price;
        this.priceChange = changeInPrice;
        this.percentage = percentage;
    }

    //Just added
    public Stock(String ticker, String name) {
        this.ticker = ticker;
        this.stockName = name;
    }

    public Stock( ){

    }

    public String getStockName() {
        return stockName;
    }

    public String getTicker() {
        return ticker;
    }

    public Double getPrice() {
        return price;
    }

    public Double getPriceChange() {
        return priceChange;
    }

    public Double getPercentage() {
        return percentage;
    }



    public void setStockName(String n) {
        this.stockName = n;
    }

    public void setTicker(String t) {
        this.ticker = t;
    }

    public void setPrice(Double p) {
        this.price = p;
    }

    public void setPriceChange(Double c) {
        this.priceChange = c;
    }

    public void setPercentage(Double p) {
        this.percentage = p;
    }


}
