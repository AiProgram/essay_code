package GraphIO;

import GraphStructure.MyGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

public class GraphWriter {
    final String graphDataFolder="./graph_data/";
    public GraphWriter(){
        checkFolder();//首先检查一下存储的目录是否存在，不存在就创建一个
    }
    public void checkFolder(){
        File file=new File(graphDataFolder);
        if(!file.exists()){
            try {
                file.mkdir();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public JSONObject MyGraphToJsonObj(MyGraph  myGraph){
        JSONObject graphObj=new JSONObject();
        JSONArray nodeArr=new JSONArray();
        JSONArray linkArr=new JSONArray();
        graphObj.put("multigraph",false);
        graphObj.put("directed",true);
        graphObj.put("nodes",nodeArr);
        graphObj.put("links",linkArr);

        //添加点的数据
        Iterator iterator=myGraph.graph.vertexSet().iterator();
        while(iterator.hasNext())
        {
            int node=(int)iterator.next();
            JSONObject nodeObj=new JSONObject();
            nodeObj.put("id",node);
            nodeArr.put(nodeObj);
        }
        //添加边的数据
        iterator=myGraph.graph.edgeSet().iterator();
        while(iterator.hasNext())
        {
            DefaultWeightedEdge edge=(DefaultWeightedEdge) iterator.next();
            int  source=myGraph.graph.getEdgeSource(edge);
            int  target=myGraph.graph.getEdgeTarget(edge);

            int cost=myGraph.costMap.get(edge);
            int delay=myGraph.delayMap.get(edge);
            JSONObject linkObj=new JSONObject();

            linkObj.put("cost",cost);
            linkObj.put("delay",delay);
            linkObj.put("source",source);
            linkObj.put("target",target);
            linkArr.put(linkObj);
        }
        return graphObj;
    }

    public void saveGraphToJson(MyGraph myGraph,String fileName){
        JSONObject graphObj=MyGraphToJsonObj(myGraph);
        try {
            File file = new File(graphDataFolder + fileName);
            FileWriter writer=new FileWriter(file);
            writer.write(graphObj.toString());
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
