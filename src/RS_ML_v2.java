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
	
	
	OutputAction action;
	
	int waitTime = 25;
	Font font;
	
    Graphics bufferGraphics; 
    Image offscreen; 
    
    Dimension dim; 
    Robot robot;
    
    BufferedImage cap;
    
    Rectangle captureRect = new Rectangle(105,47,725,460);
    
    public void tick() {
    	cap = robot.createScreenCapture(captureRect);
    }
    
    public void paint(Graphics g)  
    { 
    	 bufferGraphics.setColor(Color.BLACK);
    	 bufferGraphics.fillRect(0,0,dim.width,dim.height);
         tick();
         g.drawImage(offscreen,0,0,this); 
    }
       
    public void init()  
    { 
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
		} catch (AWTException e) {
			// TODO Auto-generated catch block
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
		 }
		 else if(e.getKeyCode() == NativeKeyEvent.VC_X) {
			 action = OutputAction.COMBO;
		 }
		 else if(e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
			 action = OutputAction.NONE;
		 }
	}
	
	void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
