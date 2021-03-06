package edu.turtlekit2.warbot.team.message;

/**
 * 
 * @author ulia22
 *
 */
public enum MessageType {
	/**
	 * Defini la tout les type message possible.
	 */
	ASK_TICK("Tick?", "^Tick\\?$"), //Demande le tick courrant(on demande à la WarBase).
	CURR_TICK("Tick ", "^(Tick)(\\s?)(\\d+)$"),//Tick courant retourné par la base
	
	WHERE("Where?", "^Where\\?$"),
	ICI("Ici", "^Ici$"),
	
	BASE_ATTACKED("BaseAttaque", "^BaseAttaque$"),
	I_DEFEND("JeDefend","^JeDefend$"),
	
	BASE_SPYED("BaseSpy", "^BaseSpy$"),
	I_RETURN("return","^return$"),
	
	BASE_END_ALERT("BaseEndAlert","^BaseEndAlert$"),
	
	BASE_ENEMY_FOUND("Founded", "^Founded$"),
	
	// "Percept angle distance id ""team"" type energy heading"
	//Example : "Percept 32 65 85 team WarBase 500 12.6"
	PERCEPT("Percept ", "^Percept ([\\d\\-]+) (\\d+) (\\d+) \"\"(.*)\"\" (\\w+) (\\d+) ([\\d\\.])+$")
	;
	
	private final String motCle;
    private final String pattern;
    
    MessageType(String mot, String pat)
    {
        this.motCle = mot;
        this.pattern = pat;
    }

    
	public String getMotCle() {
		return motCle;
	}
	public String getPattern() {
		return pattern;
	}
    
    
	
}
