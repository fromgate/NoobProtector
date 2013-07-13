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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


/* v0.0.1
 * + Определение новый игрок или нет, запись в файл времени первого логина
 * + Отмена PVP в течении защищенного период
 * + Отключение режима защиты по истечении времени, по команде, 
 */

/* TODO
 * 1. Поддержка групп (перемещение игрока при установке/снятии защиты)
 * 2. Отключение режима защиты по началу PVP со стороны игрока.
 * 3. Поддержка модификаторов для разных типов повреждений
 */


public class NoobProtector extends JavaPlugin {
    // Конфигурация
    boolean joinprotect = true;
    boolean userealtime = true;
    boolean useplaytime = true;
    int prttime = 2880; // 2880 мин = 2 дня
    int prtplay = 300;  //  300 мин = 5 часов
    int pvponcooldown = 10; // в секундах
    int pvpupdatetime = 5;  // в секундах
    boolean playerwarn = true;  // рассказывать игроку, что он "защищен"
    int playerwarntime = 30;    // каждые хх минут
    String timezone = ""; // Europe/Moscow, GMT+4
    boolean version_check = false;
    boolean language_save = true;
    String language = "english";
    List<String> no_pvp_worlds = new ArrayList<String>();
    List<String> no_pvp_regions = new ArrayList<String>();

    // Сервисные объекты и переменные
    NPUtil u;
    NPCmd cmd;
    NPPList players;
    NPListener l;

    WorldGuardPlugin worldguard;
    boolean wg_active = false;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        loadCfg();
        saveCfg();
        wg_active = connectWorldGuard();

        u = new NPUtil (this, version_check, language_save, language, "noob-protector", "NoobProtector", "noob", "&3[NP]&f ");
        players = new NPPList (this);

        cmd = new NPCmd (this);
        getCommand("noob").setExecutor(cmd);
        getCommand("pvp-on").setExecutor(cmd);

        l = new NPListener (this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(l, this);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
        }

    }

    @Override
    public void onDisable() {
        players.savePlayerList();
    }


    public void saveCfg(){
        getConfig().set("general.version-check", version_check);
        getConfig().set("general.language", language);
        getConfig().set("general.language-save", language_save);
        getConfig().set("general.time-zone", timezone);
        getConfig().set("protect.after-join", joinprotect);
        getConfig().set("protect.realtime.enable", userealtime);
        getConfig().set("protect.realtime.time", prttime);
        getConfig().set("protect.playtime.enable", useplaytime);
        getConfig().set("protect.playtime.time", prtplay);
        getConfig().set("general.pvp-on-cool-down",pvponcooldown);
        getConfig().set("schedule.pvp-update-time",pvpupdatetime);
        getConfig().set("schedule.player-warn.enable",playerwarn);
        getConfig().set("schedule.player-warn.time",playerwarntime);
        getConfig().set("unprotected.worlds", no_pvp_worlds);
        getConfig().set("unprotected.regions", no_pvp_regions);
        saveConfig();
    }

    public void loadCfg(){
        joinprotect = getConfig().getBoolean("protect.after-join", true);
        userealtime = getConfig().getBoolean ("protect.realtime.enable", true);
        prttime = getConfig().getInt("protect.realtime.time", 2880);
        useplaytime=getConfig().getBoolean ("protect.playtime.enable", true);
        prtplay = getConfig().getInt("protect.playtime.time", 300);
        if ((!userealtime)&&(!useplaytime)) useplaytime = true;
        timezone = getConfig().getString("general.time-zone", "");
        version_check = getConfig().getBoolean("general.version-check", true);;
        language_save = getConfig().getBoolean("general.language-save", false);
        language = getConfig().getString("general.language", "english");
        pvponcooldown = getConfig().getInt("general.pvp-on-cool-down",pvponcooldown);
        pvpupdatetime=getConfig().getInt("schedule.pvp-update-time",pvpupdatetime);
        playerwarn=getConfig().getBoolean("schedule.player-warn.enable",playerwarn);
        playerwarntime=getConfig().getInt("schedule.player-warn.time",playerwarntime);
        no_pvp_worlds= getConfig().getStringList("unprotected.worlds");
        no_pvp_regions = getConfig().getStringList("unprotected.regions");
    }

    public boolean connectWorldGuard(){
        Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
        if ((worldGuard != null)&&(worldGuard instanceof WorldGuardPlugin)) {
            worldguard = (WorldGuardPlugin)worldGuard;
            return true;
        }
        return false;
    }


}
