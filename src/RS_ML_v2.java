import java.applet.Applet;
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
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class RS_ML_v2 extends Applet implements Runnable, NativeKeyListener{

	Dimension dim           = null; 
	BufferedImage cap       = null;
    Graphics bufferGraphics = null; 
    Image offscreen         = null; 
    Font font               = null;
    
    OutputAction action     = null;
    MacroPlayer macroPlayer = null;
    Robot robot             = null;
    
    Rectangle captureRect      = null;
    Rectangle enemyHealthRect  = null;
    Rectangle playerHealthRect = null;
    Rectangle bowDamageRect    = null; 
    
    Point enemyPosition   = null;
    Point weaponPosition  = null;
    Point inventoryIndex  = null;
    
    int waitTime = 25;
    int ignoreInventorySlots = 3;
    
    boolean visualRender = true;
    
    float enemyHealth      = -1f;
    float playerHealth     = -1f;
    float bowResetValue    = 0f;
    float bowResetSubtract = 0.025f;
    
    public void tick() {
    	cap = robot.createScreenCapture(captureRect);
    	bufferGraphics.drawImage(cap,0,0,null);
    	
    	getEnemyHealth();
    	getPlayerHealth();
    	bowResetValue();
    	
    	visualRenderer();
    	
    	macroPlayer.macroTick();
    }
    
    public void visualRenderer() {
    	if(visualRender==true) {
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
    	    	bufferGraphics.setColor(Color.green);
    	    	bufferGraphics.fillRect(playerHealthRect.x,playerHealthRect.y,(int)(enemyHealthRect.width*playerHealth), 25);
    	    	bufferGraphics.setColor(Color.red);
    	    	bufferGraphics.fillRect(playerHealthRect.x+(int)(enemyHealthRect.width*playerHealth),playerHealthRect.y,(int)(enemyHealthRect.width*(1f-playerHealth)), 25);
    	    	bufferGraphics.setColor(Color.black);
            	bufferGraphics.drawString((playerHealth*100f)+"%", playerHealthRect.x+25,playerHealthRect.y+15);
        	}
        	else {
        		
        	}
    	}
    }
    
    public float bowResetValue() {
    	if(bowResetValue>=0) {
    		bowResetValue -= bowResetSubtract;
    	}
    	else {
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
	   	 bufferGraphics.drawImage(playerHealth, 0, captureRect.height+50, null);
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
    	 
    	 this.playerHealth = playerHPPercent;
    	 
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
	       		if(redCount>=1)
	       			return -1f;
	       			
	       		greenCount++;
	       	}
	       	else if((hsb[0]>=0 && hsb[0]<=0.0625)||(hsb[0]<=1 && hsb[0]>=0.9375)) {
	       		redCount++;
	       	}
	       	else if(hsb[0]>=0.0625 && hsb[0]<=0.0625+0.0625) {
	       		if(redCount>=1)
	       			return-1f;
	       		orangeCount++;
	       	}
	       	else 
	       		return -1f;
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
    
    public Point getFoodPosition() {
    	int counter      = 0;
		Point foundPoint = null;
		
		//Iterate through 28 inventory slots
		for(int i =0; i <7;i++) {
    		 for(int j =0; j <4;j++) {
    			 counter++;
    			 if(counter >ignoreInventorySlots) {
	    			 int x = inventoryIndex.x+(j*41);
	    			 int y =(int)(inventoryIndex.y+(i*36f));
	    			 int intColor =cap.getRGB(x, y);
	    			 int red = (intColor>>16)&0xFF;
		        	 int green = (intColor>>8)&0xFF;
		        	 int blue = (intColor>>0)&0xFF;
		        	 float[] hsb = new float[3];
		        	 Color.RGBtoHSB(red, green, blue, hsb);
		        	 
		        	 if(Math.abs(20f-(hsb[0]*239f))>2) {
		        		 foundPoint = new Point(x,y);
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
    
    public Point getWeaponPosition() {
    	return new Point(inventoryIndex.x+captureRect.x,inventoryIndex.y+captureRect.y);
    }
    
    public void paint(Graphics g) { 
    	 bufferGraphics.setColor(Color.BLACK);
    	 bufferGraphics.fillRect(0,0,dim.width,dim.height);
         tick();
         g.drawImage(offscreen,0,0,this); 
    }
    
    public void initilizePosition() {
    	captureRect      = new Rectangle(105,47,725,460);
    	enemyHealthRect  = new Rectangle(5, 36, 121, 1);
    	inventoryIndex   = new Point(578,225);
    	enemyPosition    = new Point(213,167);
    	playerHealthRect = new Rectangle(543,42,7,26);
    	bowDamageRect    = new Rectangle (445,195,1,1);
    }
    
    public void init()   { 
    	initilizePosition();
    	
    	new Thread(this).start();
	    font = new Font ("Monospaced", Font.BOLD , 14);
	    	 
        dim = new Dimension(900,900); 
        this.setSize(dim);
        this.resize(dim);
         
        setBackground(Color.black); 

        offscreen = createImage(dim.width,dim.height); 

        bufferGraphics = offscreen.getGraphics(); 
        bufferGraphics.setFont(font);
        try {
        	robot = new Robot();
        	macroPlayer = new MacroPlayer(robot,this);
		} 
        catch (AWTException e) {
			e.printStackTrace();
		}
      
        try {
        	GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
        	System.err.println("There was a problem registering the native hook.");
        	System.err.println(ex.getMessage());

        	System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(this);  
     
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);
    }
    
   

    public void update(Graphics g) 
    { 
         paint(g); 
    } 


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
	            try {
					GlobalScreen.unregisterNativeHook();
				} catch (NativeHookException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
	}



	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		 System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
		
		 if(e.getKeyCode() == NativeKeyEvent.VC_Z) {
			 action = OutputAction.EAT;
			 macroPlayer.clearMacroTask();
			 macroPlayer.appendEatMacro();
		 }
		 else if(e.getKeyCode() == NativeKeyEvent.VC_X) {
			 action = OutputAction.COMBO;
			 macroPlayer.clearMacroTask();
			 macroPlayer.appenComboMacro();
		 }
		 else if(e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
			 enemyPosition.x = MouseInfo.getPointerInfo().getLocation().x - captureRect.x;
			 enemyPosition.y = MouseInfo.getPointerInfo().getLocation().y - captureRect.y;
		 }
	}


	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
