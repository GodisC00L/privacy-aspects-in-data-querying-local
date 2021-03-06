/* Class that reads BIG data set file and retrieves from it the necessary data */

/* CURRENT TEST FILE IS koln-pruned.tr
 * the format of the file is:
 * the time (with 1-second granularity),
 * the vehicle identifier,
 * its position on the two-dimensional plane (x and y coordinates in meters)
 * and its speed (im meters per second).*/

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
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
    WorkingWithDatasets(File path, String targetPath) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(path);
        if(!(new File(targetPath).exists())) {
            targetList = new PrintWriter(targetPath);
        }
        datasetScanner = new Scanner(inputStream);
        //randomVelocities_andCutSize(125);
    }

    Database getDB(long size) {
        if (this.db.getDb().isEmpty())
            this.db = createDB(size);
        return db;
    }

    private Database createDB(long size) {
        String[] splitted;
        DataFormat df;
        DecimalFormat df2 = new DecimalFormat("#.##");
        long total = 0;

        // Prepare file for writing
        if(targetList != null) {
            String sb = "Timestamp,X,Y,Particular Velocity,Avg Set Velocity,Attack Time for K: [ms]," +
                    "1,2,4,8,16,32,64,128,256,512,1024,2048,4096" + "\n";
            targetList.write(sb);
        }
        System.out.println("\nBuilding DB");
        while(datasetScanner.hasNextLine()) {
            String data = datasetScanner.nextLine();
            total += data.getBytes(StandardCharsets.UTF_8).length;
            splitted = data.split(" ");
            df = new DataFormat(Double.parseDouble(splitted[0]),
                    Integer.parseInt(splitted[1].contains("_") ? splitted[1].substring(13) : splitted[1]),
                    Double.parseDouble(splitted[2]),
                    Double.parseDouble(splitted[3]),
                    Double.parseDouble(df2.format(Double.parseDouble(splitted[4])))
            );
            db.addToDB(df);
            if(targetList != null)
                writeToTargetList(df);

            int precent = (int)((total * 100)/ size);
            printProgressBar(precent);
        }
        datasetScanner.close();
        if(targetList != null)
            targetList.close();
        db.balanceBST();
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
        System.out.println("Jumping 3500000 lines");

        for (int i = 0; i < 3500000; i++) {
            datasetScanner.nextLine();
            int precent = i / 3500000;
            printProgressBar(precent);
        }

        System.out.println("\nWrite File");
        while(datasetScanner.hasNextLine() && bytesToMeg(total) <= size) {
            //rand = ThreadLocalRandom.current().nextDouble(0, 30);
            inputLine = datasetScanner.nextLine();
            //splited = inputLine.split(" ");
            //splited[splited.length -1] =  String.format("%.2f", rand);
            //outputLine = String.join(" ", splited);
            //total += outputLine.getBytes(StandardCharsets.UTF_8).length;
            total += inputLine.getBytes(StandardCharsets.UTF_8).length;
            writer.println(inputLine);

            int precent = (int)((bytesToMeg(total) * 100) / size);
            printProgressBar(precent);
        }
        writer.close();
    }

    public static void printProgressBar(int percent){
        StringBuilder bar = new StringBuilder("[");
        for(int i = 0; i < 50; i++){
            if( i < (percent/2)){
                bar.append("=");
            } else if( i == (percent/2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }


        bar.append("]   ").append(percent).append("%     ");
        System.out.print("\r" + bar.toString());
    }

    private static final long  MEGABYTE = 1024L * 1024L;

    private static long bytesToMeg(long bytes) {
        return bytes / MEGABYTE ;
    }
}
