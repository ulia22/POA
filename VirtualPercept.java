package edu.turtlekit2.warbot.team;

import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
import edu.turtlekit2.warbot.team.message.MessageType;
import java.lang.Math;

/**
 * Classe permettant à un agent A de recuperer toutes les informations concernant un agent C, détecté par un autre agent B.
 * 
 *  
 *  * @author ulia22
 */
public class VirtualPercept extends Percept{

	/**
	 * Constructeur permettant de construire le percept virtuel de toute piece.
	 * 
	 * @param angle
	 * @param distance
	 * @param id
	 * @param team
	 * @param type
	 * @param energy
	 * @param heading
	 */
	public VirtualPercept(int angleAC, int distanceAC, int idC, String teamC,
			String typeC, int energyC, double headingC) {
		super(angleAC, distanceAC, idC, teamC, typeC, energyC, headingC);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Permet de créer un VirtualPercept à partir d'un message recu de B.
	 * 
	 * @return un vitual percept avec des angles et des distance correctes par rapport à l'agent appelant.
	 */
	public static VirtualPercept createVP(WarMessage wm){
		String msg = wm.getMessage();
		if(!msg.matches(MessageType.PERCEPT.getPattern())){
			return null;
		}
		
		int angleBC = Integer.parseInt(msg.replaceAll(MessageType.PERCEPT.getPattern(), "$1"));
		int distanceBC = Integer.parseInt(msg.replaceAll(MessageType.PERCEPT.getPattern(), "$2"));
		int idC = Integer.parseInt(msg.replaceAll(MessageType.PERCEPT.getPattern(), "$3"));
		String teamC = msg.replaceAll(MessageType.PERCEPT.getPattern(), "$4");
		String typeC = msg.replaceAll(MessageType.PERCEPT.getPattern(), "$5");
		int energyC = Integer.parseInt(msg.replaceAll(MessageType.PERCEPT.getPattern(), "$6"));
		double headingC = Double.parseDouble(msg.replaceAll(MessageType.PERCEPT.getPattern(), "$7"));
		
		int vectAC[] = computeCoorC(angleBC, distanceBC, wm);
		
		
		return new VirtualPercept(vectAC[0], vectAC[1], idC, teamC, typeC, energyC, headingC);
	}

	
	private static int[] computeCoorC(int angleBC, int distanceBC, WarMessage wm){
		double xb, yb, xc, yc;
		double ACd = 0.0;
		xb = wm.getDistance()*Math.cos(Math.PI * wm.getAngle()/180);
		yb = wm.getDistance()*Math.sin(Math.PI * wm.getAngle()/180);
		
		xc = distanceBC*Math.cos(Math.PI*angleBC/180)+xb;
		yc = distanceBC*Math.sin(Math.PI*angleBC/180)+yb;
		
		double square = (double)( ((Math.abs((long)xc))^2)+((Math.abs((long)yc))^2) );
		ACd = Math.sqrt(square);
		
		double angleACd = Math.acos(xc/ACd);
		angleACd = 180*angleACd/Math.PI;
		
		int res[] = {(int)angleACd, (int)ACd};
		return res;
		
	}
}
