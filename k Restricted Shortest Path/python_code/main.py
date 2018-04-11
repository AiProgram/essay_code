import algorithm as alg
import graph_gen as gg
if __name__=="__main__":
    node_num=40
    edge_num=500
    max_delay=30
    sp_num=3
    start_point=1
    des_point=35

    for i in range(100):
        graph=gg.getRandomGraph(node_num,edge_num)
        ksp=alg.get_kRSP(graph,start_point,des_point,sp_num,max_delay)
        if ksp is not None:
            print(ksp)
            print(alg.count_attr(graph,ksp,"cost"))
            print(alg.count_attr(graph,ksp,"delay"))
        else:
            print("没有结果")
        print("------")