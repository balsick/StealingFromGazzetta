package com.balsick.gazzettaparser;

import java.util.ArrayList;
import java.util.List;

public class FootballTeam {
	public String name;
	List<FootballPlayer> players;
	
	public void addPlayer(FootballPlayer player){
		if (players == null)
			players = new ArrayList<>();
		players.add(player);
	}
}
