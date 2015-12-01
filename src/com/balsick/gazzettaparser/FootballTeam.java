package com.balsick.gazzettaparser;

import java.util.ArrayList;
import java.util.List;

public class FootballTeam {
	public String name;
	List<FootballPlayer> players;
	
	public void addPlayers(List<FootballPlayer> players) {
		if (players == null)
			players = new ArrayList<>();
		players.addAll(players);
	}
	
	public void addPlayer(FootballPlayer player){
		if (players == null)
			players = new ArrayList<>();
		players.add(player);
	}
}
