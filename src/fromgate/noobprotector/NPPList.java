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


package fromgate.noobprotector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class NPPList {
    NoobProtector plg;
    Long prtreal= 3600000L;
    Long prtplay = 5L;
    NPUtil u;


    BukkitTask tid;
    BukkitTask tid2;

    boolean userealtime = true;
    boolean useplaytime = true;

    public NPPList(NoobProtector plg){
        this.plg = plg;
        this.u = plg.u;
        this.prtreal= plg.prttime*60000L;
        this.prtplay= plg.prtplay*60000L;
        this.useplaytime = plg.useplaytime;
        this.userealtime = plg.userealtime;

        this.loadPlayerList();









        tid = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plg, new Runnable(){
            public void run (){
                updateOnlinePlayersPVP();
            }
            // 1 sec = 20;
            // 1 min = 1200
            // 1 hour = 72000
            // 24 hour = 1728000
        }, 200L, plg.pvpupdatetime*20L);


        if (plg.playerwarn)
            tid2 = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plg, new Runnable(){
                public void run (){
                    warnPlayers();
                }
            }, plg.playerwarntime*1200L, plg.playerwarntime*1200L);
    }




    public class NPPlayer{
        Long rtimelimit;
        Long playtimeleft;

        public NPPlayer(){
            this.rtimelimit = System.currentTimeMillis()+prtreal;
            this.playtimeleft = prtplay;
        }		

        // конструктор для загрузки из файла
        public NPPlayer(Long rtimelimit, Long playtimeleft){
            this.rtimelimit = rtimelimit;
            this.playtimeleft = playtimeleft;
        }

        public boolean updatePlayTime (Long pt){
            this.playtimeleft = Math.max(this.playtimeleft - pt,0);
            return (this.playtimeleft>0);
        }

    }


    private HashMap<String, NPPlayer> players = new HashMap<String, NPPlayer>();


    public void warnPlayers(){
        for (Player p : Bukkit.getOnlinePlayers())
            printPlayerProtected(p, false, true);
    }


    /* Возвращает true - игрок защищен
     * 			  false - когда перелимит времени или когда игрок не учитывается
     */
    public boolean updatePlayTime(Player p){
        if (!players.containsKey(p.getName())) return false;
        Long ct = System.currentTimeMillis();
        Long pt = ct;
        if (p.hasMetadata("NP-checktime")) pt = p.getMetadata("NP-checktime").get(0).asLong();		
        p.setMetadata("NP-checktime", new FixedMetadataValue (plg, ct));
        Long playtime = ct-pt;
        return players.get(p.getName()).updatePlayTime(playtime);
    }


    public void addPlayer (Player p){
        if (!players.containsKey(p.getName())) players.put(p.getName(), new NPPlayer ());
        this.savePlayerList();
    }

    public void setPlayer (Player p){
        players.put(p.getName(), new NPPlayer ());
        this.savePlayerList();
    }

    public void setPlayer (String pname){
        players.put(pname, new NPPlayer ());
        this.savePlayerList();
    }

    public boolean unprotectPlayer (Player p){ //, boolean pvpoff){
        if (players.containsKey(p.getName())) {
            players.remove(p.getName());
            savePlayerList();
            return true;
        }
        return false;
    }

    public boolean unprotectPlayer (String pname){ //, boolean pvpoff){
        if (players.containsKey(pname)) {
            players.remove(pname);
            savePlayerList();
            return true;
        }
        return false;
    }


    public void updateOnlinePlayersPVP(){
        for (Player p : Bukkit.getOnlinePlayers()){
            if (updatePlayerPVP(p))
                u.printMSG(p, "msg_warnpvpon",'6');
        }
    }

    public boolean updatePlayerPVP(Player p){
        if (players.containsKey(p.getName())){
            if ((userealtime&&(players.get(p.getName()).rtimelimit<System.currentTimeMillis()))||
                    (useplaytime&&!updatePlayTime(p))){
                players.remove(p.getName());
                return true;
            }
        }
        return false;
    }


    public boolean getPvpOff(Player p){
        if (isPlayerInUnprotectedWorld(p)) return false;
        if (isPlayerInUnprotectedRegion(p)) return false;
        return (players.containsKey(p.getName())&&
                ((userealtime&&(players.get(p.getName()).rtimelimit>System.currentTimeMillis()))||
                        (useplaytime&&updatePlayTime(p))));
    }


    public boolean isPlayerInUnprotectedWorld(Player p){
        if (plg.no_pvp_worlds.isEmpty()) return false;
        return (plg.no_pvp_worlds.contains(p.getWorld().getName()));
    }

    public boolean isPlayerInUnprotectedRegion(Player p){
        return isLocationInUnprotectedRegion (p.getLocation());
    }

    public boolean isLocationInUnprotectedRegion(Location loc){
        if (!plg.wg_active) return false;
        if (plg.no_pvp_regions.isEmpty()) return false;
        ApplicableRegionSet regions = plg.worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc); 
        if (regions.size()>0){ 
            for (ProtectedRegion region : regions)
                if (plg.no_pvp_regions.contains(region.getId())) return true;
        }
        return false;
    }


    public void savePlayerList(){
        try {
            File f = new File (plg.getDataFolder()+File.separator+"players.yml");
            if (f.exists()) f.delete();
            if (players.size()>0){
                f.createNewFile();
                YamlConfiguration cfg = new YamlConfiguration();
                for (String name : players.keySet()){
                    if (this.useplaytime) cfg.set(name+".playtime", players.get(name).playtimeleft);
                    if (this.userealtime) cfg.set(name+".realtime", players.get(name).rtimelimit);
                }

                cfg.save(f);	
            }

        } catch (Exception e){

        }

    }

    public void loadPlayerList(){
        try {
            File f = new File (plg.getDataFolder()+File.separator+"players.yml");
            if (f.exists()){
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.load(f);
                for (String name : cfg.getKeys(false))
                    players.put(name, new NPPlayer (cfg.getLong(name+".realtime",0),cfg.getLong(name+".playtime",0)));
            }
        } catch (Exception e){
        }

    }

    public String getProtectTime (Player p){
        return getProtectTime (p.getName());
    }

    public String getProtectTime (String pname){
        if (players.containsKey(pname)){
            Date d = new Date (players.get(pname).rtimelimit);
            SimpleDateFormat f =  new SimpleDateFormat ("dd/MM/yyyy HH:mm");

            if (!plg.timezone.isEmpty()) f.setTimeZone(TimeZone.getTimeZone(plg.timezone));
            return f.format(d);
        }
        return "";
    }

    public String getPlayTimeLeft (Player p){
        return getPlayTimeLeft(p.getName());
    }

    public String getPlayTimeLeft (String pname){
        if (players.containsKey(pname)){
            Long time = players.get(pname).playtimeleft/1000;
            int seconds = (int)(time % 60);
            int minutes = (int)((time % 3600) / 60);
            int hours = (int)(time / 3600);
            return String.format("%02d:%02d:%02d", hours, minutes,seconds);
        }
        return "";
    }

    public void printPlayerProtected (Player p, boolean prtempty, boolean pvpon){
        if (players.containsKey(p.getName())){
            String msg = u.getMSG("msg_protected",'b');
            if (useplaytime) msg = msg+" "+u.getMSG("msg_playtime",'b','e', getPlayTimeLeft (p));
            if (userealtime) msg = msg+" "+u.getMSG("msg_realtime",'b','e', getProtectTime (p));
            u.printMsg(p,msg);
            if (pvpon) u.printMSG(p, "msg_typepvpon",'e','6',"/pvp-on");
        } else if (prtempty) u.printMSG(p, "msg_notprotected");
    }

    public void printTargetPlayerProtected (CommandSender p, Player tp){
        if (players.containsKey(tp.getName())){
            String msg = u.getMSG("msg_plrisunprotected",'b','e',tp.getName());
            if (useplaytime) msg = msg+" "+u.getMSG("msg_playtime",'b','e', getPlayTimeLeft (tp));
            if (userealtime) msg = msg+" "+u.getMSG("msg_realtime",'b','e', getProtectTime (tp));
            u.printMsg(p,msg);
        } else u.printMSG(p, "msg_plrisunprotected",tp.getName());
    }



    public void printList(CommandSender p, int pnum, String mask){
        if (players.size()>0){
            List<String> ln = new ArrayList<String>();
            for (String name : players.keySet()){
                String pt = "";
                if (this.userealtime) pt = pt+" : "+getProtectTime (name);
                if (this.useplaytime) pt = pt+" : "+getPlayTimeLeft (name);
                if (mask.isEmpty()||(name.contains(mask)))	ln.add("&2"+name+" &a"+pt);
            }
            if (ln.size()>0) u.printPage(p, ln, pnum, "msg_plisttitle", "msg_plistfooter", true);
            else u.printMSG (p, "msg_emptylist",'6');
        } else u.printMSG (p, "msg_emptylist",'6');
    }


}
