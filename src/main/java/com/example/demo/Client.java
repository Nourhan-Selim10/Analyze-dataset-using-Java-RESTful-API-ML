package com.example.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import smile.clustering.*;
import smile.plot.swing.ScatterPlot;

public class Client {
    public static void main(String[] args) throws IOException {
        Database db = new Database();
        db.Read_Display_Dataset("src/main/resources/Wuzzuf_Jobs.csv");
        db.summary_structure();
        db.removeNull_Duplicate_Values();
        db.mostDemanding("Company");
        db.pieChart_for_Companies();
        db.mostDemanding("Title");
        db.barChart("Title");
        db.mostDemanding("Location");
        db.barChart("Location");
        db.printSkills("Skills");
        db.mostImportantSkills("Skills");
        db.factorizeYears();
        System.out.println(db.t.first(10)); // to show the factorized years column

        smile.data.DataFrame SimleDF= db.ReadASSmileDateFrame("analyze-wuzzufJobs-ML/src/main/resources/Wuzzuf_Jobs.csv");

        double [][] KMEANS = db.KmeanGraph(SimleDF);

        KMeans clusters = PartitionClustering.run(100, () -> KMeans.fit(KMEANS,3));

        try
        {ScatterPlot.of(KMEANS, clusters.y, '.').canvas().setAxisLabels("Companies", "Jobs").window();}

        catch (InvocationTargetException | InterruptedException e)
        {e.printStackTrace();}
    }
}
