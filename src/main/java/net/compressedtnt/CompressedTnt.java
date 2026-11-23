package net.compressedtnt;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

        public static final MaterialTntBlock DIRT_TNT = new MaterialTntBlock(MaterialTntBlock.Type.DIRT, settings());
        public static final MaterialTntBlock STONE_TNT = new MaterialTntBlock(MaterialTntBlock.Type.STONE, settings());
        public static final MaterialTntBlock CROP_TNT = new MaterialTntBlock(MaterialTntBlock.Type.CROP, settings());

        public static final EntityType<CompressedTntEntity> PRIMED_COMPRESSED_TNT = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "primed_compressed_tnt"),
                        FabricEntityTypeBuilder.<CompressedTntEntity>create(SpawnGroup.MISC, CompressedTntEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.98f, 0.98f))
                                        .build());

        public static final EntityType<MaterialTntEntity> MATERIAL_TNT = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "material_tnt"),
                        FabricEntityTypeBuilder.<MaterialTntEntity>create(SpawnGroup.MISC, MaterialTntEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.98f, 0.98f))
                                        .build());

        private static AbstractBlock.Settings settings() {
                return AbstractBlock.Settings.create().strength(0.0f, 0.0f).sounds(BlockSoundGroup.GRASS);
        }

        @Override
        public void onInitialize() {
                for (int i = 0; i < TIERS; i++) {
                        int tier = i + 1;
                        CompressedTntBlock block = new CompressedTntBlock(tier, settings());
                        Identifier id = Identifier.of(MOD_ID, BLOCK_IDS[i]);
                        Registry.register(Registries.BLOCK, id, block);
                        Registry.register(Registries.ITEM, id, new CompressedTntBlockItem(block, tier, new Item.Settings()));
                        BLOCKS[i] = block;
                }

                registerMaterial(DIRT_TNT, "dirt_tnt");
                registerMaterial(STONE_TNT, "stone_tnt");
                registerMaterial(CROP_TNT, "crop_tnt");

                RegistryKey<net.minecraft.item.ItemGroup> redstoneTab = RegistryKey.of(
                                RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("redstone"));
                ItemGroupEvents.modifyEntriesEvent(redstoneTab).register(entries -> {
                        for (CompressedTntBlock block : BLOCKS) {
                                entries.add(block);
                        }
                        entries.add(DIRT_TNT);
                        entries.add(STONE_TNT);
                        entries.add(CROP_TNT);
                });

                // unified timer: hold right-click ~1 s on any TNT, then /tntfuse <seconds>
                CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                                CommandManager.literal("tntfuse")
                                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1, 600))
                                                                .executes(context -> {
                                                                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                                        ServerWorld world = player.getServerWorld();
                                                                        int seconds = IntegerArgumentType.getInteger(context, "seconds");
                                                                        if (FuseHoldTracker.primeFromCommand(player, world, seconds)) {
                                                                                player.sendMessage(Text.translatable("compressedtnt.fuse_set", seconds), false);
                                                                                return 1;
                                                                        }
                                                                        context.getSource().sendError(Text.translatable("compressedtnt.fuse_no_target"));
                                                                        return 0;
                                                                }))));

                ServerTickEvents.END_SERVER_TICK.register(server -> FuseHoldTracker.endServerTick(server));

                LOGGER.info("TNT Minecraft loaded: {} compressed tiers (9x volume each: radii 8/17/36/75/128), 3 material TNTs (dirt/stone/crop)",
                                TIERS);
        }

        private static void registerMaterial(MaterialTntBlock block, String id) {
                Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, id), block);
                Registry.register(Registries.ITEM, Identifier.of(MOD_ID, id),
                                new MaterialTntBlockItem(block, new Item.Settings()));
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
