package graphIO;

import myGraph.MyGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

public class GraphWriter {
    public static String graphFolder="./graph_data/";
    public static boolean saveGraphToJson(MyGraph myGraph,String graphFileName)
    {
        if(myGraph.graph==null)
        {
            System.out.println("要保存的图为空");
            return false;
        }
        try {
            FileWriter fileWriter = new FileWriter(new File(graphFolder + graphFileName));
            JSONObject jsonObject=new JSONObject();//整个json文件的json对象


            //加入整个图的附加属性
            JSONObject attrObject=new JSONObject();
            attrObject.put("S",myGraph.startPoint);
            attrObject.put("T",myGraph.sinkPoint);
            jsonObject.put("graph",attrObject);
            jsonObject.put("multigraph",myGraph.multiGraph);
            jsonObject.put("directed",true);

            //储存图的顶点
            JSONArray nodeArrObject=new JSONArray();
            Iterator vit=myGraph.graph.vertexSet().iterator();
            while(vit.hasNext()){
                int node=(int)vit.next();
                JSONObject nodeObject=new JSONObject();
                nodeObject.put("id",node);
                nodeArrObject.put(nodeObject);
            }
            jsonObject.put("nodes",nodeArrObject);

            //储存图的边及对应属性
            JSONArray edgeArrObject=new JSONArray();
            Iterator eit=myGraph.graph.edgeSet().iterator();
            while(eit.hasNext()){
                DefaultWeightedEdge edge=(DefaultWeightedEdge) eit.next();
                JSONObject edgeObject=new JSONObject();
                edgeObject.put("source",myGraph.graph.getEdgeSource(edge));
                edgeObject.put("target",myGraph.graph.getEdgeTarget(edge));
                edgeObject.put("weight",myGraph.graph.getEdgeWeight(edge));
                if(myGraph.costMap!=null){
                    edgeObject.put("cost",myGraph.costMap.get(edge));
                }
                edgeArrObject.put(edgeObject);
            }
            jsonObject.put("links",edgeArrObject);

            jsonObject.write(fileWriter);
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
