package fim;

import java.util.ArrayList;

/**
 * This class implements the basic Apriori algorithm. This is a simple
 * implementation as we only need to consider the item set of size 2. If
 * efficiency is a concern, a better implementation or mining algorithm should
 * be used.
 * 
 * @author Zhiyuan (Brett) Chen
 * @email czyuanacm@gmail.com
 */
public class Apriori {
	private Transactions transactions = null; // The items in each transaction
												// have been sorted in a certain
												// order (e.g., alphabetically).
	private int minSup = 0; // Minimum support.

	public Apriori(Transactions transactions2, int minSup2, double minSupPer) {
		transactions = transactions2;
		minSup = Math.max(minSup2,
				(int) Math.ceil(transactions.size() * minSupPer));
	}

	/**
	 * Run Apriori algorithm, return the frequent item sets.
	 */
	public ArrayList<ArrayList<String>> runToSizeK(int K) {
		ArrayList<ArrayList<String>> Cks = new ArrayList<ArrayList<String>>(); // Candidates.
		ArrayList<ArrayList<String>> Fks = new ArrayList<ArrayList<String>>(); // Frequents.
		int k = 1;
		// Create F1, i.e., frequent item set with 1 item only.
		for (String item : transactions.getSortedItemList()) {
			ArrayList<String> Ck = new ArrayList<String>();
			Ck.add(item);
			if (transactions.getFrequency(Ck) >= minSup) {
				Fks.add(Ck);
			}
		}

		for (k = 2; k <= K && !Fks.isEmpty(); ++k) {
			// Candidate generation.
			Cks = generateCandidate(Fks);
			// Pruning.
			Fks = pruneFrequentItemSet(Cks);
		}
		return Fks;
	}

	/**
	 * Generate candidates Cks from Fks (which is F_{k-1}).
	 */
	private ArrayList<ArrayList<String>> generateCandidate(
			ArrayList<ArrayList<String>> Fks) {
		ArrayList<ArrayList<String>> Cks = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < Fks.size(); ++i) {
			ArrayList<String> Fk_i = Fks.get(i);
			for (int j = i + 1; j < Fks.size(); ++j) {
				ArrayList<String> Fk_j = Fks.get(j);
				if (sharePrefixKMinus1(Fk_i, Fk_j)) {
					ArrayList<String> Ck = mergePrefix(Fk_i, Fk_j);
					Cks.add(Ck);
				}
			}
		}
		return Cks;
	}

	/**
	 * Judge if fk_i and fk_j share the same except the last item.
	 */
	private boolean sharePrefixKMinus1(ArrayList<String> Fk_i,
			ArrayList<String> Fk_j) {
		int len = Fk_i.size();
		for (int i = 0; i < len - 1; ++i) {
			if (Fk_i.get(i).compareTo(Fk_j.get(i)) != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create a new candidate by merge two frequent item sets that share the
	 * prefix.
	 */
	private ArrayList<String> mergePrefix(ArrayList<String> Fk_i,
			ArrayList<String> Fk_j) {
		ArrayList<String> Ck = new ArrayList<String>();
		int len = Fk_i.size();
		for (int i = 0; i < len - 1; ++i) {
			Ck.add(Fk_i.get(i));
		}
		if (Fk_i.get(len - 1).compareTo(Fk_j.get(len - 1)) < 0) {
			Ck.add(Fk_i.get(len - 1));
			Ck.add(Fk_j.get(len - 1));
		} else {
			Ck.add(Fk_j.get(len - 1));
			Ck.add(Fk_i.get(len - 1));
		}
		return Ck;
	}

	private ArrayList<ArrayList<String>> pruneFrequentItemSet(
			ArrayList<ArrayList<String>> Cks) {
		ArrayList<ArrayList<String>> Fks = new ArrayList<ArrayList<String>>();
		for (ArrayList<String> Ck : Cks) {
			if (transactions.getFrequency(Ck) >= minSup) {
				Fks.add(Ck);
			}
		}
		return Fks;
	}
}
