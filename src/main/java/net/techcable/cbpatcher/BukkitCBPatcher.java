package net.techcable.cbpatcher;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BukkitCBPatcher extends JavaPlugin {
    private final AbstractCBPatcher patcher = new AbstractCBPatcher() {
        @Override
        protected File getNativeDir() {
            return new File(getDataFolder(), "natives-v1");
        }

        @Override
        protected void log0(String s) {
            getLogger().info(s);
        }
    };

    /**
     * Inject the specified class into the original class
     *
     * @param clazz the class to inject
     */
    public static void inject(Class<?> clazz) {
        BukkitCBPatcher plugin = getPlugin(BukkitCBPatcher.class);
        AbstractCBPatcher patcher = plugin.patcher;
        patcher.injectClass(clazz);
        if (plugin.setup) {
            plugin.getLogger().warning("A plugin has registered a injector after the plugin has been loaded");
            plugin.getLogger().warning("This will reduce performance");
            patcher.transformAll();
        }
    }

    public static void addTransformListener(ClassTransformListener listener) {
        BukkitCBPatcher plugin = getPlugin(BukkitCBPatcher.class);
        AbstractCBPatcher patcher = plugin.patcher;
        patcher.addTransformListener(listener);
        if (plugin.setup) {
            plugin.getLogger().warning("A plugin has registered an injection listener after startup");
            plugin.getLogger().warning("This will reduce performance");
            patcher.transformAll();
        }
    }

    private boolean setup = false;

    @Override
    public void onEnable() {
        patcher.transformAll();
        setup = true;
    }
}
