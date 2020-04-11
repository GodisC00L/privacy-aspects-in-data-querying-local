import java.util.ArrayList;

public class DataArr extends ArrayList<DataFormat> {
    private boolean DBG = false;

    DataArr(DataFormat dataFormat) {
        this.add(dataFormat);
    }

    /**
     * Find the index of the closest lower value to target for upper bound
     * and index of the closest highest value to target for lower bound
     * @param: double target, boolean upper
     * @return index of closest lower value
     */
    public int closestNumber(double target, boolean upper) {
        int start = 0;
        int end = size() - 1;
        int mid;

        if(target < get(start).x)
            return start;
        if(target > get(end).x)
            return end;

        while (start + 1 < end) {
            mid = start + (end - start) / 2;
            if (get(mid).x == target) {
                return mid;
            } else if (mid - 1 >= 0 && get(mid - 1).x <= target && target < get(mid).x) {
                return !upper && get(mid - 1).x != target ? mid : mid - 1;
            } else if (mid + 1 < size() && get(mid).x < target && target <= get(mid + 1).x) {
                return upper && get(mid + 1).x != target ? mid : mid + 1;
            } else if (get(mid).x < target) {
                start = mid;
            } else {
                end = mid;
            }
        }
        if (DBG) {
            System.out.println("Start: " + get(start).x + ", index: " + start
            + "\nEnd: " + get(end).x + ", index: " + end
            + "\nTarget: " + target);
        }
        return (target - get(start).x) < (get(end).x - target) ? start : end;
    }

}


