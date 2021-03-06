package graphIO;

import myGraph.MyGraph;
import org.jgraph.graph.Edge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Random;

public class GraphReader {
    public MyGraph readJsonGraph(String fileName)
    {
        String graphData=null;
        String line;
        try {
            File jsonFile = new File(fileName);
            if (!jsonFile.exists())
            {
                System.out.println("cant find the json file ");
                return null;
            } else {
                FileReader reader = new FileReader(jsonFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder=new StringBuilder();
                while ((line=bufferedReader.readLine())!=null)
                {
                    builder.append(line);
                }
                graphData=builder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return parseJsonToGraph(graphData);
    }
    public MyGraph parseJsonToGraph(String jsonStr)
    {
        int id;
        int source;
        int target;
        int weight;
        int cost;
        int realSrc;
        int realTar;

        JSONObject jsonObject=new JSONObject(jsonStr);

        JSONArray nodesArr=new JSONArray();
        nodesArr=jsonObject.getJSONArray("nodes");
        JSONArray  edgesArr=new JSONArray();
        edgesArr=jsonObject.getJSONArray("links");


        MyGraph myGraph=new MyGraph();
        myGraph.nodeNum=nodesArr.length();
        JSONObject graphObject=jsonObject.getJSONObject("graph");
        myGraph.startPoint=graphObject.getInt("S");
        myGraph.sinkPoint=graphObject.getInt("T");
        myGraph.maxComVertex=graphObject.getInt("bound");

        //加入顶点
        for(int i=0;i<myGraph.nodeNum;i++)
        {
            JSONObject tmpObject=nodesArr.getJSONObject(i);
            id=tmpObject.getInt("id");
            myGraph.graph.addVertex(id);
        }


        //加入边
        for(int i=0;i<edgesArr.length();i++)
        {
            JSONObject tmpObject=edgesArr.getJSONObject(i);
            source=tmpObject.getInt("source");
            target=tmpObject.getInt("target");
            weight=tmpObject.getInt("weight");
            cost=tmpObject.getInt("cost");
            myGraph.addNewEdge(source,target,weight,cost);
        }

        myGraph.edgeNum=myGraph.graph.edgeSet().size();
        return myGraph;
    }

    /**
     * 读取SNAP图数据，并转换成自定义的图
     * @param fileName SNAP图文件名
     * @return 图
     */
    public MyGraph readSnapGraph(String fileName){
        MyGraph myGraph=new MyGraph();
        File snapFile=new File(fileName);
        if(!snapFile.exists()){
            System.err.println("文件"+snapFile+"不存在");
            return null;
        }
        try{
            Random random=new Random();
            String line;
            FileReader reader=new FileReader(snapFile);
            BufferedReader bufferedReader=new BufferedReader(reader);
            while((line=bufferedReader.readLine())!=null)
            {
                if(line.charAt(0)!='#') {
                    String strArr[]=line.split("\\s+");
                    if(strArr.length>=2){
                        int source=Integer.parseInt(strArr[0]);
                        int target=Integer.parseInt(strArr[1]);
                        myGraph.addNewEdgeWithNode(source,target,1,0);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        myGraph.nodeNum=myGraph.graph.vertexSet().size();
        myGraph.edgeNum=myGraph.graph.edgeSet().size();
        return myGraph;
    }
}
