package util;

import myGraph.MyGraph;

public interface GroupRuner {
    GraphType fileFilter(String fileName);
    boolean runOnSingleGraph(MyGraph oriGraph,String fileName);
}
