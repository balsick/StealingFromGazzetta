package com.balsick.gazzettaparser;

import java.util.HashMap;
import java.util.Map;

import com.balsick.tools.communication.JSonifiable;

public class FootballPlayer implements JSonifiable{
	
	public String name;
	public String player;
	public String status;
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
		map.put("player", player);
		map.put("status", status);
		return map;
	}
	@Override
	public void revertFromJSon(Map<String, Object> map) {
		player = (String) map.get("player");
		status = (String) map.get("status");
	}
}
