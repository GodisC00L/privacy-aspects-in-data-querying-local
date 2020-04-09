import java.util.Scanner;

public class Client2D {

    private static final double Epsilon             = 0.00001;
    private static final int ParticularVehicle      = 0;
    private static final int SetOfVehicles          = 1;
    private static final int UnsuccessfulAttack      = -1;

    private static final int FULL_CLIENT_ATTACK     = 1;
    private static final int SINGLE_CLIENT_ATTACK   = 2;
    private static final int SET_K                  = 3;
    private static final int EXIT                   = -1;


    /* runSingleAttack2D inputs is the related server (srv), X and y coordinates of the target (target)
    and timestamp (timestamp).
    The function output is a Pair of the velocity and what this velocity represents.
    0 - represents Successful Attack: particular vehicle attack
    1 - represents Successful Attack: set of vehicles smaller than k avg attack
    -1 - represents Unsuccessful Attack.
    */
    private static Pair<Double,Integer> SingleTargetAttack2D(Server srv, Pair<Double,Double> target, double timestamp) {

        int k = srv.k;
        double xMin = srv.getDb().getMin_X();
        double xMax = srv.getDb().getMax_X();
        double yMin = srv.getDb().getMin_Y();
        double yMax = srv.getDb().getMax_Y();
        double yTarget = target.getP2();
        double xTarget = target.getP1();


        if (k == 1) {
            // Specific target = ((xTarget,yTarget),(xTarget,yTarget))
            return new Pair<>(srv.getAvgVelocity2D(new Pair<>(target, target), timestamp), ParticularVehicle);
        }

        // MidArea = ((xMin,yTarget),(xMax,yTarget))
        // Sm = average velocity of midArea
        Pair<Double, Double> point1 = new Pair<>(xMin, yTarget);
        Pair<Double, Double> point2 = new Pair<>(xMax, yTarget);
        Pair<Pair<Double, Double>, Pair<Double, Double>> area = new Pair<>(point1, point2);
        double Sm = srv.getAvgVelocity2D(area, timestamp);
        if (Sm >= 0){
            //TODO: run 1D Attack search for particular vehicle's speed
            return new Pair<>(Sm, ParticularVehicle);
        }

        // TopAreaIncludeMid = ((xMin,yTarget),(xMax,yMax))
        point2.setP2(yMax);
        area = new Pair<>(point1, point2);
        double St = srv.getAvgVelocity2D(area, timestamp);

        // TopArea = ((xMin,yTarget+epsilon),(xMax,yMax))
        point1.setP2(yTarget+Epsilon);
        area = new Pair<>(point1, point2);
        double St_ = srv.getAvgVelocity2D(area, timestamp);

        // BotAreaIncludeMid = ((xMin,yMin),(xMax,yTarget))
        point1.setP2(yMin);
        point2.setP2(yTarget);
        area = new Pair<>(point1, point2);
        double Sb = srv.getAvgVelocity2D(area, timestamp);

        // BotArea = ((xMin,yMin),(xMax,yTarget-epsilon))
        point2.setP2(yTarget-Epsilon);
        area = new Pair<>(point1, point2);
        double Sb_ = srv.getAvgVelocity2D(area, timestamp);

        // Area = ((xMin,yMin),(xMax,yMax))
        point2.setP2(yMax);
        area = new Pair<>(point1, point2);
        double Sn = srv.getAvgVelocity2D(area, timestamp);


        int n = srv.getNumOfVehicles(timestamp);

        double M = (n*(Sn*(St_+Sb_-St-Sb)+Sb*St-Sb_*St_))/((Sb_-St)*(Sb-St_));
        M = Math.round(M);
        if (M < k) {
            Sm = ((Sb_ * St_ - Sb * St) * Sn + (St - St_) * Sb * Sb_ + (Sb - Sb_) * St * St_) / (Sb * St + (St_ + Sb_ - St - Sb) * Sn - Sb_ * St_);
            Sm = Math.round(Sm * 100.0) / 100.0;
            return new Pair<>(Sm, M == 1 ? ParticularVehicle : SetOfVehicles);
        }
        return new Pair<>( -1.0, UnsuccessfulAttack);
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
                    //TODO
                }
                case SINGLE_CLIENT_ATTACK: {
                    srv.setK(defaultK);
                    System.out.println ("Enter X coordinate Y coordinate and timestamp");
                    double targetX = input.nextDouble();
                    double targetY = input.nextDouble();
                    int timestamp = input.nextInt();
                    Pair<Double,Integer> attackResults = SingleTargetAttack2D(srv, new Pair<>(targetX,targetY), timestamp);
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
