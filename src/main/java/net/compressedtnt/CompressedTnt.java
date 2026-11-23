package net.compressedtnt;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressedTnt implements ModInitializer {
        public static final String MOD_ID = "compressedtnt";
        public static final Logger LOGGER = LoggerFactory.getLogger("CompressedTnt");

        public static final int TIERS = 10;
        public static final String[] BLOCK_IDS = {
                        "compressed_tnt", "double_compressed_tnt", "triple_compressed_tnt",
                        "quadruple_compressed_tnt", "quintuple_compressed_tnt", "sextuple_compressed_tnt",
                        "septuple_compressed_tnt", "octuple_compressed_tnt", "nonuple_compressed_tnt",
                        "decuple_compressed_tnt"
        };

        public static final CompressedTntBlock[] BLOCKS = new CompressedTntBlock[TIERS];

        /** fuse timer (in seconds) stored on the ITEM via the G-key input screen */
        public static final ComponentType<Integer> TIMER_SECONDS = ComponentType.<Integer>builder()
                        .codec(Codec.intRange(0, 3600))
                        .packetCodec(PacketCodecs.VAR_INT)
                        .build();

        /** registered after the blocks, in onInitialize */
        public static BlockEntityType<CompressedTntBlockEntity> BLOCK_ENTITY_TYPE;

        public static final EntityType<CompressedTntEntity> PRIMED_COMPRESSED_TNT = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "primed_compressed_tnt"),
                        FabricEntityTypeBuilder.<CompressedTntEntity>create(SpawnGroup.MISC, CompressedTntEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.98f, 0.98f))
                                        .build());

        @Override
        public void onInitialize() {
                Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "timer_seconds"), TIMER_SECONDS);

                for (int i = 0; i < TIERS; i++) {
                        int tier = i + 1;
                        CompressedTntBlock block = new CompressedTntBlock(tier,
                                        AbstractBlock.Settings.create().strength(0.0f, 0.0f).sounds(BlockSoundGroup.GRASS));
                        Identifier id = Identifier.of(MOD_ID, BLOCK_IDS[i]);
                        Registry.register(Registries.BLOCK, id, block);
                        Registry.register(Registries.ITEM, id, new CompressedTntBlockItem(block, tier, new Item.Settings()));
                        BLOCKS[i] = block;
                }

                BLOCK_ENTITY_TYPE = Registry.register(
                                Registries.BLOCK_ENTITY_TYPE,
                                Identifier.of(MOD_ID, "compressed_tnt"),
                                BlockEntityType.Builder.create(CompressedTntBlockEntity::new, BLOCKS).build(null));

                RegistryKey<net.minecraft.item.ItemGroup> redstoneTab = RegistryKey.of(
                                RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("redstone"));
                ItemGroupEvents.modifyEntriesEvent(redstoneTab).register(
                                entries -> {
                                        for (CompressedTntBlock block : BLOCKS) {
                                                entries.add(block);
                                        }
                                });

                // keyboard timer: client screen sends the seconds, server stores them
                // on the held compressed TNT item
                PayloadTypeRegistry.playC2S().register(SetTimerPayload.ID, SetTimerPayload.CODEC);
                ServerPlayNetworking.registerGlobalReceiver(SetTimerPayload.ID, (payload, context) -> {
                        ServerPlayerEntity player = context.player();
                        int seconds = MathHelper.clamp(payload.seconds(), 0, 3600);
                        ItemStack main = player.getMainHandStack();
                        ItemStack off = player.getOffHandStack();
                        ItemStack target = main.getItem() instanceof CompressedTntBlockItem ? main
                                        : off.getItem() instanceof CompressedTntBlockItem ? off : null;
                        if (target == null) {
                                return;
                        }
                        if (seconds > 0) {
                                target.set(TIMER_SECONDS, seconds);
                                player.sendMessage(Text.translatable("compressedtnt.timer_set_item", seconds).formatted(Formatting.GOLD), true);
                        } else {
                                target.remove(TIMER_SECONDS);
                                player.sendMessage(Text.translatable("compressedtnt.timer_removed").formatted(Formatting.YELLOW), true);
                        }
                });

                LOGGER.info("TNT Minecraft loaded: {} tiers (9x each), tier 10 = {}x TNT power, radius {} blocks",
                                TIERS, multiplierOf(TIERS), (int) CompressedTntEntity.radiusOf(TIERS));
        }

        public static long multiplierOf(int tier) {
                long multiplier = 1;
                for (int i = 0; i < tier; i++) {
                        multiplier *= 9L;
                }
                return multiplier;
        }

        public static float powerOf(int tier) {
                return (float) (4L * multiplierOf(tier));
        }
}
