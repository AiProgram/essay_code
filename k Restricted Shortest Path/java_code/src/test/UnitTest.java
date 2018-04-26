package test;

import algorithm.KRSPAlgBaseOnCost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnitTest {

    public static void main(String args[]){
        Integer pathArr1[]={1, 13, 20};
        Integer pathArr2[]={1, 21, 39, 20};
        Integer pathArr3[]={1, 0, 30, 37, 20};
        List<Integer> path1= Arrays.asList(pathArr1);
        List<Integer> path2= Arrays.asList(pathArr2);
        List<Integer> path3= Arrays.asList(pathArr3);

        List<List<Integer>>paths=new ArrayList<>();
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);

        Integer cycle[]={0, 14, 37, 30, 13, 1, 6, 37, 30, 0};
        List<Integer> cycleList=Arrays.asList(cycle);
        KRSPAlgBaseOnCost alg=new KRSPAlgBaseOnCost();
        List<List<Integer>> result= alg.cyclePathXor(cycleList,paths,40);
    }
}
