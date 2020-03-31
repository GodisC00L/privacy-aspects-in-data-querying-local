import java.io.*;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Client {
    private static final int FULL_CLIENT_ATTACK     = 1;
    private static final int SINGLE_CLIENT_ATTACK   = 2;
    private static final int SET_K                  = 3;
    private static final int EXIT                   = -1;
    private static final double EPSILON             = 0.000001;


    private static final int FORWARD    = 1;
    private static final int BACKWARD   = 2;
    private static double knownMaxX = -1;
    private static double knownMinX = -1;

    private static final double DEFAULT_RESOLUTION   = 0.1;

    private static boolean isInRange(Pair<Double, Double> range, Server srv){
        return  (range.getP1() >= srv.getDb().getMin_X() && range.getP2() <= srv.getDb().getMax_X());
    }

    private static Pair<Double,Double> getMinRange(Server srv, double xMin, double xMax,
                                                   double timestamp, int direction, double resolution){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1)) {
            if(direction == FORWARD)
                pair.setP2(pair.getP2() + 1);
            else if (direction == BACKWARD)
                pair.setP1(pair.getP1() - 1);
        }
        // Check if there is a tighter range
        if(sAvg != -1 && srv.k != 1) {
            if(direction == FORWARD) {
                pair.setP2(pair.getP2() - 1);
                while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
                    pair.setP2(pair.getP2() + resolution);
            }
            else if (direction == BACKWARD) {
                pair.setP1(pair.getP1() + 1);
                while (isInRange(pair, srv) &&((sAvg = srv.getAvgVelocity(pair, timestamp)) == -1))
                    pair.setP1(pair.getP1() - resolution);
            }
        }
        if(direction == FORWARD)
            return new Pair<>(pair.getP2(), sAvg);
        return new Pair<>(pair.getP1(), sAvg);
    }

    private static double Attack(Server srv, double vTarget, double timestamp, int direction, double resolution){
        int k = srv.k;
        double xMin, xMax;
        Pair<Double, Double> pair;
        if(k == 1)
            return srv.getAvgVelocity(new Pair<>(vTarget, vTarget),timestamp);
        if(knownMaxX == -1) {
            knownMaxX = vTarget;
            knownMinX = vTarget;
        }
        if(direction == FORWARD) {
            // If we cant get for the Max range there is no point to check in incrementally
            if(srv.getAvgVelocity(new Pair<>(vTarget+EPSILON, srv.getDb().getMax_X()), timestamp) < 0) {
                return -1;
            }
            pair = getMinRange(srv, vTarget+EPSILON, knownMaxX, timestamp, direction, resolution);

        } else {
            // If we cant get for the Min range there is no point to check in incrementally
            if(srv.getAvgVelocity(new Pair<>(srv.getDb().getMin_X(), vTarget-EPSILON), timestamp) < 0) {
                return -1;
            }
            pair = getMinRange(srv, knownMinX, vTarget-EPSILON, timestamp, direction, resolution);

        }

        // In case out of bounds
        if (pair.getP2() == -1) {
            return -1;
        }

        double xFinal = pair.getP1();
        double sAvg1 = pair.getP2();
        if (direction == FORWARD) {
            pair.setP1(vTarget);
            pair.setP2(xFinal);
            knownMaxX = xFinal;
        } else {
            pair.setP1(xFinal);
            pair.setP2(vTarget);
            knownMinX = xFinal;
        }
        double sAvg2 = srv.getAvgVelocity(pair,timestamp);
        return (k+1)*sAvg2 - k*sAvg1;
    }

    private static void attackAllTargets(Server srv, int numOfTests, PrintWriter logFile, int numOfKTest) throws FileNotFoundException {
        String targetListPath = "Client/target.csv";
        FileInputStream inputStream = new FileInputStream(targetListPath);
        String attackedList = "Client/target_attacked.csv";
        PrintWriter attackedFile = new PrintWriter(attackedList);
        Scanner scanner = new Scanner(inputStream);
        String[] splitted;
        boolean failFlag;

        // First line of headlines
        attackedFile.write(scanner.nextLine() + "\n");

        double xTarget, timestamp, yTarget, velocity;
        int test = 0;
        double resolution;
        while(scanner.hasNextLine() && test != numOfTests) {
            failFlag = false;
            StringBuilder timeString = new StringBuilder();
            velocity = -1;
            splitted = scanner.nextLine().split(",");
            timestamp = Double.parseDouble(splitted[0]);
            xTarget = Double.parseDouble(splitted[1]);
            yTarget = Double.parseDouble(splitted[2]);

            // reset known xTarget
            knownMinX = -1;
            knownMaxX = -1;
            resolution = DEFAULT_RESOLUTION;
            for(int i = 0, j = 1; i < numOfKTest && !failFlag; i++, j<<=1) {
                double ans;
                srv.setK(j);
                long startTime = System.nanoTime();
                ans = Attack(srv, xTarget, timestamp,FORWARD,resolution);
                if(ans == -1) {
                    ans = Attack(srv, xTarget, timestamp,BACKWARD,resolution);
                }
                if (ans != -1)
                    timeString.append((System.nanoTime() - startTime) / 1e6).append(",");
                else {
                    for(;i < numOfKTest; i++)
                        timeString.append("NaN").append(",");
                    failFlag = true;
                }
                ans = Math.round(ans * 100.0) / 100.0;
                if(velocity == -1)
                    velocity = ans;
                if(velocity != ans && ans != -1) {
                    if(resolution < DEFAULT_RESOLUTION/10000){
                        logFile.write("Failed test for k = " + j + ", timestamp: "
                                + timestamp + ", xTarget: " + xTarget
                                + " Ans = " + ans + " Velocity = " + velocity + "\n");
                        continue;
                    }
                    resolution = resolution/10;
                    i--;
                }
            }
            attackedFile.write(timestamp + "," + xTarget + ","
                                + yTarget + "," + velocity + ",," + timeString + "\n");
            test++;
            int precent = (test * 100) / numOfTests;
            printProgBar(precent);
        }
        scanner.close();
        attackedFile.close();
    }

    public static void printProgBar(int percent){
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

    private static void runAttack(int numOfTests, Server srv) {
        try {
            PrintWriter logFile = new PrintWriter("Client/Target.log");
            long startTime = System.nanoTime();
            attackAllTargets(srv, numOfTests, logFile, 13);

            double attackTime = (System.nanoTime() - startTime) / 1e9;
            logFile.write("\nTotal attack time is: " + attackTime + "[sec]");
            System.out.println("\nTotal attack time is: " + attackTime + "[sec]");
            logFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void runSingleAttack(Server srv) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        System.out.println("Enter target and timestamp");
        Scanner input = new Scanner(System.in);

        double vTarget = input.nextDouble();


        double timestamp = input.nextDouble();
        System.out.println("Start Attacking " + vTarget);
        long startTime = System.nanoTime();
        double sTarget = Attack(srv, vTarget, timestamp, FORWARD, DEFAULT_RESOLUTION);
        if(sTarget == -1) {
            System.out.println("Going Backwards");
            sTarget = Attack(srv,vTarget,timestamp,BACKWARD, DEFAULT_RESOLUTION);
            if (sTarget == -1) {
                System.out.println("Number of elements in timestamp smaller then K");
                return;
            }
        }
        double attackTime = (System.nanoTime() - startTime) / 1e6;
        System.out.println("The speed of vTarget " + vTarget + " is " + df2.format(sTarget)
                + "\nTime for attack is " + attackTime + "[ms]\n\n");
    }


    public static void main(String[] args) {
        Server srv = new Server();
        int defaultK = srv.k;

        boolean running = true;
        while (running) {
            System.out.println("1. Full Target attack\n" +
                    "2. Single target attack\n" +
                    "3. Set K\n" +
                    "-1 Exit");
            Scanner input = new Scanner(System.in);

            int clientAns = input.nextInt();
            switch(clientAns) {
                case FULL_CLIENT_ATTACK: {
                    System.out.println("How many runs?");
                    runAttack(input.nextInt(), srv);
                    break;
                }
                case SINGLE_CLIENT_ATTACK: {
                    srv.setK(defaultK);
                    runSingleAttack(srv);
                    break;
                }
                case SET_K: {
                    System.out.println("Input k:");
                    defaultK = input.nextInt();
                    System.out.println("K set to: " + defaultK);
                    break;
                }
                case EXIT: {
                    System.out.println("GoodBye! :D");
                    running = false;
                    break;
                }
                default: {
                    System.out.println("WTF? " + clientAns);
                    break;
                }
            }
        }
    }
}
