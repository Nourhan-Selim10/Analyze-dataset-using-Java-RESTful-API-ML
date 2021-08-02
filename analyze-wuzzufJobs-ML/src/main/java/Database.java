import com.univocity.parsers.csv.CsvFormat;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import smile.data.measure.NominalScale;
import smile.data.vector.IntVector;
import tech.tablesaw.aggregate.Summarizer;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import org.apache.commons.csv.CSVFormat;
import smile.io.Read;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.*;

public class Database {
    Table t;

    public void  Read_Display_Dataset(String path) throws IOException {
        this.t=Table.read().csv(path);
        System.out.println(this.t.first(10));
    }

    public void summary_structure() {
        System.out.println("\n"+this.t.structure());
        System.out.println("\n\n\t\t\t** summary **");
        System.out.println(this.t.summary());
    }

    public void removeNull_Duplicate_Values(){
        this.t=this.t.dropDuplicateRows();
        this.t= this.t.dropRowsWithMissingValues();

//        System.out.println(this.t.countBy("Company"));

    }

    private LinkedHashMap jobsmap(String col_name) {
        List<String> companyList = this.t.stringColumn(col_name).asList();
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


    public void mostDemanding(String col_name) {
        HashMap<String, Integer> jobs_map = jobsmap(col_name);
        System.out.println("\nthe most demanding "+ col_name);
        for (Map.Entry<String, Integer> val : jobs_map.entrySet()) {
            System.out.println(val.getKey() + " "
                    + ": " + val.getValue());
        }
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

    //  Qustion - 10 (print skills and how many each repeated)
    public Map<String, Integer> skills (String col_name){
        List<String> skillsList = this.t.stringColumn(col_name).asList();
        List<String> items_skillsList = Arrays.asList(skillsList.toString().split(","));  //make split because column of skills contain multi skills in one cell
        Set<String> skills = new HashSet<>(items_skillsList);

        Map<String,Integer> map= new HashMap<String, Integer>();
        for (String Sk: skills){map.put(Sk, Collections.frequency(items_skillsList, Sk));}

        return map;
       }
    public void printSkills(String col_name) {
        Map<String, Integer> map = skills(col_name);
        for (Map.Entry<String, Integer> val : map.entrySet()) {
            System.out.println(val.getKey() + " "
                    + ": " + val.getValue());
        }
    }

        //sorted the output from skills to find out the most important skills required
    public void mostImportantSkills(String col_name){
        Map<String,Integer> map = skills(col_name);
        System.out.println("Most Important Skills");
        LinkedHashMap<String, Integer> sortedMap =
                map.entrySet().stream().
                        sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(10).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
        for (Map.Entry<String, Integer> val : sortedMap.entrySet()) {
            System.out.println(val.getKey() + " "
                    + ": " + val.getValue());
        }

   }

   // Question 11 (Factorize the yearsExp feature and convert it to numbers in new col

    public void factorizeYears(){
        Map<Integer,Integer> yearsMap  =new HashMap<Integer,Integer>();
        StringColumn yearsExp = (StringColumn) this.t.column ("YearsExp");
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
        this.t.addColumns (yearsColumn);
    }



        public void pieChart_for_Companies() {
            HashMap<String, Integer> map =jobsmap("Company");

            //list of columns
            List<String> companyList =map.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            List<Integer> countList =map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());

            double total = countList.stream().mapToDouble(d ->d).sum();

            //create pie chart
            PieChart chart = new PieChartBuilder()
                    .width(1024).height(600).title("the top 10 demanding companies for jobs").build();

            // Customize Chart
            chart.getStyler().setCircular(false);
            double percentage;
           // add col to chart
            for (int i = 0; i < countList.size(); i++) {
                percentage = (countList.get(i) / total) * 100;
                chart.addSeries(companyList.get(i), percentage);
            }

            //display
           new SwingWrapper<PieChart>(chart).displayChart();
        }


    public void barChart (String col_name) {
        HashMap<String, Integer> map =jobsmap(col_name);

        //list of columns
        List<String> jobsList =map.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        List<Integer> countList =map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());



        //create bar chart
        CategoryChart chart = new CategoryChartBuilder()
                .width(1024).height(600).title("the top 10 demanding "+col_name)
                .xAxisTitle(col_name).yAxisTitle("count").build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setOverlapped(true);
        chart.getStyler().setHasAnnotations(true);
        //  add col to chart
                chart.addSeries( "Wuzzuf jobs",jobsList, countList);

                //display
                new SwingWrapper<>(chart).displayChart();
    }

        }





