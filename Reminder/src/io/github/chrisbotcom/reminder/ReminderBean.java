package io.github.chrisbotcom.reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "reminders")

public class ReminderBean {
	@Id private Long id;
	@Column private String player;
	@Column private String message;
	@Column private Long start;
	@Column private String tag;
	@Column private Integer delay;
	@Column private Integer rate;
	@Column private Integer echo;
	
	public Long getId() { return id; }
	public String getPlayer() { return player; }
	public String getMessage() { return message; }
	public Long getStart() { return start; }
	public String getTag() { return tag; }
	public Integer getDelay() { return delay; }
	public Integer getRate() { return rate; }
	public Integer getEcho() { return echo; }
	
	public void setId(Long id) { this.id = id; }
	public void setPlayer(String player) { this.player = player; }
	public void setMessage(String message) { this.message = message; }
	public void setStart(Long start) { this.start = start; }
	public void setTag(String tag) { this.tag = tag; }
	public void setDelay(Integer delay) { this.delay = delay; }
	public void setRate(Integer rate) { this.rate = rate; }
	public void setEcho(Integer echo) { this.echo = echo; }
	
	public String get(String name)
	{
		String value = null;
		TokensEnum tokenName = TokensEnum.valueOf(name);
		switch (tokenName)
		{
			case id:
				value = id.toString();
				break;
			case player:
				value = player;
				break;
			case message:
				value = message;
				break;
			case start:
				value = start.toString();
				break;
			case tag:
				value = tag;
				break;
			case delay:
				value = delay.toString();
				break;
			case rate:
				value = rate.toString();
				break;
			case echo:
				value = echo.toString();
				break;
		}
		return value;
	}
	
	public void set(String name, String value) throws ParseException
	{
		TokensEnum tokenName = TokensEnum.valueOf(name);
		switch (tokenName)
		{
			case id:
				id = Long.parseLong(value);
				break;
			case player:
				player = value;
				break;
			case message:
				message = value;
				break;
			case start:
				if (value.startsWith("+"))
				{ // number
					long date = Long.parseLong(value.substring(1));
					if (start == null)
						start = new Date().getTime();
					start = (start / 86400000) + date;
				}
				else if (value.length() > 5)
				{ // date
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					long date = simpleDateFormat.parse(value).getTime();
					start = (start % 60000) + date; 
				}
				else
				{ // time
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
					long date = simpleDateFormat.parse(value).getTime();
					start = (start / 86400000) + date;
				}
				break;
			case tag:
				tag = value;
				break;
			case delay:
				delay = Integer.parseInt(value);
				break;
			case rate:
				rate = Integer.parseInt(value);
				break;
			case echo:
				echo = Integer.parseInt(value);
				break;
		}
	}
}
