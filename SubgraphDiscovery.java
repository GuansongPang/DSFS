/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package edu.uts.aai.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * This class is the main class of the DSFS algorithm, which consists of three components,
 * including intra-feature value outlierness computing (i.e., the delta function in our ICDM2016 paper),
 * the adjacent matrix of the feature graph, and dense subgraph discovery of the feature graph.
 * Note that we skip the value graph construction and go directly to construct the feature graph, 
 * which can speed up the algorithm a bit.
 * @author Guansong Pang 
 */
public class SubgraphDiscovery {
    
    private ArrayList<ArrayList<Double>> fMatrix = new ArrayList<ArrayList<Double>>();
    private ArrayList<Double> featIndWgts = new ArrayList<Double>();   //to store total weights for each feature
    private ArrayList<Integer> featStatus = new ArrayList<Integer>();   //to record whether the feature has been removed or not
    private ArrayList<Integer> featIndice = new ArrayList<Integer>();   //to record whether the feature has been removed or not
    
    
    /**
     * the main method for calling Charikar greedy, sequential backward and Las Vegas based
     * dense subgraph discovery
     * @param cpList the list of coupled centroids: each centroid contains the co-occurrence frequency of each value with other values
     * @return the IDs of features to be removed
     */
    public String denseSubgraphDiscovery(ArrayList<CoupledValueCentroid> cpList, int dataSize) {
        double[] fo = calcIntraFeatureWeight(cpList,dataSize);
//        adjacentMatrix(cpList);
        featureAdjacentMatrix(cpList,fo);
        ArrayList<String> discardFeats = new ArrayList<String>();
        double[] den = charikarGreedySearchforFeatGraph(discardFeats);
        double max = Double.MIN_VALUE;
        int maxID = -1;
        for(int i = 0; i < discardFeats.size(); i++) {
            if(den[i] > max) {
                max = den[i];
                maxID = i;
            }
        }
        System.out.println("MAX:"+(new DecimalFormat("#0.0000")).format(max)+" ");
        Plot.plotYPoints(den, 3, DSFS4ODUtils.dataSetName, DSFS4ODUtils.dataSetName, "Iteration", "Avg. Incoming Edge Weight");
        return discardFeats.get(maxID);
    }
    
    /**
     * to calculate the outlierness of each feature value based on the extent the value frequent deviating from the mode frequency
     * @param cpList the list of coupled centroids: each centroid contains the co-occurrence frequency of each value with other values
     * @param dataSize the number of instances in the data set
     */
    public double[] calcIntraFeatureWeight(ArrayList<CoupledValueCentroid> cpList, int dataSize) {
        int dim = cpList.size();
        double [] fo = new double[dim];
        double [] mFreq = new double[dim];
        for(int i = 0; i < cpList.size(); i++) {
            CoupledValueCentroid cp = cpList.get(i);
            int len = cp.getCenList().size();
            double maxFreq = 0;
            for(int j = 0; j < len; j++) {
                ValueCentroid cen = cp.getCenList().get(j);
                double globalFreq = cen.globalFreq(i, j);
                if(globalFreq > maxFreq)
                    maxFreq = globalFreq;
            }
            mFreq[i] = maxFreq;
            fo[i] = 0;
        }
        
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for(int i = 0; i < cpList.size(); i++) {
            CoupledValueCentroid cp = cpList.get(i);
            int len = cp.getCenList().size();
            int count = 0;
            for(int j = 0; j < len; j++) {
                ValueCentroid cen = cp.getCenList().get(j);
                double globalFreq = cen.globalFreq(i, j);
                if(globalFreq == 0) {
                    continue;
                }
                double intra;
                intra = (Math.abs(globalFreq-mFreq[i])+1.0/dataSize)/(mFreq[i]); //mode absolute difference based. '1.0/dataSize' is used to avoid zero outlierness
                cen.setIntraOD(intra);
                fo[i] = fo[i] + intra;
                count++;
            }
            if(fo[i]>max)
                max = fo[i];
            if(fo[i]<min)
                min = fo[i];
        }
        double interval = max - min;
        for(int i = 0; i < dim; i++) {
            fo[i] = (fo[i] - min)/interval;
//            System.out.print(fo[i]+" ");
        }
//        System.out.println();
        return fo;
    }
    /**
     * to generate the adjacent matrix for the FEATURE graph
     * @param cpList the list of coupled centroids: each centroid contains the co-occurrence frequency of each value with other values
     */
    public void featureAdjacentMatrix(ArrayList<CoupledValueCentroid> cpList,double[] fo) {
        int dim = cpList.size();
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for(int k = 0; k < dim; k++) {
            ArrayList<Double> col = new ArrayList<Double>();
            double fWgt=0;
//            int count = 0;
            for(int i = 0; i < cpList.size(); i++) {
                if(i==k) {
//                    col.add(fo[i]);
//                    fWgt += fo[i];
                    col.add(0.0);
                    continue;
                }
                double tmp = 0;
                CoupledValueCentroid cp = cpList.get(i);
                for(int j = 0; j < cp.getCenList().size(); j++) {
                    ValueCentroid cen = cp.getCenList().get(j);
                    if(cen.globalFreq(i, j) == 0)  //skip feature values that have no occurence
                        continue;
                    
                    FeatureInfo ai = cen.getAttrList().get(k);
                    FeatureInfo gai = cp.getGlobalCentroid().getAttrList().get(k);
                    int len = ai.NumofValue();
                    for(int l=0; l < len; l++) {
                        if (k==cen.getOrgFeat() && gai.value(l) != 0) { //skip zero-appearance values
                            continue;
                        }/**/
                        double freq = ai.value(l);
                        double gFreq = gai.value(l);
                        double cenFreq = cen.globalFreq(i, j);
                        if(cenFreq != 0 && gFreq != 0) { //skip zero-appearance values
                            double w = cen.getIntraOD() * cpList.get(k).getCenList().get(l).getIntraOD() * (freq*1.0/cenFreq) 
                                    + cen.getIntraOD() * cpList.get(k).getCenList().get(l).getIntraOD() * (freq*1.0/gFreq);                            
                            if(w > 0) {
//                                wlist.add(w);
                                tmp += w;
                            }
                        }
                    }
                }
                col.add(tmp);
                if(tmp>max)
                    max = tmp;
                if(tmp<min)
                    min = tmp;
                fWgt += tmp;
            }
            fMatrix.add(col);
            featStatus.add(1);
            featIndice.add(k+1);
        }
        double interval = max - min;
        ArrayList<Double> tmp = new ArrayList<Double>();
        int len = fMatrix.size();
        for(int i = 0; i < len; i++) {
            double d = 0;
            ArrayList<Double> col = fMatrix.get(i);
            for(int j = 0; j < len; j++) {
                if(i == j){
                    col.add(fo[j]);
                    d += fo[j];
                }
                double w = (col.get(j)-min)/interval;
                col.set(j, w);
                d +=w;
            }
            tmp.add(d);
        }
        featIndWgts = tmp;
    }
    
    /**
     * to search for the densest subgraph in indirected graphs by using Charikar's greedy method presented in the paper below
     * @incollection{charikar2000greedy,
     * title={Greedy approximation algorithms for finding dense components in a graph},
     *   author={Charikar, Moses},
     *   booktitle={Approximation Algorithms for Combinatorial Optimization},
     *   pages={84--95},
     *   year={2000},
     *   publisher={Springer}
     * }
     * @param discardFeats the list to store non-relevant feature ids
     * @return the density array that records all densities of all the subgraphs
     */
    public double[] charikarGreedySearchforFeatGraph(ArrayList<String> discardFeats) {
        int len = featIndWgts.size();
        int count = len;
        int id = 0;
        double[] den = new double[count];
        StringBuilder sb = new StringBuilder();
        System.out.print("Subgraph densities:");
        while(count > 0) {
            double density = 0;
            density = computeDensity(featIndWgts,count,sb);
            den[id++] = density;
            discardFeats.add(sb.toString());
//            System.out.println(sb.toString());
            double min = Double.MAX_VALUE;
            int mid = -1;
            for(int i = 0; i < featIndWgts.size(); i++) {
                double w = featIndWgts.get(i);
                if(w < min) {
                    min = w;
                    mid = i;
                }
            }
            removeOneFeature(mid);
            sb.append(featIndice.remove(mid)+",");
            count--;
            
        }
        return den;
    }
    
    
    /**
     * to remove one feature from the feature candidates
     * @param fid the id of the feature to be removed
     */
    public void removeOneFeature(int fid) {
        fMatrix.remove(fid);
        featIndWgts.remove(fid); //virtually remove the feature
//            double fWgt = featIndWgts.get(fid);
        for(int k = 0; k < fMatrix.size(); k++) {
            ArrayList<Double> col = fMatrix.get(k);
            double fWgt = featIndWgts.get(k);
            double w = col.remove(fid);
//                double w = col.get(fid);
            featIndWgts.set(k, fWgt-w);
        }
    }
    
    /**
     * to compute the subgraph density using feature level array-list <code>featIndWgts</code>, i.e., average weight per node
     * @param edgeWeights the total incoming edge weights of individual feature values
     * @param featNum the number of features left
     * @param sb to store the non-relevant feature ids
     * @return the subgraph density
     */
    public double computeDensity(ArrayList<Double> edgeWeights, int featNum, StringBuilder sb) {
        double density = 0;
        int len = edgeWeights.size();
        for(int i = 0; i < len; i++ ) {
            double w = edgeWeights.get(i);
            density += w;
        }
        density = density / featNum;
        System.out.print((new DecimalFormat("#0.0000")).format(density)+",");
        return density;
    }
    
    /**
     * to conduct actual feature selection and generate a new data set given a list of non-relevant feature indices
     * @param data the data set
     * @param str the non-relevant feature indices
     * @param path the path for storing the new data set
     * @param name  name of the data
     */
    public void featureSelection(Instances data, String str, String path, String name){
        
        BufferedWriter writer = null;
        Remove remove = new Remove();
        remove.setAttributeIndices(str);
        remove.setInvertSelection(false);
        try {
            remove.setInputFormat(data);
            Instances newData = Filter.useFilter(data, remove);
//            File newDir = new File(path+"\\"+name);
//            if(!newDir.exists())
//                newDir.mkdir();
            // System.out.print(String.format(fm, count)+",");
            int num = 0;
            if(str.split(",").length>0)
                num = data.numAttributes()-1-str.split(",").length;
            else
                num = data.numAttributes()-1;
            writer = new BufferedWriter(new FileWriter(path+"\\DSFS_"+name+"_"+(data.numAttributes()-1)+"to"+num+".arff"));
            writer.write(newData.toString());
            writer.flush();
            writer.close();
            remove.setInputFormat(data);
        } catch (Exception ex) {
            Logger.getLogger(SubgraphDiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
}
