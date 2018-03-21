/*
 *	Author: Aditya Prakash 			NetID: axp171931 
*/
import java.util.Random;	// To generate random numbers
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

// Class to build, prune and trace the Decision Tree
public class DecisionTree 
{
	private static int count = 0;
	// Method to get the features and length
	private static int[] getFeatAndLen(String file) 
	{
		String lines = "";
		String delimit = ",";
		BufferedReader br = null;
		@SuppressWarnings("unused")
		int count = 0;
		int[] featAndLen = new int[2];
		try 
		{
			br = new BufferedReader(new FileReader(file));
			while ((lines = br.readLine()) != null) 
			{
				if (count == 0) 
				{
					String[] c = lines.split(delimit);
					featAndLen[0] = c.length;
				}
				count++;
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			if (br != null) 
			{
				try 
				{
					br.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		featAndLen[1] = count;
		return featAndLen;
	}

	// Method to parse the training data and use them for creating tree
	private static void loadVals(String path, int[][] vals, String[] fNames, int[] done, int[] indices, int ft) 
	{
		String lines = "";
		String delimit = ",";
		String file = path;
		BufferedReader br = null;
		for (int k = 0; k < vals.length; k++) 
		{
			indices[k] = k;
		}
		for (int k = 0; k < ft; k++) 
		{
			done[k] = 0;
		}
		try 
		{
			int i = 0;
			br = new BufferedReader(new FileReader(file));
			while ((lines = br.readLine()) != null) 
			{
				int j = 0;
				String[] paras = lines.split(delimit);
				if (i == 0) 
				{
					for (String para : paras) 
					{
						fNames[j++] = para;
					}
				}
				else 
				{
					for (String para : paras) 
					{
						vals[i][j++] = Integer.parseInt(para);
					}
				}
				i++;
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			if (br != null) 
			{
				try 
				{
					br.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}

	// Method to create Decision Tree on Training Data and return the root node
	public static Node createTree(Node root, int[][] vals, int[] done, int ft, int[] indices, Node parent,int h) 
	{
		if (root == null) 
		{
			root = new Node();
			if (indices == null || indices.length == 0) 
			{
				root.isLeaf = true;
				root.label = getMaxValue(root, vals, ft);
				return root;
			}
			if (ifAllOne(indices, vals, ft)) 
			{
				root.isLeaf = true;
				root.label = 1;
				return root;
			}
			if (ifAllZero(indices, vals, ft)) 
			{
				root.isLeaf = true;
				root.label = 0;
				return root;
			}
			if (ft == 1 || ifAllAttributesDone(done)) 
			{
				root.isLeaf = true;
				root.label = getMaxValue(root, vals, ft);
				return root;
			}
		}
		root = constructNodeOnAttribute(root, vals, done, ft, indices,h);
		root.parent = parent;
		if (root.targetAttribute != -1)
		{
			done[root.targetAttribute] = 1;
		}
		int doneRight[] = new int[done.length];
		int doneLeft[] = new int[done.length];
		for (int j = 0; j < done.length; j++) 
		{
			doneRight[j] = done[j];
			doneLeft[j] = done[j];
		}

		root.left = createTree(root.left, vals, doneLeft, ft, root.lIndices, root,h);
		root.right = createTree(root.right, vals, doneRight, ft, root.rIndices, root,h);
		return root;
	}

	// Method to return maximum value of Target Attribute from the examples upon splitting
	public static int getMaxValue(Node root, int[][] vals, int ft) 
	{
		int countOne = 0, countZero = 0;
		if (root.parent == null) 
		{
			int i = 0;
			for (i = 0; i < vals.length; i++) 
			{
				if (vals[i][ft] == 1) 
				{
					countOne++;
				} 
				else 
				{
					countZero++;
				}
			}
		} 
		else 
		{
			for (int i : root.parent.lIndices) 
			{
				if (vals[i][ft] == 1) 
				{
					countOne++;
				} 
				else 
				{
					countZero++;
				}
			}

			for (int i : root.parent.rIndices) 
			{
				if (vals[i][ft] == 1) 
				{
					countOne++;
				} 
				else 
				{
					countZero++;
				}
			}
		}
		if(countOne>countZero)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	// Method to Prune the tree
	public static Node postPruning(String path, int L, int K, Node root, int[][] vals, int ft) 
	{
		Node prunedTree = new Node();
		int i = 0;
		prunedTree = root;
		double bestAccuracy = validationAccuracy(path, root);
		System.out.println("Accuracy(Original Tree): \t\t\t" + bestAccuracy + "\n");
		for (i = 0; i < L; i++) 
		{
			Node n = duplicateTree(root);
			Random r = new Random();
			int M = 1 + r.nextInt(K);
			for (int j = 1; j <= M; j++) 
			{
                count =0;
				int nonLeafNodes = totalNonLeafNodes(n);
				if (nonLeafNodes == 0)
				{
					break;
				}
				Node nodes[] = new Node[nonLeafNodes];
				createArray(n, nodes);
				int P = r.nextInt(nonLeafNodes);
				nodes[P] = createBestLeaf(nodes[P], vals, ft);
			}
			double accuracy = validationAccuracy(path, n);

			if (accuracy > bestAccuracy)
			{
				prunedTree = n;
				bestAccuracy = accuracy;
			}
		}
		System.out.println("Accuracy(Pruned Tree):  \t\t\t" + bestAccuracy + "\n");
		return prunedTree;
	}

	// Method to create and get the leaf node with the max value of the target attribute among the examples
	private static Node createBestLeaf(Node n, int[][] vals, int ft) 
	{
		n.isLeaf = true;
		n.label = getMaxValueAtNode(n, vals, ft);
		n.left = null;
		n.right = null;
		return n;
	}

	// Method to split attributes on information gain and variance impurity heuristics
	private static Node constructNodeOnAttribute(Node root, int[][] vals, int[] done, int ft, int[] indices,int h) 
	{	
		@SuppressWarnings("unused")
		double maxHeu = 0;
		int j = 0, k = 0, iMax = -1;
		int lMax[] = null;
		int rMax[] = null;
		
		for (int i=0; i < ft; i++)
		{	
			if (done[i] == 0)
			{
				double neg = 0, pos = 0, left = 0, right = 0, eLeft = 0, eRight = 0, lVI=0, rVI=0;
				double entropy = 0,varImpurity=0, rPos = 0, heu = 0, rNeg = 0, lPos = 0, lNeg = 0;
				int[] lIndex = new int[vals.length];
				int[] rIndex = new int[vals.length];
				
				for (k = 0; k < indices.length; k++)
				{
					if (vals[indices[k]][ft] == 1)
					{
						pos++;
					} 
					else 
					{
						neg++;
					}
					if (vals[indices[k]][i] == 1) 
					{
						rIndex[(int) right++] = indices[k];
						if (vals[indices[k]][ft] == 1) 
						{
							rPos++;
						} 
						else 
						{
							rNeg++;
						}

					}
					else 
					{
						lIndex[(int) left++] = indices[k];
						if (vals[indices[k]][ft] == 1) 
						{
							lPos++;
						} 
						else 
						{
							lNeg++;
						}
					}
				}
				if(h==1) 
				{
					entropy = (-1 * logValue(pos / indices.length) * ((pos / indices.length))) + (-1 * logValue(neg / indices.length) * (neg / indices.length));
					eLeft = (-1 * logValue(lPos / (lPos + lNeg)) * (lPos / (lPos + lNeg))) + (-1 * logValue(lNeg / (lPos + lNeg)) * (lNeg / (lPos + lNeg)));
					eRight = (-1 * logValue(rPos / (rPos + rNeg)) * (rPos / (rPos + rNeg))) + (-1 * logValue(rNeg / (rPos + rNeg)) * (rNeg / (rPos + rNeg)));	
					if (Double.compare(Double.NaN, eLeft) == 0) 
					{
						eLeft = 0;
					}
					if (Double.compare(Double.NaN, eRight) == 0) 
					{
						eRight = 0;
					}
					if (Double.compare(Double.NaN, entropy) == 0) 
					{
						entropy = 0;
					}					
					heu = entropy - ((left / (left + right) * eLeft) + (right / (left + right) * eRight));
				}
				else if(h==0) 
				{
					lVI = (lPos / (lPos + lNeg))*(lNeg / (lPos + lNeg));
					rVI = (rPos / (rPos + rNeg))*(rNeg / (rPos + rNeg));
					varImpurity = (pos / indices.length)*(neg / indices.length);
					if (Double.compare(Double.NaN, lVI) == 0) 
					{
						lVI = 0;
					}
					if (Double.compare(Double.NaN, rVI) == 0) 
					{
						rVI = 0;
					}
					if (Double.compare(Double.NaN, varImpurity) == 0) 
					{
						varImpurity = 0;
					}
					heu = varImpurity - ((left / (left + right) * lVI) + (right / (left + right) * rVI));
				}
				
				if (heu >= maxHeu) 
				{
					iMax = i;
					maxHeu = heu;
					int leftTempArray[] = new int[(int) left];
					for (int index = 0; index < left; index++) 
					{
						leftTempArray[index] = lIndex[index];
					}
					int rightTempArray[] = new int[(int) right];
					for (int index = 0; index < right; index++) 
					{
						rightTempArray[index] = rIndex[index];
					}
					lMax = leftTempArray;
					rMax = rightTempArray;
				}
			}
		}
		root.lIndices = lMax;
		root.rIndices = rMax;
		root.targetAttribute = iMax;
		return root;
	}

	// To check if Target_Attribute of all examples = 1
	public static boolean ifAllOne(int[] indices, int[][] vals, int ft) 
	{
		boolean allOne = true;
		for (int i : indices) 
		{
			if (vals[i][ft] == 0)
			{
				allOne = false;
			}
		}
		return allOne;
	}

	// To check if Target_Attribute of all examples = 0
	public static boolean ifAllZero(int[] indices, int[][] vals, int ft) 
	{
		boolean allZero = true;
		for (int i : indices) 
		{
			if (vals[i][ft] == 1)
			{
				allZero= false;
			}
		}
		return allZero;
	}

	// Method to check if all attributes are processed or not
	public static boolean ifAllAttributesDone(int[] list) 
	{
		boolean done = true;
		for (int i : list) 
		{
			if (i == 0)
			{	
				done = false;
			}
		}
		return done;
	}

	// Method to trace the tree
	public static void treeTrace(Node n) 
	{
		if (n != null) 
		{
			if (n.lIndices != null) 
			{
				for (int i : n.lIndices) 
				{
					System.out.print(i + " ");
				}
			}
			if (n.rIndices != null) 
			{
				for (int i : n.rIndices) 
				{
					System.out.print(i + " ");
				}
			}
			System.out.println();
			treeTrace(n.left);
			treeTrace(n.right);
		}
	}
 
	// Method to create a copy of the tree and return the root
	public static Node duplicateTree(Node root) 
	{
		if (root == null)
		{	
			return root;
		}
		Node n = new Node();
		n.targetAttribute = root.targetAttribute;
		n.parent = root.parent;
		n.label = root.label;
		n.isLeaf = root.isLeaf;
		n.lIndices = root.lIndices;
		n.rIndices = root.rIndices;
		n.left = duplicateTree(root.left); // cloning left child
		n.right = duplicateTree(root.right); // cloning right child
		return n;
	}
	 
	// Method to get Accuracy of the tree over Validation Set
	private static double validationAccuracy(String path, Node n) 
	{
		int[][] validation = createValidationSet(path);
		double total = 0;
		for (int i = 1; i < validation.length; i++) 
		{
			total += checkClassification(validation[i], n);
		}
		return (total/validation.length);
	}

	// Method to verify the classification of example as per the constructed tree
	private static int checkClassification(int[] vals, Node n) 
	{
		int pos = n.targetAttribute, correct = 0;
		Node tNode = n;
		while (tNode.label == -1) 
		{
			if (vals[pos] == 1) 
			{
				tNode = tNode.right;
			} 
			else 
			{
				tNode = tNode.left;
			}
			if (tNode.label == 1 || tNode.label == 0) 
			{
				if (vals[vals.length - 1] == tNode.label) 
				{
					correct = 1;
					break;
				} 
				else 
				{
					break;
				}
			}
			pos = tNode.targetAttribute;
		}
		return correct;
	}

	// Method to construct and return Validation set
	private static int[][] createValidationSet(String path) 
	{
		String file = path;
		BufferedReader br = null;
		String lines = "";
		String delimit = ",";
		int[] ft = getFeatAndLen(path);
		int[][] validSet = new int[ft[1]][ft[0]];
		try 
		{
			int i = 0, count = 0;
			br = new BufferedReader(new FileReader(file));
			while ((lines = br.readLine()) != null) 
			{
				String[] ls = lines.split(delimit);
				int j = 0;
				if (count == 0) 
				{
					count++;
					continue;
				} 
				else 
				{
					for (String l : ls) 
					{
						validSet[i][j++] = Integer.parseInt(l);
					}
				}
				i++;
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			if (br != null) 
			{
				try 
				{
					br.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		return validSet;
	}

	// Method to get the maximum value of the Target Attribute among the nodes present in the tree
	private static int getMaxValueAtNode(Node root, int[][] vals, int ft) 
	{
		int oneCount = 0, zeroCount = 0;
		if (root.lIndices != null) 
		{
			for (int i : root.lIndices) 
			{
				if (vals[i][ft] == 1) 
				{
					oneCount++;
				} 
				else 
				{
					zeroCount++;
				}
			}
		}

		if (root.rIndices != null) 
		{
			for (int i : root.rIndices) 
			{
				if (vals[i][ft] == 1) 
				{
					oneCount++;
				} 
				else 
				{
					zeroCount++;
				}
			}
		}
		if(oneCount>zeroCount)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	// Method to buil the index and the nodes to use in pruning
	private static void createArray(Node root, Node[] nList) 
	{
		if (root == null || root.isLeaf) 
		{
			return;
		}
		nList[count++] = root;
		if (root.left != null) 
		{
			createArray(root.left, nList);
		}
		if (root.right != null) 
		{
			createArray(root.right, nList);
		}
	}

	// Method to count the number of non-leaf nodes.
	private static int totalNonLeafNodes(Node root) 
	{
		if (root == null || root.isLeaf)
		{	
			return 0;
		}
		else
		{
			return (1 + totalNonLeafNodes(root.left) + totalNonLeafNodes(root.right));
		}
	}

	// Method to load test data into local data structures
	private static int[][] getTestingData(String path) 
	{
		String file = path;
		BufferedReader br = null;
		String lines = "";
		String delimit = ",";
		int[] fLen = getFeatAndLen(path);
		int[][] valid = new int[fLen[1]][fLen[0]];
		try 
		{
			br = new BufferedReader(new FileReader(file));
			int i = 0, count = 0;
			while ((lines = br.readLine()) != null) 
			{
				String[] paras = lines.split(delimit);
				int j = 0;
				if (count == 0) 
				{
					count++;
					continue;
				}
				else 
				{
					for (String para : paras) 
					{
						valid[i][j++] = Integer.parseInt(para);
					}
				}
				i++;
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally 
		{	
			if (br != null) 
			{	
				try 
				{
					br.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		return valid;
	}

	// Method to print tree if passed "Yes"
	private static void treeTrace(Node root, int lines, String[] fn) 
	{
		int print = lines;
		if (root.isLeaf) 
		{
			System.out.println(" " + root.label);
			return;
		}
		for (int i = 0; i < print; i++) 
		{
			System.out.print("| ");
		}
		if (root.left != null && root.left.isLeaf && root.targetAttribute !=-1)
		{	
			System.out.print(fn[root.targetAttribute] + "= 0 :");
		}
		else if(root.targetAttribute !=-1)
		{
			System.out.println(fn[root.targetAttribute] + "= 0 :");
		}
		lines++;
		treeTrace(root.left, lines, fn);
		for (int i = 0; i < print; i++) 
		{
			System.out.print("| ");
		}
		if (root.right != null && root.right.isLeaf&& root.targetAttribute !=-1)
		{
			System.out.print(fn[root.targetAttribute] + "= 1 :");
		}
		else if(root.targetAttribute !=-1)
		{
			System.out.println(fn[root.targetAttribute] + "= 1 :");
		}
		treeTrace(root.right, lines, fn);
	}

	// Method to get accuracy over Test Data
	private static double testAccuracy(String path, Node root) 
	{
		int[][] test = getTestingData(path);
		double accuracy = 0;
		for (int i = 0; i < test.length; i++) 
		{
			accuracy += checkClassification(test[i], root);
		}
		return (accuracy / test.length);
	}

	// To return the log value
	private static double logValue(double d) 
	{
		return (Math.log10(d) / Math.log10(2));
	}

	// Main method
	public static void main(String[] args) 
	{
		if (args.length != 6) // Check if 6 arguments
		{
			System.out.println("Please provide 6 arguments.\n");
			return;
		}

		int L = Integer.parseInt(args[0]), K = Integer.parseInt(args[1]), h;
		int[] fLen = getFeatAndLen(args[2]);
		int[][] vals = new int[fLen[1]][fLen[0]];
		String[] ft = new String[fLen[0]];
		Node root[] = new Node[2];
		Node treePruned[] = new Node[2];
		for(h=1;h>=0;h--)
		{
			int[] done = new int[fLen[0]];
			int[] indices = new int[vals.length];
			loadVals(args[2], vals, ft, done, indices, fLen[0]);
			if(h==0)
			{	
				System.out.println("\nDecision Tree(Variance Impurity heuristic): \t\n");
			}
			else
			{
				System.out.println("\nDecision Tree(Information Gain heuristic): \t\n");
			}
			root[h] = createTree(null, vals, done, fLen[0] - 1, indices, null,h);
			treePruned[h] = postPruning(args[3], L, K, root[h], vals, fLen[0] - 1);
			System.out.println("Accuracy(Tesing data) for Decision Tree: \t" + testAccuracy(args[4], root[h])+ "\n");
			System.out.println("Accuracy(Tesing data) for Pruned Tree: \t\t" + testAccuracy(args[4], treePruned[h])+ "\n");
			if (args[5].equalsIgnoreCase("yes"))
			{
				System.out.println("Before Pruning: \t\n");
				treeTrace(root[h], 0, ft);
				System.out.println("\nAfter Pruning: \t\n");
				treeTrace(treePruned[h], 0, ft);
			}
		}
	}
}
