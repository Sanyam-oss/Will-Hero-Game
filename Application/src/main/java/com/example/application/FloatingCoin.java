package com.example.application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public class FloatingCoin extends Reward{
    private boolean retrieved;
    private Coins coinreward;
    public FloatingCoin(double x, double y) {
        super(x, y);
        render();
        coinreward = new Coins(1);
        retrieved = false;
    }

    private void render(){
        Image img = new Image("coin1.png");
        ImageView iv = new ImageView(img);
        iv.setX(this.getLocation().getX()); iv.setY(this.getLocation().getY());
        iv.setPreserveRatio(true);
        iv.setPickOnBounds(true);
        iv.setFitHeight(30);
        iv.setFitWidth(30);
        super.setImageView(iv);
    }
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    @Serial
    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        render();
    }

    //@Override
    public void ifHeroCollides(Hero hero) {
        if(retrieved) return;
        else {
        }
    }
}