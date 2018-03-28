#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import networkx as nx
import copy
import matplotlib.pyplot as plt
import sys
import numpy as np
import utilities as util
INFINITY=sys.maxsize/10
def get_residual_graph(start_point_num=0,des_point_num=0,graph=None,debug=False,SCALE=1):
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
        shortest_path=nx.dijkstra_path(graph,start_point_num,des_point_num,"weight")
    except :
        return 
    if debug is True:
        print("original shortest path:\t"+str(shortest_path))

    #generate splitted residual graph

    #Add to G' each edge in G' that does not belong to P∗
    residual_graph=copy.deepcopy(graph)
    for p in shortest_path:
        if p != start_point_num and p != des_point_num:
            residual_graph.remove_node(p)
    if len(shortest_path) is 2:
        residual_graph.remove_edge(start_point_num,des_point_num)

    # For each edge e ∈G' Set c(e) := 0
    for s,t in residual_graph.edges():
        residual_graph.add_edge(s,t,cost=0)

    
    #For each interior vertex v ∈ P∗ \ {s, t} add v1,v2 with its weight and cost 
    for p in shortest_path:
        if p != start_point_num and p != des_point_num:
            p1=p+node_num
            p2=p+2*node_num
            residual_graph.add_edge(p1,p2,weight=0,cost=SCALE)
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
                    residual_graph.add_edge(u2,v+node_num,weight=w,cost=0)
                else:
                    residual_graph.add_edge(u2,v,weight=w,cost=0)
            #v ∈ P∗ \ {s, t}
            if shortest_path.count(v)>0 and v!=start_point_num and v!=des_point_num:
                v1=v+node_num
                if shortest_path.count(u)>0 and u!=start_point_num and u!=des_point_num:
                    residual_graph.add_edge(u+2*node_num,v1,weight=w,cost=0)
                else:
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
    #con_shortest_path_small(start_point_num,des_point_num,graph,max_com_vertex,path)
    dist,path=RSP_no_recrusive(start_point_num,des_point_num,graph,dyn_mat,max_com_vertex,debug)
    if debug is True:
        print("shortest distance: %d"%(int(dist)))
    return dist,path

# function abandoned
def RSP_no_recrusive(start_point_num,des_point_num,graph,dyn_mat,max_com_vertex,debug=False):
    path=[]
    path_map=np.zeros((graph.number_of_nodes()*3,max_com_vertex+1),dtype=int)
    min_node=0

    #initiallization 
    for node in graph.nodes():
        if node !=start_point_num:
            for mcv in range(max_com_vertex+1):
                dyn_mat[node][mcv]=INFINITY
    for mcv in range(max_com_vertex+1):
        dyn_mat[start_point_num][mcv]=0
    
    
    #main algorithm    
    for mcv in range(1,max_com_vertex+1):
        for node in graph.nodes():
            dyn_mat[node][mcv]=dyn_mat[node][mcv-1]
            path_map[node][mcv]=node
        improveFlag=True
        time=0
        while True:
            time+=1
            improveFlag=False
            for u,v in graph.edges():
                if graph[u][v]["cost"]==0:
                    if dyn_mat[v][mcv]>dyn_mat[u][mcv]+graph[u][v]["weight"]:
                        dyn_mat[v][mcv]=dyn_mat[u][mcv]+graph[u][v]["weight"]
                        path_map[v][mcv]=u
                        improveFlag=True
                elif graph[u][v]["cost"]==1:
                    if dyn_mat[v][mcv]>dyn_mat[u][mcv-1]+graph[u][v]["weight"]:
                        dyn_mat[v][mcv]=dyn_mat[u][mcv-1]+graph[u][v]["weight"]
                        path_map[v][mcv]=u
                        improveFlag=True
            if improveFlag is False:
                break
        #print(time) 

    if debug==True:
        file=open("debug.txt","w+")
        i=0
        for line in path_map:
            file.write(str(i)+"  "+str(line)+"\r\n")
            i+=1
        file.write(str(dyn_mat[des_point_num][max_com_vertex]))
        file.close()

    tmp=[]
    return float(dyn_mat[des_point_num][max_com_vertex]),tmp
"""block comment
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
    for node in path:
        if node not in tmp:
            tmp.append(node) 
"""

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

def get_graph_for_LP(graph,start_point_num,des_point_num,max_com_vertex):
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
    LP_graph.graph["S"]=start_point_num
    LP_graph.graph["T"]=des_point_num
    LP_graph.graph["bound"]=max_com_vertex
    return LP_graph

def mwld_alg(graph,start_point_num,des_point_num,max_com_vertex):
    """the main function of mwld algorithm"""
    aux_graph=mwld_get_aux_graph(graph,des_point_num)
    #max common vertex is n means tha max common edge in aux_graph is n-1
    w,path=constrained_shortest_path(start_point_num,des_point_num,aux_graph,max_com_vertex-1)
    return w,path

def get_SP_reverse_graph(graph,shortest_path):
    """
    reverse_graph=copy.deepcopy(graph)
    for index in range(len(shortest_path)-1):
            point_u=shortest_path[index]
            point_v=shortest_path[index+1]
            w=graph[point_u][point_v]["weight"]
            reverse_graph.remove_edge(point_u,point_v)
            reverse_graph.add_edge(point_v,point_u,weight=-w)
    """
    s=shortest_path[0]  #最短路径的起点以及终点
    t=shortest_path[len(shortest_path)-1]

    reverse_graph=nx.DiGraph()
    for node in graph.nodes():
        if node in shortest_path:#shortest_path的中间点拆掉
            if node==s or node==t:
                reverse_graph.add_node(node)
            else:
                v1=node+graph.number_of_nodes()
                v2=node+2*graph.number_of_nodes()
                reverse_graph.add_node(v1)
                reverse_graph.add_node(v2)
                reverse_graph.add_edge(v1,v2,weight=0,cost=0)#拆的点从1到2的边加上
        else:
            reverse_graph.add_node(node)#其他点也不拆
            

    shortest_path_edges=[]#收集shortest_path 中的边，以(u,v)形式储存在list中
    for index in range(len(shortest_path)-1):
        point_u=shortest_path[index]
        point_v=shortest_path[index+1]
        shortest_path_edges.append((point_u,point_v))

    for u,v in graph.edges():#遍历原图中的边
        w=graph[u][v]["weight"]
        u1=u+graph.number_of_nodes()
        v2=v+2*graph.number_of_nodes()
        if (u,v)  not in shortest_path_edges:#遍历的边不在最短路径中
            if u==s or u==t or u not in shortest_path:#不管在不在shortest_path中，s、t一律不拆,点没被拆过也不拆
                new_u=u
            else:
                new_u=u1

            if v==s or v==t or v not in shortest_path:
                new_v=v
            else:
                new_v=v2
            reverse_graph.add_edge(new_u,new_v,weight=w)
        else:
            #这里也要特别处理s,t,但是u不会为t，v不会为s
            if u==s:
                new_u=s
            else:
                new_u=u1

            if v==t:
                new_v=t
            else:
                new_v=v2
            reverse_graph.add_edge(new_v,new_u,weight=-w)
    
    return reverse_graph

def mwld_path_xor(path_p,path_q):
    """get the pair of shortest path"""
    #we assume that the path_p is the path in original graph and path_q is in the reverse graph
    #which means that the reversed edge is in path_q and not in path-p
    path_p1=[]
    path_p2=[]
    path_graph=nx.DiGraph()
    for node in path_p:
        path_graph.add_node(node)
    for node in path_q:
        path_graph.add_node(node)

    edge_p1=[]
    for index in range(len(path_p)-1):
        edge_p1.append((path_p[index],path_p[index+1]))
    edge_shared=[]
    for index in range(len(path_q)-1):
        if (path_q[index+1],path_q[index]) in edge_p1:
            edge_shared.append((path_q[index+1],path_q[index]))
        else:
            path_graph.add_edge(path_q[index],path_q[index+1])
    for index in range(len(path_p)-1):
        if (path_p[index],path_p[index+1]) not in edge_shared:
            path_graph.add_edge(path_p[index],path_p[index+1])
    
    path_list=[]
    for path in nx.all_simple_paths(path_graph,path_p[0],path_p[len(path_p)-1]):
        path_list.append(path)
    return path_list[0],path_list[1]

def mwld_get_aux_graph_edge(graph,point_s,point_t):
    """Get the edges between two points in the auxiliary graph for mwld alogrithm"""
    try:
        path_p=nx.bellman_ford_path(graph,point_s,point_t,"weight")
    except:
        return 0,None,None
    if path_p is None:
        return 0,None,None
    
    reverse_graph=get_SP_reverse_graph(graph,path_p)
    try:
        path_q=nx.bellman_ford_path(reverse_graph,point_s,point_t,"weight")
    except:
        return 0,None,None
    if path_q is None:
        return 0,None,None
    del reverse_graph
    
    tmp=[]#因为reverse_graph有拆点，这里处理拆过的点
    for node in path_q:
        if node>=2*graph.number_of_nodes():
            tmp.append(node-2*graph.number_of_nodes())
        elif node>=graph.number_of_nodes():
            tmp.append(node-graph.number_of_nodes())
        else:
            tmp.append(node)
    path_q=tmp

    path_p1,path_p2=mwld_path_xor(path_p,path_q)
    #print("P: "+str(path_p)+"  Q: "+str(path_q))
    #print("p1: "+str(path_p1)+"  p2: "+str(path_p2))
    w_p1=util.get_SP_weight(graph,path_p1)
    w_p2=util.get_SP_weight(graph,path_p2)
    w_sum=w_p1+w_p2
    return w_sum,path_p1,path_p2

def mwld_get_aux_graph(graph,des_point_num):
    """Get an auxiliary graph for mwld algorithm"""
    aux_graph=nx.DiGraph()
    for node in graph.nodes():
        aux_graph.add_node(node)
    
    for point_s in graph.nodes():
        for point_t in graph.nodes():
            if point_s==point_t:
                continue
            else:
                w_sum,path_p1,path_p2=mwld_get_aux_graph_edge(graph,point_s,point_t)
                if path_p1==None or path_p2==None:
                    continue
                else:
                    if point_t is des_point_num:
                        aux_graph.add_edge(point_s,point_t,weight=w_sum,cost=0)
                    else:
                        aux_graph.add_edge(point_s,point_t,weight=w_sum,cost=1)
    return aux_graph
