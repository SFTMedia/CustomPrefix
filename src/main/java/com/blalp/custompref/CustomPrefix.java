package com.blalp.custompref;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public class CustomPrefix extends JavaPlugin {
	private static CustomPrefix instance;
	private static Permission permission = null;
	private static Chat chat = null;

	public void onEnable() {
		saveDefaultConfig();
		Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), this);
		instance=this;
		setupPermissions();
		setupChatManager();
	}
	// Setup vault permissions
	private void setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
	}
	// Setup vault chat
	private void setupChatManager() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}
	}
	@SuppressWarnings({ "unchecked", "deprecation" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("prefix")){
			if(sender instanceof Player&&(permission.has(sender, "customprefix.prefix") || (getConfig().contains("players."+((Player)sender).getUniqueId()+".prefixrequests")) && getConfig().getInt("players."+((Player)sender).getUniqueId()+".prefixrequests")>0)){
				if(getConfig().getInt("players.time."+((Player)sender).getUniqueId())+getConfig().getInt("delayDays")<=getCustomDate()){
					if (args.length == 0){
						sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+ChatColor.GREEN+"Usage: "+ChatColor.WHITE+"/"+cmd.getName()+" <newPrefix>"+"\n"+ChatColor.AQUA+"Example:"+ChatColor.WHITE+" /prefix &f[&3CoolCustomRank&f]&3");
						return false;
					}
					//PrefixRequest prefixRequest = new PrefixRequest();
					//prefixRequest.setPlayername(sender.getName());
					//prefixRequest.setRequestedPrefix(join(args));
					List<String> players = (List<String>) getConfig().getList("playerList");
					if(players==null){
						players = new ArrayList<String>();
					}
					if(!players.contains(((Player)sender).getUniqueId().toString())){
						players.add(((Player)sender).getUniqueId().toString());
						getConfig().set("playerList", players);
					}
					getConfig().set("players."+((Player)sender).getUniqueId()+".requestedPrefix", join(args));
					getConfig().set("players."+((Player)sender).getUniqueId()+".status", "pending");
					saveConfig();
					sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"You requested the prefix \""+handleColors(join(args))+ChatColor.WHITE+"\" Please hold on while our staff screen this prefix.");
					Bukkit.broadcast(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+sender.getName()+" requested "+handleColors(join(args))+sender.getName()+ChatColor.WHITE+". do /prefixpending [accept/reject] "+sender.getName()+" [reason]", "customprefix.admin");
				} else {
					sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"Please wait "+(getConfig().getInt("players.time."+((Player)sender).getUniqueId())+getConfig().getInt("delayDays")-getCustomDate())+" days before you can request another prefix.");
				}
			} else {
				sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"Please donate for this permission at /buy!");
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("prefixpending")){//usage: /<command> <accept/reject/get> <playername> <reason>
			if(permission.has(sender, "customprefix.admin")){
				if(args.length==0 ){
					sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+ChatColor.GREEN+"Usage: "+ChatColor.WHITE+"/"+cmd.getName()+" <get|list|accept|reject> <player> <reason>");
					return false;
				}
				if(args.length<2&&!(args[0].equalsIgnoreCase("get")||args[0].equalsIgnoreCase("list"))){
					sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"Error, please follow this format. /prefixpending get or /prefixpending <accept/reject> <playername> <reason>");
					return true;
				} else {
					List<String> players = (List<String>) getConfig().getList("playerList");
					if(args[0].equalsIgnoreCase("get")||args[0].equalsIgnoreCase("list")){
						sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"Below is a list of players who are requesting a prefix. Please approve or reject.");
						if(players!=null){
							for(int i=0;i<players.size();i++){
								sender.sendMessage(handleColors(getConfig().getString("players."+players.get(i)+".requestedPrefix")+Bukkit.getOfflinePlayer(UUID.fromString(players.get(i))).getName()+ChatColor.WHITE+": "+"Fake message!"));
							}
						}
					} else if (args[0].equalsIgnoreCase("accept")||args[0].equalsIgnoreCase("yes")||args[0].equalsIgnoreCase("approve")){
						if(args.length<3){
							sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+ChatColor.GREEN+"Usage: "+ChatColor.WHITE+"/"+cmd.getName()+" accept <name> <reason>");
							return false;
						}
						Boolean found = false;
						if(players!=null){
							for(int i=0;i<players.size();i++){
								if(players.get(i).equals(Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString())){
									found=true;
									//PrefixRequest prefixRequest = new PrefixRequest();
									//prefixRequest.setPlayername(args[1]);
									String[] newArgs = new String[args.length-2];
									for(int ii=2;ii<args.length;ii++){
										newArgs[ii-2]=args[ii];
									}
									getConfig().set("players."+players.get(i)+".reason", join(newArgs));
									getConfig().set("players."+players.get(i)+".status", "accepted");
									//prefixRequest.setDeniedReason(join(newArgs));
									//prefixRequest.setRequestedPrefix(pending.get(i).getRequestedPrefix());
									//pending.remove(i);
									//getConfig().set("pending", pending);
									saveConfig();
									getConfig().set("players.time."+players.get(i), getCustomDate());
									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user "+args[1]+" "+"meta setprefix "+'"'+(getConfig().getString("players."+players.get(i)+".requestedPrefix").equals("")?"":getConfig().getString("players."+players.get(i)+".requestedPrefix"))+'"');
									sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] You accepted "+args[1]+"'s Request ("+handleColors(getConfig().getString("players."+players.get(i)+".requestedPrefix"))+ChatColor.WHITE+")"+((getConfig().getString("players."+players.get(i)+".reason").equals(""))?"":" because "+getConfig().getString("players."+players.get(i)+".reason")+"!"));
									if(Bukkit.getPlayer(args[1])!=null){
										Player player = Bukkit.getPlayer(args[1]);
										String newPrefix = getConfig().getString("players."+players.get(i)+".requestedPrefix");
										chat.setPlayerPrefix(player, newPrefix);
										Bukkit.getPlayer(args[1]).sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"]"+" Your prefix was accepted! Thank you for donating! "+((join(newArgs).equals(""))? "":("Your prefix was accepted"+((join(newArgs).equals(""))?"":" because: '"+join(newArgs)+"'"+"!"))));
										int count = getConfig().getInt("players."+players.get(i)+".prefixrequests");
										getConfig().set("players."+players.get(i), "");
										if(count!=1){
											if(count-1!=0){
												getConfig().set("players."+players.get(i)+".prefixrequests",count-1);
											}
											getConfig().set("players."+players.get(i)+".status", "ready");
										}
										List<String> players1 = (List<String>) getConfig().getList("playerList");
										players1.remove(players.get(i));
										getConfig().set("playerList", players1);
									}
									else {
										sender.sendMessage("Player not found");
									}
									saveConfig();
								}
							}
							if(!found){
								sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"I am sorry, but "+args[1]+" isnt found. please use /prefixpending get to get the list of requested prefiexes.");
							}
						} else {
							sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"No pending requests.");
						}
					} else if (args[0].equalsIgnoreCase("reject")||args[0].equalsIgnoreCase("no")||args[0].equalsIgnoreCase("deny")){
						if(args.length<3){
							sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+ChatColor.GREEN+"Usage: "+ChatColor.WHITE+"/"+cmd.getName()+" reject <name> <reason>");
							return false;
						}
						Boolean found = false;
						if(players!=null){
							for(int i=0;i<players.size();i++){
								if(players.get(i).equals(Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString())){
									found=true;
									//PrefixRequest prefixRequest = new PrefixRequest();
									//prefixRequest.setPlayername(args[1]);
									String[] newArgs = new String[args.length-2];
									for(int ii=2;ii<args.length;ii++){
										newArgs[ii-2]=args[ii];
									}
									getConfig().set("players."+players.get(i)+".reason", join(newArgs));
									getConfig().set("players."+players.get(i)+".status", "denied");
									//prefixRequest.setDeniedReason(StringUtils.join(newArgs," "));
									//prefixRequest.setRequestedPrefix(pending.get(i).getRequestedPrefix());
									//pending.remove(i);
									//getConfig().set("pending", pending);
									saveConfig();
									sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] You denied "+args[1]+"'s Request ("+handleColors(getConfig().getString("players."+players.get(i)+".requestedPrefix"))+ChatColor.WHITE+")"+((getConfig().getString("players."+players.get(i)+".reason").equals(""))?"":(" because "+getConfig().getString("players."+players.get(i)+".reason")+"!")));
									if(Bukkit.getPlayer(args[1])!=null){
										Bukkit.getPlayer(args[1]).sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"]"+" I am sorry, but your custom prefix was denied"+((getConfig().getString("players."+players.get(i)+".reason").equals(""))?"":" because "+getConfig().getString("players."+players.get(i)+".reason"))+".");
										if(getConfig().contains("players."+players.get(i)+".prefixrequests")){
											int count = getConfig().getInt("players."+players.get(i)+".prefixrequests");
											getConfig().set("players."+players.get(i), "");
											if(count-1!=0){
												getConfig().set("players."+players.get(i)+".prefixrequests",count);
											}
											getConfig().set("players."+players.get(i)+".status", "ready");
										} else {
											getConfig().set("players."+players.get(i), "");
										}
										List<String> players1 = (List<String>) getConfig().getList("playerList");
										players1.remove(players.get(i));
										getConfig().set("playerList", players1);
									}
									saveConfig();
								}
							}
							if(!found){
								sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"I am sorry, but "+args[1]+" isnt found. please use /prefixpending get to get the list of requested prefixes.");
							}
						} else {
							sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"No pending requests.");
						}
					} else {
						sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"Error, please follow this format. /prefixpending get or /prefixpending <accept/reject> <playername> <reason>");
					}
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("prefixadmin")){
			if(args.length!=1){
				sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+ChatColor.GREEN+"Usage: "+ChatColor.WHITE+"/"+cmd.getName()+" <name>");
				sender.sendMessage("This will give them the ability to request a prefix.");
				return false;
			}
			if(permission.has(sender, "customprefix.console")){
				if(getConfig().contains("players."+Bukkit.getOfflinePlayer(args[0]).getUniqueId()+".prefixrequests")){
					getConfig().set("players."+Bukkit.getOfflinePlayer(args[0]).getUniqueId()+".prefixrequests", getConfig().getInt("players."+Bukkit.getOfflinePlayer(args[0]).getUniqueId()+".prefixrequests")+1);
					if(!getConfig().contains("players."+Bukkit.getOfflinePlayer(args[0]).getUniqueId()+".status")){
						getConfig().set("players."+Bukkit.getOfflinePlayer(args[0]).getUniqueId()+".status", "ready");
					}
				} else {
					getConfig().set("players."+Bukkit.getOfflinePlayer(args[0]).getUniqueId()+".prefixrequests", 1);
					getConfig().set("players."+Bukkit.getOfflinePlayer(args[0]).getUniqueId()+".status", "ready");
				}
				saveConfig();
				sender.sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"] "+"You allowed "+args[0]+" to request a prefix.");
				if(Bukkit.getPlayer(args[0])!=null){
					Bukkit.getPlayer(args[0]).sendMessage(ChatColor.WHITE+"["+ChatColor.BLUE+"CustomPrefix"+ChatColor.WHITE+"]"+" You can request a prefix with /pre [prefix]! Thank you for donating :)");
				}
			}
			return true;
		}
		return false;
	}
	private String handleColors(String input){
		if(input!=null){
			return input.replaceAll("&","§");
		} else {
			return null;
		}
	}
	private static String join(Object[] in){
		String output="";
		for(Object item: in){
			output+=item.toString()+" ";
		}
		output=output.substring(0,output.length()-1);
		return output;
	}
	@SuppressWarnings("deprecation")
	public static int getCustomDate(){
		Date date = new Date();
		return date.getYear()*365+date.getMonth()*12+date.getDate();
	}
	public static CustomPrefix getInstance() {
		return instance;
	}
}
