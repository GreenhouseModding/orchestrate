package dev.greenhouseteam.orchestrate.network.serverbound;

import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record UpdateAuthorServerboundPacket(String author) implements CustomPacketPayload {
    public static final ResourceLocation ID = Orchestrate.asResource("update_author");
    public static final Type<UpdateAuthorServerboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAuthorServerboundPacket> STREAM_CODEC = StreamCodec.of(UpdateAuthorServerboundPacket::write, UpdateAuthorServerboundPacket::new);

    public UpdateAuthorServerboundPacket(RegistryFriendlyByteBuf buf) {
        this(ByteBufCodecs.STRING_UTF8.decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, UpdateAuthorServerboundPacket packet) {
        ByteBufCodecs.STRING_UTF8.encode(buf, packet.author);
    }

    public void handle(ServerPlayer player) {
        player.server.execute(() -> {
            if (player.containerMenu instanceof CompositionMenu menu) {
                if (!menu.stillValid(player))
                    return;

                menu.setAuthor(author);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
