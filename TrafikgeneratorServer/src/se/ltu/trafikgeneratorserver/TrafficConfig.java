package se.ltu.trafikgeneratorserver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import ch.ethz.inf.vs.californium.network.config.NetworkConfig;

public class TrafficConfig {
	private String  meta_author                     = "";
	private String  meta_title                      = "";

	private String  test_server;
	private Integer test_testport                   = 56830;
	private Integer test_repeats                    = 1;
	private Float   test_intermission               = (float) 10000.0;
	private Integer test_paralleltransfers          = 1;

	private String  coap_messagetype                = "CON";
	private Integer coap_ack_timeout                = 2000;
	private Float   coap_ack_random_factor          = (float) 1.5;
	private Integer coap_max_retransmit             = 4;
	private Integer coap_nstart                     = 1;
	private Integer coap_probing_rate               = 1;

	private String  traffic_type                    = "CONSTANT_SOURCE";
	private String  traffic_mode                    = "TIME";
	private Float   traffic_constant_maxsendtime    = (float) 10.0;
	private Integer traffic_maxmessages             = 2500;
	private Float   traffic_onoff_maxsendtime       = (float) 60.0;
	private Integer traffic_rate                    = 25000;
	private Integer traffic_messagesize             = 100;
	private Float   traffic_intermission            = (float) 0.0;
	private Float   traffic_randomfactor            = (float) 1.0;
	private Float   traffic_burst_time              = (float) 500.0;
	private Float   traffic_idle_time               = (float) 500.0;

	public TrafficConfig(String configuration) {
		String[] all_rows = configuration.split("\n");
		// Remove comments.
		for (int i = 0; i < all_rows.length; i++) {
			all_rows[i] = all_rows[i].split("#", 2)[0];
		}
		// Set... settings.
		for (int i = 0; i < all_rows.length; i++) {
			if (all_rows[i].trim().equals("")) { continue; }
			String[] row   = all_rows[i].split("\\s+", 2);
			String type    = row[0];
			String setting = row[1].split("=", 2)[0];
			String data    = row[1].split("=", 2)[1].trim();
			setting = (type + "_" + setting).toUpperCase(Locale.getDefault());
			if (setting.equals("META_TESTVERSION"))
					continue;
			switch (Settings.valueOf(setting)) {
				case TEST_INTERMISSION:            test_intermission            = Float.valueOf(data); continue;
				case COAP_ACK_RANDOM_FACTOR:       coap_ack_random_factor       = Float.valueOf(data); continue;
				case TRAFFIC_MAXSENDTIME:          if (traffic_type.equals("ONOFF_SOURCE"))
					                                   { traffic_onoff_maxsendtime    = Float.valueOf(data); continue; }
					                               else
					                                   { traffic_constant_maxsendtime = Float.valueOf(data); continue; }
				case TRAFFIC_INTERMISSION:         traffic_intermission         = Float.valueOf(data); continue;
				case TRAFFIC_RANDOMFACTOR:         traffic_randomfactor         = Float.valueOf(data); continue;
				case TRAFFIC_BURST_TIME:           traffic_burst_time           = Float.valueOf(data); continue;
				case TRAFFIC_IDLE_TIME:            traffic_idle_time            = Float.valueOf(data); continue;
				case TEST_TESTPORT:                test_testport                = Integer.valueOf(data); continue;
				case TEST_REPEATS:                 test_repeats                 = Integer.valueOf(data); continue;
				case TEST_PARALLELTRANSFERS:       test_paralleltransfers       = Integer.valueOf(data); continue;
				case COAP_ACK_TIMEOUT:             coap_ack_timeout             = Integer.valueOf(data); continue;
				case COAP_MAX_RETRANSMIT:          coap_max_retransmit          = Integer.valueOf(data); continue;
				case COAP_NSTART:                  coap_nstart                  = Integer.valueOf(data); continue;
				case COAP_PROBING_RATE:            coap_probing_rate            = Integer.valueOf(data); continue;
				case TRAFFIC_MAXMESSAGES:          traffic_maxmessages          = Integer.valueOf(data); continue;
				case TRAFFIC_RATE:                 traffic_rate                 = Integer.valueOf(data); continue;
				case TRAFFIC_MESSAGESIZE:          traffic_messagesize          = Integer.valueOf(data); continue;
				case META_AUTHOR:                  meta_author                  = data; continue;
				case META_TITLE:                   meta_title                   = data; continue;
				case TEST_SERVER:                  test_server                  = data.replaceAll("\\s+", ""); continue;
				case COAP_MESSAGETYPE:             coap_messagetype             = data; continue;
				case TRAFFIC_TYPE:                 traffic_type                 = data; continue;
				case TRAFFIC_MODE:                 traffic_mode                 = data; continue;
				default:                           continue;
			}
		}
	}
	public Float getDecimalSetting(Settings setting) {
		switch (setting) {
			case TEST_INTERMISSION:            return test_intermission;
			case COAP_ACK_RANDOM_FACTOR:       return coap_ack_random_factor;
			case TRAFFIC_MAXSENDTIME:          if (traffic_type.equals("ONOFF_SOURCE"))
				                                   return traffic_onoff_maxsendtime;
				                               else
				                            	   return traffic_constant_maxsendtime;
			case TRAFFIC_INTERMISSION:         return traffic_intermission;
			case TRAFFIC_RANDOMFACTOR:         return traffic_randomfactor;
			case TRAFFIC_BURST_TIME:           return traffic_burst_time;
			case TRAFFIC_IDLE_TIME:            return traffic_idle_time;
			default:                           return null;
		}
	}
	public Integer getIntegerSetting(Settings setting) {
		switch (setting) {
			case TEST_TESTPORT:                return test_testport;
			case TEST_REPEATS:                 return test_repeats;
			case TEST_PARALLELTRANSFERS:       return test_paralleltransfers;
			case COAP_ACK_TIMEOUT:             return coap_ack_timeout;
			case COAP_MAX_RETRANSMIT:          return coap_max_retransmit;
			case COAP_NSTART:                  return coap_nstart;
			case COAP_PROBING_RATE:            return coap_probing_rate;
			case TRAFFIC_MAXMESSAGES:          return traffic_maxmessages;
			case TRAFFIC_RATE:                 return traffic_rate;
			case TRAFFIC_MESSAGESIZE:          return traffic_messagesize;
			default:                           return null;
		}
	}
	public String getStringSetting(Settings setting) {
		switch (setting) {
			case META_AUTHOR:                  return meta_author;
			case META_TITLE:                   return meta_title;
			case TEST_SERVER:                  return test_server;
			case COAP_MESSAGETYPE:             return coap_messagetype;
			case TRAFFIC_TYPE:                 return traffic_type;
			case TRAFFIC_MODE:                 return traffic_mode;
			default:                           return null;
		}
	}
	public NetworkConfig toNetworkConfig() {
		NetworkConfig config = new NetworkConfig();
		config.setInt("DEFAULT_COAP_PORT", this.getIntegerSetting(Settings.TEST_TESTPORT));
		config.setInt("ACK_TIMEOUT", this.getIntegerSetting(Settings.COAP_ACK_TIMEOUT));
		config.setInt("NSTART", this.getIntegerSetting(Settings.COAP_NSTART));
		config.setInt("MAX_RETRANSMIT", this.getIntegerSetting(Settings.COAP_MAX_RETRANSMIT));
		config.setInt("MAX_MESSAGE_SIZE", this.getIntegerSetting(Settings.TRAFFIC_MESSAGESIZE));
		config.setFloat("ACK_RANDOM_FACTOR", this.getDecimalSetting(Settings.COAP_ACK_RANDOM_FACTOR));
		return config;
	}
	static public String fileToString(String filename) {
		FileReader fil;
		StringBuilder stringBuilder;
		try {
			fil = new FileReader (filename);
			BufferedReader reader = new BufferedReader(fil);
			String line = null;
			stringBuilder = new StringBuilder();
			String endofline = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(endofline);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return stringBuilder.toString();
	}
	static public String networkConfigToStringList (NetworkConfig config) {
		String list = "";
		list += "DEFAULT_COAP_PORT=" + Integer.toString(config.getInt("DEFAULT_COAP_PORT"));
		list += ",ACK_TIMEOUT=" + Integer.toString(config.getInt("ACK_TIMEOUT"));
		list += ",ACK_RANDOM_FACTOR=" + Float.toString(config.getFloat("ACK_RANDOM_FACTOR"));
		list += ",ACK_TIMEOUT_SCALE=" + Integer.toString(config.getInt("ACK_TIMEOUT_SCALE"));
		list += ",NSTART=" + Integer.toString(config.getInt("NSTART"));
		list += ",DEFAULT_LEISURE=" + Integer.toString(config.getInt("DEFAULT_LEISURE"));
		list += ",MAX_RETRANSMIT=" + Integer.toString(config.getInt("MAX_RETRANSMIT"));
//		list += ",EXCHANGE_LIFECYCLE=" + Long.toString(config.getLong("EXCHANGE_LIFECYCLE"));
		list += ",MAX_MESSAGE_SIZE=" + Integer.toString(config.getInt("MAX_MESSAGE_SIZE"));
		return list;
	}
	static public NetworkConfig stringListToNetworkConfig (String list) {
		String[] array = list.split(",");
		NetworkConfig config = new NetworkConfig();
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(""))
				continue;
			String[] setting = array[i].split("=");
			//else if (setting.equals(""))
			if (setting[0].equals("DEFAULT_COAP_PORT") || setting[0].equals("ACK_TIMEOUT") || setting[0].equals("ACK_TIMEOUT_SCALE") || setting[0].equals("NSTART") || setting[0].equals("DEFAULT_LEISURE") || setting[0].equals("MAX_RETRANSMIT") || setting[0].equals("MAX_MESSAGE_SIZE")) {
				config.setInt(setting[0], Integer.valueOf(setting[1]));
			}
			else if (setting[0].equals("ACK_RANDOM_FACTOR"))
				config.setFloat(setting[0], Float.valueOf(setting[1]));
			//else if (setting[0].equals("EXCHANGE_LIFECYCLE"))
				//config.setFloat(setting[0], Long.valueOf(setting[1]));
		}
		return config;
	}
}
