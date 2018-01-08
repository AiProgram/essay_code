#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import networkx as nx
import copy
import matplotlib.pyplot as plt
def get_residual_graph(start_point_num=0,des_point_num=0,graph=None,debug=False):
    #find original shortest path
    node_num=0
    edge_num=0
    if graph is None:
        return None
    else:
        node_num=graph.number_of_nodes()
        edge_num=graph.number_of_edges()
    try:
        shortest_path=nx.shortest_path(graph,start_point_num,des_point_num,"weight")
    except :
        print(" the source point is unreachable ")
        return 
    if debug is True:
        print("original shortest path:\t"+str(shortest_path))

    #generate splitted residual graph

    #Add to G' each edge in G' that does not belong to P∗
    residual_graph=copy.deepcopy(graph)
    for p in shortest_path:
        if p is not start_point_num and p is not des_point_num:
            residual_graph.remove_node(p)
    # For each edge e ∈G' Set c(e) := 0
    for s,t in residual_graph.edges():
        residual_graph.add_edge(s,t,cost=0)
    
    #For each interior vertex v ∈ P∗ \ {s, t} add v1,v2 with its weight and cost
    for p in shortest_path:
        if p is not start_point_num and p is not des_point_num:
            p1=p+node_num
            p2=p+2*node_num
            residual_graph.add_edge(p1,p2,weight=0,cost=1)
            residual_graph.add_edge(p2,p1,weight=0,cost=0)
    
    #
    for u,v in graph.edges():
        w=graph[u][v]["weight"]
        # e = (u, v) ∈ P∗
        if shortest_path.count(u)>0 and shortest_path.count(v)>0 and shortest_path.index(v)-shortest_path.index(u) is 1:
            u2=u
            v1=v
            if u is not start_point_num and u is not des_point_num:
                u2=u+2*node_num
            if v is not start_point_num and v is not des_point_num:
                v1=v+node_num
            residual_graph.add_edge(v1,u2,weight=-w,cost=0)
        else:
            #u ∈ P∗ \ {s, t}
            if shortest_path.count(u)>0 and u!=start_point_num and u!=des_point_num:
                u2=u+2*node_num
                residual_graph.add_edge(u2,v,weight=w,cost=0)
            #v ∈ P∗ \ {s, t}
            if shortest_path.count(v)>0 and v!=start_point_num and v!=des_point_num:
                v1=v+node_num
                residual_graph.add_edge(u,v1,weight=w,cost=0)

    if debug is True:
        plt.figure( figsize=(10,10),dpi=80)
        plt.subplot(111)
        nx.draw(graph, with_labels=True, font_weight='light')
        plt.show()

    return residual_graph
