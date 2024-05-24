package com.tsu.vkkfilteringapp;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

public class RetouchingBrush {

    public static Bitmap retouching(int x,int y,Integer radius, Bitmap bitmap,Double compromise) {

        if (y + radius >= bitmap.getHeight() || x + radius >= bitmap.getWidth()
                || y - radius <= 0 || x - radius <= 0) return bitmap;

        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int counter = 0;
        int red = 0;
        int green = 0;
        int blue = 0;

        for (int i = y - radius; i < y + radius; i++) {
            for (int j = x; Math.pow((j - x), 2) + Math.pow((i - y), 2) <= Math.pow(radius, 2); j--) {
                int color = bitmap.getPixel(j, i);
                red += Color.red(color);
                green += Color.green(color);
                blue += Color.blue(color);
                counter++;
            }
            for (int j = x + 1; Math.pow((j - x), 2) + Math.pow((i - y), 2) <= Math.pow(radius, 2); j++) {
                int color = bitmap.getPixel(j, i);
                red += Color.red(color);
                green += Color.green(color);
                blue += Color.blue(color);
                counter++;
            }
        }
        red /= counter;
        green /= counter;
        blue /= counter;

        for (int i = y-radius, n = 0,k = radius*2; i < y+radius; i++,n--,k--) {
            Double iterX = (double) radius;
            int iterY =  (n>-radius)?Math.abs(n):k;

            for (int j = x; Math.pow((j-x),2) + Math.pow((i-y),2) <= Math.pow(radius,2); j--,iterX--) {
                double compromiseCoeff = compromise *  Math.pow((iterY +  iterX)/2  / radius,2);
                newBitmap.setPixel(j,i,Color.rgb(
                        findCompromise(red,0,j,i, compromiseCoeff,bitmap),
                        findCompromise(green,1,j,i, compromiseCoeff,bitmap),
                        findCompromise(blue,2,j,i, compromiseCoeff,bitmap)
                ));
            }
            iterX = (double) radius;
            for (int j = x+1;  Math.pow((j-x),2) + Math.pow((i-y),2) <= Math.pow(radius,2); j++,iterX--) {
                double compromiseCoeff = compromise * Math.pow((iterY +  iterX)/2  / radius,2);
                newBitmap.setPixel(j,i,Color.rgb(
                        findCompromise(red,0,j,i, compromiseCoeff,bitmap),
                        findCompromise(green,1,j,i, compromiseCoeff,bitmap),
                        findCompromise(blue,2,j,i, compromiseCoeff,bitmap)
                ));
            }
        }


        return newBitmap;
    }

    private static int findCompromise(int color, int type, int x, int y, Double compromise, Bitmap bitmap){


        if(type==0)
            return (int) (color * compromise + (1 - compromise) * Color.red(bitmap.getPixel(x, y)));
        else if(type==1)
            return (int) (color * compromise + (1 - compromise) * Color.green(bitmap.getPixel(x, y)));
        else
            return (int) (color * compromise + (1 - compromise) * Color.blue(bitmap.getPixel(x, y)));
    }

}
