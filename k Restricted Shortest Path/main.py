import algorithm as alg
import graph_gen as gg
if __name__=="__main__":
    node_num=50
    edge_num=800
    max_delay=50
    sp_num=4
    start_point=1
    des_point=20

    graph=gg.getRandomGraph(node_num,edge_num)
    ksp=alg.get_kRSP(graph,start_point,des_point,sp_num,max_delay)
    print(ksp)
    if ksp!=None:
        for path in ksp:
            tmp=[]
            tmp.append(path)
            print(alg.count_attr(graph,tmp,"cost"))