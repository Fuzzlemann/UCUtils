package de.fuzzlemann.ucutils.commands;

import de.fuzzlemann.ucutils.utils.Logger;
import de.fuzzlemann.ucutils.base.command.Command;
import de.fuzzlemann.ucutils.base.text.Message;
import de.fuzzlemann.ucutils.base.text.TextUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Fuzzlemann
 */
@SideOnly(Side.CLIENT)
public class InternetTestCommand {
    private final List<String> hosts = Arrays.asList("unicacity.de", "fuzzlemann.de", "google.de");

    @Command(value = {"internettest", "inettest"}, async = true)
    public boolean onCommand() {
        Message.builder()
                .joiner(hosts)
                .consumer((b, host) -> {
                    String result;
                    try {
                        result = ping(host);
                    } catch (IOException e) {
                        Logger.LOGGER.catching(e);
                        TextUtils.error("Konnte keine Verbindung zu " + host + " herstellen.");
                        return;
                    }

                    b.of(host).color(TextFormatting.DARK_AQUA).advance()
                            .of("> ").color(TextFormatting.DARK_GRAY).advance()
                            .of(result).color(TextFormatting.BLUE).advance();
                }).newLineJoiner().advance()
                .send();
        return true;
    }

    private String ping(String host) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("ping", "-n", "1", "-4", host);
        Process proc = processBuilder.start();

        String pingResult = null;

        try (InputStreamReader in = new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(in)) {
            int i = 0;
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                if (++i == 3) {
                    pingResult = inputLine;
                    break;
                }
            }
        }

        return toPing(Objects.requireNonNull(pingResult));
    }

    private String toPing(String pingString) {
        if (!pingString.contains("=")) return "Nicht erreichbar";

        return pingString.split(" ")[4].split("=")[1];
    }
}
