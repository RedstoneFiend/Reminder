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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReminderRecord {

    private Long id;
    private String player;
    private String message;
    private Long start;
    private String tag;
    private Integer delay;
    private Integer rate;
    private Integer echo;

    public Long getId() {
        return id;
    }

    public String getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public Long getStart() {
        return start;
    }

    public String getTag() {
        return tag;
    }

    public Integer getDelay() {
        return delay;
    }

    public Integer getRate() {
        return rate;
    }

    public Integer getEcho() {
        return echo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public void setEcho(Integer echo) {
        this.echo = echo;
    }

    public String get(String name) {
        String value = null;
        switch (name) {
            case "id":
                value = id == null ? null : id.toString();
                break;
            case "player":
                value = player;
                break;
            case "message":
                value = message;
                break;
            case "start":
                value = start == null ? null : start.toString();
                break;
            case "tag":
                value = tag;
                break;
            case "delay":
                value = delay == null ? null : delay.toString();
                break;
            case "rate":
                value = rate == null ? null : rate.toString();
                break;
            case "echo":
                value = echo == null ? null : echo.toString();
                break;
        }
        return value;
    }

    public void set(String name, String value) throws ParseException {
        switch (name) {
            case "id":
                id = Long.parseLong(value);
                break;
            case "player":
                player = value;
                break;
            case "message":
                message = value;
                break;
            case "start":
                if (value.startsWith("+")) { // minutes
                    long offset = Long.parseLong(value) * 60000;
                    start = (start == null ? new Date().getTime() : start) + offset;
                } else if (value.length() > 5) { // date
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                    String time = start == null ? "00:00" : simpleDateFormat.format(new Date(start));
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    start = simpleDateFormat.parse(value + " " + time).getTime();
                } else { // time
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = start == null ? simpleDateFormat.format(new Date()) : simpleDateFormat.format(new Date(start));
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    start = simpleDateFormat.parse(value + " " + date).getTime();
                }
                break;
            case "tag":
                tag = value;
                break;
            case "delay":
                delay = Integer.parseInt(value);
                break;
            case "rate":
                rate = Integer.parseInt(value);
                break;
            case "echo":
                echo = Integer.parseInt(value);
                break;
        }
    }
}
