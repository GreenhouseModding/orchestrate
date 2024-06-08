package dev.greenhouseteam.orchestrate.network.serverbound;

import dev.greenhouseteam.orchestrate.Orchestrate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record OrchestrateStartPlayingServerboundPacket(ItemStack stack, boolean offhand) implements CustomPacketPayload {
    public static final ResourceLocation ID = Orchestrate.asResource("start_playing");
    public static final Type<OrchestrateStartPlayingServerboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OrchestrateStartPlayingServerboundPacket> STREAM_CODEC = StreamCodec.of(OrchestrateStartPlayingServerboundPacket::write, OrchestrateStartPlayingServerboundPacket::new);

    public OrchestrateStartPlayingServerboundPacket(RegistryFriendlyByteBuf buf) {
        this(ItemStack.STREAM_CODEC.decode(buf), buf.readBoolean());
    }

    public static void write(RegistryFriendlyByteBuf buf, OrchestrateStartPlayingServerboundPacket packet) {
        ItemStack.STREAM_CODEC.encode(buf, packet.stack);
        buf.writeBoolean(packet.offhand);
    }

    public void handle(ServerPlayer player) {
        player.server.execute(() -> {
            InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            Orchestrate.playSong(player, stack, hand);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
