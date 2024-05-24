package com.tsu.vkkfilteringapp.spline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CubicSpline {
    private final ArrayList<ArrayList<Float>> coordinateMatrix;
    private ArrayList<ArrayList<Float>> matrixEquation;
    private ArrayList<ArrayList<Float>> matrixAnswer;
    private ArrayList<ArrayList<Float>> matrixSLAE;
    private ArrayList<Float> columnFreeCoefficient;


    CubicSpline(ArrayList<ArrayList<Float>> coordinateMatrix){
        this.coordinateMatrix = coordinateMatrix;
        this.matrixEquation =createMatrix(coordinateMatrix.size()-1,5);
        this.columnFreeCoefficient = createArray( 3 * coordinateMatrix.size() - 4);
        this.matrixSLAE = createMatrix(3*coordinateMatrix.size() -4,3 * coordinateMatrix.size()-4);
        createFirstCondition();
        createSecondConditionFirstPart();
        createSecondConditionSecondPart();
        createThirdCondition();
        Matrix newMatrix =new Matrix();

        this.matrixAnswer = newMatrix.findSLAE(matrixSLAE,newMatrix.coverArrayToMatrix(columnFreeCoefficient));
        setCoefficientToEquation();

    }

    private ArrayList<Float> createArray(int size){
        ArrayList<Float> array = new ArrayList<>();
        for (int i = 0;i<size;i++)
            array.add(0.0F);
        return array;
    }

    private ArrayList<ArrayList<Float>> createMatrix(int height, int width){
        ArrayList<ArrayList<Float>> matrix = new ArrayList<>();
        for (int i =0;i<height;i++)
        {
            matrix.add(new ArrayList<Float>());
            for(int j =0;j <width;j++)
                matrix.get(i).add(0.0F);
        }
        return matrix;
    }

    //first condition : the splines must pass through the nodal points
    private void createFirstCondition(){
        for(int i = 0,j=0;i<coordinateMatrix.size() - 1;i++,j+=3) {
            Float temp = coordinateMatrix.get(i + 1).get(0) - coordinateMatrix.get(i).get(0);

            columnFreeCoefficient.set
                    (i,
                            coordinateMatrix.get(i + 1).get(1) - coordinateMatrix.get(i).get(1)) ;

            matrixSLAE.get(i).set
                    (j,
                            temp);
            if (i == 0) {
                matrixSLAE.get(i).set
                        (j + 1,
                                (float) Math.pow(temp, 2));
                j--;
                continue;
            }

            matrixSLAE.get(i).set
                    (j + 1,
                            (float) Math.pow(temp, 2));


            matrixSLAE.get(i).set
                    (j + 2,
                            (float) Math.pow(temp, 3));


        }

    }

    private void createSecondConditionFirstPart(){

        //first differential
        for(int i = coordinateMatrix.size() - 1,j=0,n = 0;i < 2*coordinateMatrix.size() - 3;i++,j+=3,n++){
            Float temp = coordinateMatrix.get(n+1).get(0) - coordinateMatrix.get(n).get(0);
            matrixSLAE.get(i).set
                    (j,
                            1.0F);
            if (n == 0) {
                matrixSLAE.get(i).set
                        (1,
                                (float) (3*Math.pow(temp, 2)));

                matrixSLAE.get(i).set
                        (2,
                                -1.0F);
                j--;
                continue;
            }

            matrixSLAE.get(i).set
                    (j + 1,
                            2*temp);


            matrixSLAE.get(i).set
                    (j + 2,
                            (float) (3*Math.pow(temp, 2)));

            matrixSLAE.get(i).set
                    (j + 3,
                            -1.0F);


        }

    }

    private void createSecondConditionSecondPart() {
        //second differential
        for(int i = 2*coordinateMatrix.size() - 3,j=0,n = 0;i < 3*coordinateMatrix.size() - 5;i++,j+=3,n++){

            Float temp = coordinateMatrix.get(n+1).get(0) - coordinateMatrix.get(n).get(0);
            if (n == 0) {
                matrixSLAE.get(i).set
                        (1,
                                6*temp);

                matrixSLAE.get(i).set
                        (3,
                                -2.0F);
                j--;
                continue;
            }

            matrixSLAE.get(i).set
                    (j + 1,
                            2.0F);


            matrixSLAE.get(i).set
                    (j + 2,
                            6*temp);

            matrixSLAE.get(i).set
                    (j + 4,
                            -2.0F);


        }
    }

    private void createThirdCondition() {

        //differential at the ends

        int n = coordinateMatrix.size() - 2;
        Float temp = coordinateMatrix.get(n + 1).get(0) - coordinateMatrix.get(n).get(0);

        matrixSLAE.get(matrixSLAE.size() - 1).set
                (matrixSLAE.size() - 2,
                        2.0F);

        matrixSLAE.get(matrixSLAE.size() - 1).set
                (matrixSLAE.size() - 1,
                        6 * temp);
    }

    private void setCoefficientToEquation(){
        for(int i = 0 ;i< matrixEquation.size();i++) {
            matrixEquation.get(i).set(
                    0,
                    coordinateMatrix.get(i).get(1));

            matrixEquation.get(i).set(
                    4,
                    coordinateMatrix.get(i).get(0));

            if (i == 0) {
                matrixEquation.get(i).set
                        (1,
                                matrixAnswer.get(0).get(0));
                matrixEquation.get(i).set
                        (3,
                                matrixAnswer.get(1).get(0));
                continue;
            }

            for (int j = 0; j < 3; j++) {

                matrixEquation.get(i).set
                        (j + 1,
                                matrixAnswer.get(i * 3 + j - 1).get(0));
            }

        }
    }

    public Float findY(Float x, int numEquation){
        Float temp = x - matrixEquation.get(numEquation).get(4);
        return (float) (matrixEquation.get(numEquation).get(0)+matrixEquation.get(numEquation).get(1)*(temp)
                        + matrixEquation.get(numEquation).get(2)*Math.pow(temp,2) + matrixEquation.get(numEquation).get(3)*Math.pow(temp,3));
    }
}