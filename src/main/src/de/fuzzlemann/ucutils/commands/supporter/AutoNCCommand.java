package de.fuzzlemann.ucutils.commands.supporter;

import com.google.gson.Gson;
import de.fuzzlemann.ucutils.Main;
import de.fuzzlemann.ucutils.utils.api.APIUtils;
import de.fuzzlemann.ucutils.utils.command.Command;
import de.fuzzlemann.ucutils.utils.command.CommandExecutor;
import de.fuzzlemann.ucutils.utils.config.ConfigUtil;
import de.fuzzlemann.ucutils.utils.sound.SoundUtil;
import de.fuzzlemann.ucutils.utils.text.Message;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fuzzlemann
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber
public class AutoNCCommand implements CommandExecutor {

    private static final Pattern NOOB_CHAT_PATTERN = Pattern.compile("\\[NeulingsChat] (?:\\[UC])*([a-zA-Z0-9_]+): (.+)");
    private static final Map<Integer, String> ANSWER_MAP = new HashMap<>();
    private static final Gson GSON = new Gson();

    private static long time;
    private static int i;
    private static boolean enabled;

    @Override
    @Command(labels = {"autonc", "autonoobchat", "autoneulingschat"})
    public boolean onCommand(EntityPlayerSP p, String[] args) {
        if (args.length == 0) {
            enabled = !enabled;
            Message message = Message.builder()
                    .of("[").color(TextFormatting.DARK_GRAY).advance()
                    .of("Auto-NC").color(TextFormatting.AQUA).advance()
                    .of("]").color(TextFormatting.DARK_GRAY).advance()
                    .space()
                    .of("Automatischer Neulingschat").color(TextFormatting.DARK_AQUA).advance()
                    .of(":").color(TextFormatting.GRAY).advance()
                    .space()
                    .of(enabled ? "aktiviert" : "deaktiviert").color(enabled ? TextFormatting.GREEN : TextFormatting.RED).advance()
                    .build();

            p.sendMessage(message.toTextComponent());
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        String answer = ANSWER_MAP.get(id);
        if (answer == null) return true;

        ANSWER_MAP.remove(id);
        sendNoobChatAnswer(answer);
        return true;
    }

    @SubscribeEvent
    public static void onChatReceived(ClientChatReceivedEvent e) {
        if (!enabled) return;
        if (ConfigUtil.apiKey.isEmpty()) return;

        String text = e.getMessage().getUnformattedText();
        Matcher matcher = NOOB_CHAT_PATTERN.matcher(text);
        if (!matcher.find()) return;

        new Thread(() -> {
            String name = matcher.group(1);
            String ncMessage = matcher.group(2);

            UNiiCAResponse response = GSON.fromJson(APIUtils.post("http://tomcat.fuzzlemann.de/factiononline/uniica",
                    "apiKey", ConfigUtil.apiKey,
                    "text", ncMessage), UNiiCAResponse.class);

            String action = response.getAction();
            if (action.equals("input.unknown") || action.equals("input.welcome")) return;

            String result = response.getText();
            float confidence = response.getConfidence();

            time = System.currentTimeMillis();
            ANSWER_MAP.put(++i, result);

            Message message = Message.builder()
                    .of("[").color(TextFormatting.DARK_GRAY).advance()
                    .of("Auto-NC").color(TextFormatting.AQUA).advance()
                    .of("]").color(TextFormatting.DARK_GRAY).advance()
                    .space()
                    .of(result).color(TextFormatting.DARK_AQUA).advance()
                    .space()
                    .of("(").color(TextFormatting.DARK_GRAY).advance()
                    .of((int) confidence + "% Sicherheit").color(TextFormatting.DARK_AQUA).advance()
                    .of(")").color(TextFormatting.DARK_GRAY).advance()
                    .newLine()
                    .of("[").color(TextFormatting.GRAY).advance()
                    .of("Bestätigen").color(TextFormatting.GREEN)
                    .clickEvent(ClickEvent.Action.RUN_COMMAND, "/autonc " + i)
                    .hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.builder().of("Bestätigen").color(TextFormatting.GREEN).build()).advance()
                    .of("]").color(TextFormatting.GRAY).advance()
                    .build();

            Main.MINECRAFT.player.sendMessage(message.toTextComponent());
            Main.MINECRAFT.player.playSound(Objects.requireNonNull(SoundUtil.getSoundEvent("block.note.pling")), 1, 1);
        }).start();
    }

    @SubscribeEvent
    public static void onKeyboardClickEvent(InputEvent.KeyInputEvent e) {
        if (!enabled) return;
        if (!Main.MINECRAFT.inGameHasFocus) return;

        int key = Keyboard.getEventCharacter();
        if (key != 'g') return;

        if (System.currentTimeMillis() - time > TimeUnit.SECONDS.toMillis(30)) return;

        String answer = ANSWER_MAP.get(i);
        if (answer == null) return;

        sendNoobChatAnswer(answer);
        ANSWER_MAP.remove(i);
    }

    private static void sendNoobChatAnswer(String answer) {
        List<String> subMessages = new ArrayList<>();
        if (answer.length() <= 252) {
            subMessages.add(answer);
        } else {
            String[] words = answer.split(" ");

            StringJoiner answerJoiner = new StringJoiner(" ");
            for (int i1 = 0; i1 < words.length; i1++) {
                String word = words[i1];

                if (i1 != words.length - 1) {
                    int answerLength = answerJoiner.length() + word.length() + 1; //1 for the space

                    if (answerLength > 252) {
                        subMessages.add(answerJoiner.toString());
                        answerJoiner = new StringJoiner(" ");
                    }
                } else {
                    answerJoiner.add(word);
                    subMessages.add(answerJoiner.toString());
                    break;
                }

                answerJoiner.add(word);
            }
        }

        for (String subMessage : subMessages) {
            Main.MINECRAFT.player.sendChatMessage("/nc " + subMessage);
        }
    }

    private class UNiiCAResponse {
        public final String text;
        public final String action;
        public final int confidence;

        public UNiiCAResponse(String text, String action, int confidence) {
            this.text = text;
            this.action = action;
            this.confidence = confidence;
        }

        public String getText() {
            return text;
        }

        public String getAction() {
            return action;
        }

        public int getConfidence() {
            return confidence;
        }
    }
}
