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
	
	public MacroPlayer(Robot robot, RS_ML_v2 view) {
		this.robot = robot;
		this.view = view;
		macroTaskList = new ArrayList<MacroTask>();
	}
	
	public void macroTick() {
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
		}
		else {
			//get new task in available
			if(macroTaskList!=null && macroTaskList.size()>0) {
				currentTask = macroTaskList.get(0);
				currentTask.activateMacro();
			}
		}
	}
	
	public void applyAction(MacroAction action) {
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
	
	public void clearMacroTask() {
		macroTaskList = new ArrayList<MacroTask>();
		currentTask = null;
	}
	
	public void appenComboMacro() {
		macroTaskList.add(new MacroTask(0,MacroAction.MOVE_TO_WEAPON));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(10,MacroAction.MOVE_TO_ENEMY));
		macroTaskList.add(new MacroTask(20,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(1600,MacroAction.MOVE_TO_WEAPON));
		macroTaskList.add(new MacroTask(10,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
		macroTaskList.add(new MacroTask(20,MacroAction.MOVE_TO_ENEMY));
		macroTaskList.add(new MacroTask(5,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
	}
	
	public void appendEatMacro() {
		macroTaskList.add(new MacroTask(0,MacroAction.MOVE_TO_FOOD));
		macroTaskList.add(new MacroTask(5,MacroAction.PRESS));
		macroTaskList.add(new MacroTask(5,MacroAction.RELEASE));
	}

}
