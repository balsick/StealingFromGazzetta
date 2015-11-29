package com.balsick.gazzettaparser;

public class FootballPlayer {
	
	public String name;
	public String surname;
	public String status;
	public boolean isPlaying() {
		return status.equals(FootballConstants.PLAYING);
	}

}
