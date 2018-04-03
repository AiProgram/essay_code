import networkx as nx
import algorithm as alg
if __name__=="__main__":
    graph=nx.DiGraph()
    graph.add_node(0)#s
    graph.add_node(1)#t
    graph.add_node(2)#x
    graph.add_node(3)#y
    graph.add_node(4)#z

    graph.add_edge(0,3,cost=4,delay=1)#s->y
    graph.add_edge(1,4,cost=-1,delay=-2)#t->z
    graph.add_edge(2,0,cost=-2,delay=-1)#x->s
    graph.add_edge(3,1,cost=6,delay=2)#y->t
    graph.add_edge(3,2,cost=-1,delay=-1)#y->x
    graph.add_edge(4,3,cost=-1,delay=-2)#z->y

    #cycle=alg.get_bicameral_cycle(graph,6)
    #print(cycle)