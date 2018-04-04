import networkx as nx
import random
def getRandomGraph(node_num,edge_num):
    graph=nx.gnm_random_graph(node_num,edge_num,seed=None,directed=True)
    for u,v in graph.edges():
        graph.add_edge(u,v,cost=random.randint(1,2),delay=random.randint(1,10))
    return graph