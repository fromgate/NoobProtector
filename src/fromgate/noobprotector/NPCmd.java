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

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class NPCmd implements CommandExecutor{
	NoobProtector plg;
	NPUtil u;
	

	public NPCmd (NoobProtector plg){
		this.plg = plg;
		this.u = plg.u;

	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (sender instanceof Player){
			Player p = (Player) sender;

			if (cmdLabel.equalsIgnoreCase("noob")){
				if ((args.length>0)&&u.CheckCmdPerm(p, args[0])){
					if (args.length==1) return ExecuteCmd (p, args[0]);
					else if (args.length==2) return ExecuteCmd (p, args[0],args[1]);
					else if (args.length==3) return ExecuteCmd (p, args[0],args[1],args[2]);
					//else if (args.length==4) return ExecuteCmd (p, args[0],args[1],args[2],args[3]);
					//else if (args.length==5) return ExecuteCmd (p, args[0],args[1],args[2],args[3],args[4]);
					/*else if (args.length>=5){
						String arg4 = "";
						for (int i = 4; i<args.length;i++) 
							arg4 = arg4+" "+args[i];
						arg4 = arg4.trim();
						return ExecuteCmd (p, args[0],args[1],args[2],args[3],arg4);
					} */
				} else u.PrintPxMSG(p, "cmd_cmdpermerr",'c');
				return true;



			} else if (cmdLabel.equalsIgnoreCase("pvp-on")){
				if (!p.hasPermission("noob-protector.pvp-on")) return true;
				if (plg.players.getPvpOff(p))	{
					if (checkPvpOnCooldown(p)){
						plg.players.unprotectPlayer(p);
						u.PrintMSG(p, "msg_warnpvpon",'6');						
					} else {
						u.PrintMSG (p,"msg_currenttime",u.getServerTime(plg.timezone),'3','9');
						plg.players.printPlayerProtected(p, true,false);
						u.PrintMSG(p, "msg_pvponcooldown","/pvp-on;"+plg.pvponcooldown);
					}
				} else u.PrintMSG(p, "msg_alreadyunprotected",'c');
				
				return true;
			}
		} 
		return false;
	}
	
	private boolean checkPvpOnCooldown(Player p){
		Long ct = System.currentTimeMillis();
		
		if ((!p.hasMetadata("NP-pvp-on-cooldown"))||
				((ct-p.getMetadata("NP-pvp-on-cooldown").get(0).asLong())>(plg.pvponcooldown*1000))){
			p.setMetadata("NP-pvp-on-cooldown", new FixedMetadataValue (plg, ct));
			return false;
		}
		return true;
	}


	private boolean ExecuteCmd(Player p, String cmd) {
		if (cmd.equalsIgnoreCase("help")){
			u.PrintHLP(p);
			u.PrintMSG(p, "msg_pvponcmd","/pvp-on");
		} else if (cmd.equalsIgnoreCase("protect")){
			plg.players.setPlayer(p);
			plg.players.printPlayerProtected(p, false,true);
		} else if (cmd.equalsIgnoreCase("unprotect")){
			if (plg.players.unprotectPlayer(p)) u.PrintMSG(p, "msg_youunprotected");
			else u.PrintMSG(p, "msg_unprtfail",p.getName());
		} else if (cmd.equalsIgnoreCase("list")){
			plg.players.printList(p,1,"");
		} else if (cmd.equalsIgnoreCase("reload")){
			plg.reloadConfig();
			plg.loadCfg();
			u.PrintMSG(p, "msg_reloaded");
		} else if (cmd.equalsIgnoreCase("cfg")){
			u.PrintCfg(p);
			
		} else return false;
		return true;
	}
	
	private boolean ExecuteCmd(Player p, String cmd, String arg) {
		if (cmd.equalsIgnoreCase("help")){
			u.PrintHLP(p, arg);
		} else if (cmd.equalsIgnoreCase("protect")){
			Player prp = Bukkit.getPlayerExact(arg);
			if ((prp!=null)&&(prp.isOnline())){
				plg.players.setPlayer(prp);
				plg.players.printPlayerProtected(prp, false,true);
				plg.players.printTargetPlayerProtected(p, prp);
			} else u.PrintMSG (p, "msg_unknownplayer",arg);
		} else if (cmd.equalsIgnoreCase("unprotect")){
			Player prp = Bukkit.getPlayerExact(arg);
			if (plg.players.unprotectPlayer(arg)){
				if ((prp!=null)&&(prp.isOnline())) u.PrintMSG(prp, "msg_youunprotected");
				u.PrintMSG(p, "msg_plrisunprotected",arg);	
			} else u.PrintMSG(p, "msg_unprtfail",arg);
		} else if (cmd.equalsIgnoreCase("list")){
			int pnum = 1;
			String mask = "";
			if (arg.matches("[1-9]+[0-9]*")) pnum = Integer.parseInt(arg);
			else mask = arg;
			plg.players.printList(p,pnum,mask);
		} else return false;		 
		return true;
	}
	
	private boolean ExecuteCmd(Player p, String cmd, String arg1,String arg2) {
		if (cmd.equalsIgnoreCase("list")){
			int pnum = 1;
			String mask = "";
			if (arg1.matches("[1-9]+[0-9]*")) {
				pnum = Integer.parseInt(arg1);
				mask = arg2;
			} else if (arg2.matches("[1-9]+[0-9]*")) {
				pnum = Integer.parseInt(arg2);
				mask = arg1;				
			}
			plg.players.printList(p,pnum,mask);
		} else return false;
		return true;
	}

}
