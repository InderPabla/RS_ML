import java.awt.Color;
import java.awt.Rectangle;

public class AnimateButton {
	public Rectangle rect;
	public Color endColor; 
	public Color startColor; 
	public float animateTick = 1;
	public float animateTickRate;
	float maxTick = 1;
	public String buttonName;
	
	public AnimateButton(Rectangle rect, Color endColor, Color startColor, float animateTickRate, String name) {
		this.rect = rect;
		this.endColor = endColor;
		this.startColor = startColor;
		this.animateTickRate = animateTickRate;
		this.buttonName = name;
	}
	
	public Color tick () {
		if(animateTick > 0) {
			animateTick-=animateTickRate;
		}
		
		if(animateTick < 0) {
			animateTick=0;
		}
		
		int red =  (int)((startColor.getRed() - endColor.getRed()) * animateTick*animateTick* animateTick)+endColor.getRed();
		int green =  (int)((startColor.getGreen() - endColor.getGreen()) * animateTick*animateTick* animateTick)+endColor.getGreen();
		int blue =  (int)((startColor.getBlue() - endColor.getBlue()) * animateTick*animateTick* animateTick)+endColor.getBlue();
		
		return new Color(red,green,blue);
	}

	public void animate() {
		animateTick = 1;
	}
	
	public boolean isAnimating() {
		return animateTick>0;
	}
}
