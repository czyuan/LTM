package fim;

import java.util.ArrayList;

import utility.ExceptionUtility;

public class ItemSet {
	public ArrayList<Integer> items = null;
	public int support = 0;

	public ItemSet() {
		items = new ArrayList<Integer>();
	}

	public ItemSet(ArrayList<Integer> items2) {
		items = items2;
	}

	public int get(int index) {
		ExceptionUtility.assertAsException(index >= 0 && index < items.size());
		return items.get(index);
	}

	/**
	 * Check if this itemset shares the prefix (except the last one) with the
	 * other itemset.
	 */
	public boolean sharesPrefixExceptLastOne(ItemSet fk_j) {
		for (int i = 0; i < this.size() - 1; ++i) {
			if (this.get(i) != fk_j.get(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create a new itemset by merge two itemsets that share the prefix (except
	 * the last one).
	 */
	public ItemSet getPrefixMergedItemSet(ItemSet fk_j) {
		ArrayList<Integer> items = new ArrayList<Integer>();
		int len = this.size();
		for (int i = 0; i < len - 1; ++i) {
			items.add(this.get(i));
		}
		if (this.get(len - 1) < fk_j.get(len - 1)) {
			items.add(this.get(len - 1));
			items.add(fk_j.get(len - 1));
		} else {
			items.add(fk_j.get(len - 1));
			items.add(this.get(len - 1));
		}
		return new ItemSet(items);
	}

	/**
	 * Compare two itemsets ingoring the item in one position.
	 */
	public int compareToExcludingIndex(ItemSet itemSet, int ignorePosition) {
		for (int i = 0; i < this.size(); ++i) {
			if (i >= itemSet.size()) {
				return 1;
			}
			if (i == ignorePosition) {
				continue;
			}
			int compareValue = this.get(i) - itemSet.get(i);
			if (compareValue != 0) {
				return compareValue;
			}
		}
		if (this.size() < itemSet.size()) {
			return -1;
		} else {
			return 0;
		}
	}

	public int size() {
		return items.size();
	}

}
