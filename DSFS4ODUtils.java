package edu.uts.aai.utils;

import weka.core.converters.ConverterUtils;
import weka.core.Instances;
import java.io.File;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class serves as the main class to run the DSFS algorithm. 
 * 
 * The only input parameter  * of the main function is the path of 
 * an ARFF data set or a folder that contains ARFF data sets.
 * 
 * @author Guansong Pang 
 *
 */

public class DSFS4ODUtils {
    
    private static Instances testInstances;
    private static Instances trainInstances;
    public static String[] dataSetFullNameList;
    public static String[] dataSetNameList;
    public static String dataFilename;
    public static String dataSetName;
    public static String dirPath= "X:\\Datasets\\nominaldata\\feature selection";
    
    private static String[][] resultList;
    private static double AUC;
    private static double timeofBuilding = 0;
    private static double timeofTesting = 0;
    
    
    public static void main(String[] args) throws Exception
    {
          String str = "X:\\Datasets\\nominaldata\\ICDM"; //path of an ARFF data set or a folder that contains ARFF data sets
        
        boolean dataFileIsDir = new File(str).isDirectory();
        if(dataFileIsDir)
            buildDataSetsPathList(str);
        else
            dataFilename = str;
        featureselectionOptions(dataFileIsDir);
    }
    
    /**
     * to invoke the feature selection method
     * @param flag boolean parameter used to determine whether we are trying to filter features in a data set or a folder of data sets.
     * @throws Exception
     */
    public static void featureselectionOptions(boolean flag) throws Exception {
        
        if(flag) {
            resultList = new String[dataSetFullNameList.length][7];
            for (int count = 0; count < dataSetFullNameList.length ; count++) //batch processing
            {
                dataFilename = dataSetFullNameList[count];
                dataSetName = dataFilename.substring(dataFilename.lastIndexOf("\\")+1,dataFilename.lastIndexOf("."));
                System.out.println(dataSetName+" ");
                trainInstances = readDataSet(dataFilename);
                testInstances = readDataSet(dataFilename);
                featureSubsetSelection();
            }
        } else { //for handling single data set
            System.out.print(dataFilename.substring(dataFilename.lastIndexOf("\\")+1)+" ");
            trainInstances = readDataSet(dataFilename);
            testInstances = readDataSet(dataFilename);
            featureSubsetSelection();            
        }
    }
    
    
    public static void featureSubsetSelection() {
        long begin = System.currentTimeMillis();
        CoupledValueCentroid cp = new CoupledValueCentroid();
        ArrayList<CoupledValueCentroid> cpList = new ArrayList<CoupledValueCentroid>();
        cpList = cp.initialCentroidList(trainInstances);
        cpList = cp.generateCoupledCentroids(cpList, trainInstances);
        cp.obtainGlobalCentroid(cpList, trainInstances);
        SubgraphDiscovery dsd = new SubgraphDiscovery();
        
        String remIDs = dsd.denseSubgraphDiscovery(cpList,trainInstances.numInstances());
        long end = System.currentTimeMillis();
        System.out.println("The removed features' ID(s):"+remIDs);
        dsd.featureSelection(trainInstances, remIDs, dirPath, dataSetName);
//        System.out.println(formatOutput((end-begin)/1000.0));        
    }
    
    
    /**
     * to store the file names contained in a folder
     * @param dataSetFilesPath the path of the folder
     */
    public static void buildDataSetsPathList(String dataSetFilesPath)
    {
        System.out.println(dataSetFilesPath);
        // dirPath = dataSetFilesPath;
        File filePath = new File(dataSetFilesPath);
        String[] fileNameList =  filePath.list();
        int dataSetFileCount = 0;
        for (int count=0;count < fileNameList.length;count++)
        {
            if (fileNameList[count].toLowerCase().endsWith(".arff"))
            {
                dataSetFileCount = dataSetFileCount +1;
            }
        }
        dataSetFullNameList = new String[dataSetFileCount];
        dataSetNameList = new String[dataSetFileCount];
        dataSetFileCount = 0;
        for (int count =0; count < fileNameList.length; count++)
        {
            if (fileNameList[count].toLowerCase().endsWith(".arff"))
            {
                dataSetFullNameList[dataSetFileCount] = dataSetFilesPath+"\\"+fileNameList[count];
                dataSetNameList[dataSetFileCount] = fileNameList[count].substring(0,fileNameList[count].lastIndexOf(".arff"));
                dataSetFileCount = dataSetFileCount +1;
            }
        }
    }
    
    /**
     * to read data from a specific file
     * @param dataSetFileFullPath the full path of the file
     */
    public static Instances readDataSet(String dataSetFileFullPath)
    {
        Instances instances;
        try
        {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataSetFileFullPath);
            instances =  source.getDataSet();
            instances.setClassIndex(instances.numAttributes() - 1);
        }
        catch (Exception e)
        {
            instances = null;
        }
        return instances;
    }
    
    public static void getDataSetInfo(String path) {
        buildDataSetsPathList(path);
        for(int i = 0; i < dataSetFullNameList.length; i++) {
            String fullname = dataSetFullNameList[i];
            Instances insts = readDataSet(fullname);
            int numAttrs = 0, catAttrs = 0, numOutliers = 0;
            double outlierRatio = 0;
            for(int j = 0 ; j < insts.numAttributes()-1; j++) {
                if(insts.attribute(j).isNumeric())
                    numAttrs++;
                if(insts.attribute(j).isNominal())
                    catAttrs++;
            }
            for(int k = 0; k < insts.numInstances(); k++) {
                if(insts.instance(k).value(insts.numAttributes()-1) == 0)
                    numOutliers++;
            }
            outlierRatio = numOutliers * 1.0 / insts.numInstances();
            System.out.println(dataSetNameList[i]+","+insts.numInstances()+","+insts.numAttributes()
                    +","+numAttrs+","+catAttrs+","+outlierRatio);
        }
    }
    
    /**
     * to format the output digit
     * @param outputValue the digit intend to output
     * @return the formated digit
     */
    public static String formatOutput(double outputValue)
    {
        DecimalFormat doubleFormat = new DecimalFormat("#0.0000");
        return doubleFormat.format(outputValue);
    }
}
