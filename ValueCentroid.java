package edu.uts.aai.utils;

import java.util.ArrayList;
import java.util.HashMap;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This class capture the basic information of each value.
 * In categorical data, each value occurs with many other feature values. These co-occurrence
 * information can be treated as a centroid of the value.
 * @author Guansong Pang
 */
public class ValueCentroid {
    private int orgFeat = -1;
    private int valueIndex = -1;
    private int dim;
    private int size;
    private ArrayList<FeatureInfo> attrList = new ArrayList<>();
    private int mostCoupledFeat=-1;
    private double outlierDegree = 0;
    private double intraOD = -1;
    private double[] uncertainty;
    private double outdegree = 0;
    private int degree = 0;
    private String content = "";
    
    public ValueCentroid(int attrID, int valueID, Instances insts) {
        orgFeat = attrID;
        valueIndex = valueID;
        dim = insts.numAttributes()-1;
        size = insts.numInstances();
        for(int i = 0; i < dim; i++) {
            //if(i != attrID) {
            FeatureInfo ai = new FeatureInfo(i,insts.attribute(i).numValues());
            attrList.add(ai);
            //  }
        }
    }
    
    public ValueCentroid(Instances insts) {
        dim = insts.numAttributes()-1;
        size = insts.numInstances();
        uncertainty = new double[dim];
        for(int i = 0; i < dim; i++) {
            //if(i != attrID) {
            FeatureInfo ai = new FeatureInfo(i,insts.attribute(i).numValues());
            attrList.add(ai);
            uncertainty[i] = 0;
            //  }
        }
    }
    
    
    public void updateCentroid(Instance inst) {
        for(int i = 0; i < dim; i++) {
            FeatureInfo ai = attrList.get(i);
            int index = ((Double)inst.value(i)).intValue();
            ai.addFreq(index);
        }
    }
    
    
    public void generateGlobalCentroid(ValueCentroid localCentd) {
        FeatureInfo ai0 = localCentd.attrList.get(0);
        int setSize = ai0.NumofValue();
        for(int i = 0; i < dim; i++) {
            FeatureInfo ai1 = localCentd.attrList.get(i);
            FeatureInfo gai1 = attrList.get(i);
            int setSize1 = ai1.NumofValue();
            for (int id = 0; id < setSize1; id++ ) {
                double freq1 = ai1.value(id);
                gai1.addFreq(id, freq1);
            }
            double u = 0; // compute the normalised entropy
            if(setSize1 == 1) {
                setUncertainty(i, -u);
                continue;
            }
            for (int id = 0; id < setSize1; id++ ) {
                double gFreq = globalFreq(i,id);
                double p = gFreq / size;
                double tmp = 0;
                if(p!=0)
                    tmp = p * Math.log10(p);
                u +=  tmp / (Math.log10(setSize1)/Math.log10(2));
            }
            setUncertainty(i, -u);
        }
    }
    
    public double globalFreq(int attrID, int valueID) {
        FeatureInfo ai = attrList.get(attrID);
        return ai.value(valueID);
    }
    
    public void printCentroidInfo() {
        for(int i = 0; i < attrList.size(); i++) {
            FeatureInfo ai = attrList.get(i);
            ai.printAttributeInfo();
            System.out.print("##");
        }
        System.out.println("\r\n");
        for(int i = 0; i < dim; i++) {
            System.out.print(uncertainty[i]+",");
        }
        System.out.println("\r\n");
    }
    
    public int mostCoupledFeatureID() {
        return mostCoupledFeat;
    }
    
    public ArrayList<FeatureInfo> getAttrList() {
        return attrList;
    }
    
    public void setOutlierDegree ( double od ) {
        outlierDegree = od;
    }
    
    public void addOutlierDegree ( double od ) {
        outlierDegree += od;
    }
    
    public double getOutlierDegree () {
        return outlierDegree;
    }
    
    public void setIntraOD (double od) {
        intraOD = od;
    }
    
    public double getIntraOD () {
        return intraOD;
    }
    public int getOrgFeat() {
        return orgFeat;
    }
    
    public int getValueIndex() {
        return valueIndex;
    }
    
  
    public void setUncertainty(int index, double u) {
        uncertainty[index] = u;
    }
    
    public double getUncertainty(int index) {
        return uncertainty[index];
    }
    
    public void setOutdegree(double outdegree) {
        this.outdegree = outdegree;
    }
    
    public double getOutdegree() {
        return outdegree;
    }
    public void setDegree(int d) {
        this.degree = d;
    }
    public double getDegree() {
        return degree;
    }
    
    public String toString() {
        return orgFeat+"_"+valueIndex;
    }
    
    
    public void setCategoricalContent(String content) {
        this.content = content;
    }
    
    public String getCategoricalContent(){
        return this.content;
    }
}
