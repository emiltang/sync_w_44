package open.threading.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Various testcases for paranoid collections
 *
 * @author ups
 */
@SuppressWarnings("WeakerAccess")
public class TestParanoidCollections {

    public static void testReentrantOK() {
        System.out.println("***OK:");
        ReentrantListTest rlt = new ReentrantListTest();
        List<String> ls = ParanoidCollections.paranoidList(rlt);
        rlt.setCallback(ls);
        System.out.println(ls.get(0));
        System.out.println(ls.get(1));
        System.out.println(ls.get(7));
    }

    public static void testReentrantBAD() {
        System.out.println("***BAD:");
        BorkenReentrantListTest brlt = new BorkenReentrantListTest();
        List<String> bls = ParanoidCollections.paranoidList(brlt);
        brlt.setCallback(bls);
        System.out.println(bls.get(0));
        System.out.println(bls.get(1));
        System.out.println(bls.get(7));

    }

    public static void main(String argv[]) {
        ParanoidCollections.LOGGING = true;
        testReentrantOK();
        testReentrantBAD();
    }

    @SuppressWarnings("serial")
    static class ReentrantListTest extends ArrayList<String> {
        private List<String> callback;

        public void setCallback(List<String> _callback) {
            this.callback = _callback;
        }

        @Override
        public String get(int index) {
            if (index > 0) return callback.get(index - 1);
            return "OK";
        }
    }

    @SuppressWarnings("serial")
    static class BorkenReentrantListTest extends ArrayList<String> {
        private List<String> callback;

        public void setCallback(List<String> _callback) {
            this.callback = _callback;
        }

        @Override
        public String get(int index) {
            if (index > 0) {
                Thread t = new Thread(() -> callback.get(index));
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new Error("Fatal interruption");
                }
            }
            return "OK";
        }
    }
}
