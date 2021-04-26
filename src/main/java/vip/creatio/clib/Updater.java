package vip.creatio.clib;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;

public class Updater implements Runnable {

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int resourceId = 85307;
        final Consumer<String> consumer = version -> {
            String present = Creatio.getBootstrap().getDescription().getVersion();
            if (require_update(version, present)) {
                Creatio.getSender().sendStatic(Level.INFO, "MAIN.UPDATE", present, version);
            }
        };
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream();
             Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                consumer.accept(scanner.next());
            }
        } catch (IOException exception) {
            Creatio.intern("&8&l[&b&lCreatio&3&lLib&8&l] &4Cannot found update: " + exception.getMessage());
        }
    }

    private boolean require_update(String ver1, String ver2) {
        float[] Ver1 = getVersion(ver1);
        float[] Ver2 = getVersion(ver2);
        for (int k = 0; k < Ver1.length; k++) {
            if (Ver1[k] > Ver2[k]) return true;
        }
        return false;
    }

    private float[] getVersion(String a) {
        String[] split = a.split("-")[0].split("\\.");
        float[] result = new float[split.length];
        int k = 0;
        for (String val : split) {
            result[k] = Float.parseFloat(val);
            k++;
        }
        return result;
    }
}
