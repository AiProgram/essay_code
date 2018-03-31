import networkx as nx
import copy
def get_cost_reverse_graph(graph,paths):
    """将图中的所有相关路径反向，并且只有cost取反,默认不修改原图"""
    reverse_graph=copy.deepcopy(graph)

    for path in paths:
        for index in range(len(path)-1):
            u=path[index]
            v=path[index+1]
            cost=graph[u][v]["cost"]
            delay=graph[u][v]["delay"]
            reverse_graph.remove_edge(u,v)
            reverse_graph.add_edge(v,u,cost=-cost,delay=delay)

    return reverse_graph

def get_ksp_with_cost(graph,start_point,des_point,sp_num):
    """获得k条最短路径，但是只考虑cost，不考虑delay"""
    aux_graph=copy.deepcopy(graph)
    all_sp=[]
    cur_sp_num=0
    while(cur_sp_num<sp_num):
        sp=nx.bellman_ford_path(aux_graph,start_point,des_point,weight="cost")
        if sp is None:
            return None
        else:
            all_sp.append(sp)
            cur_sp_num+=1
        aux_graph=get_cost_reverse_graph(graph,all_sp)
    return all_sp

def get_delay_reverse_graph(graph,paths):
    """将图中的所有相关路径反向，并且只有delay取反,默认不修改原图"""
    reverse_graph=copy.deepcopy(graph)

    for path in paths:
        for index in range(len(path)-1):
            u=path[index]
            v=path[index+1]
            cost=graph[u][v]["cost"]
            delay=graph[u][v]["delay"]
            reverse_graph.remove_edge(u,v)
            reverse_graph.add_edge(v,u,cost=cost,delay=-delay)

    return reverse_graph

def get_ksp_with_delay(graph,start_point,des_point,sp_num):
    """获得k条最短路径，但是只考虑delay，不考虑cost"""
    aux_graph=copy.deepcopy(graph)
    all_sp=[]
    cur_sp_num=0
    while(cur_sp_num<sp_num):
        sp=nx.bellman_ford_path(aux_graph,start_point,des_point,weight="delay")
        if sp is None:
            return None
        else:
            all_sp.append(sp)
            cur_sp_num+=1
        aux_graph=get_delay_reverse_graph(graph,all_sp)
    return all_sp

def count_attr(graph,paths,attr):
    """统计路径集合的属性之和，属性的名称就是attr参数，是一个字符串"""
    sum=0
    for path in paths:
        for index in range(len(path)-1):
            u=path[index]
            v=path[index+1]
            sum+=graph[u][v][attr]
    return sum

def get_split_node(ori_node,upper_num,node_num):
    return ori_node+upper_num*node_num

def get_ori_node(cur_node,node_num):
    ori_node=cur_node%node_num
    upper_num=(cur_node-ori_node)/node_num
    return ori_node,upper_num

def get_cycle_aux_graph(graph,cost_bound):
    """获得环O需要拆点的辅助图，这个就是获得辅助图"""
    node_num=graph.number_of_nodes()
    aux_graph=nx.DiGraph()
    node_num=graph.number_of_nodes()
    for node in graph.nodes():
        for upper_num in range(cost_bound+1):
            split_node=get_split_node(node,upper_num,node_num)
            aux_graph.add_node(split_node)
    
    for u,v in graph.edges():
        cost=graph[u][v]["cost"]
        delay=graph[u][v]["delay"]
        for u_upper_num in range(cost_bound+1):
            v_upper_num=u_upper_num+cost
            if v_upper_num<=cost_bound and v_upper_num>=0:
                new_u=get_split_node(u,u_upper_num,node_num)
                new_v=get_split_node(v,v_upper_num,node_num)
                aux_graph.add_edge(new_u,new_v,delay=delay)
    aux_graph["cost_bound"]=cost_bound
    return aux_graph

def get_ori_path(path,node_num):
    tmp=[]
    for node in path:
        ori_node,upper_num=get_ori_node(node,node_num)
        tmp.append(ori_node)
    return tmp

def get_bicameral_cycle(reversed_graph,cost_bound):
    aux_graph=get_cycle_aux_graph(reversed_graph,cost_bound)
    node_num=reversed_graph.number_of_nodes()
    if nx.negative_edge_cycle(aux_graph,weight="delay"):
        #发现负环时直接使用负环,目前等待处理
        for cycle in nx.simple_cycles(aux_graph):
            print(cycle)
        return None
    else:
        #没有负环
        for node in reversed_graph.nodes():
            for upper_num in range(1,cost_bound+1):
                start_node=get_split_node(node,upper_num,node_num)
                path_delay=nx.bellman_ford_path_length(aux_graph,start_node,node,weight="delay")
                if path_delay>=0:#当找到的路径没有改善时放弃
                    continue
                else:
                    #获得的是辅助图中的路径，需要转成普通路径
                    cycle_path=nx.bellman_ford_path(aux_graph,start_node,node,weight="delay")
                    ori_cycle=get_ori_path(cycle_path,node_num)
                    return ori_cycle#这里返回的时环的简单点路径，且首尾点重复

        return None

def cycle_path_xor(cycle_path,paths):
    """将k条路径与一个bicameral cycle进行异或"""
    graph=nx.DiGraph()
    start_point=paths[0][0]
    des_point=paths[0][len(paths[0])-1]
    #事先添加所有的点
    for node in cycle_path:
        graph.add_node(node)
    for path in paths:
        for node in path:
            graph.add_node(node)

    for path in paths:
        for index in range(len(path)-1):
            u=path[index]
            v=path[index+1]
            graph.add_edge(u,v)

    for index in range(len(cycle_path)-1):
        u=cycle_path[index]
        v=cycle_path[index+1]
        if graph.has_edge(v,u):
            graph.remove_edge(v,u)
        else:
            graph.add_edge(u,v)

    new_paths=[]
    for path in nx.all_simple_paths(graph,start_point,des_point):
        new_paths.append(path)
    return new_paths

def get_all_reverse_graph(graph,paths):
    """将图中的所有相关路径反向，并且cost和delay全部取反,默认不修改原图"""
    reverse_graph=copy.deepcopy(graph)

    for path in paths:
        for index in range(len(path)-1):
            u=path[index]
            v=path[index+1]
            cost=graph[u][v]["cost"]
            delay=graph[u][v]["delay"]
            reverse_graph.remove_edge(u,v)
            reverse_graph.add_edge(v,u,cost=-cost,delay=-delay)

    return reverse_graph

def get_kRSP(graph,start_point,des_point,sp_num,max_delay):
    ksp_for_delay=get_ksp_with_delay(graph,start_point,des_point,sp_num)
    total_delay=count_attr(graph,ksp_for_delay,"delay")
    if total_delay>max_delay:#提前结束算法，不会有结果
        return None
    
    ksp_for_cost=get_ksp_with_cost(graph,start_point,des_point,sp_num)
    total_delay=count_attr(graph,ksp_for_cost,"delay")
    if total_delay<=max_delay:#提前结束，已经找到结果
        return ksp_for_cost

    low_bound_cost=count_attr(graph,ksp_for_cost,"cost")
    up_bound_cost=count_attr(graph,ksp_for_delay,"cost")
    while(low_bound_cost<up_bound_cost):
        #此处使用二分法缩短cost_bound
        mid_bound_cost=(low_bound_cost+up_bound_cost)/2
        reverse_graph=get_all_reverse_graph(graph,ksp_for_cost)
        bicameral_cycle=get_bicameral_cycle(reverse_graph,mid_bound_cost-low_bound_cost)
        if bicameral_cycle!=None:
            ksp_for_cost=cycle_path_xor(bicameral_cycle,ksp_for_cost)
            up_bound_cost=mid_bound_cost
        else:
            low_bound_cost=mid_bound_cost
    return ksp_for_cost

