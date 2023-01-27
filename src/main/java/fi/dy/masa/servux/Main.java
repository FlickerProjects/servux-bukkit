package fi.dy.masa.servux;

import cn.apisium.papershelled.annotation.Mixin;
import cn.apisium.papershelled.plugin.PaperShelledPlugin;
import cn.apisium.papershelled.plugin.PaperShelledPluginDescription;
import cn.apisium.papershelled.plugin.PaperShelledPluginLoader;
import fi.dy.masa.servux.dataproviders.DataProviderManager;
import fi.dy.masa.servux.dataproviders.StructureDataProvider;
import fi.dy.masa.servux.mixin.IMixinCustomPayloadC2SPacket;
import fi.dy.masa.servux.mixin.MixinMinecraftServer;
import fi.dy.masa.servux.mixin.MixinServerPlayNetworkHandler;
import fi.dy.masa.servux.mixin.MixinThreadedAnvilChunkStorage;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@SuppressWarnings("unused")
@Plugin(name = "Servux", version = "0.1.0")
@Description("Server-side support and integration for masa's client mods")
@Author("masa")
@Website("https://github.com/maruohon/servux")
@ApiVersion(ApiVersion.Target.v1_17)
@Mixin({
        IMixinCustomPayloadC2SPacket.class,
        MixinMinecraftServer.class,
        MixinServerPlayNetworkHandler.class,
        MixinThreadedAnvilChunkStorage.class
})
public class Main extends PaperShelledPlugin
{
    public static Logger logger;

    public Main(@NotNull PaperShelledPluginLoader loader, @NotNull PaperShelledPluginDescription paperShelledDescription, @NotNull PluginDescriptionFile description, @NotNull File file) {
        super(loader, paperShelledDescription, description, file);
    }

    @Override
    public void onLoad()
    {
        logger = getLog4JLogger();
        DataProviderManager.INSTANCE.registerDataProvider(StructureDataProvider.INSTANCE);
        DataProviderManager.INSTANCE.readFromConfig();
    }

}
