import GraphRandGen as grg
import networkx as nx
import numpy as np
def verify_RSP_result(graph,path,max_com_vertex):
    success=True
    cur_com_vertex=0
    node_number=graph.number_of_nodes()
    for i in range(len(path)-1):
        if graph.successors(path[i]).count(path[i+1])==0:
            print("edge not existed")
            success=False
            break
        cur_com_vertex+=graph[path[i]][path[i+1]]["cost"]
    if cur_com_vertex>max_com_vertex:
        success=False
    return success