import java.util.*;
import java.util.concurrent.*;
import java.lang.*;
import java.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.*;

public class shootingProject extends JPanel{
	private int[] windowSize = {800,500};
	private int[] gameArea = {windowSize[0]*2,windowSize[1]};
	private int groundLevel = windowSize[1]*5/6;
	private double playerPos[] = new double[2];
	private int cameraOffset = 0;
	private double[] mouseStartPos = {windowSize[0]*1/4,windowSize[1]*2/3};
	private double[] mouseMovingPos = {windowSize[0]*1/4,windowSize[1]*2/3};
	private double[] mouseEndPos = {windowSize[0]*1/4,windowSize[1]*2/3};
	private boolean mousePressed;
	private boolean mouseDrag;
	private int FPS = 175;
	private boolean running = true;
	
	private ArrayList<Arrow> arrows = new ArrayList<>();
	private ArrayList<Player> players = new ArrayList<>();
	private int playerIndex = 0;
	private double velocity = 0;
	private double angle = 0;
	private double velMultiplier = 0.03;
	private double [] wind = {-0.03,0,0.03};
	private double currentWind = wind[1];
	private final double GRAVITY = 9.8/FPS;
	
	private boolean freezeCtrl = false;
	
	private Image background;
	
	private shootingProject() {
		setFocusable(true);
		requestFocusInWindow();
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && !freezeCtrl) {
					mouseStartPos[0] = e.getX();
					mouseStartPos[1] = e.getY();
				} 
				mousePressed = true;
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && !freezeCtrl) {	
					mouseEndPos[0] = mouseMovingPos[0];
					mouseEndPos[1] = mouseMovingPos[1];
					arrows.add(new Arrow(playerPos[0],playerPos[1],velMultiplier*(mouseStartPos[0]-mouseEndPos[0]),velMultiplier*(mouseStartPos[1]-mouseEndPos[1])));
					mousePressed = false;
					mouseDrag = false;
				} 
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (!freezeCtrl) {
					if (mousePressed) {
						mouseMovingPos[0] = e.getX();
						mouseMovingPos[1] = e.getY();
						
						//Limit movement only towards front of player
						if (playerIndex == 0 && mouseMovingPos[0] > mouseStartPos[0]) {
							mouseMovingPos[0] = mouseStartPos[0];
						}
						else if (playerIndex == 1 && mouseMovingPos[0] < mouseStartPos[0]) {
							mouseMovingPos[0] = mouseStartPos[0];
						}
					}
					mouseDrag = true;
				}
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
		
		try {
            loadImages();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void loadImages() throws IOException {
		background = Toolkit.getDefaultToolkit().createImage(System.getProperty("user.dir")+"\\images\\background.gif").getScaledInstance(gameArea[0], windowSize[1], Image.SCALE_DEFAULT);
	}
	
	@Override
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		super.paintComponent(g1);
		g.drawImage(background,-cameraOffset,0,this);
		
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2));
		g.drawLine(0, groundLevel, windowSize[0], groundLevel);
		g.setStroke(new BasicStroke(1));
		
		if (currentWind == wind[0]) {
			g.drawString("Wind:  <<",30,50);
		}
		else if (currentWind == wind[1]) {
			g.drawString("Wind:  --",30,50);
		}
		else {
			g.drawString("Wind:  >>",30,50);
		}
		
		if (players.get(0).life > 0 && players.get(1).life > 0)
			g.drawString("Player "+(playerIndex+1)+"'s turn",30,30);
		else if (players.get(0).life <= 0) {
			g.setColor(Color.RED);
			g.drawString("Player 2 wins!",30,30);
			running = false;
		}
		else if (players.get(1).life <= 0) {
			g.setColor(Color.RED);
			g.drawString("Player 1 wins!",30,30);
			running = false;
		}
		
		if (mouseDrag){
			g.setColor(Color.WHITE);
			g.drawString(new DecimalFormat("#.#").format(velocity)+" m/s",(int)mouseMovingPos[0]+10,(int)mouseMovingPos[1]+10);
			g.drawString(new DecimalFormat("#.#").format(angle)+" \u00B0",(int)mouseStartPos[0],(int)mouseStartPos[1]);
			g.drawLine((int)mouseStartPos[0],(int)mouseStartPos[1],(int)mouseMovingPos[0],(int)mouseMovingPos[1]);
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
			velocity = velMultiplier*Math.sqrt(Math.pow(mouseStartPos[0]-mouseMovingPos[0],2)+Math.pow(mouseStartPos[1]-mouseMovingPos[1],2));
			
			//when start dragging mouse, the camera automatically move to that player screen
			if (playerIndex == 0) {
				cameraOffset = 0;
			}
			else {
				cameraOffset = gameArea[0]-windowSize[0];
				angle *= -1;
			}
		}
		
		freezeCtrl = !(arrows.size() == 0 ||!arrows.get(arrows.size()-1).arrowMoving) || !running;
		
		for(int i = players.size() - 1; i >= 0; i--) {
			players.get(i).update(); 
		}
		for(int i = arrows.size() - 1; i >= 0; i--) {
			arrows.get(i).update(); 
		}
	}
	
	private void cameraMove(int movedPixels) {
		cameraOffset += movedPixels;
		if (cameraOffset < 0)
			cameraOffset = 0;
		else if (cameraOffset > gameArea[0]-windowSize[0])
			cameraOffset = gameArea[0]-windowSize[0];
	}
	
	private void cameraTo(int target) {
		if (target >= 0 && target <= gameArea[0]-windowSize[0]) {
			cameraOffset = target;
		}
	}
	
	private class Arrow {
		private double x, y, x2, y2, velx, vely;
		private int arrowLength = 20;
		private int arrowDistance;
		private boolean arrowMoving = true;
		private Color arrowColor = Color.BLACK;
		private Arrow(double x, double y, double velx, double vely) {
			this.x = x;
			this.y = y;
			this.velx = velx;
			this.vely = vely;
		}
		
		private void update() {
			if (arrowMoving && y < groundLevel && (!hitPlayer())) {
				velx += currentWind;
				vely += GRAVITY;
				x += velx;
				y += vely;
				
				if (playerIndex == 0)
					arrowDistance = (int)(x-playerPos[0]);
				else
					arrowDistance = (int)(gameArea[0]-windowSize[0]-(playerPos[0]-x));
				cameraTo(arrowDistance);
			}
			if (arrowMoving && (x < -30 || x > gameArea[0]+30 || y >= groundLevel || hitPlayer())) {
				if (hitPlayer())
					reduceLife();
				playerIndex = Math.abs(playerIndex-1);
				Random randomWind = new Random();
				currentWind = wind[randomWind.nextInt(3)];
				arrowMoving = false;
				
				//Move camera to another player
				if (playerIndex == 0){
					while (cameraOffset > 0){
						try {
							cameraMove(-20);
							Thread.sleep(5);
						}
						catch(InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}
				}
				else {
					while (cameraOffset < gameArea[0]-windowSize[0]){
						try {
							cameraMove(20);
							Thread.sleep(5);
						}
						catch(InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}
				}
				
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
			g.setStroke(new BasicStroke(2));
			g.drawLine((int)x-cameraOffset,(int)y,(int)x2-cameraOffset,(int)y2);
			g.setStroke(new BasicStroke(1));
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
		private double [] pos = {windowSize[0]*1/4,windowSize[1]*3/4};
		private double [] mouseDist = new double[2];
		private double [] endPos = new double[2];
		private double adjust, adjust2;
		private Shape head;
		private Shape body;
		
		private Player (int index) {
			this.index = index;											//index 0 for player 1, index 1 for player 2
		}
		
		private void draw(Graphics2D g) {
			g.setColor(Color.WHITE);
			g.setStroke(new BasicStroke(2));
			g.draw(head);
			g.draw(body);
			g.setStroke(new BasicStroke(1));
			adjust = index*(gameArea[0]-2*(pos[0]-10));					//position adjustment for Player 2
			endPos[0] = pos[0]-10+adjust-cameraOffset;					//bow resting position
			endPos[1] = pos[1];
			
			//mouse-dragged string
			if (mouseDrag) {
				mouseDist[0] = mouseMovingPos[0]-mouseStartPos[0];
				mouseDist[1] = mouseMovingPos[1]-mouseStartPos[1];
				
				//Limit movement to certain extent and only towards front of player
				if (playerIndex == 0) {
					if (mouseDist[0] > 0)
						mouseDist[0] = 0;
					else if (mouseDist[0] < -200)
						mouseDist[0] = -200;
				}
				else {
					if (mouseDist[0] > 200)
						mouseDist[0] = 200;
					else if (mouseDist[0] < 0)
						mouseDist[0] = 0;
				}
				
				if (mouseDist[1] > 200)
					mouseDist[1] = 200;
				else if (mouseDist[1] < -200)
					mouseDist[1] = -200;
				
				endPos[0] += mouseDist[0]/8;
				endPos[1] += mouseDist[1]/8;
				adjust = index*(gameArea[0]-2*pos[0]);

				g.drawLine((int)(pos[0]+adjust-cameraOffset),(int)pos[1],(int)endPos[0],(int)endPos[1]);					//arrow while stretching
			}
			else {
				adjust = index*(gameArea[0]-2*(pos[0]-20));
				adjust2 = index*(gameArea[0]-2*pos[0]);
				g.drawLine((int)(pos[0]-20+adjust-cameraOffset),(int)pos[1],(int)(pos[0]+adjust2-cameraOffset),(int)pos[1]);	//arrow while resting
			}
			
			//bow and string
			adjust = index*180;
			int startAngle = (int)(-90+adjust);
			adjust = index*(gameArea[0]-(2*(pos[0]-20)+20));	
			g.drawArc((int)(pos[0]-20+adjust-cameraOffset),(int)(pos[1]-20),20,40,startAngle,180);
			adjust = index*(gameArea[0]-2*(pos[0]-10));
			g.drawLine((int)(pos[0]-10+adjust-cameraOffset),(int)pos[1]-20,(int)endPos[0],(int)endPos[1]);
			g.drawLine((int)endPos[0],(int)endPos[1],(int)(pos[0]-10+adjust-cameraOffset),(int)pos[1]+20);
			
			//life bar
			adjust = index*(gameArea[0]-(2*(pos[0]-72)+103));
			g.drawRect((int)(pos[0]-20-50-2+adjust-cameraOffset),(int)(pos[1]-35-20-15-2),103,13);
			g.setColor(Color.GREEN);
			adjust = index*(gameArea[0]-(2*(pos[0]-70)+99));
			g.fillRect((int)(pos[0]-20-50+adjust-cameraOffset),(int)(pos[1]-35-20-15),life,10);
			g.setColor(Color.BLACK);
		}
		
		private void update() {
			adjust = index*(gameArea[0]-(2*(pos[0]-30)+20));		
			head = new Ellipse2D.Double(pos[0]-30+adjust-cameraOffset,pos[1]-35,20,20);
			body = new Rectangle2D.Double(pos[0]-30+adjust-cameraOffset,pos[1]-15,20,groundLevel-(pos[1]-15));
		}
		
		private double getPlayerPosx() {
			adjust = index*(gameArea[0]-windowSize[0]/2);
			return pos[0]+adjust;
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
				while (true) {
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