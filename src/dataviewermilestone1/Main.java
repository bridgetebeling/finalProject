package dataviewermilestone1;

import java.io.FileNotFoundException;

 
public class Main {
    public static void main(String[] args) throws FileNotFoundException {
    	String data = "data/GlobalLandTemperaturesByState.csv";
    	//String data = "data/sample.csv";
//        new DataOrganizer(data);
        NewDataOrganizer loadData = new NewDataOrganizer(data);
        PlotClass plot = new PlotClass(loadData);
    }
}

