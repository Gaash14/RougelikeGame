package com.example.rougelikegame.android.models.meta;

/**
 * Represents an option for selecting an image source (e.g., Camera, Gallery).
 */
public class ImageSourceOption {
    private String title;
    private String description;
    private int iconResource;

    /**
     * Constructs a new ImageSourceOption.
     * @param title The title of the option.
     * @param description The description of the option.
     * @param iconResource The resource ID for the icon.
     */
    public ImageSourceOption(String title, String description, int iconResource) {
        this.title = title;
        this.description = description;
        this.iconResource = iconResource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
}
