
public class MacroTask {

	
	private MacroAction action;
	
	private boolean activateMacro;
	
	private long taskWait;
	private long macroEndTime;
	
	public MacroTask(long taskWait,MacroAction action) {
		this.taskWait = taskWait;
		this.action = action;
		
		activateMacro = false;
	}
	
	//Macro becomes the current macro
	public void activateMacro() {
		activateMacro = true;
		macroEndTime = System.currentTimeMillis()+taskWait;
	}
	
	//macro tick
	public boolean isMacroPlay() {
		long currentTime = System.currentTimeMillis();
		if(currentTime>macroEndTime) {
			return true;
		}
		
		return false;
	}
	
	public MacroAction getAction() {
		return action;
	}
}
