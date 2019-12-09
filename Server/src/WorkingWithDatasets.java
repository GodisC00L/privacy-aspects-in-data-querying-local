/* Class that reads BIG data set file and retrieves from it the necessary data */

/* CURRENT TEST FILE IS koln-pruned.tr
 * the format of the file is:
 * the time (with 1-second granularity),
 * the vehicle identifier,
 * its position on the two-dimensional plane (x and y coordinates in meters)
 * and its speed (im meters per second).*/

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;


class WorkingWithDatasets {

    private Scanner datasetScanner;
    private Database db = new Database();
    private PrintWriter targetList = null;

    /* Constructor for class */
    WorkingWithDatasets(String path, String targetPath) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(path);
        if(!(new File(targetPath).exists()))
            targetList = new PrintWriter(targetPath);
        datasetScanner = new Scanner(inputStream);
    }

    Database getDB() {
        if (this.db.getDb().isEmpty())
            this.db = createDB();
        return db;
    }

    private class DataFormat  {
        private double timestamp, x, y, velocity;
        private int carID;

        DataFormat(double timestamp, int carID, double x, double y, double velocity) {
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
            this.velocity = velocity;
            this.carID = carID;
        }

        @Override
        public String toString() {
            return "dataFormat{" +
                    "timestamp=" + timestamp +
                    ", x=" + x +
                    ", y=" + y +
                    ", velocity=" + velocity +
                    ", carID=" + carID +
                    '}';
        }
    }

    private Database createDB() {
        String[] splitted;
        DataFormat df;
        // Prepare file for writing
        if(targetList != null) {
            String sb = "Timestamp,X,Y,Velocity,Attack Time for K: ," +
                    "1,10,20,30,40,50,60,70,80,90,100" + "\n";
            targetList.write(sb);
        }
        while(datasetScanner.hasNextLine()) {
            splitted = datasetScanner.nextLine().split(" ");
            df = new DataFormat(Double.parseDouble(splitted[0]),
                    Integer.parseInt(splitted[1].contains("_") ? splitted[1].substring(13) : splitted[1]),
                    Double.parseDouble(splitted[2]),
                    Double.parseDouble(splitted[3]),
                    Double.parseDouble(splitted[4])
            );
            db.addToDB(df.x, df.velocity, df.timestamp);
            if(targetList != null)
                writeToTargetList(df);
        }
        datasetScanner.close();
        if(targetList != null)
            targetList.close();
        return db;
    }

    private void writeToTargetList(DataFormat df){
        targetList.write((df.timestamp + "," + df.x + "," + df.y + "\n"));
    }


    private void randomVelocities_andCutSize(int size) throws FileNotFoundException {
        String inputLine;
        String outputLine;
        String[] splited;
        double rand;
        long total=0;
        PrintWriter writer = new PrintWriter("Server/fixedVelocities_" + size + "_MB.txt");
        //PrintWriter writer = new PrintWriter("D:/fixedVelocities.txt");
        while(datasetScanner.hasNextLine() && bytesToMeg(total) <= size) {
            rand = ThreadLocalRandom.current().nextDouble(0, 30);
            inputLine = datasetScanner.nextLine();
            splited = inputLine.split(" ");
            splited[splited.length -1] =  String.format("%.2f", rand);
            outputLine = String.join(" ", splited);
            total += outputLine.getBytes(StandardCharsets.UTF_8).length;
            writer.println(outputLine);
        }
        writer.close();
    }

    private static final long  MEGABYTE = 1024L * 1024L;

    private static long bytesToMeg(long bytes) {
        return bytes / MEGABYTE ;
    }
}
