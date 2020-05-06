package com.blalp.custompref;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.milkbowl.vault.chat.Chat;

public class Listeners implements Listener {
	private Chat chat;

	public Listeners(Chat chat) {
		this.chat = chat;
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("customprefix.admin")
				&& CustomPrefix.getInstance().getConfig().isSet("playerList")) {
			event.getPlayer()
					.sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE + "CustomPrefix" + ChatColor.WHITE + "]"
							+ " There are " + CustomPrefix.getInstance().getConfig().getList("playerList").size()
							+ " pending proposals.");
		}
		List<String> players = (List<String>) CustomPrefix.getInstance().getConfig().getList("playerList");
		if (players != null) {
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).equals(event.getPlayer().getUniqueId().toString())) {
					if (CustomPrefix.getInstance().getConfig().getString("players." + players.get(i) + ".status")
							.equals("denied")) {
						event.getPlayer()
								.sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE + "CustomPrefix" + ChatColor.WHITE
										+ "]" + " I am sorry, but your custom prefix was denied"
										+ ((CustomPrefix.getInstance().getConfig()
												.getString("players." + players.get(i) + ".reason").equals(""))
														? ""
														: " because " + CustomPrefix.getInstance().getConfig()
																.getString("players." + players.get(i) + ".reason"))
										+ ".");
						if (CustomPrefix.getInstance().getConfig()
								.contains("players." + players.get(i) + ".prefixrequests")) {
							int count = CustomPrefix.getInstance().getConfig()
									.getInt("players." + players.get(i) + ".prefixrequests");
							CustomPrefix.getInstance().getConfig().set("players." + players.get(i), "");
							CustomPrefix.getInstance().getConfig().set("players." + players.get(i) + ".prefixrequests",
									count);
							CustomPrefix.getInstance().getConfig().set("players." + players.get(i) + ".status",
									"ready");
						} else {
							CustomPrefix.getInstance().getConfig().set("players." + players.get(i), "");
						}
						List<String> players1 = (List<String>) CustomPrefix.getInstance().getConfig()
								.getList("playerList");
						players1.remove(players.get(i));
						CustomPrefix.getInstance().getConfig().set("playerList", players1);
						CustomPrefix.getInstance().saveConfig();
					} else if (CustomPrefix.getInstance().getConfig().getString("players." + players.get(i) + ".status")
							.equals("accepted")) {
						event.getPlayer()
								.sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE + "CustomPrefix" + ChatColor.WHITE
										+ "]" + " Your prefix was accepted"
										+ ((CustomPrefix.getInstance().getConfig()
												.getString("players." + players.get(i) + ".reason").equals(""))
														? ""
														: " because " + CustomPrefix.getInstance().getConfig()
																.getString("players." + players.get(i) + ".reason"))
										+ "! Thank you for donating!");
						chat.setPlayerPrefix(event.getPlayer(), CustomPrefix.getInstance().getConfig()
								.getString("players." + players.get(i) + ".requestedPrefix"));
						int count = CustomPrefix.getInstance().getConfig()
								.getInt("players." + players.get(i) + ".prefixrequests");
						CustomPrefix.getInstance().getConfig().set("players." + players.get(i), "");
						if (count != 1) {
							CustomPrefix.getInstance().getConfig().set("players." + players.get(i) + ".prefixrequests",
									count - 1);
							CustomPrefix.getInstance().getConfig().set("players." + players.get(i) + ".status",
									"ready");
						}
						List<String> players1 = (List<String>) CustomPrefix.getInstance().getConfig()
								.getList("playerList");
						players1.remove(players.get(i));
						CustomPrefix.getInstance().getConfig().set("playerList", players1);
						CustomPrefix.getInstance().saveConfig();
					} else if (CustomPrefix.getInstance().getConfig().getString("players." + players.get(i) + ".status")
							.equals("pending")) {
						event.getPlayer().sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE + "CustomPrefix"
								+ ChatColor.WHITE + "]" + " Your prefix is still pending!");
					} else if (CustomPrefix.getInstance().getConfig().getString("players." + players.get(i) + ".status")
							.equals("ready")) {
						event.getPlayer()
								.sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE + "CustomPrefix" + ChatColor.WHITE
										+ "]"
										+ " You can request a prefic with /pre [prefix]! Thank you for donating :)");
					}
				}
			}
		}
	}
}