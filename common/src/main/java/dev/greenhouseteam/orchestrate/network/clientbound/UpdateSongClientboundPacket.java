package dev.greenhouseteam.orchestrate.network.clientbound;

import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.client.screen.CompositionScreen;
import dev.greenhouseteam.orchestrate.song.Song;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateSongClientboundPacket(int channel, Song song) implements CustomPacketPayload {
    public static final ResourceLocation ID = Orchestrate.asResource("update_song");
    public static final Type<UpdateSongClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSongClientboundPacket> STREAM_CODEC = StreamCodec.of(UpdateSongClientboundPacket::write, UpdateSongClientboundPacket::new);

    public UpdateSongClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(ByteBufCodecs.INT.decode(buf), Song.STREAM_CODEC.decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, UpdateSongClientboundPacket packet) {
        ByteBufCodecs.INT.encode(buf, packet.channel);
        Song.STREAM_CODEC.encode(buf, packet.song);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof CompositionScreen screen) {
                screen.fromNotes(channel, song.notes());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
