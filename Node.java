/*
 *	Author: Aditya Prakash 			NetID: axp171931 
*/
// Data structure for the nodes used in the Decision Tree
class Node
{
	Node parent;	// Parent Node
	Node left;		// Left child
	Node right;		// Right child
	int targetAttribute = -1;
	int lIndices[];
	int rIndices[];
	int label = -1;
	boolean isLeaf = false;
}
