package com.example.capstone_dswms;

public class ImageData {
    private String caption;
    private String downloadUrl;

    public ImageData() {
        // Default constructor required for DataSnapshot.getValue(ImageData.class)
    }

    public ImageData(String caption, String downloadUrl) {
        this.caption = caption;
        this.downloadUrl = downloadUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
