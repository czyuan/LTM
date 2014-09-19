package fim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

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
	public ArrayList<ItemSet> runToSizeK(int K) {
		// Create F1, i.e., frequent item set with 1 item only.
		ArrayList<String> frequent1 = new ArrayList<String>();
		for (Entry<String, Integer> entry : transactions.mpItemToCount
				.entrySet()) {
			String item = entry.getKey();
			int support = entry.getValue();
			if (support >= minSup) {
				frequent1.add(item);
			}
		}
		// We sort the list of candidates by lexical order because Apriori needs
		// a total in order to work correctly.
		Collections.sort(frequent1, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});

		if (frequent1.size() == 0) {
			return new ArrayList<ItemSet> (); // No frequent patterns found.
		}

		ArrayList<ItemSet> frequents = null;
		for (int k = 2; k <= K; ++k) {
			ArrayList<ItemSet> candidates = null;
			if (k == 2) {
				candidates = generateCandidateOfSize2(frequent1);
			} else {
				candidates = generateCandidateOfSizeK(frequents);
			}
			frequents = pruneCandiatesBySupport(candidates);
			if (frequents.size() == 0) {
				break;
			}
		}

		return frequents;
	}

	/**
	 * Generate candidates from F1 (i.e., frequent item set with 1 item only).
	 */
	private ArrayList<ItemSet> generateCandidateOfSize2(
			ArrayList<String> frequent1) {
		ArrayList<ItemSet> candidates = new ArrayList<ItemSet>();
		for (int i = 0; i < frequent1.size(); ++i) {
			String item1 = frequent1.get(i);
			for (int j = i + 1; j < frequent1.size(); ++j) {
				String item2 = frequent1.get(j);
				ArrayList<String> items = new ArrayList<String>();
				items.add(item1);
				items.add(item2);
				candidates.add(new ItemSet(items));
			}
		}
		// No need to check the subsets of candidates.
		return candidates;
	}

	/**
	 * Generate candidates Cks from Fks (which is F_{k-1}).
	 */
	private ArrayList<ItemSet> generateCandidateOfSizeK(
			ArrayList<ItemSet> frequents) {
		ArrayList<ItemSet> candidates = new ArrayList<ItemSet>();
		for (int i = 0; i < frequents.size(); ++i) {
			ItemSet fk_i = frequents.get(i);
			for (int j = i + 1; j < frequents.size(); ++j) {
				ItemSet fk_j = frequents.get(j);
				if (fk_i.sharesPrefixExceptLastOne(fk_j)) {
					ItemSet candidate = fk_i.getPrefixMergedItemSet(fk_j);
					if (checkAllSubsetsAreFrequent(candidate, frequents)) {
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}

	/**
	 * Check if all subsets of candidate are in frequents. Since frequents are
	 * sorted, we can use binary search here.
	 */
	private boolean checkAllSubsetsAreFrequent(ItemSet candidate,
			ArrayList<ItemSet> frequents) {
		for (int removePosition = 0; removePosition < candidate.size(); ++removePosition) {
			// Check the subset of candidate (remove the item in
			// removePosition).

			int left = 0;
			int right = frequents.size();
			boolean found = false;
			while (left <= right) {
				int mid = (left + right) >> 1;
				int compareValue = candidate.compareToExcludingIndex(
						frequents.get(mid), removePosition);
				if (compareValue < 0) {
					right = mid - 1;
				} else if (compareValue > 0) {
					left = mid + 1;
				} else {
					found = true;
					break;
				}
			}
			if (!found) {
				// This subset is not in the frequents, so this candidate is
				// not valid.
				return false;
			}
		}
		return true;
	}

	private ArrayList<ItemSet> pruneCandiatesBySupport(
			ArrayList<ItemSet> candidates) {
		ArrayList<ItemSet> frequents = new ArrayList<ItemSet>();
		for (ItemSet candidate : candidates) {
			if (transactions.getFrequency(candidate.items) >= minSup) {
				frequents.add(candidate);
			}
		}
		return frequents;
	}
}
