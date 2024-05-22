package com.tsu.vkkfilteringapp.spline;

import java.util.ArrayList;

public class CloseBSpline {
    private ArrayList<ArrayList<Float>> coordinateArray;

    private int size;
    private ArrayList<ArrayList<Float>> matrixBezier;
    Matrix methods = new Matrix();
    //Матриа Безье получаается перемножением Эрмитовой матрицы на форму Безье

    CloseBSpline(ArrayList<ArrayList<Float>> coordinateArray){

        this.coordinateArray = (ArrayList<ArrayList<Float>>) coordinateArray.clone();
        this.size = coordinateArray.size();

        addBasePoint();
        setBezierMatrix();
    }



    private void addBasePoint(){
        if(size>2)
            for(int i = 0 ;i < 3;i++)
                coordinateArray.add(coordinateArray.get(i));
    }

    private void setBezierMatrix(){
        matrixBezier = new ArrayList<>();

        matrixBezier.add(new ArrayList<>());
        matrixBezier.get(0).add(-1F);
        matrixBezier.get(0).add(3F);
        matrixBezier.get(0).add(-3F);
        matrixBezier.get(0).add(1F);

        matrixBezier.add(new ArrayList<>());
        matrixBezier.get(1).add(3F);
        matrixBezier.get(1).add(-6F);
        matrixBezier.get(1).add(3F);
        matrixBezier.get(1).add(0F);

        matrixBezier.add(new ArrayList<>());
        matrixBezier.get(2).add(-3F);
        matrixBezier.get(2).add(0f);
        matrixBezier.get(2).add(3f);
        matrixBezier.get(2).add(0f);

        matrixBezier.add(new ArrayList<>());
        matrixBezier.get(3).add(1f);
        matrixBezier.get(3).add(4f);
        matrixBezier.get(3).add(1f);
        matrixBezier.get(3).add(0f);

        matrixBezier = methods.multiplyByNumber(matrixBezier,   1F/6F);
    }

    private ArrayList<ArrayList<Float>> createBasisMatrix(float t){
        ArrayList<ArrayList<Float>> TMatrix = new ArrayList<>();
        TMatrix.add(new ArrayList<>());

        for(int i = 3; i >= 0;i--)TMatrix.get(0).add((float) Math.pow(t,i));

        return TMatrix;
    }

    public ArrayList<ArrayList<Float>> createIntermediateMatrix (int start){
        ArrayList<ArrayList<Float>> intermediateMatrix = new ArrayList<>();
        for(int i = 0;i<4;i++)
            intermediateMatrix.add(coordinateArray.get(start+i));
        intermediateMatrix = methods.multiplyByMatrix(matrixBezier,intermediateMatrix);
        return intermediateMatrix;
    }

    public ArrayList<ArrayList<Float>> getPoint(Float t,ArrayList<ArrayList<Float>> intermediateMatrix){
        return methods.multiplyByMatrix(createBasisMatrix(t),
                intermediateMatrix);

    }

}
