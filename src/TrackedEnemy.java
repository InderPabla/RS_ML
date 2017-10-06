import java.awt.Rectangle;

public class TrackedEnemy {
	
	public Rectangle rect = null;;
	public boolean isEnemy = true;
	
	public float redCount = 0;
	public float greenCount = 0;
	public float hp = 0;
	
	public TrackedEnemy(Rectangle rect) {
		this.rect =rect;
	}
	
	boolean contains (int x , int y) {
		return rect.contains(x,y);
	}

}
