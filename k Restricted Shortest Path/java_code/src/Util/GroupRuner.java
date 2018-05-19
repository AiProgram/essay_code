package Util;

import myGraph.MyGraph;

public interface GroupRuner {
    GraphType fileFilter(String fileName);
    boolean runOnSingeleGraph(MyGraph oriGraph,String fileName);
}
