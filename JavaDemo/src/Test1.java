import java.io.File;


public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File file = new File(File.separator + "aaaa");
		String name = file.getName();
		
		int dotPost = name.lastIndexOf(".");
		if (dotPost != -1) {
			String n = name.substring(dotPost + 1).toLowerCase();
			System.out.println(dotPost + ", " + n);
		} else {
		}
		
	}
}
