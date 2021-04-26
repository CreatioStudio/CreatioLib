package vip.creatio.clib.test.expired;

import org.junit.Test;

public class CppExternalTest {

    static {
        System.loadLibrary("ClibNatives");
    }

    private static native String replace(String src, String target, String replacement);

    public static String replaceAll(String src, String target, String replacement) {
        return replace(src, target, replacement);
    }

        @Test
    public void test() {
        String a = "dod<Replace>ded";
        String b = "toPlace";
        String c = "Replace";
        System.out.println(replace(a,c,b));
    }
}
