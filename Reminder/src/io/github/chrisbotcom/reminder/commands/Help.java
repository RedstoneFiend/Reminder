/* 
 * Reminder - Reminder plugin for Bukkit
 * Copyright (C) 2014 Chris Courson http://www.github.com/Chrisbotcom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/gpl-3.0.html.
 */

package io.github.chrisbotcom.reminder.commands;

//import java.util.List;
//import java.util.Map;

//import org.bukkit.ChatColor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.plugin.Plugin;

//import ru.tehkode.permissions.commands.Command;
//import ru.tehkode.permissions.commands.CommandsManager.CommandBinding;
//import ru.tehkode.permissions.bukkit.commands.UtilityCommands;

public class Help {
/*	@Command(name = "pex",
			syntax = "help [page] [count]",
			permission = "permissions.manage",
			description = "PermissionsEx commands help")
	public void showHelp(Plugin plugin, CommandSender sender, Map<String, String> args) {
		List<CommandBinding> commands = this.manager.getCommands();

		int count = tryGetInt(sender, args, "count", 4);
		int page = tryGetInt(sender, args, "page", 1);

		if (page == Integer.MIN_VALUE || count == Integer.MIN_VALUE) {
			return; // method already prints error message
		}

		if (page < 1) {
			sender.sendMessage(ChatColor.RED + "Page couldn't be lower than 1");
			return;
		}

		int totalPages = (int) Math.ceil(commands.size() / count);

		sender.sendMessage(ChatColor.BLUE + "PermissionsEx" + ChatColor.WHITE + " commands (page " + ChatColor.GOLD + page + "/" + totalPages + ChatColor.WHITE + "): ");

		int base = count * (page - 1);

		for (int i = base; i < base + count; i++) {
			if (i >= commands.size()) {
				break;
			}

			Command command = commands.get(i).getMethodAnnotation();
			String commandName = String.format("/%s %s", command.name(), command.syntax()).replace("<", ChatColor.BOLD.toString() + ChatColor.RED + "<").replace(">", ">" + ChatColor.RESET + ChatColor.GOLD.toString()).replace("[", ChatColor.BOLD.toString() + ChatColor.BLUE + "[").replace("]", "]" + ChatColor.RESET + ChatColor.GOLD.toString());


			sender.sendMessage(ChatColor.GOLD + commandName);
			sender.sendMessage(ChatColor.AQUA + "    " + command.description());
		}
	}
*/
}
