package fr.pandacube.so_long.players;

import fr.pandacube.lib.paper.players.PaperOnlinePlayer;
import fr.pandacube.lib.paper.reflect.wrapper.craftbukkit.CraftPlayer;
import fr.pandacube.lib.paper.reflect.wrapper.minecraft.network.FriendlyByteBuf;
import fr.pandacube.lib.paper.reflect.wrapper.minecraft.network.protocol.ClientboundCustomPayloadPacket;
import fr.pandacube.lib.paper.reflect.wrapper.minecraft.network.protocol.Packet;
import fr.pandacube.lib.paper.reflect.wrapper.netty.Unpooled;
import fr.pandacube.lib.reflect.wrapper.ReflectWrapper;
import org.bukkit.entity.Player;

public class OnlinePlayer extends OffPlayer implements PaperOnlinePlayer {

    private final Player player;

    public OnlinePlayer(Player p) {
        super(p.getUniqueId());
        player = p;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public String getServerName() {
        return null;
    }



    public CraftPlayer getWrappedCraftPlayer() {
        return ReflectWrapper.wrapTyped(player, CraftPlayer.class);
    }

    @Override
    public OnlinePlayer getOnlineInstance() {
        return this;
    }





    /*
     * Sending packet and stuff to player
     */

    @Override
    public void sendServerBrand(String brand) {
        try {
            sendNMSPacket(new ClientboundCustomPayloadPacket(
                    ClientboundCustomPayloadPacket.BRAND(),
                    new FriendlyByteBuf(Unpooled.buffer()).writeUtf(brand)
            ));
        } catch(Exception ignored) { }
    }

    public void sendNMSPacket(Packet nmsPacket) {
        getWrappedCraftPlayer().getHandle().connection().send(nmsPacket);
    }





    /*
     * Client options
     */

    @Override
    public PaperClientOptions getClientOptions() {
        return new PaperClientOptionsImpl();
    }

    private class PaperClientOptionsImpl extends PaperClientOptions {

        public PaperClientOptionsImpl() {
            super(OnlinePlayer.this);
        }

        @Override
        public boolean isTextFilteringEnabled() {
            // TODO when this will be possible without reflexion, move this into PaperClientOptions in pandalib-paper-players
            return getWrappedCraftPlayer().getHandle().isTextFilteringEnabled();
        }
    }

}
