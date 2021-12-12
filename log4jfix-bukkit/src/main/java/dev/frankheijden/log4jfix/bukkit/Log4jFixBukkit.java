package dev.frankheijden.log4jfix.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.frankheijden.log4jfix.common.PatternChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public class Log4jFixBukkit extends JavaPlugin {

    private static final boolean adventure;
    static {
        boolean hasAdventureComponentClass;
        try {
            Class.forName("net.kyori.adventure.text.Component");
            hasAdventureComponentClass = true;
        } catch (ClassNotFoundException ex) {
            hasAdventureComponentClass = false;
        }
        adventure = hasAdventureComponentClass;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.CHAT, PacketType.Play.Client.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                for (int i = 0; i < event.getPacket().getModifier().size(); i++) {
                    Object modifier = event.getPacket().getModifier().read(i);
                    if (adventure && modifier instanceof Component) {
                        if (PatternChecker.isExploit(GsonComponentSerializer.gson().serialize((Component) modifier))
                                || PatternChecker.isExploit(PlainComponentSerializer.plain().serialize((Component) modifier))) {
                            event.setCancelled(true);
                            return;
                        }
                    } else if (modifier instanceof BaseComponent[]) {
                        if (PatternChecker.isExploit(ComponentSerializer.toString((BaseComponent[]) modifier))
                                || PatternChecker.isExploit(BaseComponent.toPlainText((BaseComponent[]) modifier))) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                for (int i = 0; i < event.getPacket().getChatComponents().size(); i++) {
                    WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(i);
                    if (chatComponent == null)
                        continue;

                    String json = chatComponent.getJson();
                    if (PatternChecker.isExploit(json)
                            || PatternChecker.isExploit(toPlainText(json))) {
                        event.setCancelled(true);
                        return;
                    }
                }
                for (int i = 0; i < event.getPacket().getChatComponentArrays().size(); i++) {
                    WrappedChatComponent[] chatComponents = event.getPacket().getChatComponentArrays().read(i);
                    if (chatComponents == null || chatComponents.length == 0)
                        continue;

                    for (WrappedChatComponent chatComponent : chatComponents) {
                        String json = chatComponent.getJson();
                        if (PatternChecker.isExploit(json)
                                || PatternChecker.isExploit(toPlainText(json))) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientChat wrapper = new WrapperPlayClientChat(event.getPacket());
                if (PatternChecker.isExploit(wrapper.getMessage())) {
                    getLogger().severe(event.getPlayer().getName() + " attempted the log4j exploit.");
                    event.setCancelled(true);
                    PacketContainer packet = new PacketContainer(PacketType.Play.Client.CHAT);
                    event.setPacket(packet);
                }
            }
        });
    }

    private String toPlainText(String json) {
        if (adventure) {
            return PlainComponentSerializer.plain().serialize(GsonComponentSerializer.gson().deserialize(json));
        } else {
            return BaseComponent.toPlainText(ComponentSerializer.parse(json));
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }
}
