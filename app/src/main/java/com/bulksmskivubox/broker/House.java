package com.bulksmskivubox.broker;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

public class House implements Serializable {

    double price;
    String capacity, description, owner;
    transient List<String> images;
    double latitude;
    double longitude;
    Boolean land;
    Boolean buy ;
    Boolean active;

    public House(double price, String capacity, String description, List<String> images, double latitude, double longitude,Boolean land, Boolean buy, Boolean active, String owner) {
        this.price = price;
        this.capacity = capacity;
        this.description = description;
        this.images = images;
        this.latitude = latitude;
        this.longitude = longitude;
        this.land = land;
        this.buy = buy;
        this.active = active;
        this.owner = owner;
    }

    public House(){

    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Boolean getLand() {
        return land;
    }

    public void setLand(Boolean land) {
        this.land = land;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
