import com.balsick.gazzettaparser.FootballParser;
import com.balsick.tools.communication.JSonParser;

public class ThisIsTheFuckingMainYEAH {
	
	public static void main(String[] args){
		System.out.println("YEAH WE STARTED!!!");
		FootballParser fp = new FootballParser();
		fp.parse();
		System.out.print(JSonParser.getJSon(fp.getPlayersMap(null)));
	}

}
