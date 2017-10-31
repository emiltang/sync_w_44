package open.threading.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for common java.util collection interfaces that causes them
 * to become "paranoid" with regards to threading, failing immediately
 * if any two methods are called concurrently by different threads.
 *
 * @author ups
 */
@SuppressWarnings("WeakerAccess")
public class ParanoidCollections {
    /**
     * Set to true for verbose printing to the screen
     */
    public static boolean LOGGING = false;

    /**
     * Return a wrapped list with the "paranoid" behavior
     *
     * @param list the list to wrap
     * @return a paranoid list using the argument list for all its operations
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> paranoidList(List<T> list) {
        InvocationHandler handler = new ParanoidInvocationHandler(list);
        return (List<T>) Proxy.newProxyInstance(List.class.getClassLoader(), new Class[]{List.class}, handler);
    }

    /**
     * Return a wrapped set with the "paranoid" behavior
     *
     * @param set the set to wrap
     * @return a paranoid set using the argument set for all its operations
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> paranoidList(Set<T> set) {
        InvocationHandler handler = new ParanoidInvocationHandler(set);
        return (Set<T>) Proxy.newProxyInstance(Set.class.getClassLoader(), new Class[]{Set.class}, handler);
    }

    /**
     * Return a wrapped map with the "paranoid" behavior
     *
     * @param map the map to wrap
     * @return a paranoid map using the argument map for all its operations
     */
    @SuppressWarnings("unchecked")
    public static <S, T> Map<S, T> paranoidList(Map<S, T> map) {
        InvocationHandler handler = new ParanoidInvocationHandler(map);
        return (Map<S, T>) Proxy.newProxyInstance(Map.class.getClassLoader(), new Class[]{Map.class}, handler);
    }

    /**
     * Invocation handler (a proxy to the underlying collection)
     *
     * @author ups
     */
    private static class ParanoidInvocationHandler implements InvocationHandler {
        private final Object lock = new Object(); // Used for synchronization
        private Object target; // Underlying collection
        private Thread activeThread = null; // Null means no active calls
        private int reentrantLevel = 0; // Allow reentrant calls

        public ParanoidInvocationHandler(Object _target) {
            target = _target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                synchronized (lock) {
                    if (activeThread != null && Thread.currentThread() != activeThread)
                        throw new ConcurrentAccessException(method.getName());
                    activeThread = Thread.currentThread();
                    reentrantLevel++;
                    if (LOGGING)
                        System.out.println("sync " + method.getName() + " @ " + activeThread.getName() + " : " + reentrantLevel);
                }
                try {
                    return method.invoke(target, args);
                } finally {
                    synchronized (lock) {
                        if (--reentrantLevel == 0) activeThread = null;
                    }
                    if (LOGGING)
                        System.out.println("unsync " + method.getName() + " @ " + (activeThread == null ? "NULL" : (activeThread.getName() + " : " + reentrantLevel)));
                }
            } catch (InvocationTargetException exn) {
                if (exn.getCause() != null) throw exn.getCause();
                throw new Error("Internal error: Unexpected exception chaining");
            }
        }

    }

}
