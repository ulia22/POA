package edu.turtlekit2.warbot.team.message;

import java.util.List;

import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
public interface MessageEncapsule {

	
	/**
	 * Retourne une sous-liste de listeMsg, contenant tout les messages qui correspondent au pattern
	 * "pattern".
	 * @param listeMsg liste à analyser
	 * @param pattern à employer sur les messages
	 * @return
	 */
	List<WarMessage> getMsgPattern(List<WarMessage> listeMsg, String pattern);
	
	
	/**
	 * Envoi un message à tous les percepts (alliés) dans un rayon prédéfini;
	 * @param env liste des percepts à analyser
	 * @param Message message à envoyer
	 * @param content content du message
	 * @param radius rayon d'envoi du message
	 */
	void sendMessageRadius(List<Percept> env, String Message, String[] content, int radius);
	
	
	/**
	 * Envoi un message à tous les percepts d'un type choisi dans un rayon choisi.
	 * @param env liste des percepts à analyser
	 * @param Message message à envoyer
	 * @param content content du message
	 * @param radius rayon d'envoi du message
	 * @param type des persepts à selectionner
	 */
	void sendMessageRadius(List<Percept> env, String Message, String[] content, int radius, String type);
	
	
	
}
