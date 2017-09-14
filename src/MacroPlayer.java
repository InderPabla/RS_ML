import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MacroPlayer {

	private ArrayList<MacroTask> macroTaskList;
	public MacroTask currentTask = null;
	
	public MacroPlayer() {
		macroTaskList = new ArrayList<MacroTask>();
	}
	
	public void macroTick() {
		if(currentTask!=null) {
			if(currentTask.isMacroPlay() == true) {
				//do something with action
				applyAction(currentTask.getAction());
				//do something with action
				
				
				currentTask = null;
				macroTaskList.remove(0);
				if(macroTaskList!=null && macroTaskList.size()>0) {
					currentTask = macroTaskList.get(0);
				}
			}
		}
		else {
			if(macroTaskList!=null && macroTaskList.size()>0) {
				currentTask = macroTaskList.get(0);
			}
		}
	}
	
	public void applyAction(MacroAction action) {
		
	}
	
	public void clearMacroTask() {
		macroTaskList = new ArrayList<MacroTask>();
		currentTask = null;
	}
	
	public void appenComboMacro() {
		macroTaskList.add(new MacroTask(0,MacroAction.MOVE));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(10,MacroAction.MOVE));
		macroTaskList.add(new MacroTask(20,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(1600,MacroAction.MOVE));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(20,MacroAction.MOVE));
		macroTaskList.add(new MacroTask(5,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
	}
	
	public void appendEatMacro() {
		macroTaskList.add(new MacroTask(0,MacroAction.MOVE));
		macroTaskList.add(new MacroTask(5,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
	}

}
