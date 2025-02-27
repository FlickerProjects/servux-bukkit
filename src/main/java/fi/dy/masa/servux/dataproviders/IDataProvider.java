package fi.dy.masa.servux.dataproviders;

import fi.dy.masa.servux.network.IPluginChannelHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public interface IDataProvider
{
    /**
     * Returns the simple name for this data provider.
     * This should preferably be a lower case alphanumeric string with no
     * other special characters than '-' and '_'.
     * This name will be used in the enable/disable commands as the argument
     * and also as the config file key/identifier.
     * @return
     */
    String getName();

    /**
     * Returns the description of this data provider.
     * Used in the command to list the available providers and to check the status
     * of a given provider.
     * @return
     */
    String getDescription();

    /**
     * Returns the network channel name used by this data provider to listen
     * for incoming data requests and to respond and send the requested data.
     * @return
     */
    ResourceLocation getNetworkChannel();

    /**
     * Returns the current protocol version this provider supports
     * @return
     */
    int getProtocolVersion();

    /**
     * Returns true if this data provider is currently enabled.
     * @return
     */
    boolean isEnabled();

    /**
     * Enables or disables this data provider
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Returns whether or not this data provider should get ticked to periodically send some data,
     * or if it's only listening for incoming requests and responds to them directly.
     * @return
     */
    default boolean shouldTick()
    {
        return false;
    }

    /**
     * Returns the interval in game ticks that this data provider should be ticked at
     * @return
     */
    int getTickRate();

    /**
     * Called at the given tick rate
     * @param server
     * @param tickCounter The current server tick (since last server start)
     */
    default void tick(MinecraftServer server, int tickCounter)
    {
    }

    /**
     * Returns the network packet handler used for this data provider.
     * @return
     */
    IPluginChannelHandler getPacketHandler();
}
