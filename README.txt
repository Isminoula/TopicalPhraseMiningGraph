TopicalPhraseMiningGraph takes as input: (to run the algorithm, the main class is included in the run package)
A list of sentences or a list of titles, each sentence/title in a new line
The output of CTM topic modeling, meaning the term distributions per topic
A filename for output of the results, which should be "force.csv", alongside with the path of the file (for example outputFile = "test/allafrica/force.csv")
The number of the topic we are interested in
A boolean variable (removeEdges) that is true if edges with score lower than the median should be removed before visualizing the graph
An integer (topK) that should be -1 if we would prefer to visualize all edges (irrespectively of removing edges) or any other value k if we would visualize the top-k higher scored edges


The algorithm forms a directed graph of the sentences (titles) where each word is a node and two nodes are connected with an edge if the cooccur in a sentence (title)
The direction of the edge represents the order of the two words connected, while the weight of the edge is the number of times these concurrence happens.
The score of each edge is different than the weight; it is defined as the topical term probability of the first word times the topical term probability of the second word times the number of concurrence of these two terms.
This scoring represents a weighted topical language modeling approach.

The results are saved in the force.csv file to be used for visualization purposes.
index.html is the appropriate file to open in the browser, which leverages javascript and D3.js to showcase the graph.
Edges with score higher than the median are highlighted, while clicking on a node makes it larger and double clicking it smaller.

It should be noted that the option of leaving all edges without any pruning could result in a large graph that is difficult to interpret, especially when dealing with the whole dataset.

The next steps of this procedure should be:
1) Creating a random walk on the graph to produce a list of phrases that appear in a topic OR
2) Computing graph similarities between the graph based on titles and the graph based on sentences to find key topical phrases that appear in both cases.
The second idea could incorporate a measure of similarity or dissimilarity among topics.