package ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import game.Game;
import game.PowerUp;
import game.Target;
import game.Tank;
import game.TankAIBase;
import game.Vec2;

// single & comp ver
public class TankAI extends TankAIBase {
    

    public static final double EPSILON = 0.01;
    public static final double MOVE_STEP = 4;
    public static final double WEIGHT = 2;


    public String getPlayerName() {
        return "new";  // <---- Put your first name here
    }
    public int getPlayerPeriod() {
        return 4;                // <---- Put your period here
    }
        
    // You are free to add member variables & methods to this class (and delete this comment).
    //  You should use the methods in its base class (TankAIBase) to query the world. 
    //  Note that you are not allowed to reach into game code directly or make any
    //  modifications to code in the game package. Use your judgement and ask your 
    //  teacher if you are not sure. If it feels like cheating, it probably is.
    
    public Vec2 findMin(Vec2 tankPos, List <PowerUp> powerUps) {
        Vec2 min = powerUps.get(0).getPos();
        Double distMin = min.distance(tankPos);

        for(int i = 0; i < powerUps.size()-1; i++) {
            Vec2 vec = powerUps.get(i).getPos();
            if(vec.distance(tankPos) < distMin) {
                min = powerUps.get(i).getPos();
                distMin = min.distance(tankPos);
            }
        }
        return min;
    }

    public Vec2 findTargetMin(Vec2 tankPos, List<Target> target) {
        Vec2 min = target.get(0).getPos();
        Double distMin = min.distance(tankPos);

        for(int i = 0; i < target.size()-1; i++) {
            Vec2 vec = target.get(i).getPos();
            if(vec.distance(tankPos) < distMin && vec.distance(tankPos) != 0) {
                min = target.get(i).getPos();
                distMin = min.distance(tankPos);
            }
        }
        return min;
    }

    public Vec2 findMinVec(Vec2 tankPos, Vec2 powerPosition, Vec2 targetPos, double weight) {
        if (tankPos.distance(powerPosition)*weight < tankPos.distance(targetPos)) {
            return powerPosition;
        }
        else {
            return targetPos;
        }
    }

    public boolean shootTargetWithinReach(Vec2 tankPos, double shotRange, Vec2 tarPos) {
        if(tankPos.distance(tarPos) < shotRange) {
            //queueCmd("turn", tarPos);
            queueCmd("shoot", tarPos.minus(tankPos));
            return true;
        } 
        return false;
    }

    
    public List<Target> targetInRange(Tank myTank, List<Target> targets) {
        List<Target> result = new ArrayList<Target>();
        
        for(Target target : targets) {
            if(target.getPos().distance(myTank.getPos()) <= myTank.getShotRange()) {
                result.add(target);
            }
        }
        return result;
    }

    public Target minTargetOutRange(Tank myTank, List<Target> targets) {
        Target result = targets.get(0);
        double minDist = 99999;
        
        for(Target target : targets) {
            double dist = target.getPos().distance(myTank.getPos());
            if( dist > myTank.getShotRange() &&
                dist < minDist) {
                result = target;
            }
        }

        return result;
    }

    public Vec2 findClosestPowerNearTar(Vec2 Tank, List<Target> targets, List<PowerUp> powerups) {
        double distMin = 4;
        Vec2 closestPowerUp = findMin(Tank, powerups); 
        for(PowerUp powerUp: powerups) {
            for(Target target: targets) {
                if(powerUp.getPos().distance(target.getPos()) < distMin) {
                    closestPowerUp = powerUp.getPos();
                }
            }
        }
        return closestPowerUp;
    }

    public double moveStep(double target, double source) {
        if (target > source + MOVE_STEP) {
            return MOVE_STEP;
        }
        else if (target < source - MOVE_STEP) {
            return -1* MOVE_STEP;
        }
        else {
            return target - source;
        }
    }
    
    public List<Target> clusteredWhere(List<Target> targets, List<PowerUp> powerups) {
        List<Target> newTargets = new ArrayList<Target>();
        double minDist = 6;
        for(Target target: targets) {
            if(target.getPos().distance(findTargetMin(target.getPos(), targets)) < minDist) {
                newTargets.add(target);
            }
        }
        return newTargets;
    }

    public void shootOpponent(Tank myTank, Tank otherTank) {
        if((myTank.getPos().x == otherTank.getPos().x && myTank.getPos().distance(otherTank.getPos()) <= myTank.getShotRange() ) || myTank.getPos().y == otherTank.getPos().y && myTank.getPos().distance(otherTank.getPos()) <= myTank.getShotRange()) {
            System.out.println("SHOOT OTHER");
            queueCmd("shoot", otherTank.getPos().minus(myTank.getPos()));
        }
    }

    public boolean updateAI() {
        Tank myTank = getTank();
        
        List <PowerUp> allPowers = Arrays.asList(getPowerUps()[0], getPowerUps()[1], getPowerUps()[2]);
        List <Target> allTargets = Arrays.asList(getTargets()[0], getTargets()[1], getTargets()[2]);
        
        Vec2 minPower = findMin(myTank.getPos(), allPowers);
        Vec2 nextGoal;
        
        List<Target> targetSet = targetInRange(myTank, allTargets);
        Target targetOutRange = minTargetOutRange(myTank, allTargets);
        


        if (getLevelTimeRemaining() > 0.0) { 
            //if(myTank.getPos().distance(getOther().getPos())<2) {
               // return true;
            //}
            if(clusteredWhere(allTargets, allPowers).size() > 1) {
                System.out.println("OKOKOKOK");
                nextGoal = minPower;
                nextGoal = findClosestPowerNearTar(tank.getPos(), clusteredWhere(allTargets, allPowers), allPowers);
            } else {
                System.out.println("NONONO");
                nextGoal = minPower; 
            }
        
            nextGoal = minPower;    
            
            for (Target target : targetSet) {
                shootTargetWithinReach(myTank.getPos(), myTank.getShotRange(), target.getPos());      
            }

            nextGoal = findMinVec(myTank.getPos(), nextGoal, targetOutRange.getPos(), WEIGHT);
            double dirX = moveStep(nextGoal.x, myTank.getPos().x);
            double dirY = moveStep(nextGoal.y, myTank.getPos().y);

            if(Math.abs(dirX) < EPSILON) {
                queueCmd("move",new Vec2(0, dirY));
            } else if (Math.abs(dirY) < EPSILON) {
                queueCmd("move",new Vec2(dirX, 0));
            } else {
                if(targetOutRange.getPos().distance(new Vec2 (myTank.getPos().x + dirX, myTank.getPos().y)) < 
                targetOutRange.getPos().distance(new Vec2 (myTank.getPos().x, myTank.getPos().y+dirY))) {
                    queueCmd("move", new Vec2(dirX, 0));
                }   
                else {
                    queueCmd("move",new Vec2(0, dirY));
                }
            }
        }

        return true;
    }
    
    public boolean updateAIDouble() {
        
        Tank myTank = getTank();
        
        List <PowerUp> allPowers = Arrays.asList(getPowerUps()[0], getPowerUps()[1], getPowerUps()[2]);
        List <Target> allTargets = Arrays.asList(getTargets()[0], getTargets()[1], getTargets()[2]);
        
        Vec2 minPower = findMin(myTank.getPos(), allPowers);
        Vec2 nextGoal;
        
        List<Target> targetSet = targetInRange(myTank, allTargets);
        Target targetOutRange = minTargetOutRange(myTank, allTargets);
        

        if (getLevelTimeRemaining() > 0.0) { 
            shootOpponent(myTank, getOther());
            if(myTank.getPos().distance(getOther().getPos())<2) {
                return true;
            }
            /*
            if(clusteredWhere(allTargets, allPowers).size() > 1) {
                System.out.println("OKOKOKOK");
                nextGoal = minPower;
                nextGoal = findClosestPowerNearTar(tank.getPos(), clusteredWhere(allTargets, allPowers), allPowers);
            } else {
                System.out.println("NONONO");
                nextGoal = minPower; 
            }
            */

            
            for (Target target : targetSet) {
                shootTargetWithinReach(myTank.getPos(), myTank.getShotRange(), target.getPos());      
            }

            nextGoal = findMinVec(myTank.getPos(), minPower, targetOutRange.getPos(), 1);

            double dirX = moveStep(nextGoal.x, myTank.getPos().x);
            double dirY = moveStep(nextGoal.y, myTank.getPos().y);

        if(Math.abs(dirX) < EPSILON) {
            queueCmd("move",new Vec2(0, dirY));
        } else if (Math.abs(dirY) < EPSILON) {
            queueCmd("move",new Vec2(dirX, 0));
        } else {
            if(targetOutRange.getPos().distance(new Vec2 (myTank.getPos().x + dirX, myTank.getPos().y)) < 
            targetOutRange.getPos().distance(new Vec2 (myTank.getPos().x, myTank.getPos().y+dirY))) {
                queueCmd("move", new Vec2(dirX, 0));
            }   
            else {
                queueCmd("move",new Vec2(0, dirY));
            }
        }

        return true;
    } 
    }
}
