#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import networkx as nx
import copy
import matplotlib.pyplot as plt
import sys
import numpy as np
INFINITY=sys.maxsize/2
def get_residual_graph(start_point_num=0,des_point_num=0,graph=None,debug=False):
    #find original shortest path
    node_num=0
    edge_num=0
    shortest_path=[]
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
        if p != start_point_num and p != des_point_num:
            residual_graph.remove_node(p)

    # For each edge e ∈G' Set c(e) := 0
    for s,t in residual_graph.edges():
        residual_graph.add_edge(s,t,cost=0)

    
    #For each interior vertex v ∈ P∗ \ {s, t} add v1,v2 with its weight and cost 
    for p in shortest_path:
        if p != start_point_num and p != des_point_num:
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
                if shortest_path.count(v)>0 and v!=start_point_num and v!=des_point_num:
                    v+=node_num
                residual_graph.add_edge(u2,v,weight=w,cost=0)
            #v ∈ P∗ \ {s, t}
            if shortest_path.count(v)>0 and v!=start_point_num and v!=des_point_num:
                v1=v+node_num
                if shortest_path.count(u)>0 and u!=start_point_num and u!=des_point_num:
                    u+=2*node_num
                residual_graph.add_edge(u,v1,weight=w,cost=0)

    if debug is True:
        plt.figure( figsize=(10,10),dpi=80)
        plt.subplot(111)
        nx.draw(graph, with_labels=True, font_weight='light')
        #plt.show()
    
    return shortest_path,residual_graph

def constrained_shortest_path(start_point_num,des_point_num,graph=None,max_com_vertex=0,debug=False):
    if graph==None:
        return
    dyn_mat=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1))
    for i in range(graph.number_of_nodes()):
        if i is start_point_num:
            continue
        else:
            for j in range(max_com_vertex+1):
                dyn_mat[i][j]=INFINITY
    #con_shortest_path_small(start_point_num,des_point_num,graph,max_com_vertex,path)
    path=RSP_no_recrusive(start_point_num,des_point_num,graph,dyn_mat,max_com_vertex)
    if debug is True:
        print("shortest distance: %d"%(dyn_mat[des_point_num][max_com_vertex]))
    return path

# function abandoned
def RSP_no_recrusive(start_point_num,des_point_num,graph,dyn_mat,max_com_vertex):
    path=[]
    path_map=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1),dtype=int)
    min_node=0

    for node in range(graph.number_of_nodes()*3):
        path_map[node][0]=node


    for mcv in range(max_com_vertex+1):
        for node in graph.nodes():
            if node==start_point_num:
                dyn_mat[node][mcv]=0
            else:
                if mcv==0 :
                    dyn_mat[node][mcv]=INFINITY
                else:
                    dyn_mat[node][mcv]=dyn_mat[node][mcv-1]
                    path_map[node][mcv]=node
                    for pred in graph.predecessors(node):
                        mcv_next=graph[node][succ]["cost"]+mcv
                        if mcv_next<=max_com_vertex:
                            old=dyn_mat[succ][mcv_next]
                            new=dyn_mat[node][mcv]+graph[node][succ]["weight"]
                            if new<old:
                                dyn_mat[succ][mcv_next]=new
                                path_map[succ][mcv_next]=node
    
    if debug==True:
        file=open("debug.txt","w+")
        i=0
        for line in path_map:
            file.write(str(i)+"  "+str(line)+"\r\n")
            i+=1
        file.write(str(dyn_mat[des_point_num][max_com_vertex]))
        file.close()

    cur_node=des_point_num
    cur_mcv=max_com_vertex
    next_mcv=cur_mcv
    while(cur_mcv>=0):
        next_node=path_map[cur_node][cur_mcv]
        if next_node==cur_node:
            next_mcv=cur_mcv-1
        else:
            next_mcv=cur_mcv-graph[next_node][cur_node]["cost"]
        path.append(cur_node)
        cur_node=next_node
        cur_mcv=next_mcv
    path=path[::-1]
    tmp=[]
    for node in path:
        if node not in tmp:
            tmp.append(node) 

    return tmp,float(dyn_mat[des_point_num][max_com_vertex])

# recommand function
def RSP_with_recursion(graph,start_point_num,des_point_num,max_com_vertex):
    #the following mamory usages can be improved
    visited=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1),dtype=bool)
    pred=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1),dtype=int)
    dyn_mat=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1),dtype=float)

    for node in range(graph.number_of_nodes()*3):
        for mcv in range(max_com_vertex+1):
            dyn_mat[node][mcv]=-1

    dist=RSP_recursion_small(graph,start_point_num,des_point_num,max_com_vertex,dyn_mat,visited,pred)

#    for i in range(graph.number_of_nodes()*3):
#       print(str(i)+"  "+str(pred[i]))

    path=[]

    if dist>=INFINITY-1:
        print("cannot find a path that satisfy the restriction")
    else:
        cur_node=des_point_num
        cur_mcv=max_com_vertex
        next_mcv=cur_mcv
        while cur_node!=start_point_num:
            next_node=pred[cur_node][cur_mcv]
            if next_node==cur_node:
                next_mcv=cur_mcv-1
            else:
                next_mcv=cur_mcv-graph[next_node][cur_node]["cost"]
            path.append(cur_node)
            cur_node=next_node
            cur_mcv=next_mcv
    path.append(start_point_num)
    
    path=path[::-1]
    tmp=[]
    for node in path:
        if node not in tmp:
            tmp.append(node)
    path=tmp
    return dist,path

# sub function of "RSP_with_recursion()"
def RSP_recursion_small(graph,start_point_num,des_point_num,max_com_vertex,dyn_mat,visited,pred):
    if dyn_mat[des_point_num][max_com_vertex]!=-1:
        return dyn_mat[des_point_num][max_com_vertex]

    if des_point_num==start_point_num:
        dyn_mat[des_point_num][max_com_vertex]=0
        return 0

    if max_com_vertex==0:
        dyn_mat[des_point_num][max_com_vertex]=INFINITY
        return INFINITY

    min_node=des_point_num
    min_dist=RSP_recursion_small(graph,start_point_num,des_point_num,max_com_vertex-1,dyn_mat,visited,pred)
    for node in graph.predecessors(des_point_num):
        if max_com_vertex>=graph[node][des_point_num]["cost"]:
            new_com_vertex=max_com_vertex-graph[node][des_point_num]["cost"]

            if(visited[node][new_com_vertex]==True):
                continue
            visited[node][new_com_vertex]=True

            cur_dist=RSP_recursion_small(graph,start_point_num,node,new_com_vertex,dyn_mat,visited,pred)+\
            graph[node][des_point_num]["weight"]

            

            if cur_dist<min_dist:
                min_dist=cur_dist
                min_node=node

    pred[des_point_num][max_com_vertex]=min_node
    dyn_mat[des_point_num][max_com_vertex]=min_dist

    return min_dist