package org;

import ilog.cplex.IloCplex;
import myGraph.MyGraph;
import util.GraphType;

public interface AlgRuner {
    GraphType fileFilter(String fileName);
    int onGetStartPoint(final MyGraph oriGraph,int time,String fileName);
    int onGetSinkPoint(final MyGraph oriGraph,int time,String fileName);
    int onGetMaxComVertex(final MyGraph oriGraph,int time,String fileName);
    int onGetCSVName(final MyGraph oriGraph,String fileName);
}
