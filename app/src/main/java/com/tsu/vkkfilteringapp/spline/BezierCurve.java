package com.tsu.vkkfilteringapp.spline;

import java.util.ArrayList;

public class BezierCurve {

    private ArrayList<Integer> arrayCoefficients;
    private ArrayList<ArrayList<Float>> coordinate;

    public BezierCurve(ArrayList<ArrayList<Float>> coordinate){
        this.coordinate = coordinate;
        createArrayCoefficients();
    }

    private void createArrayCoefficients(){
        arrayCoefficients = new ArrayList<>();
        int nFact = findFactorial(coordinate.size()-1);
        for(int i = 0;i<coordinate.size();i++)
        {
            arrayCoefficients.add(nFact/(findFactorial(i)*findFactorial(coordinate.size()-1-i)));
        }
    }
    private int findFactorial(int num){
        if( num==0)return 1;
        if( num==1)return 1;
        int newNum = 1;
        for(int i =1;i<=num;i++)
            newNum*=i;
        return newNum;
    }

    public ArrayList<Float> findCoordinate(Float t){
        float answerX = 0F;
        float answerY = 0F;
        for(int i = 0;i<this.coordinate.size();i++){
            Float tempCoefficient =(float) (this.arrayCoefficients.get(i)*Math.pow(t,i)*Math.pow((1-t),(this.coordinate.size()-1-i)));

            answerX+=tempCoefficient*this.coordinate.get(i).get(0);
            answerY+=tempCoefficient*this.coordinate.get(i).get(1);
        }
        ArrayList<Float> coordinate = new ArrayList<>();
        coordinate.add(answerX);
        coordinate.add(answerY);

        return coordinate;
    }
}
