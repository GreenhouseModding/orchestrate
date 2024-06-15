package dev.greenhouseteam.orchestrate.network.serverbound;

import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import dev.greenhouseteam.orchestrate.song.Note;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record UpdateNotesServerboundPacket(int channel, List<Note> notes) implements CustomPacketPayload {
    public static final ResourceLocation ID = Orchestrate.asResource("update_notes");
    public static final Type<UpdateNotesServerboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNotesServerboundPacket> STREAM_CODEC = StreamCodec.of(UpdateNotesServerboundPacket::write, UpdateNotesServerboundPacket::new);

    public UpdateNotesServerboundPacket(RegistryFriendlyByteBuf buf) {
        this(ByteBufCodecs.INT.decode(buf), Note.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, UpdateNotesServerboundPacket packet) {
        ByteBufCodecs.INT.encode(buf, packet.channel);
        Note.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, packet.notes);
    }

    public void handle(ServerPlayer player) {
        player.server.execute(() -> {
            if (player.containerMenu instanceof CompositionMenu menu) {
                if (!menu.stillValid(player))
                    return;

                menu.setNotes(channel, notes);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
