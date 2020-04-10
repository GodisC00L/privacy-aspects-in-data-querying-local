import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Client2D {

    private static final double Epsilon             = 0.00001;
    private static final int ParticularVehicle      = 0;
    private static final int SetOfVehicles          = 1;
    private static final int UnsuccessfulAttack     = -1;

    private static final int FULL_CLIENT_ATTACK     = 1;
    private static final int SINGLE_CLIENT_ATTACK   = 2;
    private static final int SET_K                  = 3;
    private static final int EXIT                   = -1;

    private static final int FORWARD                = 1;
    private static final int BACKWARD               = 2;

    private static final double DEFAULT_RESOLUTION  = 0.1;


    /** runSingleAttack2D inputs is the related server (srv), X and y coordinates of the target (target)
     * and timestamp (timestamp).
     * The function output is a Pair of the velocity and what this velocity represents.
     * 0 - represents Successful Attack: particular vehicle attack
     * 1 - represents Successful Attack: set of vehicles smaller than k avg attack
     * -1 - represents Unsuccessful Attack.
     */
    private static Pair<Double,Integer> singleTargetAttack2D(Server srv, Pair<Double,Double> target, double timestamp) {
        int k = srv.k;
        double xMin = srv.getDb().getMin_X();
        double xMax = srv.getDb().getMax_X();
        double yMin = srv.getDb().getMin_Y();
        double yMax = srv.getDb().getMax_Y();
        double yTarget = target.getP2();
        boolean failFlag = false;

        if (k == 1) {
            // Specific target = ((xTarget,yTarget),(xTarget,yTarget))
            return new Pair<>(srv.getAvgVelocity2D(new Pair<>(target, target), timestamp), ParticularVehicle);
        }

        Pair<Double, Double> point1 = new Pair<>(xMin, yTarget);
        Pair<Double, Double> point2 = new Pair<>(xMax, yTarget);
        Pair<Pair<Double, Double>, Pair<Double, Double>> area;

        // TopAreaIncludeMid = ((xMin,yTarget),(xMax,yMax))
        point2.setP2(yMax);
        area = new Pair<>(point1, point2);
        double St = srv.getAvgVelocity2D(area, timestamp);
        if(St < 0) return attack1D(srv, target, timestamp);

        // TopArea = ((xMin,yTarget+epsilon),(xMax,yMax))
        point1.setP2(yTarget+Epsilon);
        area = new Pair<>(point1, point2);
        double St_ = srv.getAvgVelocity2D(area, timestamp);
        if(St_ < 0) return attack1D(srv, target, timestamp);

        // BotAreaIncludeMid = ((xMin,yMin),(xMax,yTarget))
        point1.setP2(yMin);
        point2.setP2(yTarget);
        area = new Pair<>(point1, point2);
        double Sb = srv.getAvgVelocity2D(area, timestamp);
        if(Sb < 0) return attack1D(srv, target, timestamp);

        // BotArea = ((xMin,yMin),(xMax,yTarget-epsilon))
        point2.setP2(yTarget-Epsilon);
        area = new Pair<>(point1, point2);
        double Sb_ = srv.getAvgVelocity2D(area, timestamp);
        if(Sb_ < 0) return attack1D(srv, target, timestamp);

        // Area = ((xMin,yMin),(xMax,yMax))
        point2.setP2(yMax);
        area = new Pair<>(point1, point2);
        double Sn = srv.getAvgVelocity2D(area, timestamp);

        int n = srv.getNumOfVehicles(timestamp);

        final double v = Sn * (St_ + Sb_ - St - Sb);
        double M = (n*(v +Sb*St-Sb_*St_))/((Sb_-St)*(Sb-St_));
        M = Math.round(M);

        if (M < k) {
            double Sm = ((Sb_ * St_ - Sb * St) * Sn + (St - St_) * Sb * Sb_ + (Sb - Sb_) * St * St_)
                    / (Sb * St + v - Sb_ * St_);
            Sm = Math.round(Sm * 100.0) / 100.0;
            return new Pair<>(Sm, M == 1 ? ParticularVehicle : SetOfVehicles);
        } else if (M > k) {
            return attack1D(srv, target, timestamp);
        }
        return new Pair<>( -1.0, UnsuccessfulAttack);
    }

    private static boolean isInRange(Pair<Double, Double> range, Server srv){
        return  (range.getP1() >= srv.getDb().getMin_X() && range.getP2() <= srv.getDb().getMax_X());
    }

    /**
     *  return Pair<>(xLocation, sAvg)
     *  */
    private static Pair<Double,Double> getMinRange(Server srv, Pair<Double,Double> p1, Pair<Double,Double> p2,
                                                   double timestamp, int direction, double resolution) {
        Pair<Double, Double> pair = new Pair<>(p1.getP1(),p2.getP1());
        double sAvg = -1;
        while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity2D(new Pair<>(p1, p2), timestamp)) == -1)) {
            if(direction == FORWARD) {
                p2.setP1(p2.getP1() + 1);
                pair.setP2(p2.getP1());
            } else {
                p1.setP1(p1.getP1() - 1);
                pair.setP1(p1.getP1());
            }

        }
        // Check if there is a tighter range
        if(sAvg != -1 || (p2.getP1() > srv.getDb().getMax_X()) || (p1.getP1() < srv.getDb().getMin_X())) {
            if(direction == FORWARD) {
                p2.setP1(pair.getP1() - 1);
                pair.setP2(p2.getP1());
                while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity2D(new Pair<>(p1, p2), timestamp)) == -1)) {
                    p2.setP1(p2.getP1() + resolution);
                    pair.setP2(p2.getP1());
                }
            }
            else {
                p1.setP1(p1.getP1() + 1);
                pair.setP1(p1.getP1());
                while (isInRange(pair, srv) &&((sAvg = srv.getAvgVelocity2D(new Pair<>(p1, p2), timestamp)) == -1)) {
                    p1.setP1(p1.getP1() - resolution);
                    pair.setP1(p1.getP1());
                }
            }
        }
        if(direction == FORWARD)
            return new Pair<>(p2.getP1(), sAvg);
        return new Pair<>(p1.getP1(), sAvg);
    }

    private static double attack1DInner(Server srv, Pair<Double,Double> target, double timestamp,
                                        int direction, double resolution) {
        int k = srv.k;
        double xMin = srv.getDb().getMin_X();
        double xMax = srv.getDb().getMax_X();

        double xTarget = target.getP1();
        double yTarget = target.getP2();

        Pair<Double, Double> pair;

        Pair<Double, Double> point1 = new Pair<>(xTarget, yTarget);
        Pair<Double, Double> point2 = new Pair<>(xTarget, yTarget);

        if(direction == FORWARD) {
            // If we cant get for the Max range there is no point to check in incrementally
            point1.setP1(xTarget+Epsilon);
            point2.setP1(xMax);
            if(srv.getAvgVelocity2D(new Pair<>(point1, point2), timestamp) < 0) {
                return -1;
            }
            point2.setP1(point1.getP1() + Epsilon);
        } else {
            // If we cant get for the Min range there is no point to check in incrementally
            point1.setP1(xMin);
            point2.setP1(xTarget-Epsilon);
            if(srv.getAvgVelocity2D(new Pair<>(point1, point2), timestamp) < 0) {
                return -1;
            }
            point1.setP1(point2.getP1() - Epsilon);
        }

        pair = getMinRange(srv, point1, point2, timestamp, direction, resolution);

        // In case out of bounds
        if (pair.getP2() == -1) {
            return -1;
        }

        double xFinal = pair.getP1();
        double sAvg1 = pair.getP2();

        if (direction == FORWARD) {
            point1.setP1(xTarget);
            point2.setP1(xFinal);
        } else {
            point1.setP1(xFinal);
            point2.setP1(xTarget);
        }
        double sAvg2 = srv.getAvgVelocity2D(new Pair<>(point1, point2), timestamp);
        return (k+1)*sAvg2 - k*sAvg1;
    }

    private static Pair<Double, Integer> attack1D(Server srv, Pair<Double,Double> target, double timestamp) {
        double resolution = DEFAULT_RESOLUTION;
        double velocity1;
        double velocity2 = -1;

        velocity1 = attack1DInner(srv, target, timestamp, FORWARD, resolution);
        if (velocity1 < 0)
            velocity1 = attack1DInner(srv, target, timestamp, BACKWARD, resolution);

        do {
            resolution = resolution / 10;
            velocity2 = attack1DInner(srv, target, timestamp, FORWARD, resolution);
            if (velocity2 < 0) {
                velocity2 = attack1DInner(srv, target, timestamp, BACKWARD, resolution);
            }
            if(velocity1 == velocity2)
                break;
            velocity1 = velocity2;
        } while (resolution > DEFAULT_RESOLUTION / 10000);

        if(velocity2 == -1)
            return new Pair<>(velocity2, UnsuccessfulAttack);
        return new Pair<>(velocity2, ParticularVehicle);
    }

    private static void attackAllTargets(Server srv, int numOfTests, PrintWriter logFile, int numOfKTest) throws FileNotFoundException {
        String targetListPath = "Client/target.csv";
        FileInputStream inputStream = new FileInputStream(targetListPath);
        String attackedList = "Client/target_attacked_2D.csv";
        PrintWriter attackedFile = new PrintWriter(attackedList);
        Scanner scanner = new Scanner(inputStream);
        String[] splitted;
        boolean failFlag;

        // First line of headlines
        attackedFile.write(scanner.nextLine() + "\n");

        double xTarget, timestamp, yTarget, particularVelocity, setAvgVelocity;
        int test = 0;

        while(scanner.hasNextLine() && test != numOfTests) {
            failFlag = false;
            StringBuilder timeString = new StringBuilder();
            particularVelocity = -1;
            setAvgVelocity = -1;
            splitted = scanner.nextLine().split(",");
            timestamp = Double.parseDouble(splitted[0]);
            xTarget = Double.parseDouble(splitted[1]);
            yTarget = Double.parseDouble(splitted[2]);
            Pair<Double, Double> target = new Pair<>();

            for(int i = 0, j = 1; i < numOfKTest && !failFlag; i++, j<<=1) {
                Pair<Double, Integer> ans;
                int resultType;
                srv.setK(j);
                long startTime = System.nanoTime();

                target.setP1(xTarget);
                target.setP2(yTarget);

                ans = singleTargetAttack2D(srv, target, timestamp);
                resultType = ans.getP2();
                switch (resultType) {
                    case ParticularVehicle: {
                        particularVelocity = Math.round(ans.getP1() * 100.0) / 100.0;
                        timeString.append(ParticularVehicle + " - ").append((System.nanoTime() - startTime) / 1e6).append(",");
                        break;
                    }
                    case SetOfVehicles: {
                        setAvgVelocity = Math.round(ans.getP1() * 100.0) / 100.0;
                        timeString.append(SetOfVehicles + " - ").append((System.nanoTime() - startTime) / 1e6).append(",");
                        break;
                    }
                    case UnsuccessfulAttack: {
                        for(;i < numOfKTest; i++)
                            timeString.append("NaN").append(",");
                        failFlag = true;
                    }
                }
            }
            attackedFile.write(timestamp + "," + xTarget + ","
                    + yTarget + "," + particularVelocity + ","+ setAvgVelocity +",," + timeString + "\n");
            test++;
            int precent = (test * 100) / numOfTests;
            printProgressBar(precent);
        }
        scanner.close();
        attackedFile.close();
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
                    System.out.println ("Enter X coordinate Y coordinate and timestamp");
                    double targetX = input.nextDouble();
                    double targetY = input.nextDouble();
                    int timestamp = input.nextInt();
                    Pair<Double,Integer> attackResults =
                            singleTargetAttack2D(srv, new Pair<>(targetX,targetY), timestamp);
                    switch (attackResults.getP2()){
                        case ParticularVehicle:
                            System.out.println ("Target Speed: " + attackResults.getP1().toString());
                            break;
                        case SetOfVehicles:
                            System.out.println ("We could not find the target speed but we found a set of vehicles smaller than K=" +
                                    defaultK + " with average speed of " + attackResults.getP1());
                            break;
                        case UnsuccessfulAttack:
                            System.out.println ("We could not find the target speed, please try another target with a different y coordinate");
                            break;
                        default:
                            System.out.println ("Why we arrived HERE?");
                            break;
                    }
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
