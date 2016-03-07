/*  
 *  NoobProtector, Minecraft bukkit plugin
 *  (c)2012-2013, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/noob-protector/
 *    
 *  This file is part of NoobProtector.
 *  
 *  NoobProtector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoobProtector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoobProtector.  If not, see <http://www.gnorg/licenses/>.
 * 
 */

package me.fromgate.noobprotector;

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
    public boolean onCommand(CommandSender p, Command cmd, String cmdLabel, String[] args) {
        if (cmdLabel.equalsIgnoreCase("noob")){
            if ((args.length>0)&&u.checkCmdPerm(p, args[0])){
                if (args.length==1) return ExecuteCmd (p, args[0]);
                else if (args.length==2) return ExecuteCmd (p, args[0],args[1]);
                else if (args.length==3) return ExecuteCmd (p, args[0],args[1],args[2]);
            } else u.printMSG(p, "cmd_cmdpermerr",'c');
            return true;
        } else if (cmdLabel.equalsIgnoreCase("pvp-on")){
            if (!(p instanceof Player)) return false;
            Player player = (Player) p;
            if (!player.hasPermission("noob-protector.pvp-on")) return true;
            if (plg.players.getPvpOff(player))	{
                if (checkPvpOnCooldown(player)){
                    plg.players.unprotectPlayer(player);
                    u.printMSG(p, "msg_warnpvpon",'6');						
                } else {
                    u.printMSG (p,"msg_currenttime",'3','9',u.getServerTime(plg.timezone));
                    plg.players.printPlayerProtected(player, true,false);
                    u.printMSG(p, "msg_pvponcooldown","/pvp-on;"+plg.pvponcooldown);
                }
            } else u.printMSG(p, "msg_alreadyunprotected",'c');

            return true;
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


    private boolean ExecuteCmd(CommandSender p, String cmd) {
        if (cmd.equalsIgnoreCase("help")){
            u.PrintHlpList(p, 1, 15);
            u.printMSG(p, "msg_pvponcmd","/pvp-on");
        } else if (cmd.equalsIgnoreCase("protect")){
            if (!(p instanceof Player)) return false;
            Player player = (Player) p;
            plg.players.setPlayer(player);
            plg.players.printPlayerProtected(player, false,true);
        } else if (cmd.equalsIgnoreCase("unprotect")){
            if (!(p instanceof Player)) return false;
            Player player = (Player) p;
            if (plg.players.unprotectPlayer(player)) u.printMSG(p, "msg_youunprotected");
            else u.printMSG(p, "msg_unprtfail",p.getName());
        } else if (cmd.equalsIgnoreCase("list")){
            plg.players.printList(p,1,"");
        } else if (cmd.equalsIgnoreCase("reload")){
            plg.reloadConfig();
            plg.loadCfg();
            u.printMSG(p, "msg_reloaded");
        } else if (cmd.equalsIgnoreCase("cfg")){
            u.PrintCfg(p);
        } else return false;
        return true;
    }

    private boolean ExecuteCmd(CommandSender p, String cmd, String arg) {
        if (cmd.equalsIgnoreCase("help")){
            int page = 1;
            if (u.isInteger(arg)) page = Integer.parseInt(arg);
            u.PrintHlpList(p, page, 15);
        } else if (cmd.equalsIgnoreCase("protect")){
            Player prp = u.getPlayerByName(arg);
            if ((prp!=null)&&(prp.isOnline())){
                plg.players.setPlayer(prp);
                plg.players.printPlayerProtected(prp, false,true);
                plg.players.printTargetPlayerProtected(p, prp);
            } else u.printMSG (p, "msg_unknownplayer",arg);
        } else if (cmd.equalsIgnoreCase("unprotect")){
            Player prp = u.getPlayerByName(arg);
            if (plg.players.unprotectPlayer(arg)){
                if ((prp!=null)&&(prp.isOnline())) u.printMSG(prp, "msg_youunprotected");
                u.printMSG(p, "msg_plrisunprotected",arg);	
            } else u.printMSG(p, "msg_unprtfail",arg);
        } else if (cmd.equalsIgnoreCase("list")){
            int pnum = 1;
            String mask = "";
            if (arg.matches("[1-9]+[0-9]*")) pnum = Integer.parseInt(arg);
            else mask = arg;
            plg.players.printList(p,pnum,mask);
        } else return false;		 
        return true;
    }

    private boolean ExecuteCmd(CommandSender p, String cmd, String arg1,String arg2) {
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
