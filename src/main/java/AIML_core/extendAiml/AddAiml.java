package AIML_core.extendAiml;

import org.alicebot.ab.Bot;
import org.alicebot.ab.MagicBooleans;

import java.io.File;

public class AddAiml {
    private static final boolean TRACE_MODE = false;
    static String botName = "super";

    public static void main (String[] args) {
        try{
            String resourcePath = getResourcePath();
            MagicBooleans.trace_mode = TRACE_MODE;
            Bot bot = new Bot(botName, resourcePath);

            bot.writeAIMLFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getResourcePath () {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();

        path = path.substring(0, path.length() - 2);

        return path + File.separator + "src" + File.separator + "main" + File.separator + "resources";
    }
}
