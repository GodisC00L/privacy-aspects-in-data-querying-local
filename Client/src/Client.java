import java.text.DecimalFormat;
import java.util.Scanner;

public class Client {

    private static boolean isInRange(Pair<Double, Double> range, Server srv){
        return  (range.getP1() >= srv.getDb().getMin_X() && range.getP2() <= srv.getDb().getMax_X());
    }

    private static Pair<Double,Double> getMinRangeForward(Server srv, double xMin, double xMax, double timestemp){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) &&((sAvg = srv.getAvgVelocity(pair, timestemp))==-1)) pair.setP2(pair.getP2()+1);
        return new Pair<>(pair.getP2(),sAvg);
    }

    private static double AttackForward(Server srv, double vTarget, double timestemp){
        int k = srv.k;
        double xMin, xMax;
        Pair<Double,Double> pair = getMinRangeForward(srv,vTarget,vTarget,timestemp);
        if (pair.getP2() == -1) {
            System.out.println("Out of bounds");
            return -1;
        }
        xMax = pair.getP1();
        xMin = vTarget+1;
        pair = getMinRangeForward(srv,xMin,xMax,timestemp);
        if (pair.getP2() == -1) {
            System.out.println("Out of bounds");
            return -1;
        }
        double xFinal = pair.getP1();
        double sAvg1 = pair.getP2();
        pair.setP1(vTarget);
        pair.setP2(xFinal);
        double sAvg2 = srv.getAvgVelocity(pair,timestemp);
        return (k+1)*sAvg2 - k*sAvg1;
    }

    private static Pair<Double,Double> getMinRangeBackwards(Server srv, double xMin, double xMax, double timestemp){
        Pair<Double, Double> pair = new Pair<>(xMin,xMax);
        double sAvg = -1;
        while (isInRange(pair, srv) && ((sAvg = srv.getAvgVelocity(pair, timestemp))==-1)) pair.setP1(pair.getP1()-1);
        return new Pair<>(pair.getP1(),sAvg);
    }

    private static double AttackBackward(Server srv, double vTarget, double timestemp){
        int k = srv.k;
        double xMin, xMax;
        Pair<Double,Double> pair = getMinRangeBackwards(srv,vTarget,vTarget,timestemp);
        if (pair.getP2() == -1) {
            System.out.println("Out of bounds");
            return -1;
        }
        xMax = vTarget - 1;
        xMin = pair.getP1();
        pair = getMinRangeBackwards(srv,xMin,xMax,timestemp);
        if (pair.getP2() == -1) {
            System.out.println("Out of bounds");
            return -1;
        }
        double xFinal = pair.getP1();
        double sAvg1 = pair.getP2();
        pair.setP1(xFinal);
        pair.setP2(vTarget);
        double sAvg2 = srv.getAvgVelocity(pair,timestemp);
        return (k+1)*sAvg2 - k*sAvg1;
    }



    public static void main(String[] args) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        Server srv = new Server();
        while (true) {
            System.out.println("Enter target and timestamp");
            Scanner input = new Scanner(System.in);

            double vTarget = input.nextDouble(), timestemp = input.nextDouble();
            System.out.println("Start Attacking " + vTarget);
            long startTime = System.nanoTime();
            double sTarget = AttackForward(srv, vTarget, timestemp);
            if(sTarget == -1) {
                System.out.println("Going Backwards");
                sTarget = AttackBackward(srv,vTarget,timestemp);
                if (sTarget == -1) {
                    System.out.println("Number of elements in timestamp smaller then K");
                    continue;
                }
            }
            double attackTime = (System.nanoTime() - startTime) / 1e6;
            System.out.println("The speed of vTarget " + vTarget + " is " + df2.format(sTarget)
                    + "\nTime for attack is " + attackTime + "[ms]\n\n");
        }
    }
}
