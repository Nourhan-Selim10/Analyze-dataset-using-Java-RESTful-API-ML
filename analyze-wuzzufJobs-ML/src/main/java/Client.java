import java.io.IOException;

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
    }
}
