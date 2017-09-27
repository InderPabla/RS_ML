import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class FixTheSet {

	public FixTheSet() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Scanner scanner = new Scanner(new FileReader("fighting_data_set_8_combo_2.txt"));
			/*File fixed = new File("fighting_data_set_12_norm_2.txt");
			PrintWriter writer = new PrintWriter(fixed);*/
			
			String oldLine = "";
			int count = 0;
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String split[] = line.split(",");
				line = split[0]+","+split[1]+split[3]+","+split[1]+split[4]+","+split[5];
				/*String split[] = line.split(",");
				String newLine = "";
				for(int i = 3; i<split.length;i++) {
					newLine+=split[i];
					if(i<split.length-1)
						newLine+=",";
				}*/
				//writer.println(newLine);
				
				if(line.equals(oldLine) == false)
					count++;
				oldLine = line;
			}
			System.out.println(count);
			//writer.close();
			scanner.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
