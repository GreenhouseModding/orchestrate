package dev.greenhouseteam.orchestrate.network.serverbound;

import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record UpdateNameServerboundPacket(String name) implements CustomPacketPayload {
    public static final ResourceLocation ID = Orchestrate.asResource("update_name");
    public static final Type<UpdateNameServerboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNameServerboundPacket> STREAM_CODEC = StreamCodec.of(UpdateNameServerboundPacket::write, UpdateNameServerboundPacket::new);

    public UpdateNameServerboundPacket(RegistryFriendlyByteBuf buf) {
        this(ByteBufCodecs.STRING_UTF8.decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, UpdateNameServerboundPacket packet) {
        ByteBufCodecs.STRING_UTF8.encode(buf, packet.name);
    }

    public void handle(ServerPlayer player) {
        player.server.execute(() -> {
            if (player.containerMenu instanceof CompositionMenu menu) {
                if (!menu.stillValid(player))
                    return;

                menu.setName(name);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
