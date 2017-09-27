import java.awt.Color;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MacroPlayer {

	private ArrayList<MacroTask> macroTaskList;
	
	public MacroTask currentTask = null;
	public Robot robot;
	private RS_ML_v2 view;
	private float progressValue = 0;
	private long macroStartTime = 0;
	private float maxTime;
	public  boolean isMacroRunning = false;
	public MacroPlayer(Robot robot, RS_ML_v2 view) {
		this.robot = robot;
		this.view = view;
		macroTaskList = new ArrayList<MacroTask>();
	}
	
	public float macroTick() {
		if(currentTask!=null) {
			if(currentTask.isMacroPlay() == true) {
				//apply current task
				applyAction(currentTask.getAction()); 
				
				//get new task if available
				currentTask = null;
				macroTaskList.remove(0);
				if(macroTaskList!=null && macroTaskList.size()>0) {
					currentTask = macroTaskList.get(0);
					currentTask.activateMacro();
				}
			}
			progressValue = (float)(System.currentTimeMillis()-macroStartTime)/maxTime;
			progressValue = progressValue<=1f?progressValue:1f;
			
			return progressValue; 
		}
		else {
			//get new task in available
			if(macroTaskList!=null && macroTaskList.size()>0) {
				currentTask = macroTaskList.get(0);
				currentTask.activateMacro();
			}
			else {
				isMacroRunning = false;
			}
		}
		
		return -1;
	}
	
	public void applyAction(MacroAction action) {
		//System.out.println("Doing "+action);
		if(action==MacroAction.PRESS) {
			robot.mousePress(MouseEvent.BUTTON1_MASK);
		}
		else if(action==MacroAction.RELEASE) {
			robot.mouseRelease(MouseEvent.BUTTON1_MASK);
		}
		else if(action==MacroAction.MOVE_TO_ENEMY) {
			Point enemyPosition = view.getEnemyPosition();
			robot.mouseMove(enemyPosition.x, enemyPosition.y);
		}
		else if(action==MacroAction.MOVE_TO_FOOD) {
			Point foodPosition = view.getFoodPosition();
			robot.mouseMove(foodPosition.x, foodPosition.y);
		}
		else if(action==MacroAction.MOVE_TO_WEAPON) {
			Point weaponPosition = view.getWeaponPosition();
			robot.mouseMove(weaponPosition.x, weaponPosition.y);
		}
		else {
			
		}
	}
	
	private void clearMacroTask() {
		macroStartTime = System.currentTimeMillis();
		progressValue = 0;
		macroTaskList = new ArrayList<MacroTask>();
		currentTask = null;
	}

	public void appendComboMacro() {
		clearMacroTask();
		maxTime = 1675f+100f;
		macroTaskList.add(new MacroTask(0,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(5,MacroAction.MOVE_TO_WEAPON));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(10,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(10,MacroAction.MOVE_TO_ENEMY));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(10,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(1600,MacroAction.MOVE_TO_WEAPON));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(10,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(100,MacroAction.NONE));
		/*macroTaskList.add(new MacroTask(10,MacroAction.MOVE_TO_ENEMY));
		macroTaskList.add(new MacroTask(20,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(10,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(400,MacroAction.NONE));*/
		isMacroRunning = true;
	}
	
	public void appendEatMacro() {
		clearMacroTask();
		maxTime = 100f+75f;
		macroTaskList.add(new MacroTask(0,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(5,MacroAction.MOVE_TO_FOOD));
		macroTaskList.add(new MacroTask(20,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(10,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(10,MacroAction.MOVE_TO_ENEMY));
		macroTaskList.add(new MacroTask(20,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(10,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(100,MacroAction.NONE));
		isMacroRunning = true;
	}
	
	public void appendClickEnemyMacro() {
		clearMacroTask();
		maxTime = 100f+ 30f;
		macroTaskList.add(new MacroTask(0,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(10,MacroAction.MOVE_TO_ENEMY));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(10,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(100,MacroAction.NONE));
		isMacroRunning = true;
	}
	
	
}
