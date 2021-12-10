package dev.frankheijden.log4jfix.bungee;

import dev.frankheijden.log4jfix.common.PatternChecker;
import dev.frankheijden.log4jfix.common.ReflectionUtils;
import io.netty.channel.Channel;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.Chat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Log4jFixBungee extends Plugin implements Listener {

    private static MethodHandle channelWrapperHandle;
    private static Field channelField;

    static {
        try {
            Field channelWrapperField = UserConnection.class.getDeclaredField("ch");
            channelWrapperField.setAccessible(true);
            channelWrapperHandle = MethodHandles.lookup().unreflectGetter(channelWrapperField);
            channelField = ChannelWrapper.class.getDeclaredField("ch");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getProxy().getPluginManager().registerListener(this, this);

        // Hot load
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player instanceof UserConnection) {
                proxyChannel((UserConnection) player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player instanceof UserConnection) {
            proxyChannel((UserConnection) player);
        }
    }

    private void proxyChannel(UserConnection userConnection) {
        ChannelWrapper ch;
        try {
            ch = (ChannelWrapper) channelWrapperHandle.invoke(userConnection);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return;
        }

        Object proxiedChannel = Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[] { Channel.class },
                new ChannelInvocationHandler(userConnection, ch.getHandle())
        );

        ReflectionUtils.doPrivilegedWithUnsafe(unsafe -> {
            long offset = unsafe.objectFieldOffset(channelField);
            unsafe.putObject(ch, offset, proxiedChannel);
        });
    }

    public class ChannelInvocationHandler implements InvocationHandler {

        private final UserConnection userConnection;
        private final Channel channel;

        public ChannelInvocationHandler(UserConnection userConnection, Channel channel) {
            this.userConnection = userConnection;
            this.channel = channel;
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
                String message = TextComponent.toPlainText(ComponentSerializer.parse(chat.getMessage()));

                if (PatternChecker.isExploit(message)) {
                    getLogger().severe("Prevented log4j exploit from being sent to " + userConnection.getName());
                    return false;
                }
            }
            return true;
        }
    }
}
