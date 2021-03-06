package es.bsc.pmes.types;

import java.util.UUID;

/**
 * Created by scorella on 8/5/16.
 */
public class Image {
    private String id;
    private String imageName;
    private String imageType;

    public Image(){
        this.id = UUID.randomUUID().toString();
        this.imageName = "";
        this.imageType = "";
    }
    public Image(String imageName, String imageType){
        this.id = UUID.randomUUID().toString();
        this.imageName = imageName;
        this.imageType = imageType;
    }

    /** GETTERS AND SETTERS*/
    public String getId() {
        return id;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}
