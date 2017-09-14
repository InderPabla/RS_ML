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

public class RS_ML extends Applet implements Runnable, NativeKeyListener{
	
    Graphics bufferGraphics; 

    Image offscreen; 

    Dimension dim; 
    int curX, curY;
    Robot robot;
    int waitTime = 25;
    boolean zammyGrabMode = false;
    public void init()  
    { 
    	 new Thread(this).start();
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
    
    BufferedImage cap;
    
    Rectangle captureRect = new Rectangle(105,47,725,460);
    Rectangle enemyHealthRect = new Rectangle(5, 36, 121, 1);
    Rectangle playerHealthrect = new Rectangle(543,42,7,26);
    Rectangle damageDoneRect = new Rectangle (445,195,1,1);
    Rectangle inventoryIndexRect = new Rectangle(578,225,1,1);
    Rectangle enemyLocationRect = new Rectangle(213,167,1,1);
    
    
    //Rectangle playerHealthRect = new Rectangle(captureRect.width/2-25, captureRect.height/2-40, 50, 30);
    Font f = new Font ("Monospaced", Font.BOLD , 14);
    int damageDoneCounter = 0;
    int damageDoneWait = 8;
    int damageDoneWaitMax = 8;
    float damageDoneValue = 0f;
    boolean damageDone = false;
    float comboValue = 0f;
    float eatValue = 0f;
    boolean spawnned = false;
    float enemyHP,playerHP;
    public void paint(Graphics g)  
    { 
    	 bufferGraphics.setColor(Color.BLACK);
    	 bufferGraphics.fillRect(0,0,dim.width,dim.height);
    	 
         cap = robot.createScreenCapture(captureRect);
         
         if(zammyGrabMode == false) {
	         BufferedImage enemyHealthBuf = cap.getSubimage(enemyHealthRect.x,enemyHealthRect.y,enemyHealthRect.width,enemyHealthRect.height);
	         
	         int[] enemyHealthRaster = new int[enemyHealthRect.width*enemyHealthRect.height];
	         enemyHealthBuf.getRGB(0, 0, enemyHealthRect.width, enemyHealthRect.height, enemyHealthRaster, 0, enemyHealthRect.width);
	         
	         float greenCount = 0;
	         float randomCount = 0;
	         float redCount = 0;
	         float greenAverage = 0;
	         float redAverage = 0;
	         float orangeCount = 0;
	         float orangeAverage = 0;
	         //float enemyHP = 0;
	         
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
	        		 greenCount++;
	        		 greenAverage+=hsb[0];
	        	 }
	        	 else if((hsb[0]>=0 && hsb[0]<=0.0625)||(hsb[0]<=1 && hsb[0]>=0.9375)) {
	        		 redCount++;
	        		 redAverage+=hsb[0];
	        	 }
	        	 else if(hsb[0]>=0.0625 && hsb[0]<=0.0625+0.0625) {
	        		 orangeCount++;
	        		 orangeAverage+=hsb[0];
	        	 }
	 
	        	 
	  
	        	 bufferGraphics.setColor(new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])));
	        	 bufferGraphics.fillRect(i, captureRect.height, 1, 50);
	         }
	         
	         
	         
	         bufferGraphics.drawImage(cap, 0, 0, null);
	         
	         float totalCount = greenCount+orangeCount+redCount;
	    	 float damage=orangeCount+redCount;
	    	 float hpPercent = ((totalCount-damage)/totalCount);
	    	 enemyHP = hpPercent;
	    	 bufferGraphics.setColor(Color.white);
	    	 //bufferGraphics.drawImage(enemyHealthBuf, 0, captureRect.height, null);
	    	 
	  
	    	 bufferGraphics.setFont(f);
	    	 bufferGraphics.drawString((hpPercent*100f)+"%, Red:"+redCount+", Green:"+greenCount+", Orange:"+orangeCount+", Damage: "+damage+", Total:"+totalCount, 10,captureRect.height+25);
	
	    	 BufferedImage playerHealth = cap.getSubimage(playerHealthrect.x,playerHealthrect.y,playerHealthrect.width,playerHealthrect.height);
	    	 bufferGraphics.drawImage(playerHealth, 0, captureRect.height+50, null);
	    	 int playerHealthRaster[] = new int[playerHealthrect.width*playerHealthrect.height];
	    	 playerHealth.getRGB(0, 0, playerHealthrect.width, playerHealthrect.height, playerHealthRaster, 0, playerHealthrect.width);
	    	 
	    	 redCount = 0;
	    	 boolean redStopFound = false;
	    	 float height = 0;
	    	 float playerHPPercent = 0;
	    	 for(int i = 0;i<6;i++) {
	    		 int intColor = playerHealthRaster[i*playerHealthrect.width+ playerHealthrect.width-1];
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
	    		 int intColor = playerHealthRaster[i*playerHealthrect.width];
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
	    		 int intColor = playerHealthRaster[i*playerHealthrect.width+ playerHealthrect.width-1];
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
	    	 
	    	 /*if(height == 0)
	    		 playerHPPercent = 1f;
	    	 else if(redStopFound == false){
	    		 playerHPPercent = 0f;
	    	 }
	    	 else {*/
	    		 playerHPPercent = (float) ((float) ((13f*13f)*Math.acos((13f-height)/13f)-(13f-height)*(Math.sqrt(2*13f*height-(height*height))))/(Math.PI*13f*13f));
	    	 //}
	    	 
	    	 playerHP = playerHPPercent;
	    	 
	    	 //playerHPPercent = redCount/26f;
	    	 bufferGraphics.drawString("Player Health: "+(playerHPPercent*100f),25,captureRect.height+75);
	    	 
	    	 
	    	 if(damageDoneWait == damageDoneWaitMax ) {
		    	 int color1 = cap.getRGB(damageDoneRect.x, damageDoneRect.y);
		    	 int color2 = cap.getRGB(damageDoneRect.x, damageDoneRect.y+1);
		    	 //int color3 = cap.getRGB(damageDoneRect.x+1, damageDoneRect.y+1);
		    	 //int color4 = cap.getRGB(damageDoneRect.x, damageDoneRect.y+1);
		    	 if(color1 == color1) {
		    		 int red = (color1>>16)&0xFF;
		        	 int green = (color1>>8)&0xFF;
		        	 int blue = (color1>>0)&0xFF;
		        	 if(red==221 && green ==79 && blue==1 && damageDone==false) {
		        		 
		        		 damageDone = true;
		        		 
		            	 damageDoneCounter ++;
		            	 damageDoneWait = 0;
		            	 damageDoneValue = 1f;
		        	 }
		        	 else {
		        		 damageDone = false;
		        	 }
		    	 }
		    	 else {
		    		 damageDone = false;
		    	 }
	    	 }//223 and 284
	    	 else {
	    		 damageDoneWait++; 
	    	 }
	    	 
	    	 if(damageDoneValue>0) {
	    		 damageDoneValue-=0.025f;
	    		 bufferGraphics.setColor(Color.red);
	        	 bufferGraphics.fillRect(0, captureRect.height+50+playerHealthrect.height, 400, 50);
	    	 } 
	    	 else 
	    		 damageDoneValue= 0;
	    	
	    	 
	    	 bufferGraphics.setColor(Color.white);   	 
	    	 bufferGraphics.drawString("Counter: "+damageDoneCounter,25, captureRect.height+75+playerHealthrect.height);
	    	 
	    	 
	    	 for(int i =0; i <7;i++) {
	    		 for(int j =0; j <4;j++) {
	    			 int x = inventoryIndexRect.x+(j*41);
	    			 int y =(int)(inventoryIndexRect.y+(i*36f));
	    			 int intColor =cap.getRGB(x, y);
	    			 int red = (intColor>>16)&0xFF;
		        	 int green = (intColor>>8)&0xFF;
		        	 int blue = (intColor>>0)&0xFF;
		        	 float[] hsb = new float[3];
		        	 Color.RGBtoHSB(red, green, blue, hsb);
		        	 
		        	 if(Math.abs(20f-(hsb[0]*239f))<=3 ) {
		        		 
		        		 if(hsb[1]*1000f>300f) {
		        			 bufferGraphics.setColor(Color.green);
		        		 }
		        		 else {
		        			 bufferGraphics.setColor(Color.red);
		        		 }
		        		 
		        		 
		        	 }
		        	 else {
		        		 bufferGraphics.setColor(Color.green);
		        	 }
		        	
	    			 bufferGraphics.drawOval(x,y,8,8);
	
	    		 }
	    	 }
	    	 
	    	 bufferGraphics.setColor(Color.red);
	    	 bufferGraphics.fillRect(enemyLocationRect.x,enemyLocationRect.y,10,10);
	    	 
	    	 if(comboValue >0) {
	    		 bufferGraphics.setColor(Color.green);
	        	 bufferGraphics.fillRect(0, captureRect.height+100+playerHealthrect.height, 400, 50);
	    	 }
	    	 if(eatValue >0) {
	    		 bufferGraphics.setColor(Color.cyan);
	        	 bufferGraphics.fillRect(0, captureRect.height+150+playerHealthrect.height, 400, 50);
	    	 }
	    	 
	    	 
         }
         else {
        	 
        	 //bufferGraphics.drawImage(cap, 0, 0, null);
        	 
        	 int color1 = cap.getRGB((zammySpawnRect.x), (zammySpawnRect.y));
        	
        	 
        	 int red = (color1>>16)&0xFF;
        	 int green = (color1>>8)&0xFF;
        	 int blue = (color1>>0)&0xFF;
        	 
        	
        	 if(red>150  ) {
        		 
        		
        		if(spawnned == false && ready == true) {
        			spawnned = true;
        			
        			/*robot.mouseMove(157+captureRect.x,212+captureRect.y);
        			sleep(10);*/
        			//sleep(10);
        			robot.mousePress(MouseEvent.BUTTON1_MASK);
        			sleep(20);
        			robot.mouseRelease(MouseEvent.BUTTON1_MASK);
        			sleep(20);
        			System.out.println("DONE");
        			//sleep(1000);
        			//spawnned = false;
        		}
        	 }
        	 g.drawImage(cap.getSubimage(captureRect.width-80, 81, 10, 10), 0, captureRect.height, null);
         }
        
         g.drawImage(offscreen,0,0,this); 
         
         //INPUTS: combo,enemyHP,myHP,eat status,damageDoneValue
    	 //OUTPUTS: combo,eat,nothing
         input[0] = comboValue;
    	 input[1] = enemyHP;
    	 input[2] = playerHP;
    	 input[3] = eatValue;
    	 input[4] = damageDoneValue;
         
         
         Float[] nInput = new Float[5];
    	 Float[] nOuputs = new Float[3];
    	 
    	
    	 
    	 for(int i = 0; i <nInput.length;i++)
    		 nInput[i] = (float)input[i];
    	 
    	 for(int i = 0; i <nOuputs.length;i++)
    		 nOuputs[i] = (float)output[i];
    	 
         if(inputs.size()<10) {
        	 
        	 inputs.add(0, nInput);
        	 outputs.add(0, nOuputs);
         }
         else {
        	 inputs.remove(inputs.size()-1);
        	 outputs.remove(outputs.size()-1);
        	 
        	 inputs.add(0, nInput);
        	 outputs.add(0, nOuputs);
         }
         
         for(int i = 0; i <output.length;i++)
        	 output[i] = 0f;
    }
    
    ArrayList<Float[]> inputs = new ArrayList<Float[]>();
    ArrayList<Float[]> outputs = new ArrayList<Float[]>();
    int maxSize= 10;
    
    boolean ready = false;
    Rectangle zammySpawnRect = new Rectangle(0,0,0,0);

    float[] input = new float[5];
    float[] output = new float[3];
    
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
		 if(e.getKeyCode() == NativeKeyEvent.VC_P && zammyGrabMode == true) {
			 ready = true;
			 spawnned = false;
			
		 }
		 else if (e.getKeyCode() == NativeKeyEvent.VC_O && zammyGrabMode == true) {
			 zammySpawnRect.x = MouseInfo.getPointerInfo().getLocation().x -captureRect.x ;
			 zammySpawnRect.y = MouseInfo.getPointerInfo().getLocation().y -captureRect.y;
		 }
		
		 if(e.getKeyCode() == NativeKeyEvent.VC_Z) {
			eatValue = 1;
			int counter = 0;
			Point foundPoint = null;
			for(int i =0; i <7;i++) {
	    		 for(int j =0; j <4;j++) {
	    			 counter++;
	    			 if(counter >3) {
		    			 int x = inventoryIndexRect.x+(j*41);
		    			 int y =(int)(inventoryIndexRect.y+(i*36f));
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
				robot.mouseMove(foundPoint.x, foundPoint.y);
				robot.mousePress(MouseEvent.BUTTON1_MASK);
				robot.mouseRelease(MouseEvent.BUTTON1_MASK);
			}
			
			eatValue = 0f;
		}
		else if(e.getKeyCode() == NativeKeyEvent.VC_X) {
			 
			comboValue = 1f;
			robot.mouseMove(inventoryIndexRect.x+captureRect.x, inventoryIndexRect.y+captureRect.y);
			sleep(10);
			robot.mousePress(MouseEvent.BUTTON1_MASK);
			sleep(5);
			robot.mouseRelease(MouseEvent.BUTTON1_MASK);
			sleep(10);
			robot.mouseMove(enemyLocationRect.x+captureRect.x, enemyLocationRect.y+captureRect.y);
			sleep(20);
			robot.mousePress(MouseEvent.BUTTON1_MASK);
			sleep(5);
			robot.mouseRelease(MouseEvent.BUTTON1_MASK);
			sleep(1600);
			robot.mouseMove(inventoryIndexRect.x+captureRect.x, inventoryIndexRect.y+captureRect.y);
			sleep(10);
			robot.mousePress(MouseEvent.BUTTON1_MASK);
			sleep(5);
			robot.mouseRelease(MouseEvent.BUTTON1_MASK);
			sleep(20);
			robot.mouseMove(enemyLocationRect.x+captureRect.x, enemyLocationRect.y+captureRect.y);
			sleep(10);
			robot.mousePress(MouseEvent.BUTTON1_MASK);
			sleep(5);
			robot.mouseRelease(MouseEvent.BUTTON1_MASK);
			comboValue = 0f;
		}
		if(e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
			
			enemyLocationRect.x = MouseInfo.getPointerInfo().getLocation().x -captureRect.x ;
			enemyLocationRect.y = MouseInfo.getPointerInfo().getLocation().y -captureRect.y;
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
