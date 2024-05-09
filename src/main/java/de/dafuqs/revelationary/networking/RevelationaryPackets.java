package de.dafuqs.revelationary.networking;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.dafuqs.revelationary.Revelationary;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.util.Map;

public class RevelationaryPackets {
	
	public static final Identifier REVELATION_SYNC = new Identifier(Revelationary.MOD_ID, "revelation_sync");

	public record RevelationSync(Object2ObjectOpenHashMap<Identifier, ObjectArrayList<BlockState>> advToBlockStates,
                                 Object2ObjectOpenHashMap<BlockState, Identifier> blockStateToAdv,
                                 Object2ObjectOpenHashMap<BlockState, BlockState> blockStateCloaks,
                                 Object2ObjectOpenHashMap<Block, Block> blockCloaks,
                                 Object2ObjectOpenHashMap<Identifier, ObjectArrayList<Item>> advToItems,
                                 Object2ObjectOpenHashMap<Item, Identifier> itemToAdv,
                                 Object2ObjectOpenHashMap<Item, Item> itemCloaks,
                                 Object2ObjectOpenHashMap<Block, MutableText> cloakedBlockNameTranslations,
                                 Object2ObjectOpenHashMap<Item, MutableText> cloakedItemNameTranslations) implements CustomPayload {
		public static final PacketCodec<RegistryByteBuf, RevelationSync> CODEC = CustomPayload.codecOf(RevelationSync::write, RevelationSync::read);
		public static final CustomPayload.Id<RevelationSync> ID = new Id<>(REVELATION_SYNC);

		private static void writeText(RegistryByteBuf buf, Text text) {
			TextCodecs.REGISTRY_PACKET_CODEC.encode(buf, text);
		}

		private static Text readText(RegistryByteBuf buf) {
			return TextCodecs.REGISTRY_PACKET_CODEC.decode(buf);
		}

		public static RevelationSync read(RegistryByteBuf buf) {
			/* Block States */

			final Object2ObjectOpenHashMap<Block, Block> blockCloaks = new Object2ObjectOpenHashMap<>(buf.readInt());
			final Object2ObjectOpenHashMap<BlockState, Identifier> blockStateToAdv = new Object2ObjectOpenHashMap<>(buf.readInt());
			final Object2ObjectOpenHashMap<BlockState, BlockState> blockStateCloaks = new Object2ObjectOpenHashMap<>(buf.readInt());
			int blockEntries = buf.readInt();
			final Object2ObjectOpenHashMap<Identifier, ObjectArrayList<BlockState>> advToBlockStates = new Object2ObjectOpenHashMap<>(blockEntries);
			for (int i = 0; i < blockEntries; i++) {
				Identifier advancementIdentifier = buf.readIdentifier();
				int blockStateCount = buf.readInt();
				ObjectArrayList<BlockState> advancementStates = new ObjectArrayList<>(blockStateCount);
				for (int j = 0; j < blockStateCount; j++) {
					try {
						BlockState sourceState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), buf.readString(), true).blockState();
						BlockState targetState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), buf.readString(), true).blockState();

						advancementStates.add(sourceState);
						blockStateToAdv.put(sourceState, advancementIdentifier);
						blockStateCloaks.put(sourceState, targetState);
						blockCloaks.putIfAbsent(sourceState.getBlock(), targetState.getBlock());
					} catch (CommandSyntaxException e) {
						Revelationary.logError(e.getMessage());
					};
				}
				advToBlockStates.put(advancementIdentifier, advancementStates);
			}

			/* Items */

			final Object2ObjectOpenHashMap<Item, Identifier> itemToAdv = new Object2ObjectOpenHashMap<>(buf.readInt());
			Object2ObjectOpenHashMap<Item, Item> itemCloaks = new Object2ObjectOpenHashMap<>(buf.readInt());
			int itemEntries = buf.readInt();
			final Object2ObjectOpenHashMap<Identifier, ObjectArrayList<Item>> advToItems = new Object2ObjectOpenHashMap<>(itemEntries); // preallocate this map too
			for (int i = 0; i < itemEntries; i++) {
				Identifier advancementIdentifier = buf.readIdentifier();
				int itemCount = buf.readInt();
				ObjectArrayList<Item> advancementItems = new ObjectArrayList<>(itemCount);
				for (int j = 0; j < itemCount; j++) {
					Identifier sourceId = Identifier.tryParse(buf.readString());
					Identifier targetId = Identifier.tryParse(buf.readString());
					Item sourceItem = Registries.ITEM.get(sourceId);
					Item targetItem = Registries.ITEM.get(targetId);

					advancementItems.add(sourceItem);
					itemToAdv.put(sourceItem, advancementIdentifier);
					itemCloaks.put(sourceItem, targetItem);
				}
				advToItems.put(advancementIdentifier, advancementItems);
			}

			/* Block Translations */
			int blockTranslations = buf.readInt();
			final Object2ObjectOpenHashMap<Block, MutableText> cloakedBlockNameTranslations = new Object2ObjectOpenHashMap<>(blockTranslations); // preallocate translations
			for (int i = 0; i < blockTranslations; i++) {
				Block block = Registries.BLOCK.get(buf.readIdentifier());
				MutableText text = (MutableText) readText(buf);
				cloakedBlockNameTranslations.put(block, text);
			}

			/* Item Translations */
			int itemTranslations = buf.readInt();
			final Object2ObjectOpenHashMap<Item, MutableText> cloakedItemNameTranslations = new Object2ObjectOpenHashMap<>(itemTranslations); // preallocate translations
			for (int i = 0; i < itemTranslations; i++) {
				Item item = Registries.ITEM.get(buf.readIdentifier());
				MutableText text = (MutableText) readText(buf);
				cloakedItemNameTranslations.put(item, text);
			}
			return new RevelationSync(advToBlockStates,
									  blockStateToAdv,
									  blockStateCloaks,
									  blockCloaks,
									  advToItems,
									  itemToAdv,
									  itemCloaks,
									  cloakedBlockNameTranslations,
									  cloakedItemNameTranslations);
		}

		public void write(RegistryByteBuf buf) {
			// Block States
			buf.writeInt(blockCloaks.size());      // for preallocation on packet read
			buf.writeInt(blockStateToAdv.size());  // for preallocation on packet read
			buf.writeInt(blockStateCloaks.size()); // for preallocation on packet read
			buf.writeInt(advToBlockStates.size());
			for (Map.Entry<Identifier, ObjectArrayList<BlockState>> advancementBlocks : advToBlockStates.entrySet()) {
				buf.writeIdentifier(advancementBlocks.getKey());
				buf.writeInt(advancementBlocks.getValue().size());
				for (BlockState blockState : advancementBlocks.getValue()) {
					buf.writeString(BlockArgumentParser.stringifyBlockState(blockState));
					buf.writeString(BlockArgumentParser.stringifyBlockState(blockStateCloaks.get(blockState)));
				}
			}

			// Items
			buf.writeInt(itemToAdv.size());  // for preallocation on packet read
			buf.writeInt(itemCloaks.size()); // for preallocation on packet read
			buf.writeInt(advToItems.size());
			for (Map.Entry<Identifier, ObjectArrayList<Item>> advancementItems : advToItems.entrySet()) {
				buf.writeIdentifier(advancementItems.getKey());
				buf.writeInt(advancementItems.getValue().size());
				for (Item item : advancementItems.getValue()) {
					buf.writeString(Registries.ITEM.getId(item).toString());
					buf.writeString(Registries.ITEM.getId(itemCloaks.get(item)).toString());
				}
			}

			// Block Translations
			buf.writeInt(cloakedBlockNameTranslations.size());
			for (Map.Entry<Block, MutableText> blockTranslation : cloakedBlockNameTranslations.entrySet()) {
				buf.writeIdentifier(Registries.BLOCK.getId(blockTranslation.getKey()));
				writeText(buf, blockTranslation.getValue());
			}

			// Item Translations
			buf.writeInt(cloakedItemNameTranslations.size());
			for (Map.Entry<Item, MutableText> itemTranslation : cloakedItemNameTranslations.entrySet()) {
				buf.writeIdentifier(Registries.ITEM.getId(itemTranslation.getKey()));
				writeText(buf, itemTranslation.getValue());
			}
		}

		@Override
		public Id<RevelationSync> getId() {
			return ID;
		}
	}
}
