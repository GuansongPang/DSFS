package edu.uts.aai.utils;

import java.util.HashMap;

/**
 * This class stores the basic information for each feature.
 * @author Guansong Pang
 */
public class FeatureInfo {
    private int index;
    private double[] freqArray;
    private double[] wArray;
    
    public FeatureInfo(int i) {
        index = i;
    }
    
    
    public FeatureInfo(int i, int len) {
        index = i;
        freqArray = new double[len];
        wArray = new double[len];
        for(int k=0; k < freqArray.length; k++) {
            freqArray[k]=0.0;
            wArray[k]=0.0;
         //   uncertainty[k]=0;
        }
    }
    
    
    public void addFreq(int index) {
        freqArray[index]++;
    }
    
    public void setFreq(int index, double freq) {
        freqArray[index]=freq;
    }
    public void addFreq(int index, double freq) {
        freqArray[index]+=freq;
    }
    public double value(int index) {
        return freqArray[index];
    }
    
    public void setWeight(int index,double weight) {
        wArray[index]= weight;
    }   
    
    public double getWeight(int id) {
        return wArray[id];
    }
    
        
    public void addWeight(int index,double weight) {
        wArray[index]+= weight;
    } 
    
    public double weightValue(int index) {
        return wArray[index];
    }
    
    public int getIndex(){
        return index;
    }
    
    public int NumofValue() {
        return freqArray.length;
    }
    
    public int numNonZeroFreq() {
        int count=0;
        for(int i = 0; i < freqArray.length; i++) {
            if(freqArray[i]>=1)
                count++;
        }
        return count;
    }
    
    public void printAttributeInfo() {
        for(int i = 0; i < freqArray.length; i++) {
            System.out.print(freqArray[i]+",");
        }
        
    }
}
