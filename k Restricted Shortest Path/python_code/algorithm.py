import networkx as nx
import copy
import math
import  numpy as np
INF=1<<28
def paths_xor(paths,pathP):
    """求k条路径时，没加入一条路径，处理一次,因为pathP中会有反向边"""
    start_point=pathP[0]
    des_point=pathP[len(pathP)-1]
    graph=nx.DiGraph()
    for path in paths:
        for index in range(len(path)-1):
            u=path[index]
            v=path[index+1]
            graph.add_edge(u,v)
    deleted_edge=[]#记录已经存在过反向边的边，因为cycle中可能有重复的反向边
    for index in range(len(pathP)-1):
        u=pathP[index]
        v=pathP[index+1]
        if graph.has_edge(v,u):
            graph.remove_edge(v,u)
            deleted_edge.append((u,v))
        else:
            if (u,v) not in deleted_edge:
                graph.add_edge(u,v)
    
    #一次性输出的simple path 可能会有边相交，所以生成一条以后删除该条的边继续生成
    tmp=[]
    while graph.number_of_edges()>0:
        for path in nx.all_simple_paths(graph,start_point,des_point):
            tmp.append(path)
            for index in range(len(path)-1):
                u=path[index]
                v=path[index+1]
                graph.remove_edge(u,v)#每一次只取一条，然后删边防止重复
            break
    return tmp

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
        try:
            sp=nx.bellman_ford_path(aux_graph,start_point,des_point,weight="cost")
        except:
            return None
        if sp is None:
            return None
        else:
            all_sp=paths_xor(all_sp,sp)
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
            if not reverse_graph.has_edge(u,v):
                print("没有边:  "+str(u)+"---->"+str(v))
                print("当前paths  "+str(paths))
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
        try:
            sp=nx.bellman_ford_path(aux_graph,start_point,des_point,weight="delay")
        except:
            return None
        if sp is None:
            return None
        else:
            all_sp=paths_xor(all_sp,sp)
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

def get_cycle_aux_graph(graph,cost_bound,des_point):
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
    #aux_graph["cost_bound"]=cost_bound
    return aux_graph

def get_ori_path(path,node_num):
    tmp=[]
    for node in path:
        ori_node,upper_num=get_ori_node(node,node_num)
        tmp.append(ori_node)
    return tmp

def find_negative_cycle(graph,start_point):
    node_num=graph.number_of_nodes()
    dist_arr=np.zeros((node_num),dtype=int)
    pre=np.zeros((node_num),dtype=int)
    for i in  range(node_num):
        dist_arr[i]=INF
        pre[i]=-1
    
    for i in range(1,node_num):
        for u,v in graph.edges():
            w=graph[u][v]["delay"]
            if dist_arr[u]+w<dist_arr[v]:
                dist_arr[v]=dist_arr[u]+w
                pre[v]=u

    exist_arr=np.zeros((node_num),dtype=bool)
    for i  in range(node_num):
        exist_arr[i]=False

    negative_ori=0
    has_cycle=False
    for u,v in graph.edges():
        w=graph[u][v]["delay"]
        if  dist_arr[u]+w<dist_arr[v]:
            has_cycle=True
            negative_ori=u

    if has_cycle:
        cur_node=negative_ori
        cycle=[]
        while True:
            if not exist_arr[cur_node]:
                exist_arr[cur_node]=True
                cycle.append(cur_node)
                cur_node=pre[cur_node]
            else:
                cycle.append(cur_node)
                tmp=[]
                flag=False
                for node in cycle:
                    if not flag:
                        if node==cur_node:
                            flag=True
                            tmp.append(cur_node)
                    else:
                        tmp.append(node)
                tmp.reverse()
                return tmp
    else:
        return None


def get_bicameral_cycle(reversed_graph,ksp,cost_bound,start_point,des_point,sp_num):
    aux_graph=get_cycle_aux_graph(reversed_graph,cost_bound,des_point)
    node_num=reversed_graph.number_of_nodes()
    #发现负环时直接使用负环,目前等待处理
    for upper_num in range(cost_bound+1):
        cycle=find_negative_cycle(aux_graph,get_split_node(start_point,upper_num,node_num))
        if cycle is not None:
            cycle=get_ori_path(cycle,node_num)
            if cycle_path_xor(cycle,ksp,sp_num) is not None:#负圈可能会无效，待解决
                print("负圈")
                return  cycle
            else:
                return None
    #没有负环
    for node in reversed_graph.nodes():
        for upper_num_s in range(0,cost_bound):
            for upper_num_t in range(upper_num_s+1,cost_bound+1):
                s=get_split_node(node,upper_num_s,node_num)
                t=get_split_node(node,upper_num_t,node_num)
                try:#可能存在不可达的出错情况
                    path_delay=nx.bellman_ford_path_length(aux_graph,s,t,weight="delay")
                except:
                    continue
                if path_delay>=0:#当找到的路径没有改善时放弃
                    continue
                else:
                    #获得的是辅助图中的路径，需要转成普通路径
                    cycle_path=nx.bellman_ford_path(aux_graph,s,t,weight="delay")
                    ori_cycle=get_ori_path(cycle_path,node_num)
                    if cycle_path_xor(ori_cycle,ksp,sp_num) is  not None:
                        return ori_cycle#这里返回的时环的简单点路径，且首尾点重复
    return None

def cycle_path_xor(cycle_path,paths,sp_num):
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

    deleted_edge=[]
    for index in range(len(cycle_path)-1):
        u=cycle_path[index]
        v=cycle_path[index+1]
        if graph.has_edge(v,u):
            graph.remove_edge(v,u)
            deleted_edge.append((u,v))
        else:
            if (u,v) not in deleted_edge:
                graph.add_edge(u,v)

    #这里也可能出现有多条简单路径共用边的情况，这是错误的，所以取出一条边就删一条边 
    #但是边不一定会像path_xor一样删完，所以用路径条数限制
    tmp=[]
    p_num=0
    while p_num<sp_num:
        for path in nx.all_simple_paths(graph,start_point,des_point):
            if path is None:
                return None
            tmp.append(path)
            p_num+=1
            for index in range(len(path)-1):
                u=path[index]
                v=path[index+1]
                graph.remove_edge(u,v)#每一次只取一条，然后删边防止重复
            break
    return tmp

def get_all_reverse_graph(graph,paths):
    """将图中的所有相关路径反向，并且cost和delay全部取反,默认不修改原图"""
    reverse_graph=copy.deepcopy(graph)

    for path in paths:
        for index in range(len(path)-1):
            u=path[index]
            v=path[index+1]
            cost=reverse_graph[u][v]["cost"]
            delay=reverse_graph[u][v]["delay"]
            reverse_graph.remove_edge(u,v)
            reverse_graph.add_edge(v,u,cost=-cost,delay=-delay)

    return reverse_graph

def get_kRSP(graph,start_point,des_point,sp_num,max_delay):
    ksp_for_delay=get_ksp_with_delay(graph,start_point,des_point,sp_num)
    if ksp_for_delay is None:#可能delay的也无法得到ksp
        return None
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
        print("bound:  "+str(low_bound_cost)+"----"+str(up_bound_cost))
        print("current delay: "+str(count_attr(graph,ksp_for_cost,"delay"))+"  cur cost:  "+str(count_attr(graph,ksp_for_cost,"cost")))
        print("curPath: "+str(ksp_for_cost))
        #此处使用二分法缩短cost_bound
        mid_bound_cost=math.floor((low_bound_cost+up_bound_cost)/2)
        if mid_bound_cost==low_bound_cost:#不跳出会死循环
            break
        reverse_graph=get_all_reverse_graph(graph,ksp_for_cost)
        cur_cost=count_attr(graph,ksp_for_cost,"cost")
        bicameral_cycle=get_bicameral_cycle(reverse_graph,ksp_for_cost,mid_bound_cost-cur_cost,start_point,des_point,sp_num)
        print("cycle:  "+str(bicameral_cycle))
        #if bicameral_cycle is not None:
        #    print("cycle delay: "+str(count_attr(reverse_graph,[bicameral_cycle],"delay")))
        if bicameral_cycle!=None:
            ksp_for_cost=cycle_path_xor(bicameral_cycle,ksp_for_cost,sp_num)
            if count_attr(graph,ksp_for_cost,"delay")<=max_delay:
                up_bound_cost=mid_bound_cost
                return ksp_for_cost
            else:
                low_bound_cost=mid_bound_cost
        else:
            low_bound_cost=mid_bound_cost
    if count_attr(graph,ksp_for_cost,"delay")>max_delay:#如果到了最后delay还是不满足条件，说明只能取delay最小的路径集合了
        return ksp_for_delay
        
    return ksp_for_cost

