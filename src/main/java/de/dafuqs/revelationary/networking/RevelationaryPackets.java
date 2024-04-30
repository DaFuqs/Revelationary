package de.dafuqs.revelationary.networking;

import de.dafuqs.revelationary.Revelationary;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class RevelationaryPackets {
	
	public static final Identifier REVELATION_SYNC = new Identifier(Revelationary.MOD_ID, "revelation_sync");

	// 1.20.5 port note: this whole buf-juggling thing is an obvious workaround for the fact that RevelationRegistry
	// relies heavily on reading the buffer straight up
	public record RevelationSync(PacketByteBuf bufCopy) implements CustomPayload {
		public static final PacketCodec<RegistryByteBuf, RevelationSync> CODEC = CustomPayload.codecOf(RevelationSync::write, RevelationSync::read);
		public static final CustomPayload.Id<RevelationSync> ID = new Id<>(REVELATION_SYNC);

		private static RevelationSync read(RegistryByteBuf buf) {
			// copy buffer to retain it for RevelationRegistry reading
			PacketByteBuf copy = new PacketByteBuf(buf.copy());
			// skip all the readable bytes in the original buffer to prevent decoder exception
			buf.skipBytes(buf.readableBytes());
			return new RevelationSync(copy);
		}

		private void write(RegistryByteBuf buf) {
			buf.writeBytes(bufCopy);
		}

		@Override
		public Id<RevelationSync> getId() {
			return ID;
		}
	}
}
