package sensorqualitychecker;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Ushang-PC
 */
public class SensorQualityChecker {
    
    // Define the types of sensor input to be taken
    private static final String SENSOR_THERMOMETER = "thermometer";
    private static final String SENSOR_HUMIDITY = "humidity";
    
    // Define the offsets and output values for humidity sensor
    private static final double HUMIDITY_OFFSET = 1;
    private static final String HUMIDITY_OK = "OK";
    private static final String HUMIDITY_DISCARD = "discard";
    
    // Define the offsets and output values for thermometer sensor
    private static final double TEMPERATURE_OFFSET = 0.5;
    private static final double STANDARD_DEVIATION_LOW = 3;
    private static final double STANDARD_DEVIATION_HIGH = 5;
    private static final String THERMOMETER_ULTRA_PRECISE = "ultra precise";
    private static final String THERMOMETER_VERY_PRECISE = "very precise";
    private static final String THERMOMETER_PRECISE = "precise";
    
    // Declare the room temperature and humidity values
    public static double REFERENCE_TEMPERATURE = 0;
    public static double REFERENCE_HUMIDITY = 0;
    
    /**
     * Calculates and prints the accuracy of the sensor.
     * If sensorType is thermometer it is one of:
     * ultra precise, very precise, precise
     * If sensorType is humidity it is one of:
     * OK, discard
     * @param sensorType The type of the sensor. It is one of SENSOR_THERMOMETER or SENSOR_HUMIDITY
     * @param sensorName The name of the sensor as read from the input file
     * @param logValues  The array list of log values for that particular sensor
     */
    private static void calucateSensorAccuracy(String sensorType, String sensorName, ArrayList<Double> logValues) {
        
        if(sensorType.equals(SENSOR_THERMOMETER)){ //SENSOR TYPE IS HUMIDITY THERMOMETER 
            double mean = average(logValues);
            double stdDeviation = standardDeviation(mean, logValues); // Passing mean here so that it doesn't needs to            
                                                                      // be recalculated for standard deviation function
            if(mean > REFERENCE_TEMPERATURE - TEMPERATURE_OFFSET && 
                    mean < REFERENCE_TEMPERATURE + TEMPERATURE_OFFSET &&        // Case 1: Ultra Precise
                        stdDeviation < STANDARD_DEVIATION_LOW){
                System.out.println(sensorName+" "+THERMOMETER_ULTRA_PRECISE);
            }
            else if(mean > REFERENCE_TEMPERATURE - TEMPERATURE_OFFSET && 
                        mean < REFERENCE_TEMPERATURE + TEMPERATURE_OFFSET &&    // Case 2: Very Precise
                            stdDeviation < STANDARD_DEVIATION_HIGH)
                System.out.println(sensorName+" "+THERMOMETER_VERY_PRECISE);
            else
                System.out.println(sensorName+" "+THERMOMETER_PRECISE);         // Case 3: Precise
            
        }
        else{   //SENSOR TYPE IS HUMIDITY SENSOR            
            double avg = average(logValues);            
            if(avg > REFERENCE_HUMIDITY - HUMIDITY_OFFSET &&                    // Case 1: OK
                    avg < REFERENCE_HUMIDITY + HUMIDITY_OFFSET)
                System.out.println(sensorName+" "+HUMIDITY_OK);
            else
                System.out.println(sensorName+" "+HUMIDITY_DISCARD);            // Case 2: Discard
        }
    }
    
    /**
     * Calculates the average or mean of the values of the list
     * @param logValues The array list of log values for that particular sensor
     * @return The average of the log values
     */
    private static double average(ArrayList<Double> logValues){
        double sum = 0;
        for(double value : logValues){
            sum+=value;
        }
        double avg = sum/logValues.size();        
        return avg;
    }
    
    /**
     * Calculates the standard deviation of the values of the list
     * @param mean The mean or average of the log values
     * @param logValues The array list of log values for that particular sensor 
     * @return The standard deviation of the log values
     */
    private static double standardDeviation(double mean, ArrayList<Double> logValues){        
        double tempSum = 0;
        for(double val : logValues){
            tempSum += Math.pow((val-mean),2);            
        }        
        double variance = tempSum/Double.valueOf(logValues.size()-1);   // Using doube value here otherwise if 1>tempSum>0 
                                                                        // variance will be NaN. Also prevents divide by 0 exception 
        return Math.sqrt(variance);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Take input in form of a file
        int line = 0;                                           //Stores the line number being read from the fie
        try{
            FileReader file = new FileReader("logs.txt");
            Scanner scanner = new Scanner(file);            
            String sensorType="";                               //Stores the current sensor type ie thermometer or humidity
            String sensorName="xxxxxxxxxxx";                    //Stores the current sensor name
            ArrayList<Double> logValues = new ArrayList<>();    //Stores the log values for the current sensor
            while(scanner.hasNextLine()){                       //Accept input till it is available
                String ip = scanner.nextLine();                
                if(line == 0 && ip.contains("reference")){      //Check if its the first reference line
                    String[] references = ip.split(" ");        
                    REFERENCE_TEMPERATURE = Double.parseDouble(references[1]);  //Stores the room temperature
                    REFERENCE_HUMIDITY = Double.parseDouble(references[2]);     //Stores the humidty of the room                                                         
                }
                else{                                                       //Not the first line
                    if(ip.contains(sensorName)){                            //Take a reading
                        String[] logData = ip.split(" ");        
                        logValues.add(Double.parseDouble(logData[2]));                        
                    }
                    else if(ip.contains(SENSOR_THERMOMETER) || ip.contains(SENSOR_HUMIDITY)){     // Identify a sensor                    
                        if(line!=1){   // If its the first time identifying a sensor dont calculate the accuracy of the previous sensor
                            calucateSensorAccuracy(sensorType,sensorName,logValues);
                            logValues.clear();                          //Clear the values for next sensor
                        }                            
                        String[] sensorData = ip.split(" ");
                        sensorType = sensorData[0];                     //Store the current sensor type
                        sensorName = sensorData[1];                     //Store the current sensor name                        
                    }
                    else{
                        System.out.println("Invalid Input at line "+(line+1));      // Handling invalid input corner case            
                    }                    
                } 
                line++;
            }
            if(!logValues.isEmpty())
                calucateSensorAccuracy(sensorType, sensorName, logValues);  // Need to calculate for the last sensor
            if(line==0) 
                System.out.println("No input found");                       // Handling corner case for no input
            scanner.close();                                                // Close the buffer since no more input expected
        }
        catch(FileNotFoundException | NumberFormatException e){
            System.out.println("Invalid Input at line "+(line+1));          //Adding 1 since line starts at 0 in code
        }
    }        
}


