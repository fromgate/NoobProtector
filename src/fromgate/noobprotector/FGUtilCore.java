package fromgate.noobprotector;
/*  
 *  FGUtilCore, Utilities class for Minecraft bukkit plugins
 *  
 *    (c)2012, fromgate, fromgate@gmail.com
 *  
 *      
 *  FGUtilCore is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FGUtilCore is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with WeatherMan.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */


import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* +    1. Проверка версий
 * +	2. Процедуры для обработчика комманда (перечень, печать хелпа -)
 */


public abstract class FGUtilCore {
	JavaPlugin plg;


	//конфигурация утилит
	public String px = "";


	private String permprefix="fgutilcore.";
	private boolean version_check = false; // включить после заливки на девбукит
	private String version_check_url = "";//"http://dev.bukkit.org/server-mods/skyfall/files.rss";
	private String version_name = ""; // идентификатор на девбукките (всегда должен быть такой!!!)
	private String version_info_perm = permprefix+"config"; // кого оповещать об обнволениях
	private String language="english";
	private String plgcmd = "<command>";

	// Сообщения+перевод
	YamlConfiguration lng;
	//String lngfile = this.language+".lng";

	protected HashMap<String,String> msg = new HashMap<String,String>(); //массив сообщений
	private char c1 = 'a'; //цвет 1 (по умолчанию для текста)
	private char c2 = '2'; //цвет 2 (по умолчанию для значений)
	protected String msglist ="";

	protected HashMap<String,Cmd> cmds = new HashMap<String,Cmd>();
	protected String cmdlist ="";

	PluginDescriptionFile des;
	private double version_current=0;
	private double version_new=0;
	private String version_new_str="unknown";
	private Logger log = Logger.getLogger("Minecraft");
	Random random = new Random ();
	int chId = 0;



	public FGUtilCore(JavaPlugin plg, boolean vcheck, boolean savelng, String lng, String devbukkitname, String version_name, String plgcmd, String px){
		this.plg = plg;
		this.des = plg.getDescription();
		this.version_current = Double.parseDouble(des.getVersion().replaceFirst("\\.", "").replace("/", ""));
		this.version_name = version_name;
		this.version_check=vcheck;
		this.language = lng;

		this.InitMsgFile();
		this.initStdMsg();


		// if (savelng) this.SaveMSG(); /// ммм... как бы это синхронизировать.... 

		if (devbukkitname.isEmpty()) this.version_check=false;
		else {
			this.version_check_url = "http://dev.bukkit.org/server-mods/"+devbukkitname+"/files.rss";
			this.permprefix = devbukkitname+".";
			this.version_new = this.getNewVersion(this.version_current);
			startUpdateTick();
			UpdateMsg();
		}

		if (version_name.isEmpty()) this.version_name = des.getName();
		else this.version_name = version_name;

		this.px = px;
		this.plgcmd = plgcmd;


	}

	/* Инициализация стандартных сообщений
	 * 
	 */
	private void initStdMsg(){
		addMSG ("msg_outdated", "%1% is outdated!");
		addMSG ("msg_pleasedownload", "Please download new version (%1%) from ");
		addMSG ("hlp_help", "Help");
		addMSG ("hlp_thishelp", "%1% - this help");
		addMSG ("hlp_execcmd", "%1% - execute command");
		addMSG ("hlp_typecmd", "Type %1% - to get additional help");
		addMSG ("hlp_commands", "Command list:");
		addMSG ("hlp_cmdparam_command", "command");
		addMSG ("hlp_cmdparam_parameter", "parameter");
		addMSG ("cmd_unknown", "Unknown command: %1%");
		addMSG ("cmd_cmdpermerr", "Something wrong (check command, permissions)");
		addMSG ("enabled", "enabled");
		msg.put("enabled", ChatColor.DARK_GREEN+msg.get("enabled"));
		addMSG ("disabled", "disabled");
		msg.put("disabled", ChatColor.RED+msg.get("disabled"));
	}


	/* Включение/выключение проверки версий. По идее не нужно ;)
	 *  
	 */
	public void SetVersionCheck (boolean vc){
		this.version_check = vc;
	}


	/* Вывод сообщения о выходе новой версии, вызывать из
	 * обработчика события PlayerJoinEvent
	 */
	public void UpdateMsg (Player p){
		if ((version_check)&&(p.hasPermission(this.version_info_perm))&&(version_new>version_current)){
			PrintMSG(p, "msg_outdated","&6"+des.getName()+" v"+des.getVersion(),'e','6');
			PrintMSG(p,"msg_pleasedownload",version_new_str,'e','6');
			PrintMsg(p, "&3"+version_check_url.replace("files.rss", ""));
		}
	}



	/* Вызывается автоматом при старте плагина,
	 * пишет сообщение о выходе новой версии в лог-файл
	 */
	public void UpdateMsg (){
		if (version_new>version_current){
			log.info(px+des.getName()+" v"+des.getVersion()+" is outdated! Recommended version is v"+version_new_str);
			log.info(px+version_check_url.replace("files.rss", ""));
		}			
	}

	/* Проверяет вышла ли новая версия
	 * не рекомендуется вызывать из стандартных обработчиков событий (например, PlayerJoinEvent)
	 */
	private double getNewVersion(double currentVersion){
		if (version_check){
			try {
				URL url = new URL(version_check_url);
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
				doc.getDocumentElement().normalize();
				NodeList nodes = doc.getElementsByTagName("item");
				Node firstNode = nodes.item(0);
				if (firstNode.getNodeType() == 1) {
					Element firstElement = (Element)firstNode;
					NodeList firstElementTagName = firstElement.getElementsByTagName("title");
					Element firstNameElement = (Element)firstElementTagName.item(0);
					NodeList firstNodes = firstNameElement.getChildNodes();
					version_new_str = firstNodes.item(0).getNodeValue().replace(version_name+" v", "").trim();
					return Double.parseDouble(version_new_str.replaceFirst("\\.", "").replace("/", ""));
				}
			}
			catch (Exception e) {
			}
		}
		return currentVersion;
	}


	/* Процесс проверяющий выход обновления каждые полчаса
	 * 
	 */
	private void startUpdateTick(){

		chId = plg.getServer().getScheduler().scheduleAsyncRepeatingTask(plg, new Runnable() {
			public void run() {
				version_new = getNewVersion (version_current);
			}
		}, 30 * 1200, 30 * 1200);
	}


	/*
	 * Процедуры для обработчика комманд
	 * 
	 */

	/* Добавляет новую команду в список
	 * 
	 */
	/* TODO надо сделать, чтобы описание команды
	 * автоматически добавлялось в перечень сообщений
	 */
	
	public void addCmd (String cmd, String perm, String desc){
		addCmd (cmd, perm,desc,false);
	}
	public void addCmd (String cmd, String perm, String desc, boolean console){
		cmds.put(cmd, new Cmd(this.permprefix+perm,desc,console));
		if (cmdlist.isEmpty()) cmdlist = cmd;
		else cmdlist = cmdlist+", "+cmd;
	}



	/* Проверка пермишенов и наличия команды
	 * 
	 */
	
	public boolean CheckCmdPerm (Player p, String cmd){
		return ((cmds.containsKey(cmd.toLowerCase()))&&
		(cmds.get(cmd.toLowerCase()).perm.isEmpty()||((!cmds.get(cmd.toLowerCase()).perm.isEmpty())&&
				p.hasPermission(cmds.get(cmd.toLowerCase()).perm))));
	}
	
	public boolean checkCmdPerm (CommandSender p, String cmd){
		if (!cmds.containsKey(cmd.toLowerCase())) return false;
		Cmd cm = cmds.get(cmd.toLowerCase());
		if (p instanceof Player){
			if ((!cm.perm.isEmpty())&&(!p.hasPermission(cm.perm))) return false;
		} else if (!cm.console) return false;
		return true;
/*		return ((cmds.containsKey(cmd.toLowerCase()))&&
				(cmds.get(cmd.toLowerCase()).perm.isEmpty()||((!cmds.get(cmd.toLowerCase()).perm.isEmpty())&&
						p.hasPermission(cmds.get(cmd.toLowerCase()).perm))));*/
	}


	/* Класс, описывающий команду:
	 * perm - постфикс пермишена
	 * desc - описание команды
	 */
	public class Cmd {
		String perm;
		String desc;
		boolean console;
		public Cmd (String perm, String desc){
			this.perm = perm;
			this.desc = desc;
			this.console = false;
		}
		public Cmd (String perm, String desc, boolean console){
			this.perm = perm;
			this.desc = desc;
			this.console = console;
		}
	}

	
	
	public class PageList {
		private List<String> ln;
		private int lpp = 15;
		private String title_msgid="msg_footer";
		private String footer_msgid="msg_footer";
		private boolean shownum=false;
		
		public PageList(List<String> ln, String title_msgid,String footer_msgid, boolean shownum){
			this.ln = ln;
			this.title_msgid =title_msgid; 
			this.footer_msgid = footer_msgid;
			this.shownum = shownum;
		}
		
		
		public void addLine (String str){
			ln.add(str);
		}
		
		public boolean isEmpty(){
			return ln.isEmpty();
		}
		
		public void setTitle(String title_msgid){
			this.title_msgid = title_msgid;
		}
		
		public void setShowNum(boolean shownum){
		}
		
		public void setFooter(String footer_msgid){
			this.footer_msgid = footer_msgid;
		}
		
		
		public void printPage(Player p, int pnum){
			int maxp = ln.size()/lpp;
			if ((ln.size()%lpp)>0) maxp++;
			if (pnum>maxp) pnum = maxp;
			int maxl = lpp;
			if (pnum == maxp) {
				maxl = ln.size()%maxp;
				if (maxp==1) maxl = ln.size();
			}
			if (maxl == 0) maxl = lpp;
			PrintMsg(p, "&6&l"+MSGnc(title_msgid));
			String numpx="";
			for (int i = 0; i<maxl; i++){
				if (shownum) numpx ="&3"+ Integer.toString(1+i+(pnum-1)*lpp)+". ";
				PrintMsg(p, numpx+"&a"+ln.get(i+(pnum-1)*lpp));
			}
			PrintMSG(p, this.footer_msgid, pnum+";"+maxp,'e','6');	
		}

	}

	public void printPage (Player p, List<String> ln, int pnum, String title, String footer, boolean shownum){
		PageList pl = new PageList (ln, title, footer, shownum);
		pl.printPage(p, pnum);
	}


	/*
	 * Разные полезные процедурки 
	 * 
	 */

	/* Функция проверяет входит ли число (int)
	 * в список чисел представленных в виде строки вида n1,n2,n3,...nN
	 */
	public boolean isIdInList (int id, String str){
		if (!str.isEmpty()){
			String [] ln = str.split(",");
			if (ln.length>0) 
				for (int i = 0; i<ln.length; i++)
					if ((!ln[i].isEmpty())&&ln[i].matches("[0-9]*")&&(Integer.parseInt(ln[i])==id)) return true;
		}
		return false;
	}

	/* Функция проверяет входит ли слово (String) в список слов
	 * представленных в виде строки вида n1,n2,n3,...nN
	 */
	public boolean isWordInList (String word, String str){
		String [] ln = str.split(",");
		if (ln.length>0) 
			for (int i = 0; i<ln.length; i++)
				if (ln[i].equalsIgnoreCase(word)) return true;
		return false;
	}

	/* Функция проверяет входит есть ли item (блок) с заданным id и data в списке,
	 * представленным в виде строки вида id1:data1,id2:data2,MATERIAL_NAME:data
	 * При этом если data может быть опущена
	 */
	public boolean isItemInList (int id, int data, String str){
		String [] ln = str.split(",");
		if (ln.length>0) 
			for (int i = 0; i<ln.length; i++)
				if (compareItemStr (id, data,ln[i])) return true;
		return false;
	}


	public boolean compareItemStr (ItemStack item, String itemstr){
		return compareItemStr (item.getTypeId(), item.getData().getData(), item.getAmount(), itemstr);
	}

	public boolean compareItemStr (int item_id, int item_data, String itemstr){
		return compareItemStr (item_id,item_data,1,itemstr);
	}





	// Надо использовать маску: id:data*amount, id:data, id*amount
	public boolean compareItemStr (int item_id, int item_data, int item_amount, String itemstr){
		if (!itemstr.isEmpty()){
			int id = -1;
			int amount =1;
			int data =-1;
			String [] si = itemstr.split("\\*");
			if (si.length>0){
				if ((si.length==2)&&si[1].matches("[1-9]+[0-9]*")) amount = Integer.parseInt(si[1]);
				String ti[] = si[0].split(":");
				if (ti.length>0){
					if (ti[0].matches("[0-9]*")) id=Integer.parseInt(ti[0]);
					else id=Material.getMaterial(ti[0]).getId();						
					if ((ti.length==2)&&(ti[1]).matches("[0-9]*")) data = Integer.parseInt(ti[1]);
					return ((item_id==id)&&((data<0)||(item_data==data))&&(item_amount>=amount));
				}
			}
		}									
		return false;
	}

	public boolean removeItemInHand(Player p, String itemstr){
		if (!itemstr.isEmpty()){
			int id = -1;
			int amount =1;
			int data =-1;
			String [] si = itemstr.split("\\*");
			if (si.length>0){
				if ((si.length==2)&&si[1].matches("[1-9]+[0-9]*")) amount = Integer.parseInt(si[1]);
				String ti[] = si[0].split(":");
				if (ti.length>0){
					if (ti[0].matches("[0-9]*")) id=Integer.parseInt(ti[0]);
					else id=Material.getMaterial(ti[0]).getId();						
					if ((ti.length==2)&&(ti[1]).matches("[0-9]*")) data = Integer.parseInt(ti[1]);
					return removeItemInHand (p, id,data,amount);
				}
			}
		}
		return false;
	}

	public boolean removeItemInHand(Player p, int item_id, int item_data, int item_amount){
		if ((p.getItemInHand() != null)&&
				(p.getItemInHand().getTypeId()==item_id)&&
				(p.getItemInHand().getAmount()>=item_amount)&&
				((item_data<0)||(item_data==p.getItemInHand().getData().getData()))){

			if (p.getItemInHand().getAmount()>item_amount) p.getItemInHand().setAmount(p.getItemInHand().getAmount()-item_amount);
			else p.setItemInHand(new ItemStack (Material.AIR));

			return true;
		}
		return false;
	}


	/*
	 * Вывод сообщения пользователю 
	 */
	public void PrintMsg(Player p, String msg){
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	/*
	 *  Вывод сообещения пользователю (с префиксом)
	 */
	public void PrintPxMsg(Player p, String msg){
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}


	/*
	 * Бродкаст сообщения, использую при отладке 
	 */
	public void BC (String msg){
		plg.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}

	/*
	 * Отправка цветного сообщения в консоль 
	 */
	public void SC (String msg){
		plg.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}



	/*
	 * Перевод
	 * 
	 */

	/*
	 *  Инициализация файла с сообщениями
	 */
	public void InitMsgFile(){
		try {
			lng = new YamlConfiguration();
			File f = new File (plg.getDataFolder()+File.separator+this.language+".lng");
			if (f.exists()) lng.load(f);
		} catch (Exception e){
			e.printStackTrace();
		}
	}


	/*
	 * Добавлене сообщения в список
	 * Убираются цвета.
	 * Параметры:
	 * key - ключ сообщения
	 * txt - текст сообщения
	 */
	public void addMSG(String key, String txt){
		msg.put(key, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lng.getString(key,txt))));
		if (msglist.isEmpty()) msglist=key;
		else msglist=msglist+","+key;
	}


	/*
	 * Сохранение сообщений в файл 
	 */
	public void SaveMSG(){
		String [] keys = this.msglist.split(",");
		try {
			File f = new File (plg.getDataFolder()+File.separator+this.language+".lng");
			if (!f.exists()) f.createNewFile();
			YamlConfiguration cfg = new YamlConfiguration();
			for (int i = 0; i<keys.length;i++)
				cfg.set(keys[i], msg.get(keys[i]));
			cfg.save(f);
		} catch (Exception e){
			e.printStackTrace();
		}
	} 

	/* 
	 * Получение сообщения по ключу 
	 */
	public String MSG(String id){
		return MSG (id,"",this.c1, this.c2);
	}

	/*
	 *  Получение сообщения по ключу (с - цвет, одним символом)
	 */
	public String MSG(String id, char c){
		return MSG (id,"",c, this.c2);
	}

	/*  Получаем сообщение, при этом keys будет использовано
	 *  для подмены %1%, %2% и т.д.
	 */
	public String MSG(String id, String keys){
		return MSG (id,keys,this.c1, this.c2);
	}

	/*  Получаем сообщение (цвет с), при этом keys будет использовано
	 *  для подмены %1%, %2% и т.д.
	 */
	public String MSG(String id, String keys, char c){
		return MSG (id,keys,this.c1, c);
	}


	/*  Получаем сообщение (с1 - цвет текст, c2 - цвет значения), при этом keys будет использовано
	 *  для подмены %1%, %2% и т.д.
	 */
	public String MSG(String id, String keys, char c1, char c2){
		String str = "&4Unknown message ("+id+")";
		if (msg.containsKey(id)){
			str = "&"+c1+msg.get(id);
			String ln[] = keys.split(";");
			if (ln.length>0)
				for (int i = 0; i<ln.length;i++)
					str = str.replace("%"+Integer.toString(i+1)+"%", "&"+c2+ln[i]+"&"+c1);
		} 
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	public String MSG(String id, char c1, char c2, Object... keys){
		String str = "&4Unknown message ("+id+")";
		if (msg.containsKey(id)){
			String clr1 = "";
			String clr2 = "";
			if (c1 != 'z') clr1 = "&"+c1;
			if (c2 != 'z') clr2 = "&"+c2;
			str = clr1 + msg.get(id);
			if (keys.length>0)
				for (int i =0; i<keys.length;i++)
					str.replace("%"+Integer.toString(i+1)+"%", clr2+keys[i].toString()+clr1);
		}
		return ChatColor.translateAlternateColorCodes('&', str);
	}


	/* 
	 * Печать сообщения
	 */
	public void PrintMSG (Player p, String msg_key, int key){
		p.sendMessage(MSG (msg_key, Integer.toString(key), this.c1, this.c2));
	}

	/* 
	 * Печать сообщения
	 */
	public void PrintMSG (Player p, String msg_key, String keys){
		p.sendMessage(MSG (msg_key, keys, this.c1, this.c2));
	}

	public void PrintPxMSG (Player p, String msg_key, String keys){
		PrintPxMsg(p,MSG (msg_key, keys, this.c1, this.c2));
	}



	/* 
	 * Печать сообщения
	 */
	public void PrintMSG (Player p, String msg_key, String keys, char c1, char c2){
		p.sendMessage(MSG (msg_key, keys, c1, c2));
	}

	/* 
	 * Печать сообщения
	 */
	public void PrintMSG (Player p, String msg_key, char c1){
		p.sendMessage(MSG (msg_key, c1));
	}

	public void PrintPxMSG (Player p, String msg_key, char c1){
		PrintPxMsg(p,MSG (msg_key, c1));
	}

	/* 
	 * Печать сообщения
	 */
	public void PrintMSG (Player p, String msg_key){
		p.sendMessage(MSG (msg_key));
	}

	public void PrintPxMSG (Player p, String msg_key){
		PrintPxMsg(p,MSG (msg_key));
	}


	/* 
	 * Печать справки
	 */
	public void PrintHLP (Player p){
		PrintMsg(p, "&6&l"+version_name+" v"+des.getVersion()+" &r&6| "+MSG("hlp_help",'6'));
		PrintMSG(p, "hlp_thishelp","/"+plgcmd+" help");
		PrintMSG(p, "hlp_execcmd","/"+plgcmd+" <"+MSG("hlp_cmdparam_command",'2')+"> ["+MSG("hlp_cmdparam_parameter",'2')+"]");
		PrintMSG(p, "hlp_typecmd","/"+plgcmd+" help <"+MSG("hlp_cmdparam_command",'2')+">");
		PrintMsg(p, MSG("hlp_commands")+" &2"+cmdlist);
	}


	/* 
	 * Печать справки по команде
	 */
	public void PrintHLP (Player p, String cmd){
		if (cmds.containsKey(cmd)){
			PrintMsg(p, "&6&l"+version_name+" v"+des.getVersion()+" &r&6| "+MSG("hlp_help",'6'));
			PrintMsg(p, cmds.get(cmd).desc);
		} else PrintMSG(p,"cmd_unknown",cmd,'c','e');
	}

	/* 
	 * Возврат логической переменной в виде текста выкл./вкл.
	 */
	public String EnDis (boolean b){
		return b ? MSG ("enabled",'2') : MSG ("disabled",'c'); 
	}

	public String EnDis (String str, boolean b){
		String str2 = ChatColor.stripColor(str);
		return b ? ChatColor.DARK_GREEN+str2 : ChatColor.RED+str2; 
	}

	/* 
	 * Печать значения логической переменной 
	 */
	public void PrintEnDis (Player p, String msg_id, boolean b){
		p.sendMessage(MSG (msg_id)+": "+EnDis(b));
	}

	/* 
	 * Печать значения логической переменной 
	 */
	public void PrintMSG (Player p, String msg_id, boolean b){
		PrintMSG (p,msg_id,EnDis(b));
	}


	/* 
	 * Дополнительные процедуры
	 */

	/*
	 * Переопределение префикса пермишенов 
	 */
	public void setPermPrefix(String ppfx){
		this.permprefix = ppfx+".";
		this.version_info_perm=this.permprefix+"config";
	}

	/*
	 * Проверка соответствия пермишена (указывать без префикса)
	 * заданной команде 
	 */
	public boolean equalCmdPerm(String cmd, String perm) {
		return (cmds.containsKey(cmd.toLowerCase())) && 
				((cmds.get(cmd.toLowerCase())).perm.equalsIgnoreCase(permprefix+perm));
	}


	/* 
	 * Преобразует строку вида <id>:<data> в ItemStack
	 * Возвращает null если строка кривая
	 */
	public ItemStack parseItem (String itemstr){
		if (!itemstr.isEmpty()){
			String[] ti = itemstr.split(":");
			if (ti.length>0){

				int id = -1;
				if (ti[0].matches("[1-9]+[0-9]*")) id=Integer.parseInt(ti[0]);
				else id = Material.getMaterial(ti[0]).getId();


				int count = 1;
				if ((ti.length>1)&&(ti[1].matches("[1-9]+[0-9]*")))
					count = Integer.parseInt(ti[1]);
				short data = 0;
				if ((ti.length==3)&&(ti[2].matches("[1-9]+[0-9]*")))
					data = Short.parseShort(ti[2]);				
				return new ItemStack (id, count, data);
			}
		}
		return null;
	}

	/*
	 * Проверяет, есть ли игроки в пределах заданного радиуса
	 */
	public boolean isPlayerAround (Location loc, int radius){
		for (Player p : loc.getWorld().getPlayers()){
			if (p.getLocation().distance(loc)<=radius) return true;
		}
		return false;		
	}

	/*
	 *  Тоже, что и MSG, но обрезает цвет
	 */
	public String MSGnc(String id){
		return ChatColor.stripColor(MSG (id));
	}


	/*
	 * Установка блока с проверкой на приват
	 */
	public boolean placeBlock(Location loc, Player p, Material newType, byte newData, boolean phys){
		return placeBlock (loc.getBlock(),p,newType,newData, phys);
	}

	/*
	 * Установка блока с проверкой на приват
	 */
	public boolean placeBlock(Block block, Player p, Material newType, byte newData, boolean phys){
		BlockState state = block.getState();
		block.setTypeIdAndData(newType.getId(), newData, phys);
		BlockPlaceEvent event = new BlockPlaceEvent(state.getBlock(), state, block, p.getItemInHand(), p, true);
		plg.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) state.update(true);
		return event.isCancelled();
	}
	
	public boolean rollDiceChance (int chance){
		return (random.nextInt(100)<chance);
	}




}


