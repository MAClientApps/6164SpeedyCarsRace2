package com.example.gamebox2d;

import java.util.ArrayList;

public class Category {
    private String name;
    private String image;
    ArrayList<Game> gameList;

    public Category(String name, String image) {
        this.name = name;
        this.image = image;
        gameList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String imageResource) {
        this.image = imageResource;
    }
}
