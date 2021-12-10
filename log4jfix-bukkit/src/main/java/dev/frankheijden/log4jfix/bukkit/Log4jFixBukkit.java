package dev.frankheijden.log4jfix.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.frankheijden.log4jfix.common.PatternChecker;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public class Log4jFixBukkit extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.CHAT, PacketType.Play.Client.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerChat wrapper = new WrapperPlayServerChat(event.getPacket());
                WrappedChatComponent component = wrapper.getMessage();
                if (component == null) return;
                String message = TextComponent.toPlainText(ComponentSerializer.parse(component.getJson()));

                if (PatternChecker.isExploit(message)) {
                    event.setCancelled(true);
                    event.setPacket(null);
                }
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientChat wrapper = new WrapperPlayClientChat(event.getPacket());
                if (PatternChecker.isExploit(wrapper.getMessage())) {
                    getLogger().severe(event.getPlayer().getName() + " attempted the log4j exploit.");
                    event.setCancelled(true);
                    event.setPacket(null);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }
}
