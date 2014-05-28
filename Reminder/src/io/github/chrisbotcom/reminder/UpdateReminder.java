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
package io.github.chrisbotcom.reminder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class UpdateReminder implements Runnable {

    Reminder reminder;

    public UpdateReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    @Override
    public void run() {
        URLConnection urlConnection;
        String downloadURL = null;
        String fileName = null;
        String version = null;

        // Check for update
        try {
            urlConnection = new URL("https://api.curseforge.com/servermods/files?projectIds=76647").openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.addRequestProperty("User-Agent", "Reminder Updater");
            urlConnection.setDoOutput(true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String response = bufferedReader.readLine();
            JSONArray jsonArray = (JSONArray) JSONValue.parse(response);
            if (jsonArray.size() == 0) {
                return;
            }
            JSONObject jsonObject = (JSONObject) jsonArray.get(jsonArray.size() - 1);
            fileName = (String) jsonObject.get("name");
            downloadURL = (String) jsonObject.get("downloadUrl");

			// Simple version check. Return if no update pending
            // TODO: Curse does not reliably update JSON query when version 1.1 approved. Need to use more sofisticated version checking.
            version = fileName.split(" v")[1];
            if (version.equalsIgnoreCase(reminder.getDescription().getVersion())) {
                return;
            }

            reminder.getLogger().info("An update exists: " + fileName);
        } catch (IOException e) {
            reminder.getLogger().log(Level.WARNING, "IOException retrieving update availability!");
        }

        // Download update to data folder, expand and replace existing
        if (reminder.getConfig().getBoolean("autoUpdate")) {
            try {
                String zipFilePath = reminder.getDataFolder().getAbsolutePath() + File.separator + fileName + ".zip";
                InputStream inputStream = new URL(downloadURL).openConnection().getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);
                byte[] buffer = new byte[1024];
                int count;
                while ((count = inputStream.read(buffer, 0, 1024)) != -1) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();

                // Unzip file
                ZipFile zipFile = new ZipFile(zipFilePath);
                ZipEntry zipEntry = zipFile.getEntry("Reminder.jar");
                inputStream = zipFile.getInputStream(zipEntry);
                fileOutputStream = new FileOutputStream(reminder.getDataFolder().getParent() + File.separator + zipEntry.getName());
                while ((count = inputStream.read(buffer, 0, 1024)) != -1) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();
                zipFile.close();
                reminder.getLogger().info("Reminder updated to version " + version + ".");
                if (reminder.config.getBoolean("removeZip")) {
                    File file = new File(zipFilePath);
                    file.delete();
                }
            } catch (MalformedURLException e) {
                reminder.getLogger().log(Level.WARNING, "MalformedURLException retrieving update!");
            } catch (IOException e) {
                reminder.getLogger().log(Level.WARNING, "IOException retrieving update!");
            }
        }
    }
}
