package com.balsick.gazzettaparser;

import java.util.HashMap;
import java.util.Map;

import com.balsick.tools.communication.JSonifiable;

public class FootballPlayer implements JSonifiable {
	
	public String player;
	public String status;
	public FootballTeam team;
	
	public boolean isPlaying() {
		return status.equals(FootballConstants.PLAYING);
	}
	@Override
	public String toString() {
		return player;
	}
	@Override
	public Map<String, Object> getJSonMap() {
		Map<String, Object> map = new HashMap<>();
//		map.put("player", player);
		map.put("status", status);
		try {
			map.put("vsteam", team.getVersus().toString());
		} catch (NullPointerException ex){
		}
		return map;
	}
	@Override
	public void revertFromJSon(Map<String, Object> map) {
		player = (String) map.get("player");
		status = (String) map.get("status");
	}
}
