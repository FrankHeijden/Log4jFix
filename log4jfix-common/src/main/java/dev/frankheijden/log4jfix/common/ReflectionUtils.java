package dev.frankheijden.log4jfix.common;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Consumer;

public class ReflectionUtils {

    private static MethodHandle theUnsafeFieldMethodHandle;

    static {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            theUnsafeFieldMethodHandle = MethodHandles.lookup().unreflectGetter(theUnsafeField);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private ReflectionUtils() {}

    /**
     * Performs a privileged action while accessing {@link Unsafe}.
     */
    public static void doPrivilegedWithUnsafe(Consumer<Unsafe> privilegedAction) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                privilegedAction.accept((Unsafe) theUnsafeFieldMethodHandle.invoke());
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return null;
        });
    }
}
