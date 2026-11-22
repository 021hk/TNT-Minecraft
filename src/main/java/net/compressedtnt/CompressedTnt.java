package net.compressedtnt;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
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

        public static final EntityType<CompressedTntEntity> PRIMED_COMPRESSED_TNT = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "primed_compressed_tnt"),
                        FabricEntityTypeBuilder.<CompressedTntEntity>create(SpawnGroup.MISC, CompressedTntEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.98f, 0.98f))
                                        .build());

        @Override
        public void onInitialize() {
                for (int i = 0; i < TIERS; i++) {
                        int tier = i + 1;
                        CompressedTntBlock block = new CompressedTntBlock(tier,
                                        AbstractBlock.Settings.create().strength(0.0f, 0.0f).sounds(BlockSoundGroup.GRASS));
                        Identifier id = Identifier.of(MOD_ID, BLOCK_IDS[i]);
                        Registry.register(Registries.BLOCK, id, block);
                        Registry.register(Registries.ITEM, id, new CompressedTntBlockItem(block, tier, new Item.Settings()));
                        BLOCKS[i] = block;
                }

                RegistryKey<net.minecraft.item.ItemGroup> redstoneTab = RegistryKey.of(
                                RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("redstone"));
                ItemGroupEvents.modifyEntriesEvent(redstoneTab).register(
                                entries -> {
                                        for (CompressedTntBlock block : BLOCKS) {
                                                entries.add(block);
                                        }
                                });

                // keyboard fuse setting: client sends +/- deltas while holding a
                // compressed TNT item; the server applies them to the item's data
                PayloadTypeRegistry.playC2S().register(SetFusePayload.ID, SetFusePayload.CODEC);
                ServerPlayNetworking.registerGlobalReceiver(SetFusePayload.ID, (payload, context) -> {
                        ServerPlayerEntity player = context.player();
                        net.minecraft.item.ItemStack stack = player.getMainHandStack();
                        if (!(stack.getItem() instanceof CompressedTntBlockItem)) {
                                stack = player.getOffHandStack();
                        }
                        if (stack.getItem() instanceof CompressedTntBlockItem) {
                                int next = MathHelper.clamp(
                                                CompressedTntBlockItem.getFuseSeconds(stack) + payload.delta(),
                                                CompressedTntBlockItem.MIN_FUSE_SECONDS,
                                                CompressedTntBlockItem.MAX_FUSE_SECONDS);
                                CompressedTntBlockItem.setFuseSeconds(stack, next);
                                player.sendMessage(Text.translatable("compressedtnt.fuse_set", next), true);
                        }
                });

                LOGGER.info("Compressed TNT loaded: {} tiers, tier 10 = {}x TNT power", TIERS, multiplierOf(TIERS));
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
