package Robo_IA;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;
import java.util.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * RoboIA - a robot by (your name here)
 */
public class RoboIA extends AdvancedRobot
{
  	int turnDirection = 1, ahead = 100, back = 10;
	double vidaInicialInimigo = 100;
	boolean movingForward;
	List<ScannedRobotEvent> sre = new ArrayList<ScannedRobotEvent>();
	
	public void run() {
	  setAdjustRadarForRobotTurn(true);
	  setAdjustGunForRobotTurn(true);
	  setAdjustRadarForGunTurn(true);
	  
	  turnGunRight(-90);

	  setBodyColor(new java.awt.Color(204,105,50));
	  setGunColor(new java.awt.Color(150,70,100));
	  setRadarColor(new java.awt.Color(75,15,14));
		
	  while(true) {
	  		setAhead(48000);
			movingForward = true;
			setTurnRight(90);
			waitFor(new TurnCompleteCondition(this));
			setTurnLeft(180);	
			setBack(2000); 
			waitFor(new TurnCompleteCondition(this));		
			setTurnRight(180);		
			waitFor(new TurnCompleteCondition(this));
			
		if(bordas()){
			voltarArea();
		}
		 if ( getRadarTurnRemaining() == 0.0 )
            setTurnRadarRightRadians( Double.POSITIVE_INFINITY );
			
		execute();
	  }
	}

	public void onHitRobot(HitRobotEvent e) {		
		if (e.isMyFault()) {
			reverseDirection();
		}
	}

	/**
	 * Robo inimigo sendo identificado
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		sre.add(e);
		// Radar
      	double angleToEnemya = getHeadingRadians() + e.getBearingRadians();

		    double radarTurn = Utils.normalRelativeAngle( angleToEnemya - getRadarHeadingRadians() );
		    double extraTurn = Math.min( Math.atan( 36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );
		    if (radarTurn < 0)
		        radarTurn -= extraTurn;
		    else
		        radarTurn += extraTurn;
		 
		    //Turn the radar
		    setTurnRadarRightRadians(radarTurn);
		
		// Movimentação ----------------


		//Quando atirar ----------------	
		double oldEnemyHeading = sre.get(sre.size()-2).getHeadingRadians();
		double bulletPower = Math.min(3.0,getEnergy());
		double myX = getX();
		double myY = getY();
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
		double enemyHeading = e.getHeadingRadians();
		double enemyHeadingChange = enemyHeading - oldEnemyHeading;
		double enemyVelocity = e.getVelocity();
		oldEnemyHeading = enemyHeading;
		 
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight(), 
		       battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX, predictedY = enemyY;
		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
		      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
			predictedX += Math.sin(enemyHeading) * enemyVelocity;
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			enemyHeading += enemyHeadingChange;
			if(	predictedX < 18.0 
				|| predictedY < 18.0
				|| predictedX > battleFieldWidth - 18.0
				|| predictedY > battleFieldHeight - 18.0){
		 
				predictedX = Math.min(Math.max(18.0, predictedX), 
				    battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), 
				    battleFieldHeight - 18.0);
				break;
			}
		}
		double theta = Utils.normalAbsoluteAngle(Math.atan2(
		    predictedX - getX(), predictedY - getY()));
		 
			setTurnRadarRightRadians(Utils.normalRelativeAngle(
			absoluteBearing - getRadarHeadingRadians()));
			setTurnGunRightRadians(Utils.normalRelativeAngle(
			theta - getGunHeadingRadians()));
			
			if(getEnergy() > 5 ){
				fire(bulletPower);
			}else{
				fire(1);
			}

	}
	/**
 	* Quando for atingido por um disparo:
	 * Gira perpendicularmente à bala e avança um pouco.
 	*/
	public void onHitByBullet(HitByBulletEvent e) {
			turnRight(normalRelativeAngleDegrees(90 - (getHeading() - e.getHeading())));
	}
	/**
	 * Função de direção reversa
	 */
	public void reverseDirection() {
		if (movingForward) {
			setBack(10000);
			movingForward = false;
		} else {
			setAhead(10000);
			movingForward = true;
		}
	}


	/**
	 * Quando choco com a parede
	 */
  	public void onHitWall(HitWallEvent e) {
		//quando choco com parede ando pra trás
		reverseDirection();
		setAhead(-200);
  	}
	
	public boolean bordas() {
		if(getX() > (getBattleFieldWidth() - getSentryBorderSize()) || getX() < getSentryBorderSize() ){
			return true;
		}
		else {
			if(getY() > (getBattleFieldHeight() - getSentryBorderSize()) || getY() < getSentryBorderSize() ){
				return true;
			}
			return false;
		}
	}
	
	public void voltarArea(){
		if (getX() > (getBattleFieldWidth() - getSentryBorderSize())) {
			
			turnLeft(getHeading() + 90);
			ahead(150);
		}
		
		if (getY() > (getBattleFieldWidth() - getSentryBorderSize())) {
			
			turnLeft(getHeading() + 90);
			ahead(150);
		}
		
		if (getX() < (getBattleFieldWidth() - getSentryBorderSize())) {
			ahead(150);
			turnRight(getHeading() + 180);
			
		}
		
		if (getY() < (getBattleFieldWidth() - getSentryBorderSize())) {
			ahead(150);
			turnRight(getHeading() + 180);
			
		}
	}
}

