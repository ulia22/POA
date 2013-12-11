package edu.turtlekit2.warbot.team;


import java.util.ArrayList;
import java.util.List;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
import edu.turtlekit2.warbot.team.message.MessageEncapsule;
import edu.turtlekit2.warbot.team.message.MessageType;
import edu.turtlekit2.warbot.team.state.JobsRocketLauncher;
import edu.turtlekit2.warbot.team.state.StateRocketLauncher;

public class BrainRocketLauncher extends WarBrain implements MessageEncapsule{
	
	private boolean baseFound = false;
	private boolean onDemand = false;
	private JobsRocketLauncher jobs = JobsRocketLauncher.SEARCH;
	private StateRocketLauncher state = StateRocketLauncher.INITIAL;
	private Percept target = null;
	
	public BrainRocketLauncher(){
		
	}
	
	@Override
	public String action() {
		if(!isReloaded()){
			if(!isReloading()){
				return "reload";
			}
		}
		
		while(isBlocked()){
			setRandomHeading();
		}

		List<Percept> listeP = getPercepts();
		List<WarMessage> listeM = getMessage();
		
		manageMessage(listeM);
		managePercept(listeP);

		switch(jobs){
		
		case SEARCH :
				switch(state){
					case INITIAL : 
						if(onDemand){
							jobs= JobsRocketLauncher.ATTACK;
							state = StateRocketLauncher.GO_ATTACK_ASKED_TARGET;
						}
						else if(target == null){
							state = StateRocketLauncher.SEARCHING_TARGET;
						}else{
							jobs = JobsRocketLauncher.ATTACK;
							state = StateRocketLauncher.ATTACK_TARGET;
						}
					break;
				
					case SEARCHING_TARGET :
						break;
					
					default : state = StateRocketLauncher.INITIAL;
				}
			break;
		
		case ATTACK :
			switch(state){
				case INITIAL : break;
				
				//attaque la cible
				case ATTACK_TARGET :
					if(target != null){
						setAngleTurret(target.getAngle());
						target = null;
						jobs = JobsRocketLauncher.SEARCH;
						state = StateRocketLauncher.INITIAL;
						return "fire";
					}else{
						jobs = JobsRocketLauncher.SEARCH;
						state = StateRocketLauncher.INITIAL;
					}
				
				// attaque cible par trigo
				case GO_ATTACK_ASKED_TARGET : break;
				
				default : state = StateRocketLauncher.INITIAL;
			}	
		case DEFEND :
			switch(state){
				case INITIAL : ;
				break;
			
				case GO_DEF_BASE:;
				break;	
			}
			break;
			
			default : jobs = JobsRocketLauncher.SEARCH;
		}
		
		return "move";
	}
	
	void manageMessage(List<WarMessage> liste){
		for(WarMessage wm : liste){
			if(wm.getMessage().matches(MessageType.BASE_ATTACKED.getPattern())){
				setHeading(wm.getAngle());
			}
		}
	}
	
	void managePercept(List<Percept> liste){
		for(Percept p : liste){
			if(p.getType().equals("WarBase") && !p.getTeam().equals(getTeam())){
				//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.GO_ATTACK_BASE;
				//envoi de message
				target = p;
				baseFound = true;
			}
			else if(p.getType().equals("WarRocketLauncher") && !p.getTeam().equals(getTeam()) && !baseFound){
				//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.ATTACK_TARGET;
				//envoi de message
				
				target = p;
			}
			else if(p.getType().equals("WarExplorer") && !p.getTeam().equals(getTeam()) && !baseFound){
				//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.ATTACK_TARGET;
				//envoi de message
				
				
				target = p;
			}
		}
	}

	@Override
	public List<WarMessage> getMsgPattern(List<WarMessage> listeMsg, String pattern) {
		List<WarMessage> res = new ArrayList<WarMessage>();
		for(WarMessage m : listeMsg){
			if(m.getMessage().matches(pattern))
			{res.add(m);}
		}
		return res;
	}

	@Override
	public void sendMessageRadius(List<Percept> env, String Message, String[] content, int radius) {
		for(Percept p : env){
			if(p.getDistance() < radius){
				sendMessage(p.getId(), Message, content);
			}
		}
	}

	@Override
	public void sendMessageRadius(List<Percept> env, String Message, String[] content, int radius, String type) {
		for(Percept p : env){
			if(p.getDistance() < radius && p.getType().equals(type)){
				sendMessage(p.getId(), Message, content);
			}
		}
	}
	
}
