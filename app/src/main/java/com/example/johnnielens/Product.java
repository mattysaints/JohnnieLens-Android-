package com.example.johnnielens;

import android.widget.ImageView;

import java.net.URL;

public class Product {

    private String name;
    private String image;
    private Double feedback;
    private URL url;
    private String is_prime;
    private Double price; //prendere campo denominato "crudo" nel file json

    public Product(String name, String image, URL url, Double price) {
        this.name = name;
        this.image = image;
        this.url = url;
        this.price = price;
    }

    public Product(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getFeedback() {
        return feedback;
    }

    public void setFeedback(Double feedback) {
        this.feedback = feedback;
    }

    public URL getUrl() {
        return url;
    }

    public String getUrlString (){
        return getUrl().toString();
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getIs_prime() {
        return is_prime;
    }

    public void setIs_prime(String is_prime) {
        this.is_prime = is_prime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean productValid(){
        return getUrl() != null && getPrice()!=null && getImage()!=null;
    }


    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", feedback=" + feedback +
                ", url=" + url +
                ", is_prime='" + is_prime + '\'' +
                ", price=" + price +
                '}';
    }
}
