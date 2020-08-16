package cn.mcres.luckyfish.antileakaccount.storage;

import cn.mcres.luckyfish.antileakaccount.AntiLeakAccount;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeeStorage extends PlayerStorage {
    private final List<UUID> verifiedPlayers = new ArrayList<>();

    public BungeeStorage() {
        super();
    }

    @Override
    protected void init() {
        Bukkit.getMessenger().registerIncomingPluginChannel(AntiLeakAccount.getInstance(), "ala:message", new BungeeCommunicator());
        Bukkit.getMessenger().registerOutgoingPluginChannel(AntiLeakAccount.getInstance(), "ala:message");
    }

    @Override
    public void addVerifiedPlayer(Player player) {
        ByteArrayDataOutput bado = ByteStreams.newDataOutput();
        bado.writeUTF("add");
        bado.writeLong(player.getUniqueId().getMostSignificantBits());
        bado.writeLong(player.getUniqueId().getLeastSignificantBits());
        player.sendPluginMessage(AntiLeakAccount.getInstance(), "ala:message", bado.toByteArray());
    }

    @Override
    public boolean isPlayerVerified(Player player) {
        if (verifiedPlayers.contains(player.getUniqueId())) {
            return true;
        }

        ByteArrayDataOutput bado = ByteStreams.newDataOutput();
        bado.writeUTF("fetch");
        bado.writeLong(player.getUniqueId().getMostSignificantBits());
        bado.writeLong(player.getUniqueId().getLeastSignificantBits());
        player.sendPluginMessage(AntiLeakAccount.getInstance(), "ala:message", bado.toByteArray());

        return false;
    }

    @Override
    public void save() {
    }

    private class BungeeCommunicator implements PluginMessageListener {
        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {
            ByteArrayDataInput badi = ByteStreams.newDataInput(message);
            if (badi.readUTF().equals("result")) {
                UUID uid = new UUID(badi.readLong(), badi.readLong());
                if (badi.readBoolean()) {
                    verifiedPlayers.add(uid);
                }
            }
        }
    }
}
