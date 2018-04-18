package Algorithm;

import java.util.ArrayList;
import java.util.List;

public class kRSPResult {//由于算法可能要返回多个参数，这里用于作为结果返回
    public double costSum;
    public double delaySum;
    public List<List<Integer>> paths;
    kRSPResult(){
        costSum=0;
        delaySum=0;
        paths=new ArrayList<>();
    }
}
