package com.balsick.gazzettaparser;

import java.util.ArrayList;
import java.util.List;

public class FootballTeam {
	public String name;
	List<FootballPlayer> players;
	public FootballTeam versus;
	
	public void addPlayers(List<FootballPlayer> players) {
		players.forEach(this::addPlayer);
	}
	
	public void addPlayer(FootballPlayer player){
		if (players == null)
			players = new ArrayList<>();
		players.add(player);
		player.team = this;
	}

	public FootballTeam getVersus() {
		return versus;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
