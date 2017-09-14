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

	Dimension dim = null; 
	BufferedImage cap = null;
    Graphics bufferGraphics = null; 
    Image offscreen = null; 
    Font font = null;
    
    OutputAction action = null;
    MacroPlayer macroPlayer = null;
    Robot robot;
    
    Rectangle captureRect = null;
    Point enemyPosition = null;
    Point weaponPosition = null;
    Point inventoryIndex = null;
    
    int waitTime = 25;
    int ignoreInventorySlots = 3;
    
    public void tick() {
    	cap = robot.createScreenCapture(captureRect);
    	macroPlayer.macroTick();
    }
    
    public Point getEnemyPosition() {
    	return new Point(enemyPosition.x+captureRect.x,enemyPosition.y+captureRect.y);
    }
    
    public Point getFoodPosition() {
    	int counter = 0;
		Point foundPoint = null;
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
    	captureRect    = new Rectangle(105,47,725,460);
    	inventoryIndex = new Point(578,225);
    	enemyPosition  = new Point(213,167);
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
		 System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

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
			 action = OutputAction.NONE;
		 }
	}


	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
