
package edu.uts.aai.utils;

import java.util.ArrayList;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This class captures the interactions or couplings between feature values.
 * 
 * @author Guansong Pang
 */
public class CoupledValueCentroid {
    
    private int orgFeat; // index of the dedicated feature
    private ArrayList<ValueCentroid> cenList = new ArrayList<>();   // to store centroids for a list of values in a feature, one centroid per value
    private static ValueCentroid globalCentroid; //to store frequency distribution of all the feature values
    
    /**
     * to allocate space for the centroids
     * @param data a given data set
     * @return list of centroids with empty contents
     */
    public ArrayList<CoupledValueCentroid> initialCentroidList (Instances data) {
        ArrayList<CoupledValueCentroid> cpList = new ArrayList<>();
        int d = data.numAttributes()-1;
        for (int i = 0; i < d; i++) {
            CoupledValueCentroid cp = new CoupledValueCentroid();
            cp.orgFeat = i;
            int card = data.attribute(i).numValues();
            //System.out.print(card+",");
            for(int k = 0; k < card; k++ ) {
                ValueCentroid cen = new ValueCentroid(i,k,data);
                String str = data.attribute(i).value(k);
                cen.setCategoricalContent(str);
                cp.cenList.add(cen);
            }
            cpList.add(cp);
        }
        return cpList;
    }
    
    /**
     * to generate a centroid for each feature value
     * @param cpList a empty list to save all the centroids
     * @param data a given data set
     * @return list of filled centroids
     */
    public ArrayList<CoupledValueCentroid> generateCoupledCentroids(ArrayList<CoupledValueCentroid> cpList, Instances data) {
        int size = data.numInstances();
        for(int i = 0; i < cpList.size(); i++) {
            CoupledValueCentroid cp = cpList.get(i);
            for(int j = 0; j < data.numInstances(); j++) {
                Instance inst = data.instance(j);
                int index = ((Double)inst.value(i)).intValue();
                ValueCentroid cen = cp.cenList.get(index);
                //System.out.println(j);
                cen.updateCentroid(inst);
            }
        }
        return cpList;
    }
    
    
    /**
     * to print out the centroid information of each value a given feature
     * @param cpList list of all the centroids
     * @param attrID a given feature id
     */
    public void printCoupledPatterns(ArrayList<CoupledValueCentroid> cpList,int attrID) {
        CoupledValueCentroid cp = cpList.get(attrID);
        for(int i = 0; i < cp.cenList.size(); i++) {
            ValueCentroid cen = cp.cenList.get(i);
            System.out.print("No."+i+":");
            cen.printCentroidInfo();
            System.out.println();
        }
    }
    
    
    /**
     * to obtain frequency distribution of all the feature values
     * @param cpList list of all the centroids
     * @param data a given data set
     */
    public void obtainGlobalCentroid(ArrayList<CoupledValueCentroid> cpList, Instances data) {
        globalCentroid = new ValueCentroid(data);// only one global centroid is needed
        CoupledValueCentroid cp = cpList.get(0);
        int len = cp.cenList.size();
        for(int j = 0; j < len; j++) {
            ValueCentroid cen = cp.cenList.get(j);
            globalCentroid.generateGlobalCentroid(cen);
        }
        // globalCentroid.printCentroidInfo();
    }
    
    /**
     *
     * @return the list of coupled centroids
     */
    public ArrayList<ValueCentroid> getCenList(){
        return this.cenList;
    }
    
    /**
     *
     * @return the frequencies of all feature values
     */
    public ValueCentroid getGlobalCentroid() {
        return globalCentroid;
    }
    
}
