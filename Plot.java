package edu.uts.aai.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This class is to visualise the subgraph density distribution
 * @author Guansong Pang
 */
public class Plot {
    
    public static void main(String[] args) {
        // Create a simple XY chart
        XYSeries series = new XYSeries("XYGraph");
        series.add(1, 1);
        series.add(1, 2);
        series.add(2, 1);
        series.add(3, 9);
        series.add(4, 10);
        // Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        // Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart(
                "XY Chart", // Title
                "x-axis", // x-axis Label
                "y-axis", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );
        
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);
        
        plot.setBackgroundPaint(Color.WHITE);
        try {
            File imageFile = new File("XYLineChart.png");
            int width = 640;
            int height = 480;
            ChartUtilities.saveChartAsPNG(imageFile, chart, width, height);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
    public static void plotYPoints(double [] y, int lineSize, String filename, String title, String xlabel, String ylabel) {
        
        XYSeries series = new XYSeries("XYGraph");
        for(int i=0; i <y.length; i++) {
            series.add(i+1, y[i]);
        }
        // Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        // Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, // Title
                xlabel, // x-axis Label
                ylabel, // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                false, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );
        
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesStroke(0,new BasicStroke(lineSize));
        plot.setRenderer(renderer);        
        plot.setBackgroundPaint(Color.WHITE);
        
        try {
            File imageFile = new File(filename+"_multi_SBS_FG_SL3.png");
            int width = 640;
            int height = 480;
            ChartUtilities.saveChartAsPNG(imageFile, chart, width, height);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
}
