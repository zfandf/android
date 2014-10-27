import java.util.ArrayList;

public class Test {
	
	static ArrayList<String> list = new ArrayList<String>();
	
	public static void main(String[] args) {
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");
		list.add("f");
		
		System.out.println(getIndex(list, "a"));
	}
	
	public static int getIndex(ArrayList<String> list, String v) {
		return getIndex(list, v, 0, list.size());
	}
	public static int getIndex(ArrayList<String> list, String v, int start, int end) {
		if (start == end) {
			if (list.get(start).compareTo(v) == 0) {
				return start;
			} else {
				return -1;
			}
		}
		
		int half = (start + end) / 2;
		String value = list.get(half);
		if (value.compareTo(v) == 0) {
			return half;
		} else if (value.compareTo(v) > 0) {
			if (half + 1 == end) {
			}
			return getIndex(list, v, half + 1, end);
		} else {
			return getIndex(list, v, start, half - 1);
		}
	}
}
