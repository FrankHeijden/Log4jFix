package dev.frankheijden.log4jfix.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.Chat;
import dev.frankheijden.log4jfix.common.PatternChecker;
import dev.frankheijden.log4jfix.common.ReflectionUtils;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

@Plugin(
        id = "log4jfix",
        name = "log4jfix",
        version = "%%VERSION%%",
        authors = "FrankHeijden"
)
public class Log4jFixVelocity {

    private static Field channelField;

    static {
        try {
            channelField = MinecraftConnection.class.getDeclaredField("channel");
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }

    private final ProxyServer proxy;
    private final Logger logger;

    @Inject
    public Log4jFixVelocity(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;

        // Hotload
        for (Player player : proxy.getAllPlayers()) {
            if (player instanceof ConnectedPlayer) {
                proxyChannel((ConnectedPlayer) player);
            }
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        if (player instanceof ConnectedPlayer) {
            proxyChannel((ConnectedPlayer) player);
        }
    }

    private void proxyChannel(ConnectedPlayer connectedPlayer) {
        Object proxiedChannel = Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[] { Channel.class },
                new ChannelInvocationHandler(connectedPlayer, logger)
        );

        ReflectionUtils.doPrivilegedWithUnsafe(unsafe -> {
            long offset = unsafe.objectFieldOffset(channelField);
            unsafe.putObject(connectedPlayer.getConnection(), offset, proxiedChannel);
        });
    }

    public static class ChannelInvocationHandler implements InvocationHandler {

        private final ConnectedPlayer connectedPlayer;
        private final Channel channel;
        private final Logger logger;

        public ChannelInvocationHandler(ConnectedPlayer connectedPlayer, Logger logger) {
            this.connectedPlayer = connectedPlayer;
            this.channel = connectedPlayer.getConnection().getChannel();
            this.logger = logger;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ((methodName.equals("write") || methodName.equals("writeAndFlush")) && args.length > 0) {
                if (!canWrite(args[0])) {
                    return null;
                }
            }
            return method.invoke(channel, args);
        }

        private boolean canWrite(Object obj) {
            if (obj instanceof Chat) {
                Chat chat = (Chat) obj;
                if (PatternChecker.isExploit(chat.getMessage())) {
                    logger.severe("Prevented log4j exploit from being sent to " + connectedPlayer.getUsername());
                    return false;
                }
            }
            return true;
        }
    }
}
