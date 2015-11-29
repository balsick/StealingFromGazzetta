package com.balsick.gazzettaparser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class FootballParser {
	List<FootballTeam> teams = new ArrayList<>();
	
	public void parse(){

		Document doc = null;
		try {
			doc = Jsoup.connect("http://www.gazzetta.it/Calcio/prob_form/").get();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String class_name = "matchFieldContainer";
		Elements elements = doc.getElementsByClass(class_name);
		if (elements.size() == 0){
			System.err.println("can't find anything with class = "+class_name);
			for (Node e : doc.getAllElements())
				System.err.println(e.nodeName()+"\t"+e.attributes());
			return;
		}
		for (Element e : elements){
//			Element lastUpdate = e.getElementsByClass("lastupdate").get(0);
			Elements teamPlayersInner = e.getElementsByClass("team-players-inner");
			Elements teamNames = e.getElementsByClass("teamName");
			
			FootballTeam homeTeam = new FootballTeam();
			teams.add(homeTeam);
			homeTeam.name = teamNames.get(0).text();
			getTeamPlayers(teamPlayersInner.get(0)).forEach((fp)->homeTeam.addPlayer(fp));
			getTeamBench("homeDetails", e).forEach((fp)->homeTeam.addPlayer(fp));
			
			FootballTeam awayTeam = new FootballTeam();
			teams.add(awayTeam);
			awayTeam.name = teamNames.get(1).text();
			getTeamPlayers(teamPlayersInner.get(1)).forEach((fp)->awayTeam.addPlayer(fp));
			getTeamBench("awayDetails", e).forEach((fp)->awayTeam.addPlayer(fp));
		}
		
	draw();
	}
	
	private List<FootballPlayer> getTeamPlayers(Element team){
		List<FootballPlayer> players = new ArrayList<>();
		Elements teamPlayers = team.getElementsByClass("team-player");
		teamPlayers.forEach((player)->{
			FootballPlayer fp = new FootballPlayer();
			fp.surname = player.text();
			fp.status = FootballConstants.PLAYING;
			players.add(fp);
		});
		return players;
	}
	
	private List<FootballPlayer> getTeamBench(String team, Element e){
		List<FootballPlayer> players = new ArrayList<>();
		Elements teamPlayersBench = e.getElementsByClass(team);
		String listOfBenchPlayersInString = teamPlayersBench.get(0).getElementsMatchingOwnText("^Panchina:")
				.get(0).parent().text();
		System.out.println(listOfBenchPlayersInString);
		StringTokenizer st = new StringTokenizer(listOfBenchPlayersInString, "0123456789?,");
		while (st.hasMoreTokens()){
			String a = st.nextToken();
			a = cleanFrom8194(a);
			if (a.length() == 0 || a.startsWith("Panchina:"))
				continue;
			FootballPlayer fp = new FootballPlayer();
			fp.surname = a;
			fp.status = FootballConstants.BENCH;
			players.add(fp);
			System.out.println(a);
		}
		return players;
	}
	
	private String cleanFrom8194(String a){
		for (int l = 0; l < a.length();l++){
			if (a.charAt(l) == 8194) {
				a = a.substring(0, l)+a.substring(l+1);
				l--;
			}
		}
		return a.trim();
	}
	
	@SuppressWarnings("serial")
	private void draw(){
		JFrame frame = new JFrame("CIAO");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel(new FlowLayout());
		teams.stream()
		.sorted((a,b)->a.name.compareTo(b.name))
		.forEach(ft->{
			JPanel ftPanel = new JPanel(){
				{
					setBackground(Color.white);
					JLabel teamname = new JLabel(ft.name);
					teamname.setFont(new Font("Arial", Font.BOLD, 30));
					teamname.setForeground(Color.cyan);
					add(teamname);
					ft.players.stream()
					.sorted((a,b) -> {
						if (a.isPlaying() == b.isPlaying())
							return a.surname.compareTo(b.surname);
						if (a.isPlaying())
							return -1;
						return 1;
					})
					.forEach((fp)->{
						JLabel label = new JLabel(fp.surname);
						label.setForeground(Color.white);
						label.setOpaque(true);
						label.setBackground(fp.isPlaying() ? Color.green : Color.red);
						add(label);
					});
				}
			};
			ftPanel.setLayout(new BoxLayout(ftPanel, BoxLayout.Y_AXIS));
			panel.add(ftPanel);
		});
		JScrollPane sc = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.setContentPane(sc);
		frame.setSize(new Dimension(1000, 600));
		frame.pack();
		frame.setVisible(true);
	}
}
