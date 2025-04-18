package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket implements Packet<ClientGamePacketListener> {
    private final Item item;
    private final int duration;

    public ClientboundCooldownPacket(Item p_132000_, int p_132001_) {
        this.item = p_132000_;
        this.duration = p_132001_;
    }

    public ClientboundCooldownPacket(FriendlyByteBuf p_178831_) {
        this.item = p_178831_.readById(BuiltInRegistries.ITEM);
        this.duration = p_178831_.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf p_132010_) {
        p_132010_.writeId(BuiltInRegistries.ITEM, this.item);
        p_132010_.writeVarInt(this.duration);
    }

    public void handle(ClientGamePacketListener p_132007_) {
        p_132007_.handleItemCooldown(this);
    }

    public Item getItem() {
        return this.item;
    }

    public int getDuration() {
        return this.duration;
    }
}
