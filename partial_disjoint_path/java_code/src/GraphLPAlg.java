import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class GraphLPAlg {
    public class Edge
    {
        Edge()
        {

        }
        Edge(int source,int target,int weight,int cost)
        {
            this.source=source;
            this.target=target;
            this.weight=weight;
            this.cost=cost;
        }
        public int source;
        public int target;
        public int weight;
        public int cost;
    }
    public class DoubleDimEdges
    {
        public List<Edge>inEdges;
        public List<Edge>outEdges;
    }
    public class Graph
    {
        int nodeNumber;
        int edgeNumber;
        int nodes[];
        Map<Integer, DoubleDimEdges>edgesMap;
    }

    private String readJsonGraph(String fileName)
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

        }
        return graphData;
    }

    private Graph parseJsonToGraph(String jsonStr)
    {
        int id;
        int source;
        int target;
        int weight;
        int cost;
        int realSrc;
        int realTar;

        JSONObject jsonObject=new JSONObject(jsonStr);

        JSONArray  nodesArr=new JSONArray();
        nodesArr=jsonObject.getJSONArray("nodes");
        JSONArray  edgesArr=new JSONArray();
        edgesArr=jsonObject.getJSONArray("links");

        Graph graph=new Graph();
        graph.nodeNumber=nodesArr.length();
        graph.edgeNumber=nodesArr.length();



        //deal with the nodes
        graph.nodes=new int[graph.nodeNumber];
        for(int i=0;i<graph.nodeNumber;i++)
        {
            JSONObject tmpObject=nodesArr.getJSONObject(i);
            id=tmpObject.getInt("id");
            graph.nodes[i]=id;
        }


        //deal with the edges
        graph.edgesMap=new HashMap<>();
        for(int i=0;i<graph.nodeNumber;i++)
        {
                DoubleDimEdges doubleDimEdges=new DoubleDimEdges();
                doubleDimEdges.inEdges=new ArrayList<>();
                doubleDimEdges.outEdges=new ArrayList<>();
                graph.edgesMap.put(graph.nodes[i],doubleDimEdges);
        }
        for(int i=0;i<edgesArr.length();i++)
        {
            JSONObject tmpObject=edgesArr.getJSONObject(i);
            source=tmpObject.getInt("source");
            target=tmpObject.getInt("target");
            weight=tmpObject.getInt("weight");
            cost=tmpObject.getInt("cost");
            try {
                realSrc=graph.nodes[source];
                realTar=graph.nodes[target];
                DoubleDimEdges srcV = graph.edgesMap.get(realSrc);
                DoubleDimEdges tarV = graph.edgesMap.get(realTar);
                srcV.outEdges.add(new Edge(realSrc, realTar, weight, cost));
                tarV.inEdges.add(new Edge(realSrc, realTar, weight, cost));
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(source+"-->"+target);
            }
        }
        return graph;
    }

    public void getAlgLPFile()
    {

    }

    public  void test()
    {
        String graphData=readJsonGraph("./graph_data/json_graph.json");
        Graph graph=parseJsonToGraph(graphData);
    }

    public static  void main(String args[]){
        GraphLPAlg graphLPAlg=new GraphLPAlg();
        graphLPAlg.test();
    }
}
