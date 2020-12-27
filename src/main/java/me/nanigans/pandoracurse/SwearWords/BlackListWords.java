package me.nanigans.pandoracurse.SwearWords;

import me.nanigans.pandoracurse.Inventory.BlackListInventory;
import me.nanigans.pandoracurse.Utils.YamlGenerator;

import java.util.HashMap;
import java.util.Map;

public class BlackListWords {
    BlackListInventory word;
    public BlackListWords(BlackListInventory word){
        this.word = word;
    }

    /**
     * Removes a word from the blacklist
     * @param word the word to remove
     */
    public static void removeWord(String word){

        final YamlGenerator yaml = new YamlGenerator(BlackListInventory.getPlugin().getDataFolder().getAbsolutePath()+"/BlacklistedWords.yml");
        final Map<String, Object> words = YamlGenerator.getConfigSectionValue(yaml.getData().get("Words"), true);
        words.remove(word);
        yaml.getData().set("Words", words);
        yaml.save();

    }

    /**
     * Adds a word to the blacklist. This can be used as a replacement/edit too
     * @return if it worked or not
     */
    public boolean addWord() {

        final YamlGenerator yaml = new YamlGenerator(BlackListInventory.getPlugin().getDataFolder().getAbsolutePath()+"/BlacklistedWords.yml");

        final Map<String, Object> words = YamlGenerator.getConfigSectionValue(yaml.getData().get("Words"), false);
        if(words == null || !words.containsKey(this.word.getBannedWord())){

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
