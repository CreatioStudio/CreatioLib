package vip.creatio.clib.internal;

import vip.creatio.basic.tools.loader.AbstractBootstrap;
import vip.creatio.basic.tools.loader.NmsClassLoader;
import vip.creatio.basic.tools.loader.PluginInterface;

public class CLibBootstrap extends AbstractBootstrap {
    static PluginInterface creatio;

    public CLibBootstrap() {
        super(new NmsClassLoader(CLibBootstrap.class), "vip.creatio.clib.Creatio");
        if (creatio != null) throw new IllegalCallerException("Bootstrap constructor cannot be called twice!");
        creatio = super.delegate;
    }

    @Override
    protected void onInit() {
        loader.addIncludePath("vip.creatio.clib");
    }
}
