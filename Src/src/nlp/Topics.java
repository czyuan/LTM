package nlp;

import java.util.ArrayList;
import java.util.Iterator;

import utility.ItemWithValue;

/**
 * A list of topics where each topic is represented by its top words.
 */
public class Topics implements Iterable<Topic> {
	public ArrayList<Topic> topicList = null;

	public Topics() {
		topicList = new ArrayList<Topic>();
	}

	public Topics(ArrayList<ArrayList<ItemWithValue>> topWordListOfList) {
		topicList = new ArrayList<Topic>();
		for (ArrayList<ItemWithValue> topWordList : topWordListOfList) {
			Topic topic = new Topic(topWordList);
			topicList.add(topic);
		}
	}

	public void addTopics(Topics topics) {
		for (Topic topic : topics) {
			topicList.add(topic);
		}
	}

	public int size() {
		return topicList.size();
	}

	@Override
	public Iterator<Topic> iterator() {
		return topicList.iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Topic topic : topicList) {
			sb.append(topic.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

}
