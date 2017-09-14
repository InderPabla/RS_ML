import java.util.ArrayList;

public class MacroPlayer {

	ArrayList<MacroTask> macroTaskList;
	MacroTask currentTask = null;
	
	public MacroPlayer() {
		macroTaskList = new ArrayList<MacroTask>();
	}
	
	public void macroTick() {
		if(currentTask!=null) {
			
		}
	}
	
	public void clearMacroTask() {
		macroTaskList = new ArrayList<MacroTask>();
	}

}
