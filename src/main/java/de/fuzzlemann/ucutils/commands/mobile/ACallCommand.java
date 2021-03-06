package de.fuzzlemann.ucutils.commands.mobile;

import de.fuzzlemann.ucutils.base.abstraction.UPlayer;
import de.fuzzlemann.ucutils.base.command.Command;
import de.fuzzlemann.ucutils.utils.mobile.MobileUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Fuzzlemann
 */
@SideOnly(Side.CLIENT)
public class ACallCommand {

    @Command(value = "acall", usage = "/%label% [Spieler]", async = true)
    public boolean onCommand(UPlayer p, String target) {
        int number = MobileUtils.getNumber(target);
        if (number == -1) return true;

        p.sendChatMessage("/call " + number);
        return true;
    }
}
