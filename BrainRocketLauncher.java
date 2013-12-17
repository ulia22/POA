package edu.turtlekit2.warbot.team;


import java.util.ArrayList;
import java.util.List;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.controller.WarLauncher;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
import edu.turtlekit2.warbot.team.message.MessageEncapsule;
import edu.turtlekit2.warbot.team.message.MessageType;
import edu.turtlekit2.warbot.team.state.JobsRocketLauncher;
import edu.turtlekit2.warbot.team.state.StateExplorer;
import edu.turtlekit2.warbot.team.state.StateRocketLauncher;
import edu.turtlekit2.warbot.waritems.WarFood;
import edu.turtlekit2.warbot.waritems.WarRocket;

public class BrainRocketLauncher extends WarBrain implements MessageEncapsule{
	
	private boolean baseFound;
	private boolean onDemand;
	
	private long tick;
	private int idBase;
	private int angBase = -1;
	private int distBase = -1;
	
	private int anglAtck = -1;
	private int distAtck = -1;

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
					if(target == null){
						jobs = JobsRocketLauncher.SEARCH;
						state = StateRocketLauncher.INITIAL;
					}else{
						//faire un test de distance pour voir si a porter de feux
						setHeading(target.getAngle());
						setAngleTurret(target.getAngle());
						target = null;
						return "fire";
					}
					break;
				
				case ATTACK_WARBASE:
					//on peut modifier en demandant un autre placement
					if(distAtck < 15){
						setHeading(anglAtck);
						setAngleTurret(anglAtck);
						return "fire";
					}
					break;
					
				default : state = StateRocketLauncher.INITIAL;
			}	
		case DEFEND :
			switch(state){
				case INITIAL : 
					target = null;
					state = StateRocketLauncher.GO_DEF_BASE;
				break;
				
				//doit aller a la base, s'arreter a une certaine distance puis detruire tout ce qui bouge
				// voir apres si attaque coordonn�e ou non
				case GO_DEF_BASE:
					if(idBase != -1){
						//partie pour demander ou se trouve la base
						if(distBase < 0){
								sendMessage(idBase, MessageType.WHERE.getMotCle(), null);
								return "idle";
						}
						//partie pour changement d 'etat a proximit� de la base
						else if(distBase < 50){
							jobs = JobsRocketLauncher.ATTACK;
							state = StateRocketLauncher.INITIAL;
							idBase = -1;
							distBase = -1;
						}
						else{
							setHeading(angBase);
						}
					}
					System.out.println(idBase);
					break;
			}
			break;
			
			default : jobs = JobsRocketLauncher.SEARCH;
		}
		
		return "move";
	}
	
	void managePercept(List<Percept> liste){
		for(Percept p : liste){
			//cas de detection de la warBase adverse
			if(p.getType().equals("WarBase") && !p.getTeam().equals(getTeam())){
				//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.GO_ATTACK_BASE;
				
				String[] temp = new String[2];
				temp[1] = p.getAngle()+"";
				temp[2] = p.getDistance()+"";
				anglAtck = p.getAngle();
				distAtck = p.getDistance();
				
				broadcastMessage("WarRocketLauncher", MessageType.BASE_ENEMY_FOUND.getMotCle(), temp);
				target = p;
				baseFound = true;
			}
			//cas de detection de tank ennemi
			else if(p.getType().equals("WarRocketLauncher") && !p.getTeam().equals(getTeam()) && !baseFound){
				//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.ATTACK_TARGET;
				//envoi de message
				//broadcastMessage("WarRocketLauncher", MessageType.PERCEPT.getMotCle()+ p.getAngle()+" "+p.getDistance()+" "+p.getId()+" \"\""+p.getTeam()+"\"\" "+p.getType()+" "+p.getEnergy()+" "+p.getHeading(), null);
				
				target = p;
			}
			//cas de detection d'explorer ennemi
			else if(p.getType().equals("WarExplorer") && !p.getTeam().equals(getTeam()) && !baseFound){
				//changement de state
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.ATTACK_TARGET;
				//envoi de message
				target = p;
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
					// erreur viens de la, on attaque aux coordonn�e de l'attaquant qui as vu,
					// il faut recalculer a partir des infos du messages en lui meme, et du contenu du message
					// deuxuement un test de distance pour voir si c'est la peine ou non d'executer l'ordre
					if(wm.getDistance() < 50){
						target = VirtualPercept.createVP(wm);
						jobs = JobsRocketLauncher.ATTACK;
						state = StateRocketLauncher.GO_ATTACK_ASKED_TARGET;
					}
				}
			}
			else if(wm.getMessage().matches(MessageType.BASE_ATTACKED.getPattern())){
				jobs = JobsRocketLauncher.DEFEND;
				state = StateRocketLauncher.GO_DEF_BASE;
				
			}
			else if(wm.getMessage().matches(MessageType.BASE_SPYED.getPattern())){
				//cas base surement reperer
				
			}
			else if(wm.getMessage().matches(MessageType.BASE_END_ALERT.getPattern())){
				//cas de fin d'attaque de la base
				jobs = JobsRocketLauncher.SEARCH;
				state = StateRocketLauncher.INITIAL;
			}
			//Partie pour la demande "where".
			else if(wm.getMessage().matches(MessageType.ICI.getPattern()) && wm.getType().equals("WarBase")){
				angBase = wm.getAngle();
				distBase = wm.getDistance();
				idBase = wm.getSender();
			}
			else if(wm.getMessage().matches(MessageType.BASE_ENEMY_FOUND.getPattern())){
				anglAtck = Integer.parseInt(wm.getContent()[0]);
				if(distAtck < Integer.parseInt(wm.getContent()[1])){
					distAtck = Integer.parseInt(wm.getContent()[1]);
				}
				jobs = JobsRocketLauncher.ATTACK;
				state = StateRocketLauncher.GO_ATTACK_BASE;
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
