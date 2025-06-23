package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Landmine extends GameObject {
	// Constants...
	public static final Vec2 POWERUP_HALFDIMS = new Vec2(0.4, 0.4);
	public static final double POWERUP_HEIGHT = 0.5;
	private static final double POWERUP_ROUNDED_SIZE = 0.1;

	// Member variables...
	private String type;

	// Accessors...
	public String getType() {
		return type;
	}
	public Vec2 getHalfDims() {
		return Vec2.copy(POWERUP_HALFDIMS);
	}

	// Member functions (methods)...
	protected Landmine(Vec2 pos, String type) {
		// Parent...
		super();

		// Member vars...
        this.pos = pos;
		this.type = type;
		this.timeTillDeath = 0;
    }
	protected Landmine(Vec2 pos) {
		this(pos, "P");
	}

	protected void destroy() {
		// Super...
		super.destroy();		
	}
	
	protected boolean shouldBeCulled() {
		// Check death timer...
		if (this.timeTillDeath >= 1) {
			return true;
		}
		return false;
	}
	
	protected void update(double deltaTime) {
		// Super...
		super.update(deltaTime);

		// If dying, continue...
		if (this.timeTillDeath > 0) {
			// Update it...
			this.timeTillDeath = Math.min(this.timeTillDeath + deltaTime * 2.0, 1.0);
		}
		else if (!Game.get().isGamePaused()) {
			// Check for tanks collecting us...
			ArrayList<GameObject> gameObjects = Simulation.getGameObjects();
			for (int i = 0; i < gameObjects.size(); ++i) {
				GameObject gameObject = gameObjects.get(i);
				if (gameObject instanceof Tank) {
                    Tank tank = (Tank)gameObject;
                    if(tank.hasShield()) {
                        Util.log("Shield protected the tank!");
                    } else {
                        if (Util.circlesIntersect(this.pos, POWERUP_HALFDIMS.x, tank.pos, Tank.BODY_HALFSIZE.x)) {
                            // React to it...
                            tank.onLandmine(this);
                        }
                    }
					
					
				}
			}
		}
	}

	public boolean onLandmine(Vec2 position){
		if (position == pos){
			return true;
		}
		return false;
	} 



	protected void drawShadow(Graphics2D g) {
		// Setup...
		double scale = calcDrawScale();
		Color colorShadow = new Color(255, 0, 0, 50);

		// Body...
        
		Draw.drawRectShadow(g, this.pos, calcDrawHeight(POWERUP_HEIGHT, scale), POWERUP_HALFDIMS, scale, colorShadow, POWERUP_ROUNDED_SIZE);
	}

	protected void draw(Graphics2D g) {
		// Setup...
		double scale = calcDrawScale();
		double height = calcDrawHeight(POWERUP_HEIGHT, scale);

        //Color
        Color outerGrey = new Color(200, 200, 200,200);
        Color innerGrey = new Color(160, 160, 160,200);


        //Radius
        double radius = POWERUP_HALFDIMS.x * scale;
        double innerRadius = radius * 0.6;


        //diameter
        double outerDiameter = radius * 2;
        double innerDiameter = innerRadius * 2;

        //outer circle
        double drawX = pos.x - radius;
        double drawY = pos.y - radius - height;

        //inner circle
        g.setColor(outerGrey);
        g.fillOval((int)drawX, (int)drawY, (int)outerDiameter, (int)outerDiameter);

   
        double innerX = pos.x - innerRadius;
        double innerY = pos.y - innerRadius - height;
        g.setColor(new Color(0, 255, 255, 100));
        g.fillOval((int)innerX, (int)innerY, (int)innerDiameter, (int)innerDiameter);

	}
}