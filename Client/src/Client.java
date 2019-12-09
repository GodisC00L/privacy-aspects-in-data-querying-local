import java.io.*;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Client {

    private static final int FORWARD = 1;
    private static final int BACKWARD = 2;

    private static boolean isInRange(Pair<Double, Double> range, Server srv){
        return  (range.getP1() >= srv.getDb().getMin_X() && range.getP2() <= srv.getDb().getMax_X());
    }

    private static Pair<Double,Double> getMinRangeForward(Server srv, double xMin, double xMax, double timestemp){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) &&((sAvg = srv.getAvgVelocity(pair, timestemp))==-1)) pair.setP2(pair.getP2()+1);
        return new Pair<>(pair.getP2(),sAvg);
    }

    private static double Attack(Server srv, double vTarget, double timestemp, int direction){
        int k = srv.k;
        double xMin, xMax;
        Pair<Double, Double> pair;
        if(direction == FORWARD) {
            pair = getMinRangeForward(srv, vTarget, vTarget, timestemp);
        } else {
            pair = getMinRangeBackwards(srv,vTarget,vTarget,timestemp);
        }
        // In case out of bounds
        if (pair.getP2() == -1)
            return -1;
        if(k == 1)
            return pair.getP2();
        if(direction == FORWARD) {
            xMax = pair.getP1();
            xMin = vTarget + 1;
            pair = getMinRangeForward(srv, xMin, xMax, timestemp);
        } else {
            xMax = vTarget - 1;
            xMin = pair.getP1();
            pair = getMinRangeBackwards(srv,xMin,xMax,timestemp);
        }
        // In case out of bounds
        if (pair.getP2() == -1)
            return -1;

        double xFinal = pair.getP1();
        double sAvg1 = pair.getP2();
        if (direction == FORWARD) {
            pair.setP1(vTarget);
            pair.setP2(xFinal);
        } else {
            pair.setP1(xFinal);
            pair.setP2(vTarget);
        }
        double sAvg2 = srv.getAvgVelocity(pair,timestemp);
        return (k+1)*sAvg2 - k*sAvg1;
    }

    private static Pair<Double,Double> getMinRangeBackwards(Server srv, double xMin, double xMax, double timestemp){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestemp))==-1)) pair.setP1(pair.getP1()-1);
        return new Pair<>(pair.getP1(),sAvg);
    }

    static void attackAllTargets(Server srv, int numOfTests, PrintWriter logFile) throws FileNotFoundException {
        String targetListPath = "Client/fixedVelocities_20_MB_T1_target.csv";
        FileInputStream inputStream = new FileInputStream(targetListPath);
        String attackedList = "Client/fixedVelocities_20_MB_T1_target_attacked.csv";
        PrintWriter attackedFile = new PrintWriter(attackedList);
        Scanner scanner = new Scanner(inputStream);
        String[] splitted;
        DecimalFormat df2 = new DecimalFormat("#.##");

        // First line of headlines
        attackedFile.write(scanner.nextLine() + "\n");

        double xTarget, timestamp, yTarget, velocity;
        int test = 0;
        while(scanner.hasNextLine()) {
            StringBuilder timeString = new StringBuilder();
            velocity = 0;
            splitted = scanner.nextLine().split(",");
            timestamp = Double.parseDouble(splitted[0]);
            xTarget = Double.parseDouble(splitted[1]);
            yTarget = Double.parseDouble(splitted[2]);
            for(int i = 0, j = 1; i < 11; i++, j+=10) {
                double ans;
                srv.setK(j);
                if(j == 1)
                    j = 0;
                long startTime = System.nanoTime();
                ans = Attack(srv, xTarget, timestamp,FORWARD);
                if(ans == -1){
                    ans = Attack(srv, xTarget, timestamp,BACKWARD);
                }
                if (ans != -1)
                    timeString.append((System.nanoTime() - startTime) / 1e6).append(",");
                else
                    timeString.append("failed").append(",");
                ans = Math.round(ans * 100.0) / 100.0;
                if(velocity == 0 || velocity == -1)
                    velocity = ans;
                if(velocity != ans && ans != -1)
                    logFile.write("Failed test for k = " + j +", timestamp: "
                                        + timestamp + ", xTarget: " + xTarget
                                        + " Ans = " + ans + " Velocity = " + velocity + "\n");
            }
            attackedFile.write(timestamp + "," + xTarget + ","
                                + yTarget + "," + velocity + ",," + timeString + "\n");
            test++;
            if (test == numOfTests)
                break;
        }
        scanner.close();
        attackedFile.close();
    }

    public static void main(String[] args) {

        Server srv = new Server();


        try {
            PrintWriter logFile = new PrintWriter("Client/fixedVelocities_20_MB_T1_target_attacked.log");
            long startTime = System.nanoTime();
            attackAllTargets(srv, 1000, logFile);
            double attackTime = (System.nanoTime() - startTime) / 1e9;
            logFile.write("\nTotal attack time is: " + attackTime + "[sec]");
            System.out.println("Total attack time is: " + attackTime + "[sec]");
            logFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*while (true) {
            System.out.println("Enter target and timestamp, -1 for exit");
            Scanner input = new Scanner(System.in);

            double vTarget = input.nextDouble();
            if (vTarget == -1){
                System.out.println("Exiting...");
                break;
            }
            double timestemp = input.nextDouble();
            System.out.println("Start Attacking " + vTarget);
            long startTime = System.nanoTime();
            double sTarget = Attack(srv, vTarget, timestemp, FORWARD);
            if(sTarget == -1) {
                System.out.println("Going Backwards");
                sTarget = Attack(srv,vTarget,timestemp,BACKWARD);
                if (sTarget == -1) {
                    System.out.println("Number of elements in timestamp smaller then K");
                    continue;
                }
            }
            double attackTime = (System.nanoTime() - startTime) / 1e6;
            System.out.println("The speed of vTarget " + vTarget + " is " + df2.format(sTarget)
                    + "\nTime for attack is " + attackTime + "[ms]\n\n");
        }*/
    }
}
