package net.compressedtnt;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
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
        public static final MaterialTntBlock WOOD_TNT = new MaterialTntBlock(MaterialTntBlock.Type.WOOD, settings());

        public static final TunnelTntBlock TUNNEL_TNT_H =
                        new TunnelTntBlock.Horizontal(settings());
        public static final TunnelTntBlock TUNNEL_TNT_V =
                        new TunnelTntBlock.Vertical(settings());

        public static final EntityType<CompressedTntEntity> PRIMED_COMPRESSED_TNT = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "primed_compressed_tnt"),
                        EntityType.Builder.<CompressedTntEntity>create(CompressedTntEntity::new, SpawnGroup.MISC)
                                        .dimensions(0.98f, 0.98f)
                                        .build("primed_compressed_tnt"));
        public static final EntityType<MaterialTntEntity> MATERIAL_TNT = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "material_tnt"),
                        EntityType.Builder.<MaterialTntEntity>create(MaterialTntEntity::new, SpawnGroup.MISC)
                                        .dimensions(0.98f, 0.98f)
                                        .build("material_tnt"));
        public static final EntityType<TunnelTntEntity> TUNNEL_TNT = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "tunnel_tnt"),
                        EntityType.Builder.<TunnelTntEntity>create(TunnelTntEntity::new, SpawnGroup.MISC)
                                        .dimensions(0.98f, 0.98f)
                                        .build("tunnel_tnt"));

        public static final EntityType<FriendlyCreeperEntity> FRIENDLY_CREEPER_PINK = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "friendly_creeper_pink"),
                        EntityType.Builder.<FriendlyCreeperEntity>create(FriendlyCreeperEntity::new, SpawnGroup.CREATURE)
                                        .dimensions(0.6f, 1.7f)
                                        .build("friendly_creeper_pink"));
        public static final EntityType<FriendlyCreeperEntity> FRIENDLY_CREEPER_BLUE = Registry.register(
                        Registries.ENTITY_TYPE,
                        Identifier.of(MOD_ID, "friendly_creeper_blue"),
                        EntityType.Builder.<FriendlyCreeperEntity>create(FriendlyCreeperEntity::new, SpawnGroup.CREATURE)
                                        .dimensions(0.6f, 1.7f)
                                        .build("friendly_creeper_blue"));

        public static final Item PINK_CREEPER_SPAWN_EGG = Registry.register(
                        Registries.ITEM,
                        Identifier.of(MOD_ID, "pink_creeper_spawn_egg"),
                        new SpawnEggItem(FRIENDLY_CREEPER_PINK, 0xF06FB0, 0x5CAD46, new Item.Settings()));
        public static final Item BLUE_CREEPER_SPAWN_EGG = Registry.register(
                        Registries.ITEM,
                        Identifier.of(MOD_ID, "blue_creeper_spawn_egg"),
                        new SpawnEggItem(FRIENDLY_CREEPER_BLUE, 0x4A90D9, 0x5CAD46, new Item.Settings()));

        /** data component added by the 8-TNT-around-item recipe */
        public static final net.minecraft.component.ComponentType<TntInfusedComponent> TNT_INFUSED = Registry.register(
                        Registries.DATA_COMPONENT_TYPE,
                        Identifier.of(MOD_ID, "tnt_infused"),
                        net.minecraft.component.ComponentType.<TntInfusedComponent>builder()
                                        .codec(TntInfusedComponent.CODEC)
                                        .packetCodec(TntInfusedComponent.PACKET_CODEC)
                                        .build());
        public static final net.minecraft.recipe.RecipeSerializer<TntInfuseRecipe> TNT_INFUSE_SERIALIZER = Registry.register(
                        Registries.RECIPE_SERIALIZER,
                        Identifier.of(MOD_ID, "tnt_infuse"),
                        new net.minecraft.recipe.SpecialRecipeSerializer<>(TntInfuseRecipe::new));

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
                registerMaterial(WOOD_TNT, "wood_tnt");
                registerTunnel(TUNNEL_TNT_H, "tunnel_tnt_horizontal");
                registerTunnel(TUNNEL_TNT_V, "tunnel_tnt_vertical");

                FabricDefaultAttributeRegistry.register(FRIENDLY_CREEPER_PINK, FriendlyCreeperEntity.createFriendlyCreeperAttributes());
                FabricDefaultAttributeRegistry.register(FRIENDLY_CREEPER_BLUE, FriendlyCreeperEntity.createFriendlyCreeperAttributes());

                RegistryKey<net.minecraft.item.ItemGroup> redstoneTab = RegistryKey.of(
                                RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("redstone"));
                ItemGroupEvents.modifyEntriesEvent(redstoneTab).register(entries -> {
                        for (CompressedTntBlock block : BLOCKS) {
                                entries.add(block);
                        }
                        entries.add(DIRT_TNT);
                        entries.add(STONE_TNT);
                        entries.add(CROP_TNT);
                        entries.add(WOOD_TNT);
                        entries.add(TUNNEL_TNT_H);
                        entries.add(TUNNEL_TNT_V);
                });
                RegistryKey<net.minecraft.item.ItemGroup> spawnEggTab = RegistryKey.of(
                                RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("spawn_eggs"));
                ItemGroupEvents.modifyEntriesEvent(spawnEggTab).register(entries -> {
                        entries.add(PINK_CREEPER_SPAWN_EGG);
                        entries.add(BLUE_CREEPER_SPAWN_EGG);
                });

                // friendly creepers spawn in daylight like cows and sheep
                BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE,
                                FRIENDLY_CREEPER_PINK, 5, 1, 2);
                BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE,
                                FRIENDLY_CREEPER_BLUE, 5, 1, 2);

                FuseNet.registerCodecs();
                FuseHoldTracker.registerServerReceiver();

                // right-click ANY placed TNT (vanilla or modded) -> open the fuse timer screen
                UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                        if (player.isSpectator() || player.isSneaking()) {
                                return ActionResult.PASS;
                        }
                        BlockState state = world.getBlockState(hitResult.getBlockPos());
                        if (!(state.getBlock() instanceof TntBlock)) {
                                return ActionResult.PASS;
                        }
                        if (player.getStackInHand(hand).isOf(net.minecraft.item.Items.FLINT_AND_STEEL)
                                        || player.getStackInHand(hand).isOf(net.minecraft.item.Items.FIRE_CHARGE)) {
                                return ActionResult.PASS; // normal ignition
                        }
                        if (world instanceof ServerWorld server && player instanceof ServerPlayerEntity serverPlayer) {
                                FuseHoldTracker.openFuseScreen(serverPlayer, server, hitResult.getBlockPos());
                        }
                        return ActionResult.SUCCESS;
                });

                // TNT-infused TOOLS work normally, but every use has a 4-8% chance
                // of a small (~5 block) explosion
                PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
                        if (world instanceof ServerWorld server && !player.isCreative()
                                        && player.getMainHandStack().contains(TNT_INFUSED)
                                        && server.random.nextFloat() < blastChance(server)) {
                                server.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                                3.0f, false, World.ExplosionSourceType.TNT);
                        }
                });
                AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
                        if (world instanceof ServerWorld server && hand == Hand.MAIN_HAND && !player.isCreative()
                                        && player.getStackInHand(hand).contains(TNT_INFUSED)
                                        && server.random.nextFloat() < blastChance(server)) {
                                server.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(),
                                                3.0f, false, World.ExplosionSourceType.TNT);
                        }
                        return ActionResult.PASS;
                });

                // unified timer: /tntfuse <seconds> as a keyboard backup for the GUI
                CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                                CommandManager.literal("tntfuse")
                                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0, FuseHoldTracker.MAX_SECONDS))
                                                                .executes(context -> {
                                                                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                                        ServerWorld world = player.getServerWorld();
                                                                        int seconds = IntegerArgumentType.getInteger(context, "seconds");
                                                                        if (FuseHoldTracker.primeFromCommand(player, world, seconds)) {
                                                                                player.sendMessage(Text.translatable("compressedtnt.fuse_set", seconds)
                                                                                                .formatted(Formatting.GREEN), true);
                                                                                return 1;
                                                                        }
                                                                        context.getSource().sendError(Text.translatable("compressedtnt.fuse_no_target"));
                                                                        return 0;
                                                                }))));

                ServerTickEvents.END_SERVER_TICK.register(server -> FuseHoldTracker.endServerTick(server));

                LOGGER.info("TNT Minecraft loaded: {} compressed tiers (9x volume each), 4 material TNTs (dirt/stone/crop/wood), "
                                + "2 tunnel TNTs, TNT-infused gear (8 TNT + item), friendly bow-tie creepers, GUI fuse timer for every TNT",
                                TIERS);
        }

        /** 4-8% per use */
        private static float blastChance(ServerWorld world) {
                return 0.04f + world.random.nextFloat() * 0.04f;
        }

        private static void registerMaterial(MaterialTntBlock block, String id) {
                Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, id), block);
                Registry.register(Registries.ITEM, Identifier.of(MOD_ID, id),
                                new MaterialTntBlockItem(block, new Item.Settings()));
        }

        private static void registerTunnel(TunnelTntBlock block, String id) {
                Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, id), block);
                Registry.register(Registries.ITEM, Identifier.of(MOD_ID, id),
                                new TunnelTntBlockItem(block, new Item.Settings()));
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
