package alg;

import java.util.ArrayList;
import java.util.List;

public class KRSPResult {//由于算法可能要返回多个参数，这里用于作为结果返回
    public double costSum;
    public double delaySum;
    public List<List<Integer>> paths;
    KRSPResult(){
        costSum=0;
        delaySum=0;
        paths=new ArrayList<>();
    }
}
