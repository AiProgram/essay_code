#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import networkx as nx
import copy
import matplotlib.pyplot as plt
import sys
import numpy as np
INFINITY=sys.maxsize/2
def get_residual_graph(start_point_num=0,des_point_num=0,graph=None,debug=False,SCALE=50):
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
        residual_graph.add_edge(s,t,cost=1)

    
    #For each interior vertex v ∈ P∗ \ {s, t} add v1,v2 with its weight and cost 
    for p in shortest_path:
        if p != start_point_num and p != des_point_num:
            p1=p+node_num
            p2=p+2*node_num
            residual_graph.add_edge(p1,p2,weight=0,cost=SCALE)
            residual_graph.add_edge(p2,p1,weight=0,cost=1)
    
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
            residual_graph.add_edge(v1,u2,weight=-w,cost=1)
        else:
            #u ∈ P∗ \ {s, t}
            if shortest_path.count(u)>0 and u!=start_point_num and u!=des_point_num:
                u2=u+2*node_num
                if shortest_path.count(v)>0 and v!=start_point_num and v!=des_point_num:
                    v+=node_num
                residual_graph.add_edge(u2,v,weight=w,cost=1)
            #v ∈ P∗ \ {s, t}
            if shortest_path.count(v)>0 and v!=start_point_num and v!=des_point_num:
                v1=v+node_num
                if shortest_path.count(u)>0 and u!=start_point_num and u!=des_point_num:
                    u+=2*node_num
                residual_graph.add_edge(u,v1,weight=w,cost=1)

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
    path=RSP_no_recrusive(start_point_num,des_point_num,graph,dyn_mat,max_com_vertex,debug)
    if debug is True:
        print("shortest distance: %d"%(dyn_mat[des_point_num][max_com_vertex]))
    return path

# function abandoned
def RSP_no_recrusive(start_point_num,des_point_num,graph,dyn_mat,max_com_vertex,debug=False):
    path=[]
    path_map=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1),dtype=int)
    min_node=0

    for mcv in range(max_com_vertex+1):
        dyn_mat[start_point_num][mcv]=0
    
    for node in range(graph.number_of_nodes()*3):
        if node != start_point_num:
            dyn_mat[node][0]=INFINITY

    for mcv in range(1,max_com_vertex+1):
        for node in graph.nodes():
            if node != start_point_num:
                min_node=node
                min_dist=dyn_mat[node][mcv-1]
                for pred in graph.predecessors(node):
                    if graph[pred][node]["cost"]<=mcv:
                        new_mcv=mcv-graph[pred][node]["cost"]
                        new_dist=dyn_mat[pred][new_mcv]+graph[pred][node]["weight"]
                        if new_dist< min_dist:
                            min_dist=new_dist
                            min_node=pred
                path_map[node][mcv]=min_node
        
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
    #visited=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1),dtype=bool)
    visited=None
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

#            if(visited[node][new_com_vertex]==True):
#               continue
#            visited[node][new_com_vertex]=True

            cur_dist=RSP_recursion_small(graph,start_point_num,node,new_com_vertex,dyn_mat,visited,pred)+\
            graph[node][des_point_num]["weight"]

            

            if cur_dist<min_dist:
                min_dist=cur_dist
                min_node=node

    pred[des_point_num][max_com_vertex]=min_node
    dyn_mat[des_point_num][max_com_vertex]=min_dist

    return min_dist

def path_XOR(graph,residual_graph,path_p,path_q):
    #merge the splitted node in path_q
    node_number=graph.number_of_nodes()
    for index in range(len(path_q)):
        if path_q[index]>=2*node_number:
            path_q[index]-=2*node_number
        elif path_q[index]>=node_number:
            path_q[index]-=node_number

    tmp=[]
    for node in path_q:
        if node not in tmp:
            tmp.append(node)
    path_q=tmp

    #find parrallel edges in both path
    edges_p=[]
    for index in range(len(path_p)-1):
        edges_p.append((path_p[index],path_p[index+1]))
    parrallel_edge=[]
    for index in range(len(path_q)-1):
        if (path_q[index+1],path_q[index]) in edges_p:
            parrallel_edge.append((path_q[index+1],path_q[index]))
    
    #delete the parrallel edges
    tmp=[]
    for index in range(len(path_p)-1):
        if (path_p[index],path_p[index+1]) not in parrallel_edge:
          tmp.append(path_p[index])
    tmp.append(path_p[index+1])
    path_p=tmp

    tmp=[]
    for index in range(len(path_q)-1):
        if(path_q[index+1],path_q[index]) not in parrallel_edge:
            tmp.append(path_q[index])
    tmp.append(path_q[index+1])
    path_q=tmp

    return path_p,path_q

def get_graph_for_LP(graph,start_point_num,des_point_num):
    """construct G' for the lp algorithm"""
    LP_graph=nx.DiGraph()
    LP_graph.add_node(start_point_num)
    LP_graph.add_node(des_point_num)

    #we asume that the id of nodes should be in between 0 and node_number-1
    node_number=graph.number_of_nodes()
    for node in graph.nodes():
        if node==start_point_num or node==des_point_num:
            continue
        LP_graph.add_node(node)# thie is v1
        LP_graph.add_node(node+node_number)# thie is v2
    
    #the edges entering v in G are now entering v1 and the edges leaving v are now leaving v2 in G'
    for u,v in graph.edges():
        w=graph[u][v]["weight"]
        v1=0
        u2=0
        if v==start_point_num or v==des_point_num:
            v1=v
        else:
            v1=v
        if u==start_point_num or u==des_point_num:
            u2=u
        else:
            u2=u+node_number
        LP_graph.add_edge(u2,v1,weight=w,cost=0)

    #Add an edge (v 1 , v 2 ) with cost 1 and weight 0 in G'
    for node in graph.nodes():
        if node==start_point_num or node==des_point_num:
            continue
        LP_graph.add_edge(node,node+node_number,weight=0,cost=1)
    
    return LP_graph