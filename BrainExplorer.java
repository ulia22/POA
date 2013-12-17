package edu.turtlekit2.warbot.team;

import java.util.ArrayList;
import java.util.List;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
import edu.turtlekit2.warbot.team.message.MessageEncapsule;
import edu.turtlekit2.warbot.team.message.MessageType;
import edu.turtlekit2.warbot.team.state.JobsExplorer;
import edu.turtlekit2.warbot.team.state.StateExplorer;
import edu.turtlekit2.warbot.waritems.WarFood;

public class BrainExplorer extends WarBrain implements MessageEncapsule{
	
	private JobsExplorer job;
	private StateExplorer state;
	private int idBase;
	private int angBase;
	private int distBase;
	private long tick;
	private int energyInit;
	
	private Percept target = null;
	
	public BrainExplorer(){
		this.job = JobsExplorer.GATHERER;
		this.state = StateExplorer.INITIAL;
		
		this.tick = -1;
		this.idBase = -1; //Maj au premier tick.
		this.target = null;
		this.angBase = 0;
		this.distBase = -1;
		this.energyInit=0;
	}
	
	@Override
	public String action() {
		List<Percept> liste = getPercepts();
		List<WarMessage> listeM = getMessage();
		
		//Mise à jour du tick courrant grace à un message envoyé par la base (synchronisation des ticks).
		if(tick > 0){
			tick++;
		}else{
			for(Percept p : liste){
				if(p.getType().equals("WarBase")){
					sendMessage(p.getId(), MessageType.ASK_TICK.getMotCle(), null);
					idBase = p.getId();
				}
			}
			this.energyInit = this.getEnergy();
		}
		
		//On gère les messages recu à ce tick.
		manageMessage(listeM);
		
		//On gère les états/jobs.
		if(isBlocked()){
			while(isBlocked()){
				setRandomHeading();
			}
		}
		
		switch(job){
		
		case GATHERER :
			switch(state){
			
			//Si le sac est vide on passe en état SEARCH_FOOD.
			case INITIAL: 
					if(!fullBag()){
						state = StateExplorer.SEARCH_FOOD;
					}else{
						state = StateExplorer.BRING_BACK_FOOD;
					}
				break;
			case SEARCH_FOOD:
				//On cherche de la bouffe sur le terrain
				target = null;
				for(Percept p : liste){
					if(p.getType().equals("WarFood")){
						if(target == null){target = p;}
						else if(p.getDistance() < target.getDistance()){target = p;}
					}
				}
				if(target != null){//On change d'etat.
					state = StateExplorer.GO_TAKE_FOOD;
				}
				break;
			case GO_TAKE_FOOD:
				if(target != null){
					setHeading(target.getAngle());
					if(target.getDistance() < WarFood.MAX_DISTANCE_TAKE){
						target = null;
						state = StateExplorer.INITIAL;
						return "take";
					}
				}
				target = null;
				state = StateExplorer.SEARCH_FOOD;
				break;
			case BRING_BACK_FOOD :
				if(idBase != -1){
					if(distBase < 0){
							sendMessage(idBase, MessageType.WHERE.getMotCle(), null);
							return "idle";
					}else{
						setHeading(angBase);
						target = null;
						for(Percept p : liste){
							if(p.getType().equals("WarBase")){
								if(target == null){target = p;}
							}
						}
						if(target != null){
							if(target.getDistance() < WarFood.MAX_DISTANCE_TAKE){
								setAgentToGive(target.getId());
								if(!emptyBag()){
									return "give";
								}else{
									angBase = 0;
									distBase = -1;
									target = null;
									state = StateExplorer.INITIAL;
								}
							}
						}
					}
				}
				break;
			case RUN_AWAY: break;
			default : ;
			}
		break;
		
		case SPY : 
			switch(state){
			case INITIAL : 
				if(getEnergy() < this.energyInit/3){
					//autre condition mais peut permettre la fuite ou r�afectation a la recherche de nouriture
					this.state = StateExplorer.RUN_AWAY;
				}else{
					this.state = StateExplorer.SEARCH_ENEMY_BASE;
				}
			case SEARCH_ENEMY_BASE : 
				for(Percept p : liste){
					if(p.getTeam() != this.getTeam()){
						if(p.getType().equals("WarBase")){
							//envoi message aux tank avec le percept / reste sur place ?
						}
						else if(p.getType().equals("WarRocketLauncher")){
							//envoi des messages aux tanks pour coordonner les tirs avec le percpet
						}
					}
				}
			case RUN_AWAY : 
				break; //return move;
			default :
				job = JobsExplorer.SPY;
				state = StateExplorer.INITIAL;
			}
		break;
		
		default : 
			job = JobsExplorer.GATHERER;
			state = StateExplorer.INITIAL;
			tick = -1;
		}
		return "move";
	}
	

	
	/**************************
	 * Gestion des messages. *
	 *************************/
	
	void manageMessage(List<WarMessage> liste){
		for(WarMessage wm : liste){
			//Partie pour demander/MàJ le tick.
			if(wm.getMessage().matches(MessageType.ASK_TICK.getPattern())){
				sendMessage(wm.getSender(), MessageType.CURR_TICK.getMotCle()+tick, null);
				
			}else if(wm.getMessage().matches(MessageType.CURR_TICK.getPattern())){
				String tickS = wm.getMessage().replaceAll(MessageType.CURR_TICK.getPattern(), "$3");
				tick = Long.parseLong(tickS)+1;
			}
			
			//Partie pour la demande "where".
			else if(wm.getMessage().matches(MessageType.ICI.getPattern())){
				angBase = wm.getAngle();
				distBase = wm.getDistance();
				idBase = wm.getSender();
			}
		}
	}
	
	@Override
	public List<WarMessage> getMsgPattern(List<WarMessage> listeMsg,
			String pattern) {
		List<WarMessage> res = new ArrayList<WarMessage>();
		for(WarMessage m : listeMsg){
			if(m.getMessage().matches(pattern))
			{res.add(m);}
		}
		return res;
	}

	@Override
	public void sendMessageRadius(List<Percept> env, String Message,
			String[] content, int radius) {
		for(Percept p : env){
			if(p.getDistance() < radius){
				sendMessage(p.getId(), Message, content);
			}
		}
	}

	@Override
	public void sendMessageRadius(List<Percept> env, String Message,
			String[] content, int radius, String type) {
		for(Percept p : env){
			if(p.getDistance() < radius && p.getType().equals(type)){
				sendMessage(p.getId(), Message, content);
			}
		}
	}
}
