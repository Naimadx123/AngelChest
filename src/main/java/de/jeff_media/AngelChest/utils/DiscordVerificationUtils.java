package de.jeff_media.AngelChest.utils;

import de.jeff_media.AngelChest.Main;
import de.jeff_media.AngelChest.config.Config;
import de.jeff_media.discordverifier.DiscordVerifier;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiscordVerificationUtils {
    public static void createVerificationFile(String UID, String NONCE, String RESOURCE) {

        Main main = Main.getInstance();

        File verifyFile = new File(main.getDataFolder(),"discord-verification.html");
        if(!verifyFile.exists()) {
            for(String line : new String[] {
                    "§b=====================================================",
                    "§b                 Discord Verification",
                    "§b",
                    "§bThank you for buying AngelChestPlus. To get exclusive",
                    "§bDiscord support, you have to verify your copy of",
                    "§bAngelChestPlus. Just take a look at the following file:",
                    "§b",
                    "§b"+verifyFile.getAbsolutePath(),
                    "§b====================================================="
            }) {
                main.getLogger().warning(line);
            }
            main.saveResource("discord-verification.html", false);
            String verifyCode = DiscordVerifier.getVerificationCode(UID, NONCE,
                    "Version: " + main.getDescription().getVersion()+"\n"
                            +"Resource: " + RESOURCE+"\n"
                            +"ConfigVersion: "+main.getConfig().getInt(Config.CONFIG_VERSION));
            try {
                FileUtils.replaceStringsInFile(verifyFile,"{verificationCode}",addLineBreaks(verifyCode));
            } catch (IOException ioException) {
                main.getLogger().severe("Could not update discord-verification.html");
            }
        }
    }

    private static String addLineBreaks(String code) {
        final int maxLength = 80;
        int lengthUntilBegin = 19;
        String rest = code;
        List<String> lines = new ArrayList<>();

        while(rest.length()>maxLength-lengthUntilBegin) {
            lines.add(rest.substring(0,maxLength-lengthUntilBegin));
            rest = rest.substring(maxLength-lengthUntilBegin);
            lengthUntilBegin=0;
        }
        if(rest != null && rest.length()>0) {
            lines.add(rest);
        }
        String[] linesArray = lines.toArray(new String[0]);
        return StringUtils.join(linesArray,"\n");
    }
}