package dev.greenhouseteam.orchestrate.network.clientbound;

import dev.greenhouseteam.mib.data.MibSoundSet;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.client.util.OrchestrateClientUtil;
import dev.greenhouseteam.orchestrate.song.Song;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record OrchestrateStartPlayingClientboundPacket(int entityId, boolean offhand, Song song, Holder<MibSoundSet> set) implements CustomPacketPayload {
    public static final ResourceLocation ID = Orchestrate.asResource("start_playing");
    public static final Type<OrchestrateStartPlayingClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OrchestrateStartPlayingClientboundPacket> STREAM_CODEC = StreamCodec.of(OrchestrateStartPlayingClientboundPacket::write, OrchestrateStartPlayingClientboundPacket::new);

    public OrchestrateStartPlayingClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean(), Song.STREAM_CODEC.decode(buf), MibSoundSet.STREAM_CODEC.decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, OrchestrateStartPlayingClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeBoolean(packet.offhand);
        Song.STREAM_CODEC.encode(buf, packet.song);
        MibSoundSet.STREAM_CODEC.encode(buf, packet.set);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (!(entity instanceof Player player))
                return;
            InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

            OrchestrateClientUtil.playSong(player, hand, song, set.value());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
