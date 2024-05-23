package com.tsu.vkkfilteringapp.filters;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static androidx.core.graphics.ColorKt.toColor;

import static java.lang.Math.abs;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UnsharpMasking {
    private Double amount;
    private Bitmap img;
    private int[] imgArray;
    private int[] redArray;

    private int[] greenArray;
    private int[] blueArray;
    private int[] newImgArray;
    private Bitmap newImg;
    private List<Thread> threads;
    private final int countCores = new CheckCores().getNumberOfCores()-1;
    private int from;
    private boolean[] finish = new boolean[countCores];
    private int r ;
    private int num = 0;
    private int to ;
    public UnsharpMasking(Bitmap img,Double amount,int r){
        this.amount = amount;
        this.r = r;
        this.img = img;
        this.to = (img.getHeight()-2)/countCores;
        this.from = 1;
        imgArray = new int[img.getWidth()*img.getHeight()];
        img.getPixels(imgArray,0,img.getWidth(),0,0,img.getWidth(),img.getHeight());
        redArray = new int[img.getWidth()*img.getHeight()];
        greenArray = new int[img.getWidth()*img.getHeight()];
        blueArray = new int[img.getWidth()*img.getHeight()];
        convertedColor();
        newImgArray = imgArray.clone();

        blur();
        unsharp();
        //threads = createThreads();
    }

    private void check(){
        while (true) {
            boolean work = false;
            for(int i =0 ;i <threads.size();i++)
            {
                if(finish[i])work = true;
            }
            if(!work){
                for(int i =0 ;i <threads.size();i++)
                {
                    try {
                        threads.get(i).join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                return;
            }
        }
    }

    private void convertedColor(){
        for (int i =0 ;i < imgArray.length;i++){
            redArray[i] = (imgArray[i] >> 16) & 0xff;
            greenArray[i] = (imgArray[i] >>  8) & 0xff;
            blueArray[i] = (imgArray[i]     ) & 0xff;
        }
    }
    private void convertedColorLong(){
        for (int i =0 ;i < imgArray.length;i++){
            redArray[i] = Color.red(imgArray[i]);
            greenArray[i] = Color.green(imgArray[i]);
            blueArray[i] = Color.blue(imgArray[i]);
        }
    }
    private List<Thread> createThreads(){
        List<Thread> threads = new ArrayList<>();

        for (int i =0;i<countCores;i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    blur();
                }
            }));
            threads.get(i).start();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            to+=(img.getHeight()-2)/countCores;
            from+=(img.getHeight()-2)/countCores;
            num++;

            Log.i("unsharp",""+ num);

        }
        return  threads;
    }

    public void blur(){

        ArrayList<ArrayList<Double>> core = gaussianCore(r,-0.6);
        Log.i("unsharp",img.getHeight()+" : " +img.getWidth() );
        for(int i =r;i<img.getHeight()-r;i++){
            for(int j = r ;j<img.getWidth()-r;j++){
                int iter = i* img.getWidth() + j;
                double red = 0.0;
                double green = 0.0;
                double blue = 0.0;
                int sum = 0;
                for(int coreI = i -r,iCore = 0; coreI<i+r; coreI++,iCore++){
                    for (int coreJ = j-r,jCore = 0; coreJ<j+r; coreJ++,jCore++){
                        //Log.d("unsharp","coreI: "+ coreI);
                        //Log.d("unsharp","coreJ: "+ coreJ);
                        int coreIter = coreI*img.getWidth()+coreJ;
                        red+= core.get(iCore).get(jCore)*redArray[coreIter];
                        green+= core.get(iCore).get(jCore)*greenArray[coreIter];
                        blue+= core.get(iCore).get(jCore)*blueArray[coreIter];
                    }
                }

                newImgArray[iter] = Color.rgb((int)(red), (int) (green), (int) (blue));
            }
            //Log.i("unsharp","i: "+ i);
        }

    }

    private void blur2(){
        ArrayList<ArrayList<Double>> core = gaussianCore(r,-0.6);
        Log.i("unsharp",img.getHeight()+" : " +img.getWidth() );
        for(int i =r;i<img.getHeight()-r;i++){
            for(int j = r ;j<img.getWidth()-r;j++){
                int iter = i* img.getWidth() + j;
                double red = 0;
                double green = 0;
                double blue = 0;
                int sum = 0;
                for(int coreI =  -r; coreI<r; coreI++){
                    for (int coreJ = -r; coreJ<r; coreJ++){
                        int iCore = i+coreI;
                        int jCore = j+coreJ;
                        int coreIterator = iCore*img.getWidth()+jCore;
                        red+= redArray[coreIterator];
                        green+= greenArray[coreIterator];
                        blue+= blueArray[coreIterator];
                        sum++;
                    }
                }
                newImgArray[iter] =
                        Color.rgb((int) (red / sum), (int) (green / sum), (int) (blue / sum));
                //newImgArray[iter] = Color.rgb((int)(red), (int) (green), (int) (blue));
            }
            //Log.i("unsharp","i: "+ i);
        }
        if(isEqal())
            Log.e("unsharp","bluu");

    }


    private ArrayList<ArrayList<Double>> gaussianCore(int radius, Double sigma) {
        ArrayList<ArrayList<Double>> core = new ArrayList<>();
        int matrixSize = 2*radius+1;

        Double sum = 0.0;

        for(double i =0;i< matrixSize;i++) {
            core.add(new ArrayList<>());
            for (double j = 0; j < matrixSize; j++) {
                Double matrixI =  ((2 * i) / (matrixSize - 1) - 1);
                Double matrixJ =  ((2 * j) / (matrixSize - 1) - 1);
//                Double matrixI =  i-radius;
  //              Double matrixJ =  j-radius;
                core.get((int)i).add(gaussianFun(matrixI, matrixJ ,sigma));
                sum+=core.get((int)i).get((int)j);
            }
        }

        for(int i =0;i< matrixSize;i++)
            for (int j = 0; j < matrixSize; j++)
                core.get(i).set(j,core.get(i).get(j)/sum);
        Double s = 0.0;
        for(int i =0;i< matrixSize;i++)
            for (int j = 0; j < matrixSize; j++) {
                s += core.get(i).get(j);
                Log.e("unsharp",i+", "+j+": " + core.get(i).get(j));
            }
        Log.e("unsharp","" + s);
        return core;
    }

    private Double gaussianFun(Double x,Double y,Double sigma){
        Double numerator = Math.exp(-(x*x+y*y)/(2*sigma * sigma));
        //Log.i("unsharp", x+", "+y+") "+numerator);
        Double denominator =2*Math.PI*sigma;
        //Log.i("unsharp", ""+denominator);
        return numerator/denominator;
    }
    private void blurMultyThread(){
        int localTo = to;
        int localFrom = from;
        if(img.getWidth()-1 < from +(img.getHeight()-2)/countCores) {
            localTo = img.getWidth() - 1;
            Log.i("unsharp", "ok");
        }

        Log.i("unsharp", num + ") " + from+ " - " +to);
        for(int i = localFrom;i<localTo;i++){
            for(int j = 1;j<img.getWidth()-1;j++) {
                int iter = i * img.getWidth() + j;
                int red = 0;
                int green = 0;
                int blue = 0;
                int counter = 0;
                for (int k = i - r; k < i + r; k++)
                    for (int n = i - r; n < i + r; n++) {
                        int localIter = k * img.getWidth() + n;
                        red += redArray[localIter];
                        green += greenArray[localIter];
                        blue += blueArray[localIter];
                        counter++;
                    }
                newImgArray[iter] = Color.rgb(red/counter,green/counter,blue/counter);
            }
            Log.i("unsharp", num + ") " + i);
        }
        Log.i("unsharp","okk"+ num);
        newImg.setPixels(newImgArray,0,img.getWidth(),0,0,img.getWidth(),img.getHeight());
        Log.i("unsharp","okk"+ countCores);
    }

    private boolean isEqal(){
        for(int i = 0;i < newImgArray.length;i++)
            if(newImgArray[i]!=imgArray[i])return true;
        return false;
    }


    private void unsharp(){
        for(int i =0;i<img.getHeight();i++) {
            for (int j = 0; j < img.getWidth() ; j++) {
                int original = imgArray[i * img.getWidth() + j];
                int blur =     newImgArray[i * img.getWidth() + j];
                newImgArray[i * img.getWidth() + j] = Color.rgb(
                        toRange(255,0,abs((int) ((Color.red(original) + amount*(Color.red(original) -Color.red(blur))))))
                        ,toRange(255,0,abs((int)((Color.green(original) + amount*(Color.green(original) -Color.green(blur))))))
                        ,toRange(255,0,abs((int)((Color.blue(original) + amount*(Color.blue(original) -Color.blue(blur)))))));
                //newImgArray[i * img.getWidth() + j] = Color.rgb(
                //        abs((int)(Color.red(original) + amount*(Color.red(original) -Color.red(blur)))),
                //                abs((int)(Color.green(original) + amount*(Color.green(original) -Color.green(blur)))),
                //                        abs((int)(Color.blue(original) + amount*(Color.blue(original) -Color.blue(blur)))));
//
            }
        }
    }
    private int toRange(int max,int min, int number){
        if(number>max)return max;
        else return Math.max(number, min);
    }
    public Bitmap getNewImg(){
        newImg = Bitmap.createBitmap(img.getWidth(),img.getHeight(),ARGB_8888);
        newImg.setPixels(newImgArray,0,img.getWidth(),0,0,img.getWidth(),img.getHeight());
        return newImg;
    }

}
