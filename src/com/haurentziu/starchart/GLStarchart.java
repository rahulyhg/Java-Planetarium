package com.haurentziu.starchart;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

import com.haurentziu.coordinates.EquatorialCoordinates;
import com.haurentziu.coordinates.HorizontalCoordinates;
import com.haurentziu.coordinates.SphericalCoordinates;
import com.jogamp.opengl.FPSCounter;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * 
 * @author haurentziu
 *
 */

public class GLStarchart implements GLEventListener, MouseMotionListener, MouseListener, MouseWheelListener{
	private long timestamp = System.currentTimeMillis();
	
	private Star stars[];
	private Constellation constellations[];
	
	private FPSCounter fps;
	
	private float localSideralTime = 12; //Local Sideral Time
	private float altitudeAngle = (float) Math.toRadians(-70);
	private float azimuthAngle = (float) Math.toRadians(60);
	
	private int initX, initY;
	
	private boolean showUnderHorizon = false;
	
	final byte projection = SphericalCoordinates.ORTOGRAPHIC_PROJECTION; 
//	final byte projection = SphericalCoordinates.STEREOGRAPHIC_PROJECTION;
//	final byte projection = SphericalCoordinates.GNOMOIC_PROJECTION;
	
	private int height, width;
	
	private float zoom = 2;
	
	GLStarchart(GLCanvas c){
		DataLoader loader = new DataLoader();
		stars = loader.loadStars();
		constellations = loader.loadConstellations();
		c.addMouseMotionListener(this);
		c.addMouseListener(this);
		c.addMouseWheelListener(this);
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
	
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		
		if(altitudeAngle > -Math.PI/2 || projection == SphericalCoordinates.ORTOGRAPHIC_PROJECTION)
			gl.glClearColor(0f, 0.075f, 0.125f, 1f); //prussian blue
		else
			gl.glClearColor(0.28f, 0.21f, 0.16f, 1f); //brown
			
		float fps = drawable.getAnimator().getLastFPS();
		
		System.out.println(fps);
		
		drawHorizon(gl);
		drawConstellations(gl);
		drawStars(gl);
	//	updateTime();
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		drawable.getAnimator().setUpdateFPSFrames(20, null);
	}
	

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		double aspectRatio = (double)width/height;
		gl.glOrtho(-2*aspectRatio, 2*aspectRatio, -2, 2, -1, 1);
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		this.width = width;
		this.height = height;
	}
	
	private void drawHorizon(GL2 gl){
		if(altitudeAngle > -Math.PI/2)
			gl.glColor3f(0.28f, 0.21f, 0.16f); //brown
		else
			gl.glColor3f(0f, 0.075f, 0.125f); //prussian blue

		gl.glBegin(GL2.GL_POLYGON);
		for(float i = 0; i < 2*Math.PI + 0.1; i+=0.1){
			
			HorizontalCoordinates h = new HorizontalCoordinates(i, 0);
			Point2D p = h.toProjection(azimuthAngle, altitudeAngle, projection);
			
			gl.glVertex2f((float)(zoom*p.getX()), (float)(zoom*p.getY()));
			
		}
		gl.glEnd();
	}
	
	private void drawConstellations(GL2 gl){
		gl.glColor3f(0.4f, 0.4f, 0.4f);
		for(int i = 0; i < constellations.length; i++){
			ConstellationLine[] lines = constellations[i].getLines();
			for(int j = 0; j < lines.length; j++){
				
				EquatorialCoordinates equatorial[] = lines[j].getPositions(stars);
				HorizontalCoordinates start = equatorial[0].toHorizontal(Math.toRadians(45), Math.toRadians(localSideralTime*15));
				HorizontalCoordinates end = equatorial[1].toHorizontal(Math.toRadians(45), Math.toRadians(localSideralTime*15));
				
				if(start.getAltitude() > 0 && end.getAltitude() > 0 || showUnderHorizon){
					Point2D p1, p2;
					
					p1 = start.toProjection(azimuthAngle, altitudeAngle, projection);
					p2 = end.toProjection(azimuthAngle, altitudeAngle, projection);
					
					gl.glBegin(GL2.GL_LINES);
					gl.glVertex2f((float)(zoom*p1.getX()), (float)(zoom*p1.getY()));
					gl.glVertex2f((float)(zoom*p2.getX()), (float)(zoom*p2.getY()));
					gl.glEnd();
				}
			}
		}
	}
	
	private void drawStars(GL2 gl){
		gl.glColor3f(1f, 1f, 1f);
		for(int i = 0; i < stars.length; i++){
			if(stars[i].getMagnitude() < 5.5 + 0.1*zoom){
				HorizontalCoordinates c = stars[i].toHorizontal(Math.toRadians(45), Math.toRadians(localSideralTime*15));
				if(c.getAltitude() > 0 || showUnderHorizon){
					Point2D p;
					p= c.toProjection(azimuthAngle, altitudeAngle, projection);
					drawCircle((float)(zoom*p.getX()), (float)(zoom*p.getY()), stars[i].getRadius(), gl);
				}
			}
		}
	}

	private void drawCircle(float centerX, float centerY , float radius, GL2 gl){
		gl.glBegin(GL2.GL_POLYGON);
		for(float angle = 0; angle < 2*Math.PI; angle += 0.5){
			float x = (float)(centerX + radius * Math.cos(angle));
			float y = (float)(centerY + radius * Math.sin(angle));
			gl.glVertex2f(x, y);
		}

		gl.glEnd();
	}
	
	private void updateTime(){
		localSideralTime += 0.01;
	}
	


	@Override
	public void mouseDragged(MouseEvent e) {
		int distanceX = (e.getX() - initX);
		int distanceY = (e.getY() - initY);
		
		initX = e.getX();
		initY = e.getY();
		
		azimuthAngle += (float)Math.PI*distanceX/(width*zoom);
		altitudeAngle -= (float)Math.PI*distanceY/(height*zoom);
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
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
	public void mousePressed(MouseEvent e) {
		initX = e.getX();
		initY = e.getY();
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int moves = e.getWheelRotation();
		if(moves > 0){
			zoom /= 1.1;
		}
		else{
			zoom *= 1.1;
		}
	}
}
