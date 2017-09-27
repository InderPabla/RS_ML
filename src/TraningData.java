import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TraningData {

	private ArrayList<float[]> dataList;
	File file = null;
	
	private boolean eatDataGatheringMode = false;
	private boolean comboDataGatheringMode = false;
	 
	  
	TraningData(String fileName) {
		file = new File(fileName);
		dataList = new ArrayList<float[]>();
	}

	
	public void add(float... data) {
		/*
		 eatValue, comboValue, isMacroing,enemyHealth, playerHealth, bowResetValue, COMBO, EAT, NONE
		 */
	
		if(eatDataGatheringMode == true && dataList.size()>11) {
			if(data[7]==1f) {
				dataList.add(data);
			}
			else {
				for(int i = dataList.size()-10;i<dataList.size();i++) {
					if( dataList.get(i)[7]==1f) {
						dataList.add(data);
						break;
					}
				}
			}
			
		}
		else if (comboDataGatheringMode == true && dataList.size()>11) {
			if(data[6]==1f) {
				dataList.add(data);
			}
			else {
				for(int i = dataList.size()-10;i<dataList.size();i++) {
					if( dataList.get(i)[6]==1f) {
						dataList.add(data);
						break;
					}
				}
			}
		}
		else {
			dataList.add(data);
		}
		
	}
	
	public boolean save() {
		PrintWriter writer = null;
		
		if(file.exists() == false) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		
		System.out.println("Saving: "+file.getAbsolutePath());
		try {
			writer = new PrintWriter(file);
			
			for(int i = 0; i < dataList.size();i++) {
				float[] data = dataList.get(i);
				String dataStr = "";
				for(int j = 0; j<data.length;j++) {
					if(j == 0)
						dataStr+=data[j];
					else 
						dataStr+=","+data[j];
				}
				//System.out.println(dataStr);
				writer.println(dataStr);
			} 
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			writer.close();
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public int getDataCount() {
		return dataList.size();
	}
	
	public void removeLast() {
		if(dataList.size()>0) {
			dataList.remove(dataList.size()-1);
		}
	}
	
}
