
import java.awt.AWTException;
import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class RS_ML_v2 extends JFrame implements Runnable, NativeKeyListener, MouseListener, MouseMotionListener{
	Dimension dim           = null; 
	BufferedImage cap       = null;
    Graphics bufferGraphics = null; 
    Image offscreen         = null; 
    Font font               = null;
    
    OutputAction action     = null;
    TraningData traningData = null;
    MacroPlayer macroPlayer = null;
    Robot robot             = null;
    
    Rectangle captureRect      = null;
    Rectangle enemyHealthRect  = null;
    Rectangle playerHealthRect = null;
    Rectangle bowDamageRect    = null; 
    Rectangle comboRect,eatRect,noneRect,removeRect;
    AnimateButton comboActionNet,eatActionNet,noneActionNet;
    
    Point enemyPosition   = null;
    Point weaponPosition  = null;
    Point inventoryIndex  = null;
    
    int set                  = 14;
    int waitTime             = 25;
    int ignoreInventorySlots = 3;   
    String fileName          = "";
    
    float enemyHealth      = -1f;
    float playerHealth     = -1f;
    float bowResetValue    = -1f;
    float bowResetSubtract = 0.025f;
    float macroTickValue   = -1f;

    
    int numberOfInputs       = 6;
    int numberOfOutputs      = 3;
    int enemyHealthZeroCount = 0;
    
    //Server socket for Python to connect to
  	private ServerSocket serverSocket;
  	private Socket socket;
  	private boolean connected = false; //if server is connected to client
    private boolean isRecordLocked = true;
    
    private float rndEnemyHealth = 0;
    private float rndPlayerHealth = 0;
    private float rndBowResetValue = 0;
    private float rndBowResetProb = 0.6f;
     
    private Random random = new Random();
    
    int mX,mY;
    private boolean escapePressed = false;
    
    public void tick() {
    	cap = robot.createScreenCapture(captureRect);
    	bufferGraphics.drawImage(cap,0,0,null);
    	
    	getEnemyHealth();
    	getPlayerHealth();
    	bowResetValue();
    	macroTickValue = macroPlayer.macroTick();
    	
    	if(macroPlayer.isMacroRunning == false) {
    		if(action!=OutputAction.NONE)
    			 noneActionNet.animate();
    		/*if(action!=OutputAction.NONE && action!=OutputAction.CLICK_ENEMY) {
    			action = OutputAction.CLICK_ENEMY;
    			macroPlayer.appendClickEnemyMacro();
    		}
    		else {*/
    			action = OutputAction.NONE;
    		//}
    		
    	}
    	
    	createTickData();

    	visualRenderer();
    }
    
    public void createTickData() {
    	float eatValue = -1;
    	float comboValue = -1;
    	float isMacroing = macroPlayer.isMacroRunning == true?1f:-1f;
    	
    	if(action==OutputAction.COMBO) {
    		comboValue = macroTickValue;
    		
    	}
    	else if(action==OutputAction.EAT) {
    		eatValue = macroTickValue;
    	}
    	
    	if(enemyHealth>0) {
    		
    		if(enemyHealth<=0)
    			enemyHealthZeroCount++;
    		else 
    			enemyHealthZeroCount = 0;
    		
	    	//System.out.println("Inputs: "+eatValue+" "+comboValue+" "+isMacroing+" "+enemyHealth+" "+playerHealth+" "+bowResetValue);
	    	//System.out.println("Outputs: "+action);
    		if(enemyHealthZeroCount <=3 && isRecordLocked == false) {
    			//System.out.println("Adding");
		    	traningData.add(
		    			//inputs: contains state of the fight and player status
		    			eatValue,
		    			comboValue,
		    			isMacroing,
		    			enemyHealth,
		    			playerHealth,
		    			bowResetValue,
		    			
		    			//outputs: checking if action is equal to any of the enum output action
		    			action==OutputAction.COMBO?1:0,
		    			action==OutputAction.EAT?1:0,
		    			action==OutputAction.NONE?1:0
		    			);
		    	
		    			
    		}
    		
    		if(escapePressed == false) {
	    		if(connected == true) {
	    			DataOutputStream out;
	    			float[] input = new float[] {eatValue,
	    	    			comboValue,
	    	    			isMacroing,
	    	    			enemyHealth,
	    	    			playerHealth,
	    	    			bowResetValue};
	    			
	    			
	    			try {
	    				out = new DataOutputStream(socket.getOutputStream());
	    				out.write(FloatArray2ByteArray(input));
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
	    			
	    			try {
	
						DataInputStream ins = new DataInputStream(socket.getInputStream());
						byte[] bytes = new byte[1];
						ins.readFully(bytes);
						
						int actionIndex = Integer.parseInt(new String(bytes));
						/*ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder());
						String str = bb.toString()
						
						int actionIndex = bb.getInt();*/
						
						if(actionIndex == 0) {
							
							
							if(action!=OutputAction.EAT && isMacroing==-1f) {
								 System.out.println(bowResetValue);
								 action = OutputAction.COMBO;
								 macroPlayer.appendComboMacro();
								 comboActionNet.animate();
							}
							
							 
						}
						else if(actionIndex == 1) {
							if(action!=OutputAction.COMBO && isMacroing==-1f) {
								 action = OutputAction.EAT;
								 macroPlayer.appendEatMacro();
								 eatActionNet.animate();
							}
							
						}
						else if(actionIndex == 2) {
							
							//action = OutputAction.NONE;
						}
					} 
	    			catch (IOException e) {
						e.printStackTrace();
					}
	    			
	
	    		}
	    		else {
	    			/*if(playerHealth<0.31f) {
	    				if(action==OutputAction.NONE ) {
							 action = OutputAction.EAT;
							 macroPlayer.appendEatMacro();
						}
	    			}
	    			else if(enemyHealth<=0.35f && bowResetValue>0.5f) {
	    				if(action==OutputAction.NONE ) {
							 action = OutputAction.COMBO;
							 macroPlayer.appendComboMacro();
						}
	    			}*/
	    		}
    		}
    		
    	}
    	
    	/*if(connected == true) {
			float[] input = new float[] {eatValue,
	    			comboValue,
	    			isMacroing,
	    			enemyHealth,
	    			playerHealth,
	    			bowResetValue};
			
			OutputStream output;
			try {
				output = socket.getOutputStream();
				output.write(FloatArray2ByteArray(input));
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}*/
    	
    	
    }
    
    public static byte[] FloatArray2ByteArray(float[] values){
        ByteBuffer buffer = ByteBuffer.allocate(4 * values.length);

        for (float value : values){
            buffer.putFloat(value);
        }

        return buffer.array();
    }
    
    public void visualRenderer() {
    	bufferGraphics.setColor(new Color(150,120,160));
    	bufferGraphics.fillRect(0, 525, 1000, 190);
    	
    	bufferGraphics.setColor(new Color(150,120,160));
    	bufferGraphics.fillRect(0, 740, 1000, 190);

    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString("--------------------Data Collection Panel--------------------", 10, 515);
    	
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString("--------------------Combat Status Tracking Panel--------------------", 10, 730);
    	
		if(enemyHealth>=0) {
	    	bufferGraphics.setColor(Color.green);
	    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y,(int)(enemyHealthRect.width*enemyHealth), 25);
	    	bufferGraphics.setColor(Color.red);
	    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*enemyHealth),enemyHealthRect.y,(int)(enemyHealthRect.width*(1f-enemyHealth)), 25);
	    	bufferGraphics.setColor(Color.black);
        	bufferGraphics.drawString((enemyHealth*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15);
    	}
    	
    	if(playerHealth>=0) {
	    	bufferGraphics.setColor(Color.green);
	    	bufferGraphics.fillRect(playerHealthRect.x,playerHealthRect.y,(int)(enemyHealthRect.width*playerHealth), 25);
	    	bufferGraphics.setColor(Color.red);
	    	bufferGraphics.fillRect(playerHealthRect.x+(int)(enemyHealthRect.width*playerHealth),playerHealthRect.y,(int)(enemyHealthRect.width*(1f-playerHealth)), 25);
	    	bufferGraphics.setColor(Color.black);
        	bufferGraphics.drawString((playerHealth*100f)+"%", playerHealthRect.x+25,playerHealthRect.y+15);
    	}
    	
    	if(bowResetValue>=0) {
    		bufferGraphics.setColor(new Color(bowResetValue,1f-bowResetValue,1f-bowResetValue));
    		bufferGraphics.fillRect(0,captureRect.height,(int)(captureRect.width*bowResetValue), 25);
    	}
    	
    	if(macroTickValue>=0) {
    		bufferGraphics.setColor(new Color(macroTickValue,1f-macroTickValue,1f-macroTickValue));
    		bufferGraphics.fillRect(0,captureRect.height+25,(int)(captureRect.width*macroTickValue), 25);
    	}
    	
    	bufferGraphics.setColor(Color.yellow);
    	bufferGraphics.drawString("Captured Length: ", captureRect.width+1, 50);
    	bufferGraphics.drawString(traningData.getDataCount()+"", captureRect.width+100, 65);
    	
    	bufferGraphics.drawString(connected==true?"Connected":"Not Connected",captureRect.width+1, 90);
    	
    	bufferGraphics.drawString(isRecordLocked==true?"Not Recording":"Recording",captureRect.width+1, 115);
    	bufferGraphics.drawString("Bow Reset Prob: "+(int)(rndBowResetProb*100f)+"%",captureRect.width+1, 140);
    	
    	int rndEnemyHealthDown = 500;
    	bufferGraphics.setColor(Color.green);
    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndEnemyHealthDown,(int)(enemyHealthRect.width*rndEnemyHealth), 25);
    	bufferGraphics.setColor(Color.red);
    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*rndEnemyHealth),enemyHealthRect.y+rndEnemyHealthDown,(int)(enemyHealthRect.width*(1f-rndEnemyHealth)), 25);
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.drawString((rndEnemyHealth*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndEnemyHealthDown);
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString(": Enemy Health Data", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndEnemyHealthDown);
    	
    	int rndPlayerHealthDown = 550;
    	bufferGraphics.setColor(Color.green);
    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndPlayerHealthDown,(int)(enemyHealthRect.width*rndPlayerHealth), 25);
    	bufferGraphics.setColor(Color.red);
    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*rndPlayerHealth),enemyHealthRect.y+rndPlayerHealthDown,(int)(enemyHealthRect.width*(1f-rndPlayerHealth)), 25);
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.drawString((rndPlayerHealth*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndPlayerHealthDown);
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString(": Player Health Data", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndPlayerHealthDown);
    	
    	int rndBowResetValueDown = 600;
    	if(rndBowResetValue!=-1) {
    		bufferGraphics.setColor(Color.magenta);
	    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(1f-rndBowResetValue)), 25);
	    	bufferGraphics.setColor(Color.orange);
	    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*(1f-rndBowResetValue)),enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(rndBowResetValue)), 25);
	    	bufferGraphics.setColor(Color.black);
        	bufferGraphics.drawString((rndBowResetValue*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndBowResetValueDown);
        	bufferGraphics.setColor(Color.white);
        	bufferGraphics.drawString(": Bow Reset", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndBowResetValueDown);
    	}
    	else {
    		bufferGraphics.setColor(Color.magenta);
	    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(1f-0)), 25);
	    	bufferGraphics.setColor(Color.orange);
	    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*(1f-0)),enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(0)), 25);
	    	bufferGraphics.setColor(Color.black);
        	bufferGraphics.drawString((rndBowResetValue*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndBowResetValueDown);
        	bufferGraphics.setColor(Color.white);
        	bufferGraphics.drawString(": Bow Reset Data", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndBowResetValueDown);
    	}
    	
    	Rectangle button = comboRect;
    	bufferGraphics.setColor(Color.red);
    	bufferGraphics.fillRect(button.x,button.y,button.width, button.height); 
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString("COMBO", button.x+20,button.y+50);
    	
    	button = eatRect;
    	bufferGraphics.setColor(Color.magenta);
    	bufferGraphics.fillRect(button.x,button.y,button.width, button.height); 
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString("EAT", button.x+20,button.y+50);

    	button = noneRect;
    	bufferGraphics.setColor(Color.orange);
    	bufferGraphics.fillRect(button.x,button.y,button.width, button.height); 
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.drawString("NONE", button.x+20,button.y+50);
    	
    	button = removeRect;
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.fillRect(button.x,button.y,button.width, button.height); 
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString("REMOVE LAST DATA", button.x+30,button.y+25);
    	
    	
    	
    	int w = 505;
    	int h = 304;
    	int[] colorRaster = new int[w*h];
    	cap.getRGB(0, 0, w, h, colorRaster, 0, w);
    	ArrayList<TrackedEnemy> enemyHpBars =  new ArrayList<TrackedEnemy>();
    	for(int i = 0; i <h;i++) {
    		for(int j = 0; j <w;j++) {
    			int intColor = colorRaster[i*w + j];
    			
        		if(( intColor==0xFFFF0000) || (intColor==0xFF00FF00 )) {
        			boolean found = false;
        			for(int k = 0; k<enemyHpBars.size();k++) {
        				if(enemyHpBars.get(k).contains(j,i) == true) {
        					
        					found = true;
        					break;
        				}
        			}
        			if(found == false) {
        				TrackedEnemy newEnemy = new TrackedEnemy(new Rectangle(j,i,30,5));
        				enemyHpBars.add(newEnemy);
        				
        				int xend = j;
        				if(w-xend<=30) {
        					newEnemy.isEnemy = false;
        				}
        				else {
        					for(int k = j;k<j+30;k++) {
        						intColor = colorRaster[i*w + k];
        						if(intColor==0xFFFF0000) {
        							newEnemy.redCount ++;
        						}
        						else /*if (intColor==0xFF00FF00 )*/ {
        							newEnemy.greenCount ++;
        						}
        					}
        				}
        				
        				newEnemy.hp = newEnemy.greenCount/(newEnemy.greenCount+newEnemy.redCount);
        				
        				if(w-j <=30)
        					break;
        				else 
        					j+=30;
        			}
        		}
        	}
    	}
    	
    	if(enemyHpBars.size()>0) {
    		
    		TrackedEnemy fightingThis = null;
    		float lowestSub = 1000f;
    		
    		for(int k = 0; k<enemyHpBars.size();k++) {
    			TrackedEnemy enemy = enemyHpBars.get(k);
    			bufferGraphics.setColor(Color.black);
    			bufferGraphics.fillRect(enemy.rect.x,enemy.rect.y,enemy.rect.width, enemy.rect.height); 
    			if(enemy.isEnemy == true) {
    				bufferGraphics.setColor(Color.red);
    				bufferGraphics.drawRect(enemy.rect.x,enemy.rect.y,enemy.rect.width, 30);
    				bufferGraphics.setColor(Color.green);
    				bufferGraphics.drawString((Math.round((enemy.hp*100f) * 100.0f) / 100.0f)+"", enemy.rect.x+5,enemy.rect.y-2);
    				
    				if(Math.abs(enemy.hp-enemyHealth)<lowestSub &&
    						enemy.rect.x<w/2-15 || enemy.rect.x>w/2+17 ) {
    					fightingThis = enemy;
    					lowestSub = Math.abs(enemy.hp-enemyHealth);
    				}
    			}	
    			else { 
    				bufferGraphics.setColor(Color.green);
    				bufferGraphics.drawRect(enemy.rect.x,enemy.rect.y,enemy.rect.width, 30);
    			}
			}
    		
    		if(enemyHealth>0f && fightingThis!=null) {
    			bufferGraphics.setColor(Color.yellow);
				bufferGraphics.drawRect(fightingThis.rect.x-4,fightingThis.rect.y-4,fightingThis.rect.width+8, 30+8);
				enemyPosition.x = (int)fightingThis.rect.x + fightingThis.rect.width/2;
				enemyPosition.y = (int)fightingThis.rect.y+ 15 ;
    		}
    		
    		
    	}
    		
    	
    	if(enemyHealth>0f) {
    		bufferGraphics.setColor(Color.red);
	    	bufferGraphics.fillRect(enemyPosition.x-2,enemyPosition.y-2,4,4); 
    	}
    	
    	
    	int counter = 0;
    	for(int i =0; i <7;i++) {
       		 for(int j =0; j <4;j++) {
       			 counter++;
       			 if(counter >ignoreInventorySlots) {
       				 
   	    			 int x1 = inventoryIndex.x+(j*41)+3;
   	    			 int y1 =(int)(inventoryIndex.y+(i*36f))-2;
   	    			 int intColor1 =cap.getRGB(x1, y1);
   	    			 int red1 = (intColor1>>16)&0xFF;
   		        	 int green1 = (intColor1>>8)&0xFF;
   		        	 int blue1 = (intColor1>>0)&0xFF;
   		        	 float[] hsb1 = new float[3];
   		        	 Color.RGBtoHSB(red1, green1, blue1, hsb1);
   		        	
   		        	 if((hsb1[2]*100f)>=33f) {
   		        		bufferGraphics.setColor(Color.green);
   		        	 }
   		        	 else {
   		        		bufferGraphics.setColor(Color.red);
   		        	 } 
   		        		
   		        	bufferGraphics.drawRect(x1,y1,10,10);
       			 }
       		 }
    	}
    	
    	bufferGraphics.setColor(comboActionNet.tick());
    	bufferGraphics.fillRect(comboActionNet.rect.x,comboActionNet.rect.y, 
    			comboActionNet.rect.width, comboActionNet.rect.height);
    	bufferGraphics.setColor(eatActionNet.tick());
    	bufferGraphics.fillRect(eatActionNet.rect.x,eatActionNet.rect.y, 
    			eatActionNet.rect.width, eatActionNet.rect.height);
    	bufferGraphics.setColor(noneActionNet.tick());
    	bufferGraphics.fillRect(noneActionNet.rect.x,noneActionNet.rect.y, 
    			noneActionNet.rect.width, noneActionNet.rect.height);
    	
    	
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.drawString(comboActionNet.buttonName, 
    			comboActionNet.rect.x+comboActionNet.rect.width/4,
    			comboActionNet.rect.y+comboActionNet.rect.height/2);
    	
    	bufferGraphics.drawString(eatActionNet.buttonName, 
    			eatActionNet.rect.x+eatActionNet.rect.width/4,
    			eatActionNet.rect.y+eatActionNet.rect.height/2);
    	
    	bufferGraphics.drawString(noneActionNet.buttonName, 
    			noneActionNet.rect.x+noneActionNet.rect.width/4,
    			noneActionNet.rect.y+noneActionNet.rect.height/2);
    	
    	bufferGraphics.setColor(Color.red);
    	if(comboActionNet.isAnimating() == true)
    		bufferGraphics.drawRect(comboActionNet.rect.x,comboActionNet.rect.y, 
        			comboActionNet.rect.width, comboActionNet.rect.height);	
    	
    	if(eatActionNet.isAnimating() == true)
    		bufferGraphics.drawRect(eatActionNet.rect.x,eatActionNet.rect.y, 
    				eatActionNet.rect.width, eatActionNet.rect.height);	
    	
    	if(noneActionNet.isAnimating() == true)
    		bufferGraphics.drawRect(noneActionNet.rect.x,noneActionNet.rect.y, 
    				noneActionNet.rect.width, noneActionNet.rect.height);	
    	
    	
    	
    	rndEnemyHealthDown = 550+200;
    	bufferGraphics.setColor(Color.green);
    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndEnemyHealthDown,(int)(enemyHealthRect.width*enemyHealth), 25);
    	bufferGraphics.setColor(Color.red);
    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*enemyHealth),enemyHealthRect.y+rndEnemyHealthDown,(int)(enemyHealthRect.width*(1f-enemyHealth)), 25);
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.drawString((enemyHealth*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndEnemyHealthDown);
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString(": Enemy Health", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndEnemyHealthDown);
    	
    	rndPlayerHealthDown = 550+250;
    	bufferGraphics.setColor(Color.green);
    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndPlayerHealthDown,(int)(enemyHealthRect.width*playerHealth), 25);
    	bufferGraphics.setColor(Color.red);
    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*playerHealth),enemyHealthRect.y+rndPlayerHealthDown,(int)(enemyHealthRect.width*(1f-playerHealth)), 25);
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.drawString((playerHealth*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndPlayerHealthDown);
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.drawString(": Player Health", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndPlayerHealthDown);
    	
    	rndBowResetValueDown = 550+300;
    	if(bowResetValue!=-1) {
    		bufferGraphics.setColor(Color.magenta);
	    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(1f-bowResetValue)), 25);
	    	bufferGraphics.setColor(Color.orange);
	    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*(1f-bowResetValue)),enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(bowResetValue)), 25);
	    	bufferGraphics.setColor(Color.black);
        	bufferGraphics.drawString((bowResetValue*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndBowResetValueDown);
        	bufferGraphics.setColor(Color.white);
        	bufferGraphics.drawString(": Bow Reset", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndBowResetValueDown);
    	}
    	else {
    		bufferGraphics.setColor(Color.magenta);
	    	bufferGraphics.fillRect(enemyHealthRect.x,enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(1f-0)), 25);
	    	bufferGraphics.setColor(Color.orange);
	    	bufferGraphics.fillRect(enemyHealthRect.x+(int)(enemyHealthRect.width*(1f-0)),enemyHealthRect.y+rndBowResetValueDown,(int)(enemyHealthRect.width*(0)), 25);
	    	bufferGraphics.setColor(Color.black);
        	bufferGraphics.drawString((bowResetValue*100f)+"%", enemyHealthRect.x+25,enemyHealthRect.y+15+rndBowResetValueDown);
        	bufferGraphics.setColor(Color.white);
        	bufferGraphics.drawString(": Bow Reset", enemyHealthRect.x+25+100,enemyHealthRect.y+15+rndBowResetValueDown);
    	}
    	
    	rndPlayerHealthDown = 550+250;
    	bufferGraphics.setColor(Color.black);
    	bufferGraphics.drawString("3->512->512->256->256->128->128->64->64->32->32->8->8->3", enemyHealthRect.x+275,enemyHealthRect.y+15+rndPlayerHealthDown);
    	
    }
    
    
    public Point getFoodPosition() {
    	int counter      = 0;
		Point foundPoint = null;
		
		//Iterate through 28 inventory slots
		for(int i =0; i <7;i++) {
    		 for(int j =0; j <4;j++) {
    			 counter++;
    			 if(counter >ignoreInventorySlots) {
    				 int x1 = inventoryIndex.x+(j*41)+3;
   	    			 int y1 =(int)(inventoryIndex.y+(i*36f))-2;
   	    			 int intColor1 =cap.getRGB(x1, y1);
   	    			 int red1 = (intColor1>>16)&0xFF;
   		        	 int green1 = (intColor1>>8)&0xFF;
   		        	 int blue1 = (intColor1>>0)&0xFF;
   		        	 float[] hsb1 = new float[3];
   		        	 Color.RGBtoHSB(red1, green1, blue1, hsb1);
		        	 
		        	 if(hsb1[2]*100f>=33f) {
		        		 foundPoint = new Point(x1,y1);
		        		 break;
		        	 }
    			 }
    		 }
    		 
    		 if(foundPoint!=null)
    			 break;
		}
		
		
		if(foundPoint!=null) {
			foundPoint.x+=captureRect.x;
			foundPoint.y+=captureRect.y;
			return foundPoint;
		}
		
    	return null;
    }
    
    public float bowResetValue() {
    	if(bowResetValue>0) {
    		bowResetValue -= bowResetSubtract;
    		if(bowResetValue <=0f)
    			bowResetValue = -1f;
    	}
    	else {
    		bowResetValue = -1f;
    		boolean bowDamageDone = isBowDamageDone();
    		if(bowDamageDone == true) {
    			bowResetValue = 1f;
    		}
    	}
    	
    	return bowResetValue;
    }
    
    public boolean isBowDamageDone() {
		int color1 = cap.getRGB(bowDamageRect.x, bowDamageRect.y);
		int color2 = cap.getRGB(bowDamageRect.x, bowDamageRect.y+1);
		int red = (color1>>16)&0xFF;
	   	int green = (color1>>8)&0xFF;
	   	int blue = (color1>>0)&0xFF;
	   	if(red==221 && green ==79 && blue==1 ) {
	   		return true;
	   	}
	   	else {
	   		return false;
	   	}
    }
    
    public float getPlayerHealth() {
    	BufferedImage playerHealth = cap.getSubimage(playerHealthRect.x,playerHealthRect.y,playerHealthRect.width,playerHealthRect.height);
	   	 int playerHealthRaster[] = new int[playerHealthRect.width*playerHealthRect.height];
	   	 playerHealth.getRGB(0, 0, playerHealthRect.width, playerHealthRect.height, playerHealthRaster, 0, playerHealthRect.width);
   	 
    	 int redCount = 0;
    	 boolean redStopFound = false;
    	 float height = 0;
    	 float playerHPPercent = 0;
    	 for(int i = 0;i<6;i++) {
    		 int intColor = playerHealthRaster[i*playerHealthRect.width+ playerHealthRect.width-1];
    		 int red = (intColor>>16)&0xFF;
        	 int green = (intColor>>8)&0xFF;
        	 int blue = (intColor>>0)&0xFF;
        	 if(red>50 && green<red && blue<red) {
        		 redCount++;
        		 redStopFound = true;
        		 break;
        	 }else 
        		 height++;
    	 }
    	 
    	 if(redStopFound == false)
    	 for(int i = 6;i<17;i++) {
    		 int intColor = playerHealthRaster[i*playerHealthRect.width];
    		 int red = (intColor>>16)&0xFF;
        	 int green = (intColor>>8)&0xFF;
        	 int blue = (intColor>>0)&0xFF;
        	 if(red>50 && green<red && blue<red) {
        		 redCount++;
        		 redStopFound = true;
        		 break;
        	 }else 
        		 height++;
    	 }
    	 
    	 if(redStopFound == false)
    	 for(int i = 17;i<26;i++) {
    		 int intColor = playerHealthRaster[i*playerHealthRect.width+ playerHealthRect.width-1];
    		 int red = (intColor>>16)&0xFF;
        	 int green = (intColor>>8)&0xFF;
        	 int blue = (intColor>>0)&0xFF;
        	 if(red>50 && green<red && blue<red) {
        		 redCount++;
        		 redStopFound = true;
        		 break;
        	 }else 
        		 height++;
    	 }
    	 
    	 height = 26f-height;
    	 
    	 //sector area of circle
    	 playerHPPercent = (float) ((float) ((13f*13f)*Math.acos((13f-height)/13f)-(13f-height)*(Math.sqrt(2*13f*height-(height*height))))/(Math.PI*13f*13f));
    	 
    	 this.playerHealth = (float)Math.pow(playerHPPercent,1.275f);
    	 
    	 return playerHPPercent;
    }
    public float getEnemyHealth() {
    	BufferedImage enemyHealthBuf = cap.getSubimage(enemyHealthRect.x,enemyHealthRect.y,enemyHealthRect.width,enemyHealthRect.height);
        
        int[] enemyHealthRaster = new int[enemyHealthRect.width*enemyHealthRect.height];
        enemyHealthBuf.getRGB(0, 0, enemyHealthRect.width, enemyHealthRect.height, enemyHealthRaster, 0, enemyHealthRect.width);
    	
    	float greenCount = 0;
        float redCount = 0;
        float orangeCount = 0;

        for(int i = 0; i <enemyHealthRaster.length;i++) {
	       	int intColor = enemyHealthRaster[i];
	       	int red = (intColor>>16)&0xFF;
	       	int green = (intColor>>8)&0xFF;
	       	int blue = (intColor>>0)&0xFF;
	 
	       	float[] hsb = new float[3];
	       	Color.RGBtoHSB(red, green, blue, hsb);
	       	hsb[1] = 1f;
	       	hsb[2] = 0.5f;
	       	if(hsb[0]>=0.25 && hsb[0]<=0.375) {
	       		if(redCount>=1){
		       		this.enemyHealth = -1f;
		       		return -1f;
		       	}
	       			
	       		greenCount++;
	       	}
	       	else if((hsb[0]>=0 && hsb[0]<=0.0625)||(hsb[0]<=1 && hsb[0]>=0.9375)) {
	       		redCount++;
	       	}
	       	else if(hsb[0]>=0.0625 && hsb[0]<=0.0625+0.0625) {
	       		if(redCount>=1){
		       		this.enemyHealth = -1f;
		       		return -1f;
		       	}
	       		orangeCount++;
	       	}
	       	else {
	       		this.enemyHealth = -1f;
	       		return -1f;
	       	}
        }
        
        
        float totalCount = greenCount+orangeCount+redCount;
	   	float damage=orangeCount+redCount;
	   	float hpPercent = ((totalCount-damage)/totalCount);
	   	
	   	this.enemyHealth = hpPercent;
	   	
	   	return hpPercent;
    }
    
    public Point getEnemyPosition() {
    	
    	return new Point(enemyPosition.x+captureRect.x,enemyPosition.y+captureRect.y);
    }
    
    
    
    public Point getWeaponPosition() {
    	return new Point(inventoryIndex.x+captureRect.x,inventoryIndex.y+captureRect.y);
    }
    
    public void paint(Graphics g) { 
    	if(bufferGraphics!=null) {
	    	bufferGraphics.setColor(Color.BLACK);
	    	bufferGraphics.fillRect(0,0,dim.width,dim.height);
	        tick();
	        g.drawImage(offscreen,0,0,this); 
    	}
    }
    
    public void initilizePosition() {
    	//--------CHANGE THIS----------
    	/** You have to do some trial and error to figure out the correct location.
    	 *  IF you did do it correctly. When you fight enemies their health should 
    	 *  be tracked correctly
    	 */
    	int x = 105;
    	int y = 47;
    	//-------------------------------
    	
    	
    	
    	captureRect      = new Rectangle(x,y,725,460); 
    	enemyHealthRect  = new Rectangle(5, 36, 121, 1);
    	inventoryIndex   = new Point(578,225);
    	enemyPosition    = new Point(213,167);
    	playerHealthRect = new Rectangle(543,42,7,26);
    	bowDamageRect    = new Rectangle (445,195,1,1);
    	
    	comboRect = new Rectangle(enemyHealthRect.x+300,enemyHealthRect.y+500,100, 100);
    	eatRect = new Rectangle(enemyHealthRect.x+420,enemyHealthRect.y+500,100, 100);
    	noneRect = new Rectangle(enemyHealthRect.x+540,enemyHealthRect.y+500,100, 100);
    	removeRect = new Rectangle(enemyHealthRect.x+420-50,enemyHealthRect.y+619,200, 50);
    	
    	comboActionNet = new AnimateButton(new Rectangle(750,750,100,50),Color.orange,new Color(160, 32, 240),0.015f,"COMBO");
    	eatActionNet = new AnimateButton(new Rectangle(750,810,100,50),Color.orange,new Color(160, 32, 240),0.015f,"EAT");
    	noneActionNet = new AnimateButton(new Rectangle(750,870,100,50),Color.orange,new Color(160, 32, 240),0.015f,"NONE");
    }
    
    public RS_ML_v2()   { 
    	initilizePosition();
    	
    	rndBowResetValue = random.nextFloat();
    	if(rndBowResetValue<0.5f) {
    		rndBowResetValue = -1f;
    	}    
    	else 
    		rndBowResetValue = random.nextFloat();
		rndPlayerHealth = random.nextFloat();
		rndEnemyHealth = random.nextFloat();
		
    	fileName = "fighting_data_set_"+set+".txt";
    	
	    font = new Font ("Monospaced", Font.BOLD , 14);
	    
        dim = new Dimension(900,950); 
        this.setSize(dim);
        //this.resize(dim); 
        this.setLayout(null); 

	    
	    this.setLocation(captureRect.x+captureRect.width+50,captureRect.y-40);
	    this.setVisible(true);
	    this.show();
	    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    
        setBackground(Color.black); 

        offscreen = new BufferedImage( dim.width, dim.height, BufferedImage.TYPE_INT_RGB ); 

        bufferGraphics = offscreen.getGraphics(); 
        bufferGraphics.setFont(font);
        
      
        try {
        	GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
        	System.err.println("There was a problem registering the native hook.");
        	System.err.println(ex.getMessage());

        	System.exit(1);
        }

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        GlobalScreen.addNativeKeyListener(this);  
     
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);
        
        new Thread(this).start();
        
        try {
        	robot = new Robot();
        	macroPlayer = new MacroPlayer(robot,this);
        	traningData = new TraningData(fileName);
        	
        	serverSocket = new ServerSocket(5050);
			socket = serverSocket.accept();
			connected = true;
		} 
        catch (AWTException | IOException e) {
			e.printStackTrace();
		}
        
    }
    
   

    /*public void update(Graphics g) 
    { 
         paint(g); 
    } */


	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true) {
			 this.requestFocusInWindow();
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                                                                                                                  
			repaint();
		}
	}





	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		// TODO Auto-generated method stub
		 //System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

	        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
	        	connected = false;
	        	escapePressed = true;
	            try {
					GlobalScreen.unregisterNativeHook();
				} catch (NativeHookException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
	        
	        if(escapePressed == false) {
		         if(e.getKeyCode() == NativeKeyEvent.VC_W) {
					 robot.keyPress(KeyEvent.VK_UP);
				 }
				 else if(e.getKeyCode() == NativeKeyEvent.VC_A) {
					 robot.keyPress(KeyEvent.VK_LEFT);
				 }
				 else if(e.getKeyCode() == NativeKeyEvent.VC_S) {
					 robot.keyPress(KeyEvent.VK_DOWN);
				 }
				 else if(e.getKeyCode() == NativeKeyEvent.VC_D) {
					 robot.keyPress(KeyEvent.VK_RIGHT);
				 }
	        }
			 
	}



	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		 //System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
		
		 if(escapePressed == false) {
			 if(e.getKeyCode() == NativeKeyEvent.VC_Z) {
				 action = OutputAction.EAT;
				 macroPlayer.appendEatMacro();
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_X) {
				 action = OutputAction.COMBO;
				 macroPlayer.appendComboMacro();
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
				 enemyPosition.x = MouseInfo.getPointerInfo().getLocation().x - captureRect.x;
				 enemyPosition.y = MouseInfo.getPointerInfo().getLocation().y - captureRect.y;
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_P) {
				 traningData.save();
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_W) {
				 robot.keyRelease(KeyEvent.VK_UP);
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_A) {
				 robot.keyRelease(KeyEvent.VK_LEFT);
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_S) {
				 robot.keyRelease(KeyEvent.VK_DOWN);
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_D) {
				 robot.keyRelease(KeyEvent.VK_RIGHT);
			 }
			 else if(e.getKeyCode() == NativeKeyEvent.VC_L) {
				 isRecordLocked = !isRecordLocked;
			 }	
			 else if(e.getKeyCode() == NativeKeyEvent.VC_UP) {
				 rndBowResetProb+=0.1f;
			 }	
			 else if(e.getKeyCode() == NativeKeyEvent.VC_DOWN) {
				 rndBowResetProb-=0.1f;
			 }	
		 }
	}
	
	void addNewData(int outputActionData) {
		traningData.add(
    			//inputs: contains state of the fight and player status
    			rndEnemyHealth,
    			rndPlayerHealth,
    			rndBowResetValue,
    			
    			//outputs: checking if action is equal to any of the enum output action
    			outputActionData==0?1:0,
    			outputActionData==1?1:0,
    			outputActionData==2?1:0
    			);
		
		rndBowResetValue = random.nextFloat();
    	if(rndBowResetValue<rndBowResetProb) {
    		rndBowResetValue = -1f;
    	}    
    	else 
    		rndBowResetValue = random.nextFloat();
		rndPlayerHealth = random.nextFloat();
		rndEnemyHealth = random.nextFloat();
		
		
	}
	
	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		new RS_ML_v2();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		mY = e.getY();
		mX = e.getX();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		mY = e.getY();
		mX = e.getX();
		
		if(escapePressed == false) {
			if(comboRect.contains(mX,mY) == true) {
				addNewData(0); //combo
			}
			else if(eatRect.contains(mX,mY) == true) {
				addNewData(1); //eat
			}
			else if(noneRect.contains(mX,mY) == true) {
				addNewData(2); //none
			}
			else if(removeRect.contains(mX,mY) == true) {
				traningData.removeLast();
			}
		}
	}
	
	
}
