package GraphIO;

import GraphStructure.MyGraph;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class GraphReader {
    final String graphDataFolder="./graph_data/";
    public String readJsonStr(String fileName){
        File file=new File(graphDataFolder+fileName);
        if(!file.exists()){
            System.err.println("图文件： "+fileName+" 不存在");
            return null;
        }else{
            try {
                FileInputStream inputStream=new FileInputStream(file);
                Long fileLength=file.length();
                byte bufffer[]=new byte[fileLength.intValue()];
                inputStream.read(bufffer);
                return new String(bufffer);
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }
    public MyGraph readGraph(String fileName){
        String jsonStr=readJsonStr(fileName);
        if(jsonStr==null)return null;
        else{
            JSONObject graphObj=new JSONObject(jsonStr);
            JSONArray nodeArr=graphObj.getJSONArray("nodes");
            JSONArray linkArr=graphObj.getJSONArray("links");

            MyGraph myGraph=new MyGraph();
            for(int i=0;i<nodeArr.length();i++)
            {
                JSONObject nodeObj=nodeArr.getJSONObject(i);
                int node=(int)nodeObj.getInt("id");
                myGraph.graph.addVertex(node);
            }
            for(int i=0;i<linkArr.length();i++)
            {
                JSONObject linkObj=linkArr.getJSONObject(i);
                int source=linkObj.getInt("source");
                int target=linkObj.getInt("target");
                int cost=linkObj.getInt("cost");
                int delay=linkObj.getInt("delay");
                myGraph.addNewEdge(source,target,cost,delay);
            }
            return myGraph;
        }
    }
}
