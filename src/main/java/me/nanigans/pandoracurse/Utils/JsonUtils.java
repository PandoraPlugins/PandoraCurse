package me.nanigans.pandoracurse.Utils;

import me.nanigans.pandoracurse.PandoraCurse;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonUtils {

    private static final PandoraCurse plugin = PandoraCurse.getPlugin(PandoraCurse.class);

    public static File jsonPath = new File(plugin.getDataFolder() + "/config.json");


    public static String getData(String path) {

        try {
            String[] paths = path.split("\\.");
            JSONParser jsonParser = new JSONParser();
            Object parsed = jsonParser.parse(new FileReader(jsonPath));
            JSONObject jsonObject = (JSONObject) parsed;

            JSONObject currObject = (JSONObject) jsonObject.clone();

            for (String s : paths) {
                if (currObject.get(s) instanceof JSONObject)
                    currObject = (JSONObject) currObject.get(s);
                else return ChatColor.translateAlternateColorCodes('&', currObject.get(s).toString());
            }
            return ChatColor.translateAlternateColorCodes('&', currObject.toString());
        }catch(IOException | ParseException ignored){}

        return null;

    }

}
