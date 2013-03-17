/*  
 *  NoobProtector, Minecraft bukkit plugin
 *  (c)2012, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/noob-protector/
 *    
 *  This file is part of NoobProtector.
 *  
 *  NoobProtector is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  WeatherMan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoobProtector.  If not, see <http://www.gnorg/licenses/>.
 * 
 */

package fromgate.noobprotector;
/*  
 *  (c)2012, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/weatherman/
 *   * 
 *  This file is part of ReActions.
 *  
 *  WeatherMan is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  WeatherMan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with WeatherMan.  If not, see <http://www.gnorg/licenses/>.
 * 
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bukkit.entity.Player;


public class NPUtil extends FGUtilCore {
	NoobProtector plg;

	public NPUtil(NoobProtector plugin, boolean vcheck, boolean savelng, String language, String devbukkitname, String version_name, String plgcmd, String px){
		super (plugin, vcheck, savelng, language, devbukkitname, version_name, plgcmd, px);
		this.plg = plugin;

		FillMSG();
		InitCmd();


		if (savelng) this.SaveMSG();
	}

	public void PrintCfg(Player p){
		PrintMsg(p, "&6&lNoobProtector v"+des.getVersion()+" &r&6| "+MSG("cfg_configuration",'6'));
		PrintMSG (p,"msg_currenttime",getServerTime(""));
		PrintMSG (p,"msg_servertimezone",TimeZone.getDefault().getDisplayName());
		PrintMSG (p, "msg_cfgtimezone", plg.timezone);
		PrintMSG (p,"msg_currenttime",getServerTime(plg.timezone));
		PrintEnDis (p, "msg_joinprotect", plg.joinprotect);
		PrintEnDis (p, "msg_playerwarn", plg.playerwarn);
		PrintMSG (p, "msg_playerwarntime", plg.playerwarntime);
		PrintEnDis (p, "msg_useplaytime", plg.useplaytime);
		PrintMSG (p, "msg_useplaytimevalue", plg.prtplay);
		PrintEnDis (p, "msg_userealtime", plg.userealtime);
		PrintMSG (p, "msg_userealtimevalue", plg.prttime);
		PrintMSG (p, "msg_pvponcooldowntime", plg.pvponcooldown);
		PrintMSG (p, "msg_pvpupdatetime", plg.pvpupdatetime);
		PrintMSG (p, "msg_language", plg.language);
		PrintEnDis (p, "msg_versioncheck", plg.version_check);

	}



	public void InitCmd(){
		cmds.clear();
		cmdlist = "";
		addCmd("help", "config",MSG("cmd_help","&3/noob help [command]",'b'),true);
		addCmd("protect", "protect",MSG("cmd_protect","&3/noob protect [player]",'b'));
		addCmd("unprotect", "unprotect",MSG("cmd_unprotect","&3/noob unprotect [player]",'b'));
		addCmd("list", "config",MSG("cmd_list","&3/noob list [page] [name mask]",'b'),true);
		addCmd("reload", "config",MSG("cmd_reload","&3/noob reload",'b'),true);
		addCmd("cfg", "config",MSG("cmd_cfg","&3/noob cfg",'b'),true);
	}

	public void FillMSG(){
		//NPPList
		addMSG ("msg_warnpvpon", "Warning! PVP-protection removed. You can now attack and be attacked by other players!"); 
		addMSG ("msg_plisttitle", "Noob-protected players");
		addMSG ("msg_plistfooter", "Page: [%1% / %2%]");
		
		
		addMSG ("msg_emptylist", "There's no Noob-protected players");

		//cmd 
		addMSG ("cmd_protect", "%1% - protect player (or protect yourself)");
		addMSG ("cmd_protect", "%1% - unprotect player (or unprotect yourself)");
		addMSG ("cmd_list", "%1% - list all protected players");
		addMSG ("cmd_reload", "%1% - reload configuration from file");
		addMSG ("cmd_cfg", "%1% - show plugin configuration");
		
		addMSG ("msg_reloaded", "configuration reloaded from file");
		
		addMSG ("msg_pvponcmd", "%1% - to disable your own protection");
		addMSG ("msg_youunprotected", "Your PVP-protection was removed");
		addMSG ("msg_unprtfail", "Failed to remove %1%'s protection");	
		
		addMSG ("msg_plrisprotected", "%1% is now protected");
		addMSG ("msg_plrisunprotected", "%1% is unprotected");
		
		addMSG ("msg_unknownplayer", "Player %1% is unknown. May be he is offline?");		
		addMSG ("msg_unprtfail", "Failed to remove protection from %1%");	

		addMSG ("msg_alreadyunprotected", "You are already unprotected!");
		addMSG ("msg_pvponcooldown", "Type %1% again during the %2% seconds to unprotect yourself");
		
		addMSG ("msg_currenttime", "Current (server) time is %1%");


		//Listener
		addMSG ("msg_warnpvpoff", "You are now protected from PVP-attacks. But you cannot attack other players too. PVP-protection will be removed at %1%");
		addMSG ("msg_youcantattack", "Hey! You cannot attack other players! Type %1% to remove protection.");
		addMSG ("msg_warndefender", "%1% is trying to attack you!");
		addMSG ("msg_warnatacker", "%1% is PVP-protected. Your attack failed.");		

		addMSG ("msg_protected", "You are protected.");
		addMSG ("msg_notprotected", "You are unprotected.");
		
		addMSG ("msg_playtime", "Play-time (online) limit: %1%");
		addMSG ("msg_realtime", "Unprotection time: %1%");
		addMSG ("msg_typepvpon", "Type %1% to remove protection.");
		
		
		addMSG ("cfg_configuration", "Configuration");
		
		addMSG ("msg_servertimezone", "Default (system) time zone: %1%");
		addMSG ("msg_cfgtimezone", "Time zone modifier (defined in config-file): %1%");
		addMSG ("msg_currenttime", "Time in defined time zone is %1%");
		
		addMSG ("msg_joinprotect", "Auto protect new players");
		addMSG ("msg_playerwarn", "Warn player about protection");
		addMSG ("msg_playerwarntime", "Warning message delay: %1% minutes");
		addMSG ("msg_useplaytime", "Use play-time (online) protection");
		addMSG ("msg_useplaytimevalue", "Play-time protection limit %1% minutes");
		addMSG ("msg_userealtime", "Use real-time protection");
		addMSG ("msg_userealtimevalue", "Real-time protection limit %1% minutes");
		addMSG ("msg_pvponcooldowntime", "/pvp-on command delay: %1% seconds");
		addMSG ("msg_pvpupdatetime", "Update protection status every %1% minutes");
		addMSG ("msg_language", "Language: %1%");
		addMSG ("msg_versioncheck", "Check plugin updates");
	}
	
	public String getServerTime (String tzone){
		Date d = new Date (System.currentTimeMillis());
		SimpleDateFormat f =  new SimpleDateFormat ("dd/MM/yyyy HH:mm");
		if (!tzone.isEmpty()) f.setTimeZone(TimeZone.getTimeZone(tzone));
		return f.format(d);
	}

}
