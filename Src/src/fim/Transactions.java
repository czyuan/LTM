package fim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import topicclustering.Cluster;
import topicclustering.ClusterObject;
import utility.ItemWithValue;

/**
 * The transactions for frequent itemset mining.
 * 
 * Each transaction is a list of word string that ranks on the top of a topic.
 */
public class Transactions {
	private ArrayList<ArrayList<String>> transactionList = null;
	private HashSet<String> itemSet = null;

	public Transactions() {
		transactionList = new ArrayList<ArrayList<String>>();
		itemSet = new HashSet<String>();
	}

	/**
	 * Convert a list of cluster-objects in a cluster to transactions. Each
	 * transaction is a list of top words in a cluster-object.
	 */
	public Transactions(Cluster topicCluster) {
		transactionList = new ArrayList<ArrayList<String>>();
		itemSet = new HashSet<String>();

		for (ClusterObject co : topicCluster) {
			ArrayList<String> transaction = new ArrayList<String>();
			for (ItemWithValue iwv : co.topic.topWordList) {
				transaction.add(iwv.getIterm().toString());
			}
			Collections.sort(transaction); // Sort the items alphabetically.
			this.addTransaction(transaction);
		}
	}

	public void addTransactions(ArrayList<ArrayList<String>> transactions) {
		transactionList.addAll(transactions);
		for (ArrayList<String> transaction : transactions) {
			addIntoItemSet(transaction);
		}
	}

	public void addTransaction(ArrayList<String> transaction) {
		transactionList.add(transaction);
		addIntoItemSet(transaction);
	}

	private void addIntoItemSet(ArrayList<String> transaction) {
		for (String item : transaction) {
			itemSet.add(item);
		}
	}

	public ArrayList<String> getSortedItemList() {
		ArrayList<String> list = new ArrayList<String>(itemSet);
		Collections.sort(list);
		return list;
	}

	public int size() {
		return transactionList.size();
	}

	/**
	 * Very simple implementation. For each transaction, check if it contains
	 * all the items in the list.
	 */
	public int getFrequency(ArrayList<String> itemList) {
		int count = 0;
		for (ArrayList<String> transaction : transactionList) {
			if (transactionContainsItemSet(transaction, itemList)) {
				++count;
			}
		}
		return count;
	}

	/**
	 * O(n) algorithm to compare as both list are sorted already.
	 */
	private boolean transactionContainsItemSet(ArrayList<String> transaction,
			ArrayList<String> itemList) {
		int i = 0;
		int j = 0;
		for (; i < transaction.size() && j < itemList.size();) {
			if (transaction.get(i).compareTo(itemList.get(j)) == 0) {
				++i;
				++j;
			} else if (transaction.get(i).compareTo(itemList.get(j)) < 0) {
				++i;
			} else {
				return false;
			}
		}
		return j == itemList.size();
	}

	@Override
	public String toString() {
		StringBuilder sbTransactions = new StringBuilder();
		for (ArrayList<String> transaction : transactionList) {
			StringBuilder sbLine = new StringBuilder();
			for (String word : transaction) {
				sbLine.append(word);
				sbLine.append(' ');
			}
			sbTransactions.append(sbLine.toString().trim());
			sbTransactions.append(System.getProperty("line.separator"));
		}
		return sbTransactions.toString();
	}
}
