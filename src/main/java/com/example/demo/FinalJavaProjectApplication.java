package com.example.demo;

import org.apache.commons.csv.CSVFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import smile.data.measure.NominalScale;
import smile.data.vector.IntVector;
import smile.io.Read;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@RestController
public class FinalJavaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalJavaProjectApplication.class, args);
    }

    @GetMapping("/displayData")
    public List<String> Read_Display_Dataset() throws IOException {
        Table myTable = Table.read().csv("src/main/resources/Wuzzuf_Jobs.csv");
        List<String> ls = new ArrayList<String>();

        for(int i = 0 ; i<10; i++)
        {
            String l = myTable.row(i).toString();
            ls.add(l);
        }

        return ls;
    }

    @GetMapping("/displayStatistics")
    public List summary_statistics() throws IOException {
        Table myTable = Table.read().csv("src/main/resources/Wuzzuf_Jobs.csv");
        String Shape= myTable.shape().toString();
        String Structure= myTable.structure().toString();
        String Summary= myTable.summary().toString();
        List<String> list= new ArrayList<>();
        list.add(Shape);
        list.add(Structure);
        list.add(Summary);
        return list;
    }

    @GetMapping("/removeDuplicates")
    public List<String> removeNull_Duplicate_Values() throws IOException {
        Table myTable = Table.read().csv("src/main/resources/Wuzzuf_Jobs.csv");
        myTable.dropDuplicateRows();
        myTable.dropRowsWithMissingValues();
//        List<String> ls = new ArrayList<String>();

        List<String> ls = myTable.stream().map(String::valueOf).collect(Collectors.toList());
//        List<String> ls = myTable.columnNames().stream().map(String::toUpperCase).collect(Collectors.toList());

//        for(int i = 0 ; i< myTable.rowCount(); i++)
//        {
//            String l = myTable.row(i).toString();
//            ls.add(l);
//        }

        return ls;
    }

    private LinkedHashMap jobsmap(String col_name) throws IOException {
        Table myTable = Table.read().csv("src/main/resources/Wuzzuf_Jobs.csv");
        List<String> companyList = myTable.stringColumn(col_name).asList();
        Map<String, Integer> hm = new HashMap<String, Integer>();

        for (String i : companyList) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }

        LinkedHashMap<String, Integer> sortedMap =
                hm.entrySet().stream().
                        sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(10).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap;
    }

    @GetMapping("/mostDemanding/{col_name}")
    public List most_demanding(@RequestParam(value = "col_name",defaultValue = "Company") String col_name) throws IOException {
        HashMap<String, Integer> jobs_map = jobsmap(col_name);
        List<String> ls = new ArrayList<String>();

        for (Map.Entry<String, Integer> val : jobs_map.entrySet()) {
            ls.add(val.getKey() + " "
                    + ": " + val.getValue());
        }

        return ls;
    }

    public Map<String, Integer> skills (String col_name) throws IOException{
        Table myTable = Table.read().csv("src/main/resources/Wuzzuf_Jobs.csv");
        List<String> skillsList = myTable.stringColumn(col_name).asList();
        List<String> items_skillsList = Arrays.asList(skillsList.toString().split(","));  //make split because column of skills contain multi skills in one cell
        Set<String> skills = new HashSet<>(items_skillsList);

        Map<String,Integer> map= new HashMap<String, Integer>();
        for (String Sk: skills){map.put(Sk, Collections.frequency(items_skillsList, Sk));}

        return map;
    }

    @GetMapping("/printSkills/{col_name}")
    public List printSkills(@RequestParam(value = "col_name",defaultValue = "Skills") String col_name) throws IOException{
        Map<String, Integer> map = skills(col_name);
        List<String> ls = new ArrayList<String>();

        for (Map.Entry<String, Integer> val : map.entrySet()) {
            ls.add(val.getKey() + " "
                    + ": " + val.getValue());
        }

        return ls;
    }

    @GetMapping("/mostImportantSkills/{col_name}")
    public List mostImportantSkills(@RequestParam(value = "col_name",defaultValue = "Skills") String col_name) throws IOException{
        Map<String,Integer> map = skills(col_name);
        System.out.println("Most Important Skills");
        LinkedHashMap<String, Integer> sortedMap =
                map.entrySet().stream().
                        sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(10).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));

        List<String> ls = new ArrayList<String>();

        for (Map.Entry<String, Integer> val : sortedMap.entrySet()) {
            ls.add(val.getKey() + " "
                    + ": " + val.getValue());
        }

        return ls;
    }

    @GetMapping("/factorizeYears")
    public List factorize_years() throws IOException{
        Table myTable = Table.read().csv("src/main/resources/Wuzzuf_Jobs.csv");
        Map<Integer,Integer> yearsMap  =new HashMap<Integer,Integer>();
        StringColumn yearsExp = (StringColumn) myTable.column ("YearsExp");
        List<Number > yearValues = new ArrayList<Number> ();
        Integer index = 0;

        for (String v : yearsExp) {
            Integer temp = null;
            if (v.replaceAll("[^0-9]", "").equals("")){
                temp =0;
            }else{ temp = Integer.parseInt(v.replaceAll("[^0-9]", "")); }
            if (yearsMap.containsKey(temp)==false){
                yearsMap.put(temp,index);
                yearValues.add(index);
                index +=1;
            }
            else{ yearValues.add(yearsMap.get(temp)); }
        }
        DoubleColumn yearsColumn = DoubleColumn.create("factorized years", yearValues);
        myTable.addColumns (yearsColumn);
        List<String> ls = myTable.stream().map(String::valueOf).collect(Collectors.toList());

        return ls;
    }

    public smile.data.DataFrame ReadASSmileDateFrame(String CSVFile)
    {

        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',');
        smile.data.DataFrame dfSmile = null;

        try
        {dfSmile = Read.csv(CSVFile,format);}

        catch (IOException | URISyntaxException e)
        {e.printStackTrace();}

        return dfSmile;
    }


    public int[] ColFactorize(smile.data.DataFrame df, String col_name)
    {
        String[] values = df.stringVector(col_name).distinct().toArray(new String[]{});
        return df.stringVector(col_name).factorize(new NominalScale(values)).toIntArray();
    }


    public smile.data.DataFrame FactorizeData(smile.data.DataFrame df)
    {
        df = df.merge(IntVector.of("JobsFactorize", ColFactorize(df, "Title")));
        df = df.merge(IntVector.of("CompanyFactorize", ColFactorize(df, "Company")));
        return df;
    }

    public double[][] KmeanGraph(smile.data.DataFrame df)
    {
        df = FactorizeData(df);
        smile.data.DataFrame kmean = df.select("CompanyFactorize", "JobsFactorize");

        double[][] KMEAN= kmean.toArray();
        return KMEAN;

    }

    @GetMapping("/Kmeans")
    public double [][] Kmeans(){
        smile.data.DataFrame SimleDF= ReadASSmileDateFrame("src/main/resources/Wuzzuf_Jobs.csv");
        double [][] KMEANS = KmeanGraph(SimleDF);
        return KMEANS;
    }

}
