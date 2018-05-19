package Util;

import alg.KRSPAlgBaseOnDelay;
import graphIO.GraphReader;
import myGraph.MyGraph;

import java.io.File;
import java.util.List;

public class RunCodeGroup {
    private String filePath="./graph_data/";
    public RunCodeGroup(){}
    public void setFilePath(String path){
        filePath=path;
    }
    public void runGroup(GroupRuner runer){
        if(runer==null) return;
        File path=new File(filePath);
        if(!path.exists()||!path.isDirectory()){
            System.err.println("文件夹路径出错，请检查");
            return;
        }
        try{
            GraphReader graphReader=new GraphReader();
            File files[]=path.listFiles();
            for(int i=0;i<files.length;i++){
                File graphFile=files[i];
                GraphType graphType;
                if((graphType=runer.fileFilter(graphFile.getName()))!=GraphType.NoneGraph){
                    MyGraph oriGraph;
                    switch (graphType)
                    {
                        case JsonGraph:oriGraph=graphReader.readJsonGraph(graphFile.getAbsolutePath());break;
                        case SnapGraph:oriGraph=graphReader.readSnapGraph(graphFile.getAbsolutePath());break;
                        default:oriGraph=new MyGraph();
                    }
                    boolean runningFlag=runer.runOnSingeleGraph(oriGraph,graphFile.getName());
                    if(!runningFlag) break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
