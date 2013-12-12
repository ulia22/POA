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
	
	private boolean baseFound;
	private boolean onDemand;
	
	private long tick;
	private int idBase;
	
	private JobsRocketLauncher jobs;
	private StateRocketLauncher state;
	
	private Percept target;
	
	public BrainRocketLauncher(){
		baseFound = false;
		onDemand = false;
		
		tick = -1;
		idBase = -1;
		
		jobs = JobsRocketLauncher.SEARCH;
		state = StateRocketLauncher.INITIAL;
		
		target = null;
	}
	
	@Override
	public String action() {
		List<Percept> listeP = getPercepts();
		List<WarMessage> listeM = getMessage();
		
		manageMessage(listeM);
		managePercept(listeP);
		
		//Mise à jour du tick courrant grace à un message envoyé par la base (synchronisation des ticks).
		if(tick > 0)
			tick++;
		else{
			for(Percept p : listeP){
				if(p.getType().equals("WarBase")){
					sendMessage(p.getId(), MessageType.ASK_TICK.getMotCle(), null);
					idBase = p.getId();
				}
			}
		}
				
		//Si pas chargé on recharge.
		if(!isReloaded() && !isReloading()){
			return "reload";
		}
		
		while(isBlocked()){
			setRandomHeading();
		}
		
		

		switch(jobs){
		
		case SEARCH :
				switch(state){
					case INITIAL : 
						if(onDemand){
							jobs = JobsRocketLauncher.ATTACK;
							state = StateRocketLauncher.GO_ATTACK_ASKED_TARGET;
						}
						else if(target == null){
							state = StateRocketLauncher.SEARCHING_TARGET;
						}else{
							jobs = JobsRocketLauncher.ATTACK;
							state = StateRocketLauncher.ATTACK_TARGET;
						};
					break;
				
					case SEARCHING_TARGET : ;
						break;
					
					default : state = StateRocketLauncher.INITIAL;
				}
			;
			break;
		
		case ATTACK :
			switch(state){
				case INITIAL : 
					
					break;
				
				//attaque la cible
				case ATTACK_TARGET :
					if(target != null){
						setAngleTurret(target.getAngle());
						target = null;
						return "fire";
					}else{
						jobs = JobsRocketLauncher.SEARCH;
						state = StateRocketLauncher.INITIAL;
					}
				
				// attaque cible par trigo
				case GO_ATTACK_ASKED_TARGET : 
					setHeading(target.getAngle());
					break;
				
				default : state = StateRocketLauncher.INITIAL;
			}	
		case DEFEND :
			switch(state){
				case INITIAL : 
				onDemand = false;
				target = null;
				state = StateRocketLauncher.GO_DEF_BASE;
				
				break;
				
				case GO_DEF_BASE:;
				break;	
			}
			break;
			
			default : jobs = JobsRocketLauncher.SEARCH;
		}
		
		return "move";
	}
	
	void managePercept(List<Percept> liste){
		for(Percept p : liste){
			if(p.getType().equals("WarBase") && !p.getTeam().equals(getTeam())){
				/*//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.GO_ATTACK_BASE;
				//envoi de message
				target = p;
				baseFound = true;*/
			}
			else if(p.getType().equals("WarRocketLauncher") && !p.getTeam().equals(getTeam()) && !baseFound){
				//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.ATTACK_TARGET;
				//envoi de message
				broadcastMessage("WarRocketLauncher", MessageType.PERCEPT.getMotCle()+ p.getAngle()+" "+p.getDistance()+" "+p.getId()+" \"\""+p.getTeam()+"\"\" "+p.getType()+" "+p.getEnergy()+" "+p.getHeading(), null);
				
				target = p;
			}
			else if(p.getType().equals("WarExplorer") && !p.getTeam().equals(getTeam()) && !baseFound){
				/*//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.ATTACK_TARGET;
				//envoi de message
				
				
				target = p;*/
			}
		}
	}
	
	
	

	/**********************
	 * Gérer les messages.*
	 *********************/
	
	void manageMessage(List<WarMessage> liste){
		for(WarMessage wm : liste){
			
			//Messages pour demander/mettre à jour le tick.
			if(wm.getMessage().matches(MessageType.ASK_TICK.getPattern())){
				sendMessage(wm.getSender(), MessageType.CURR_TICK.getMotCle()+tick, null);
				
			}else if(wm.getMessage().matches(MessageType.CURR_TICK.getPattern())){
				String tickS = wm.getMessage().replaceAll(MessageType.CURR_TICK.getPattern(), "$3");
				tick = Long.parseLong(tickS)+1;
			}
			
			//Message pour gérer la demande "where".
			else if(wm.getMessage().matches(MessageType.WHERE.getPattern())){
				sendMessage(wm.getSender(), MessageType.ICI.getMotCle(), null);
			}
			
			//Message demande Percept
			else if(wm.getMessage().matches(MessageType.PERCEPT.getPattern())){
				if(target == null){
					VirtualPercept vp = VirtualPercept.createVP(wm);
					target = vp;
					jobs = JobsRocketLauncher.ATTACK;
					state = StateRocketLauncher.GO_ATTACK_ASKED_TARGET;
				}
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
