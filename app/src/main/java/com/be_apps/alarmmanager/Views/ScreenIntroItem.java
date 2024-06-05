package com.be_apps.alarmmanager.Views;

public class ScreenIntroItem {

    private String  description ;
    private final int Image ;

    public ScreenIntroItem(String description, int image) {
        this.description = description;
        Image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return Image;
    }

}
