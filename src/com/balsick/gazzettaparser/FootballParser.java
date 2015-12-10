package com.balsick.gazzettaparser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	Map<String, FootballPlayer> players = new HashMap<>();
	private boolean toList = false;
	
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
			homeTeam.name = teamNames.get(0).child(0).text();
			homeTeam.addPlayers(getTeamPlayers(teamPlayersInner.get(0)));
			homeTeam.addPlayers(getTeamOthers("homeDetails", e, "Panchina:"));
			homeTeam.addPlayers(getTeamOthers("homeDetails", e, "Squalificati:"));
			homeTeam.addPlayers(getTeamOthers("homeDetails", e, "Indisponibili:"));
			homeTeam.addPlayers(getTeamOthers("homeDetails", e, "Altri:"));
			
			FootballTeam awayTeam = new FootballTeam();
			teams.add(awayTeam);
			awayTeam.name = teamNames.get(1).child(0).text();
			awayTeam.addPlayers(getTeamPlayers(teamPlayersInner.get(1)));
			awayTeam.addPlayers(getTeamOthers("awayDetails", e, "Panchina:"));
			awayTeam.addPlayers(getTeamOthers("awayDetails", e, "Squalificati:"));
			awayTeam.addPlayers(getTeamOthers("awayDetails", e, "Indisponibili:"));
			awayTeam.addPlayers(getTeamOthers("awayDetails", e, "Altri:"));
			homeTeam.versus = awayTeam;
			awayTeam.versus = homeTeam;
		}
	}
	
	private List<FootballPlayer> getTeamPlayers(Element team){
		List<FootballPlayer> players = new ArrayList<>();
		Elements teamPlayers = team.getElementsByClass("team-player");
		teamPlayers.forEach((player)->{
			FootballPlayer fp = new FootballPlayer();
			fp.player = player.text();
			fp.status = FootballConstants.PLAYING;
			players.add(fp);
			FootballParser.this.players.put(fp.player, fp);
		});
		return players;
	}
	
	private List<FootballPlayer> getTeamOthers(String team, Element e, String tag){
		List<FootballPlayer> players = new ArrayList<>();
		Elements teamPlayersBench = e.getElementsByClass(team);
		String listOfBenchPlayersInString = teamPlayersBench.get(0).getElementsMatchingOwnText("^"+tag)
				.get(0).parent().text();
//		System.out.println(listOfBenchPlayersInString);
		StringTokenizer st = new StringTokenizer(listOfBenchPlayersInString, ",");
		while (st.hasMoreTokens()){
			String a = st.nextToken();
			if (a.contains(tag))
				a = a.substring(tag.length());
			if (a.contains("("))
				a = a.substring(0, a.indexOf("("));
			if (a.matches(".*[0-9].*"))
				a = a.replaceAll("[0-9]", "");
			a = cleanFrom8194(a);
			if (a.length() == 0 || a.contains("(") || a.contains(")") || a.matches(".*[0-9].*"))
				continue;
			FootballPlayer fp = new FootballPlayer();
			fp.player = a.toUpperCase();
			fp.status = getStatusFor(tag);
			players.add(fp);
			FootballParser.this.players.put(fp.player, fp);
//			System.out.println(a);
		}
		return players;
	}
	
	private String getStatusFor(String tag){
		switch (tag){
		case "Panchina:":
			return FootballConstants.BENCH;
		case "Squalificati:":
			return FootballConstants.DISQUALIFIED;
		default:
			return FootballConstants.UNAVAILABLE;
		}
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
	
	public void setToList(boolean toList) {
		this.toList  = toList;
	}
	
	public Object getPlayers(List<String> requestParameters) {
		if (toList)
			return getPlayersList(requestParameters);
		return getPlayersMap(requestParameters);
	}
	
	public Map<String, FootballPlayer> getPlayersMap(List<String> requestParameters) {
		return stream(requestParameters)
				.peek((fp) -> fp.setToMap(true))
				.collect(Collectors.toMap(FootballPlayer::toString, Function.identity()));
	}
	
	public List<FootballPlayer> getPlayersList(List<String> requestParameters) {
			return stream(requestParameters).collect(Collectors.toList());
		}
	
	public Stream<FootballPlayer> stream(List<String> requestParameters){
		List<String> players = requestParameters;
		return FootballParser.this.players
				.keySet().stream()
				.filter((s) -> players == null || players.contains(s) || players.contains(s.toUpperCase()))
				.map(this.players::get);
	}
	
	@SuppressWarnings({ "serial", "unused"})
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
							return a.player.compareTo(b.player);
						if (a.isPlaying())
							return -1;
						return 1;
					})
					.forEach((fp)->{
						JLabel label = new JLabel(fp.player);
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
