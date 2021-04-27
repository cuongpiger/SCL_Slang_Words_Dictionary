import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Word {
    String word;
    List<String> defs;

    Word(String w, List<String> d) {
        word = w;
        defs = d;
    }
}

class History implements Serializable {
    String key;
    String word;

    History(String k, String w) {
        key = k;
        word = w;
    }

    @Override
    public String toString() {
        return "History{" +
                "key='" + key + '\'' +
                ", word='" + word + '\'' +
                "}\n";
    }
}

public class Dictionary {
    private static HashMap<String, Word> dict = null;
    private static HashMap<String, Word> dict_rev = null;
    private static ArrayList<History> history_dict = null;
    private static ArrayList<History> history_dict_rev = null;
    private static final String db = "DATABASES/slang.txt";
    private static final String db_history_dict = "DATABASES/db_history_dict.ser";
    private static final String db_history_dict_rev = "DATABASES/db_history_dict_rev.ser";

    Dictionary() {
        dict = new HashMap<String, Word>();
        dict_rev = new HashMap<String, Word>();
        history_dict = readFromHistory(db_history_dict);
        history_dict_rev = readFromHistory(db_history_dict_rev);

        if (history_dict == null) {
            history_dict = new ArrayList<>();
        }

        if (history_dict_rev == null) {
            history_dict_rev = new ArrayList<>();
        }
    }

    public HashMap<String, Word> get_dict() {
        return dict;
    }

    public HashMap<String, Word> get_dict_rev() {
        return dict_rev;
    }

    public void loadDB() {
        try (BufferedReader br = new BufferedReader(new FileReader(db))) {
            String row;
            br.readLine();

            while ((row = br.readLine()) != null) {
                String[] splits = row.split("`", 2);

                if (splits.length != 2) {
                    splits = new String[]{splits[0], ""};
                }

                String slang_key = splits[0].trim().toLowerCase();
                String slang_ori = splits[0];
                List<String> defs_ori = Arrays.stream(splits[1].split(Pattern.quote("|"))).map(String::trim).collect(Collectors.toList());
                List<String> defs_key = defs_ori.stream().map(String::toLowerCase).collect(Collectors.toList());

                if (!dict.containsKey(slang_key)) {
                    Word word = new Word(slang_ori, defs_ori);
                    dict.put(slang_key, word);
                } else {
                    Word word = dict.get(slang_key);
                    word.defs.addAll(defs_ori);
                }

                for (int i = 0; i < defs_key.size(); ++i) {
                    if (!dict_rev.containsKey(defs_key.get(i))) {
                        Word word = new Word(defs_ori.get(i), new ArrayList<String>(Arrays.asList(slang_key)));
                        dict_rev.put(defs_key.get(i), word);
                    } else {
                        Word word = dict_rev.get(defs_key.get(i));
                        word.defs.add(slang_key);
                    }
                }
            }
        } catch (IOException err) {
            System.out.println("⛔ Inside the method Dictionary.loadDB() errors occurred!!!");
        }
    }

    public void searchBasedSlang(String keyword) {
        if (dict.containsKey(keyword)) {
            var word = dict.get(keyword);

            System.out.println("\uD83D\uDD0E The meaning of '" + word.word + "' is:");
            for (var def : word.defs) {
                System.out.println("  \uD83D\uDD38 " + def);
            }

            // save to history
            history_dict.add(new History(keyword, word.word));

            if (history_dict.size() > 10) {
                history_dict.remove(0);
            }

            saveToHistory(db_history_dict, history_dict);
        } else {
            System.out.println("\uD83D\uDCAC Your word does not exist in the data!!!");
        }
    }

    public void searchBasedDefinition(String keyword) {
        if (dict_rev.containsKey(keyword)) {
            var word = dict_rev.get(keyword);

            System.out.println("\uD83D\uDD0E The slang-word of '" + word.word + "' is:");
            for (var def : word.defs) {
                System.out.println("  \uD83D\uDD38 " + dict.get(def).word);
            }

            history_dict_rev.add(new History(keyword, word.word));

            if (history_dict_rev.size() > 10) {
                history_dict_rev.remove(0);
            }

            saveToHistory(db_history_dict_rev, history_dict_rev);

        } else {
            System.out.println("\uD83D\uDCAC Your definition does not exist in the data!!!");
        }
    }

    public void saveToHistory(String db_name, ArrayList<History> history) {
        try {
            FileOutputStream writeData = new FileOutputStream(db_name);
            ObjectOutputStream writeStream = new ObjectOutputStream(writeData);

            writeStream.writeObject(history);
            writeStream.flush();
            writeStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<History> readFromHistory(String db_name) {
        try {
            FileInputStream readData = new FileInputStream(db_name);
            ObjectInputStream readStream = new ObjectInputStream(readData);

            ArrayList<History> dict = (ArrayList<History>) readStream.readObject();
            readStream.close();

            return dict;
        } catch (Exception e) {
            return null;
        }
    }

    public void showHistory() {
        System.out.println("10 most recent search words:");
        System.out.format("%20s%40s%n", "Slang-word", "Definition");
        int lim = Math.max(history_dict.size(), history_dict_rev.size());

        for (int i = 0; i < lim; ++i) {
            String slang = i < history_dict.size() ? history_dict.get(i).word : "";
            String def = i < history_dict_rev.size() ? history_dict_rev.get(i).word : "";
            System.out.format("%40s%40s%n", slang, def);
        }
    }
}
