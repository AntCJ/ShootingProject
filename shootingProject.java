import java.util.ArrayList;
import java.lang.Math;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollBar;

public class shootingProject extends JPanel{
	private int[] windowSize = {800,500};
	private double playerPos[] = new double[2];
	private double[] mouseStartPos = {windowSize[0]*1/4,windowSize[1]*2/3};
	private double[] mouseMovingPos = {windowSize[0]*1/4,windowSize[1]*2/3};
	private double[] mouseEndPos = {windowSize[0]*1/4,windowSize[1]*2/3};
	private boolean mousePressed;
	private boolean mouseDrag;
	private int cameraOffset = 0;
	private int FPS = 175;
	private boolean running = true;
	
	private ArrayList<Arrow> arrows = new ArrayList<>();
	private ArrayList<Player> players = new ArrayList<>();
	private int playerIndex = 0;
	private double velocity = 0;
	private double angle = 0;
	private int groundLevel = windowSize[1]*5/6;
	private final double GRAVITY = 9.8/FPS;
	
	private shootingProject() {
		setFocusable(true);
		requestFocusInWindow();
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					mouseStartPos[0] = e.getX();
					mouseStartPos[1] = e.getY();
				} 
				mousePressed = true;
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {	
					mouseEndPos[0] = e.getX();
					mouseEndPos[1] = e.getY();
					arrows.add(new Arrow(playerPos[0],playerPos[1],0.1*(mouseStartPos[0]-mouseEndPos[0]),0.1*(mouseStartPos[1]-mouseEndPos[1])));
					
					mousePressed = false;
					mouseDrag = false;
				} 
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (mousePressed) {
					mouseMovingPos[0] = e.getX();
					mouseMovingPos[1] = e.getY();
				}
				mouseDrag = true;
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					cameraMove(-20);
					break;
				case KeyEvent.VK_RIGHT:
					cameraMove(20);
					break;
				case KeyEvent.VK_ESCAPE:
					System.exit(0);
					break;
				} 
			}
		});
		
		JScrollBar s = new JScrollBar(JScrollBar.HORIZONTAL);
		s.setMinimum (0);
		s.setMaximum (windowSize[0]);
		//s.setPreferredSize(new Dimension(300,20));
		s.setBounds(windowSize[0]/2-150,groundLevel+20,300,20);
		s.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				cameraTo(s.getValue());
			}
		});
		this.setLayout(null);
		this.add(s);
		
		players.add(new Player(0));		//index 0 for player 1, index 1 for player 2
		players.add(new Player(1));
	}
	
	@Override
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		
		//clear screen
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, windowSize[0], windowSize[1]);
		
		g.setColor(Color.BLACK);
		g.drawLine(0, groundLevel, windowSize[0], groundLevel);
		g.drawString("Velocity = "+showDecimal(velocity)+" m/s",30,30);
		g.drawString("Angle = "+showDecimal(angle)+" deg",30,50);
		if (players.get(0).life > 0 && players.get(1).life > 0)
			g.drawString("Player "+(playerIndex+1)+"'s turn",30,70);
		else if (players.get(0).life <= 0) {
			g.setColor(Color.RED);
			g.drawString("Player 2 wins!",30,70);
			running = false;
		}
		else if (players.get(1).life <= 0) {
			g.setColor(Color.RED);
			g.drawString("Player 1 wins!",30,70);
			running = false;
		}
		
		for(int i = players.size() - 1; i >= 0; i--) {
			players.get(i).draw(g); 
		}
		
		for(int i = arrows.size() - 1; i >= 0; i--) {
			arrows.get(i).draw(g); 
		}
	}
	
	private void update() {
		playerPos[0] = players.get(playerIndex).getPlayerPosx();
		playerPos[1] = players.get(playerIndex).getPlayerPosy();
		if (mouseDrag) {
			angle = -Math.atan((mouseStartPos[1]-mouseMovingPos[1])/(mouseStartPos[0]-mouseMovingPos[0]))*180/Math.PI;
			velocity = 0.1*(mouseStartPos[0]-mouseMovingPos[0]);
			if (playerIndex == 0) {
				cameraOffset = 0;
			}
			else {
				cameraOffset = 800;
				angle *= -1;
				velocity *= -1;
			}
		}

		for(int i = players.size() - 1; i >= 0; i--) {
			players.get(i).update(); 
		}
		for(int i = arrows.size() - 1; i >= 0; i--) {
			arrows.get(i).update(); 
		}
	}
	
	private String showDecimal(double number) {
		String[] numberString = String.valueOf(number).split("");
		if (number >= 10 || (number < 0 && number > -10))
			return (numberString[0]+numberString[1]+numberString[2]+numberString[3]);
		else if (number < -10)
			return (numberString[0]+numberString[1]+numberString[2]+numberString[3]+numberString[4]);
		else
			return (numberString[0]+numberString[1]+numberString[2]);
	}
	
	private void cameraMove(int movedPixels) {
		cameraOffset += movedPixels;
		if (cameraOffset < 0)
			cameraOffset = 0;
		else if (cameraOffset > windowSize[0])
			cameraOffset = windowSize[0];
	}
	
	private void cameraTo(int target) {
		if (target >= 0 && target <= windowSize[0]) {
			cameraOffset = target;
		}
	}
	
	private class Arrow {
		private double x, y, x2, y2, velx, vely;
		private int arrowLength = 20;
		private int arrowDistance;
		private Color arrowColor = Color.BLACK;
		private boolean arrowMoving = true;
		private Arrow(double x, double y, double velx, double vely) {
			this.x = x;
			this.y = y;
			this.velx = velx;
			this.vely = vely;
		}
		
		private void update() {
			if (arrowMoving && y < groundLevel && (!hitPlayer())) {
				vely += GRAVITY;
				x += velx;
				y += vely;
				
				if (playerIndex == 0)
					arrowDistance = (int)(x-playerPos[0]);
				else
					arrowDistance = (int)(windowSize[0]-(playerPos[0]-x));
				cameraTo(arrowDistance);
			}
			else if (arrowMoving) {
				if (hitPlayer())
					reduceLife();
				playerIndex = Math.abs(playerIndex-1);
				arrowMoving = false;
			}
			
			if (hitPlayer()) 
				arrowColor = Color.RED;
			else if (arrows.indexOf(this) == arrows.size()-1)
				arrowColor = Color.GREEN;
			else
				arrowColor = Color.BLACK;
		}
		
		private void draw(Graphics2D g) {
			//create second point using unit vector in direction as velocity
			x2 = x-(arrowLength/Math.sqrt(velx*velx+vely*vely))*velx;
			y2 = y-(arrowLength/Math.sqrt(velx*velx+vely*vely))*vely;
			g.setColor(arrowColor);
			g.drawLine((int)x-cameraOffset,(int)y,(int)x2-cameraOffset,(int)y2);
		}
		
		private boolean hitPlayer() {
			return (hitPlayerBody(0) || hitPlayerBody(1) ||  hitPlayerHead(0) || hitPlayerHead(1));
		}
		
		private boolean hitPlayerHead(int i) {
			if (players.get(i).head.contains(x-cameraOffset,y))
				return true;
			return false;
		}
		
		private boolean hitPlayerBody(int i) {
			if (players.get(i).body.contains(x-cameraOffset,y))
				return true;
			return false;
		}
		
		private void reduceLife() {
			if (hitPlayerBody(0))
				players.get(0).life -= 25;
			if (hitPlayerBody(1))
				players.get(1).life -= 25;
			if (hitPlayerHead(0))
				players.get(0).life -= 50;
			if (hitPlayerHead(1))
				players.get(1).life -= 50;
		}
	}
	
	private class Player {
		private int index;
		private int life = 100;
		private double[] pos = {windowSize[0]*1/4,windowSize[1]*3/4};
		private double adjust, adjust2;
		private Shape head;
		private Shape body;
		
		private Player (int index) {
			this.index = index;											//index 0 for player 1, index 1 for player 2
		}
		
		private void draw(Graphics2D g) {
			g.setColor(Color.BLACK);
			g.draw(head);
			g.draw(body);
			adjust = 2*windowSize[0]-2*pos[0];							//position adjustment for player 2
			double [] endPos = {pos[0]+index*adjust+(mouseMovingPos[0]-mouseStartPos[0]),pos[1]+(mouseMovingPos[1]-mouseStartPos[1])};
			
			//mouse-dragged string
			if (mouseDrag) {
				g.setColor(Color.BLACK);
				g.drawLine((int)(pos[0]+index*adjust)-cameraOffset,(int)pos[1],(int)endPos[0]-cameraOffset,(int)endPos[1]);					//arrow while stretching
			}
			else {
				adjust = 2*windowSize[0]-2*(pos[0]-10);
				endPos[0] = pos[0]-10+index*adjust;						//bow return to resting position
				endPos[1] = pos[1];
				adjust = 2*windowSize[0]-2*(pos[0]-20);
				adjust2 = 2*(windowSize[0]-pos[0]);
				g.drawLine((int)(pos[0]-20+index*adjust)-cameraOffset,(int)pos[1],(int)(pos[0]+index*adjust2)-cameraOffset,(int)pos[1]);	//arrow while resting
			}
			
			//bow and string
			adjust = 180;
			int startAngle = (int)(-90+index*adjust);
			adjust = 2*windowSize[0]-(2*(pos[0]-20)+20);	
			g.drawArc((int)(pos[0]-20+index*adjust)-cameraOffset,(int)(pos[1]-20),20,40,startAngle,180);
			adjust = 2*windowSize[0]-2*(pos[0]-10);
			g.drawLine((int)(pos[0]-10+index*adjust)-cameraOffset,(int)pos[1]-20,(int)endPos[0]-cameraOffset,(int)endPos[1]);
			g.drawLine((int)endPos[0]-cameraOffset,(int)endPos[1],(int)(pos[0]-10+index*adjust)-cameraOffset,(int)pos[1]+20);
			
			//life bar
			adjust = 2*windowSize[0]-(2*(pos[0]-72)+103);
			g.drawRect((int)(pos[0]-20-50-2+index*adjust-cameraOffset),(int)(pos[1]-35-20-15-2),103,13);
			g.setColor(Color.GREEN);
			adjust = 2*windowSize[0]-(2*(pos[0]-70)+99);
			g.fillRect((int)(pos[0]-20-50+index*adjust-cameraOffset),(int)(pos[1]-35-20-15),life,10);
			g.setColor(Color.BLACK);
		}
		
		private void update() {
			adjust = 2*windowSize[0]-(2*(pos[0]-30)+20);		
			head = new Ellipse2D.Double(pos[0]-30+index*adjust-cameraOffset,pos[1]-35,20,20);
			body = new Rectangle2D.Double(pos[0]-30+index*adjust-cameraOffset,pos[1]-15,20,groundLevel-(pos[1]-15));
		}
		
		private double getPlayerPosx() {
			adjust = windowSize[0]*3/2;
			return pos[0]+index*adjust;
		}
		
		private double getPlayerPosy() {
			return pos[1];
		}
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame("My Bowman");
		shootingProject game = new shootingProject();
		f.setLayout(null);
		f.setContentPane(game);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(game.windowSize[0],game.windowSize[1]);
		f.setVisible(true);
		new Thread() {
			@Override public void run() {
				while (game.running) {
					try {
						Thread.sleep(1000/game.FPS);
						game.update();
						game.repaint();
					} catch ( InterruptedException e ) {}
				}
			}
		}.start();
	}
}