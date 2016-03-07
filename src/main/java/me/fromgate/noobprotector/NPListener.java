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

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NPListener implements Listener{
    NoobProtector plg;
    NPUtil u;

    public NPListener (NoobProtector plg){
        this.plg = plg;
        this.u = plg.u;
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin (PlayerJoinEvent event){
        Player p = event.getPlayer();
        if (p.hasMetadata("NP-checktime"))	p.removeMetadata("NP-checktime", plg); 
        if ((!p.hasPlayedBefore())&&plg.joinprotect) plg.players.addPlayer(p);
        plg.players.updatePlayerPVP(p); 
        plg.players.printPlayerProtected(p, false,true);
    }


    @EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage (EntityDamageEvent event){
        if ((event.getEntityType() != EntityType.PLAYER)||(!(event instanceof EntityDamageByEntityEvent))) return;
        EntityDamageByEntityEvent dmgev = (EntityDamageByEntityEvent) event;
        if (dmgev.getDamager().getType() != EntityType.PLAYER) return;
        Player p1 = (Player) event.getEntity();
        Player p2 = (Player) dmgev.getDamager();
        //if (p1.equals(p2)) return; //для Dogtags
        if (plg.players.getPvpOff(p1)||plg.players.getPvpOff(p2)){
            event.setCancelled(true);	
            informFailedAttack (p2,p1);
        }
    }

    @EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
    public void onProjectileDamage (EntityDamageEvent event){
        if (event.getEntityType() != EntityType.PLAYER) return;
        if (!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent evdm = (EntityDamageByEntityEvent) event;
        if (!(evdm.getDamager() instanceof Projectile)) return;
        Projectile prj = (Projectile) evdm.getDamager();
        if ((prj.getShooter()==null)||(!(prj.getShooter() instanceof Player))) return;
        Player p1 = (Player) event.getEntity();
        Player p2 = (Player) prj.getShooter();
        if (plg.players.getPvpOff(p1)||plg.players.getPvpOff(p2)){
            event.setCancelled(true);	
            informFailedAttack (p2,p1);
        }
    }


    /*
     * 		EntityDamageByEntityEvent evdm = (EntityDamageByEntityEvent) event;
		if (evdm.getDamager() instanceof Projectile) {
			Projectile prj = (Projectile) evdm.getDamager();

     */


    public void informFailedAttack (Player atacker, Player defender){
        if (plg.players.getPvpOff(atacker))	u.printMSG(atacker, "msg_youcantattack", 'c','6',"/pvp-on");
        else {
            u.printMSG(defender, "msg_warndefender",atacker.getName());
            u.printMSG(atacker, "msg_warnatacker",defender.getName());
        }
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)	
    public void onPlayerQuit (PlayerQuitEvent event){
        Player p = event.getPlayer();
        if (p.hasMetadata("NP-checktime")) p.removeMetadata("NP-checktime", plg);
        plg.players.updatePlayTime(p);
        plg.players.savePlayerList();

    }


}
