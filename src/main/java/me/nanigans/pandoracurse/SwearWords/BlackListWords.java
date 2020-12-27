package me.nanigans.pandoracurse.SwearWords;

import me.nanigans.pandoracurse.Inventory.BlackListInventory;
import me.nanigans.pandoracurse.Utils.YamlGenerator;

import java.util.HashMap;
import java.util.Map;

public class BlackListWords {
    BlackListInventory word;
    private static final String path = BlackListInventory.getPlugin().getDataFolder().getAbsolutePath()+"/BlacklistedWords.yml";

    public BlackListWords(BlackListInventory word){
        this.word = word;
    }

    public static Map<String, Object> getAllWords(){
        final YamlGenerator yaml = new YamlGenerator(path);
        final Map<String, Object> words = YamlGenerator.getConfigSectionValue(yaml.getData().get("Words"), false);
        Map<String, Object> data = new HashMap<>();
        words.forEach((i, j) -> {
            final Map<String, Object> configSectionValue = YamlGenerator.getConfigSectionValue(yaml.getData().get("Words." + i), false);
            data.put(i, configSectionValue);
        });
        return data;
    }

    /**
     * Removes a word from the blacklist if it exists
     * @param word the word to remove
     */
    public static void removeWord(String word){
        final YamlGenerator yaml = new YamlGenerator(path);
        yaml.getData().set("Words."+word, null);
        yaml.save();
    }

    /**
     * Adds a word to the blacklist. This can be used as a replacement/edit too
     * @return if it worked or not
     */
    public boolean addWord(boolean override) {

        final YamlGenerator yaml = new YamlGenerator(path);

        final Map<String, Object> words = YamlGenerator.getConfigSectionValue(yaml.getData().get("Words"), false);
        if(words == null || !words.containsKey(this.word.getBannedWord()) || override){

            Map<String, Object> data = new HashMap<>();
            data.put("added_by", this.word.getPlayer().getUniqueId() + " - " + this.word.getPlayer().getName());
            data.put("alertStaff", this.word.isAlertStaff());
            data.put("useFuzzySet", this.word.isFuzzySet());
            if(this.word.isFuzzySet())
                data.put("fuzzyTolerance", this.word.getFuzzyTolerance());
            data.put("sensitive", this.word.isHighSensitivity());

            yaml.getData().set("Words."+this.word.getBannedWord(), data);
            yaml.save();

            return true;
        }else return false;
    }
}
