package org.zimowski.bambi.editor.studio.image;


/**
 * Defines operations that can editor can issue to the image. Subsequently, 
 * container managing the image needs to subscribe to this interface if it 
 * wishes to be notified when editor fires them.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface ImageTransformListener {

    /**
     * Describes which way to rotate (left or right). Able to compute required 
     * radian values.
     */
    public static enum RotateDirection {
    	LEFT(-90), RIGHT(90);
    	
    	int angle;
    	
    	RotateDirection(int angle) {
    		this.angle = angle;
    	}
    	
    	int getAngle() {
    		return angle;
    	}
    };    

	public void reset();
	
	public void emboss();
	
	public void posterize();
	
	public void invert();
	
	public void solarize();
	
	public void deamonize();
	
	public void kaleidoscope();
	
	public void grayscale();
	
	public void oldPhoto();
	
	public void dither();
	
	public void marble();
	
	public void stamp();
	
	public void cartoonize();
	
	public void twirl();
	
	public void flip();
	
	public void rotate(RotateDirection direction);
	
	public void hue(float value);
	
	public void saturation(float value);
	
	public void brightness(float value);
	
	public void contrast(float value);
	
	public void redAdjust(float value);
	
	public void greenAdjust(float value);
	
	public void blueAdjust(float value);
		
	/**
	 * @param percent value between 0 to 100
	 */
	public void scale(int percent);
}
