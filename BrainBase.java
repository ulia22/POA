package edu.turtlekit2.warbot.team;

import java.util.ArrayList;
import java.util.List;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
import edu.turtlekit2.warbot.team.message.MessageEncapsule;
import edu.turtlekit2.warbot.team.message.MessageType;

public class BrainBase extends WarBrain implements MessageEncapsule{
	private long tick = 0;
	private boolean onAlert = false;

	public BrainBase(){
		
	}

	@Override
	public String action() {
		tick++;

		List<WarMessage> liste = getMessage();
		List<Percept> listP = getPercepts();
		
		manageMessage(liste);
		managePercept(listP);
		
		if(!emptyBag()){
			return "eat";
		}
		if(getEnergy() > 12000){
			setNextAgentCreate("Explorer");
			return "create";
		}
		
		onAlert = false;
		
		return "idle";
	}
	
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
		}
	}
	
	void managePercept(List<Percept> list){
		for(Percept p : list){
			// cas des ennemis ennemi
			if(!p.getTeam().equals(this.getTeam())){
				//cas du rocket launcher
				if(p.getType().equals("WarRocketLauncher")){
					broadcastMessage("WarRocketLauncher", MessageType.BASE_ATTACKED.getMotCle(), null);
					onAlert = true;
				}
				//cas des explorer
				else if(p.getType().equals("WarExplorer")){
					broadcastMessage("WarRocketLauncher", MessageType.BASE_SPYED.getMotCle(), null);
				}
			}
		}
		
		if(!onAlert){
			broadcastMessage("WarRocketLauncher", MessageType.BASE_END_ALERT.getMotCle(), null);
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
