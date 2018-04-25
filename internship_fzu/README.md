# δV-2EDSP算法java实现版
## 包 *myGraph*
### **类** *MyGraph*
+ `JGraphT`所提供的图最多只能在边上联系一种属性，本算法中图的边有多个属性，将`JGraphT`提供的图包装到*MyGraph*类中来保存属性信息，同时也可以储存算法的相关参数以及结果信息，方便算法的编写.

**属性**
+ *graph*：默认使用`JGraphT`中提供的`DefaultDirectedWeightedGraph`也就是带有weight的有向非重复图
+ *costMap*:使用Map数据结构储存每一条边对应的cost
+ *nodeNum*:图的顶点数
+ *edgeNum*:图的边数
+ *startPoint*:算法运行时给定的起点
+ *sinkPoint*:算法运行给定的终点
+ *maxComVertex*:算法给定的最大相交点数
+ *mutiGraph*:标记图是否允许平行边
+ *directed*:标记图是否有向
+ *shortestPath*:原图中起点到终点的最短路径
+ *restrictedShortestPaht*:运行RSP算法后得到的最短路径
+ *pathPair*:算法最后的得到的最短路径对


**方法**
```java
DefaultWeightedEdge addNewEdge(int source ,int target,double weight,int cost)
```
+ 这个方法将在图中添加一条边，然后返回这条边
+ *source*:边的起点
+ *target*:边的终点
+ *weight*:这条边的weight属性
+ *cost*:这条边的cost属性
### **类** *ILPGraph*
+ 线性规划算法需要输入允许平行边存在的图，这里除了*graph*成员与*MyGraph*不同以外，其他类似.
---
## 包 *graphIO*
### **类** *GraphRandomGenerator*
**方法**
```java
MyGraph generateRandomGraph(int nodeNum, int edgeNum)
```
+ 算法需要随机图进行测试,本方法返回生成的随机图，且边的属性也随机生成
+ *nodeNum*:随机生成图的顶点数
+ *edgeNum*:随机生成图边数