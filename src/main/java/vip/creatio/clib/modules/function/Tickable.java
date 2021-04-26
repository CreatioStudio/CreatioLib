package vip.creatio.clib.modules.function;

/**
 * A special type of Runnable interface, which means to be call every Minecraft tick
 */
public interface Tickable extends Runnable {

    void tick();

    @Override
    default void run() {
        tick();
    }
}
