import java.io.*;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Client {

    private static final int FORWARD = 0;
    private static final int BACKWARD = 1;

    private static boolean isInRange(Pair<Double, Double> range, Server srv){
        return  (range.getP1() >= srv.getDb().getMin_X() && range.getP2() <= srv.getDb().getMax_X());
    }

    private static Pair<Double,Double> getMinRange(Server srv, double xMin, double xMax, double timestamp, int dir){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1)) {
            if (dir == FORWARD) pair.setP2(pair.getP2() + 1);
            else pair.setP1(pair.getP1() - 1);
        }
        System.out.println("savg "+sAvg);
        if(sAvg != -1 && srv.k != 1) {
            if (dir == FORWARD) {
                pair.setP2(pair.getP2() - 1);
                while (isInRange(pair, srv) &&((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
                    pair.setP2(pair.getP2() + 0.1);
            }
            else {
                pair.setP1(pair.getP1() + 1);
                while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
                    pair.setP1(pair.getP1() - 0.1);
            }

        }
        if (dir == FORWARD) return new Pair<>(pair.getP2(),sAvg);
        else return new Pair<>(pair.getP1(),sAvg);
    }
    /*
    private static Pair<Double,Double> getMinRangeForward(Server srv, double xMin, double xMax, double timestamp, int dir){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
            pair.setP2(pair.getP2() + 1);
        // Check if there is a tighter range
        if(sAvg != -1 && srv.k != 1) {
            pair.setP2(pair.getP2() - 1);
            while (isInRange(pair, srv) &&((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
                pair.setP2(pair.getP2() + 0.1);
        }
        return new Pair<>(pair.getP2(),sAvg);
    }
    private static Pair<Double,Double> getMinRangeBackwards(Server srv, double xMin, double xMax, double timestamp){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
            pair.setP1(pair.getP1() - 1);
        // Check if there is a tighter range
        if(sAvg != -1 && srv.k != 1) {
            pair.setP1(pair.getP1() + 1);
            while (isInRange(pair, srv) &&((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
                pair.setP1(pair.getP1() - 0.1);
        }
        return new Pair<>(pair.getP1(),sAvg);
    }
     */

    private static double Attack(Server srv, double vTarget, double timestamp, int direction){
        int k = srv.k;
        double xMin, xMax;
        Pair<Double, Double> pair;
        if(direction == FORWARD) {
            pair = getMinRange(srv, vTarget, vTarget, timestamp, FORWARD);
            System.out.println(vTarget+ " " +pair.getP1()+ " " +srv.k);
        } else {
            pair = getMinRange(srv,vTarget,vTarget,timestamp, BACKWARD);
            System.out.println(vTarget+ " " +pair.getP1());
        }
        // In case out of bounds
        if (pair.getP2() == -1)
            return -1;
        if(k == 1)
            return pair.getP2();
        if(direction == FORWARD) {
            xMax = pair.getP1();
            xMin = vTarget + 0.1;
            pair = getMinRange(srv, xMin, xMax, timestamp, FORWARD);
        } else {
            xMax = vTarget - 0.1;
            xMin = pair.getP1();
            pair = getMinRange(srv,xMin,xMax,timestamp, BACKWARD);
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
        double sAvg2 = srv.getAvgVelocity(pair,timestamp);
        return (k+1)*sAvg2 - k*sAvg1;
    }

    private static void attackAllTargets(Server srv, int numOfTests, PrintWriter logFile) throws FileNotFoundException {
        String targetListPath = "Client/fixedVelocities_10_MB_target.csv";
        FileInputStream inputStream = new FileInputStream(targetListPath);
        String attackedList = "Client/fixedVelocities_10_MB_target_attacked.csv";
        PrintWriter attackedFile = new PrintWriter(attackedList);
        Scanner scanner = new Scanner(inputStream);
        String[] splitted;
        Boolean failFlag = false;

        // First line of headlines
        attackedFile.write(scanner.nextLine() + "\n");

        double xTarget, timestamp, yTarget, velocity;
        int test = 0;
        while(scanner.hasNextLine()) {
            failFlag = false;
            StringBuilder timeString = new StringBuilder();
            velocity = 0;
            splitted = scanner.nextLine().split(",");
            timestamp = Double.parseDouble(splitted[0]);
            xTarget = Double.parseDouble(splitted[1]);
            yTarget = Double.parseDouble(splitted[2]);
            for(int i = 0, j = 1; i < 21 && !failFlag; i++, j+=10) {
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
                else {
                    for(;i < 21; i++)
                        timeString.append("failed").append(",");
                    failFlag = true;
                }
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
        DecimalFormat df2 = new DecimalFormat("#.##");
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
            double timestamp = input.nextDouble();
            System.out.println("Start Attacking " + vTarget);
            long startTime = System.nanoTime();
            double sTarget = Attack(srv, vTarget, timestamp, FORWARD);
            if(sTarget == -1) {
                System.out.println("Going Backwards");
                sTarget = Attack(srv,vTarget,timestamp,BACKWARD);
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
