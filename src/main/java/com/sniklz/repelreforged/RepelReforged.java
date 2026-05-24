package com.sniklz.repelreforged;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.spawning.position.FishingSpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.sniklz.repelreforged.block.ModBlocks;
import com.sniklz.repelreforged.block.custom.RepelBlock;
import com.sniklz.repelreforged.item.ModItems;
import com.sniklz.repelreforged.item.custom.RepelBlockItem;

import kotlin.Unit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.GameRules;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mod(RepelReforged.MODID)
public class RepelReforged {

    public static final String MODID = "repelreforged";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MODID);
    public static final GameRules.Key<GameRules.IntegerValue> REPEL_RANGE = GameRules.register("RepelBaseRange", 
        GameRules.Category.SPAWNING, 
        GameRules.IntegerValue.create(32, (server, rule) -> {
            int val = rule.get();
            if (val < 0) rule.set(0, server);
            else if (val > 512) rule.set(512, server);
            RepelBlockItem.RANGE = rule.get();
        })
    );
    public static final GameRules.Key<GameRules.IntegerValue> SUPER_REPEL_RANGE_MULTIPLIER = GameRules.register("RepelRangeSuperMultiplier", 
        GameRules.Category.SPAWNING, 
        GameRules.IntegerValue.create(2, (server, rule) -> {
            int val = rule.get();
            if (val < 1) rule.set(1, server);
            else if (val > 512) rule.set(10, server);
            RepelBlockItem.MULTIPLIERS.put("super_repel", rule.get());
        })
    );
    public static final GameRules.Key<GameRules.IntegerValue> MAX_REPEL_RANGE_MULTIPLIER = GameRules.register("RepelRangeMaxMultiplier", 
        GameRules.Category.SPAWNING, 
        GameRules.IntegerValue.create(3, (server, rule) -> {
            int val = rule.get();
            if (val < 1) rule.set(1, server);
            else if (val > 512) rule.set(10, server);
            RepelBlockItem.MULTIPLIERS.put("max_repel", rule.get());
        })
    );

    public static final Holder<PoiType> REPEL_POI =
        POI_TYPES.register("repel", () ->
            new PoiType(
                Stream.of(
                    ModBlocks.REPEL_BLOCK.get(),
                    ModBlocks.REPEL_BLOCK_1.get(),
                    ModBlocks.REPEL_BLOCK_2.get())
                    .flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
                    .collect(Collectors.toSet()), 1, 1)
        );

    public static void registerPoI(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        var gameRules = server.getGameRules();

        // Syncing the game rules to your static variables
        RepelBlockItem.RANGE = gameRules.getInt(REPEL_RANGE);
        RepelBlockItem.MULTIPLIERS.put("super_repel", gameRules.getInt(SUPER_REPEL_RANGE_MULTIPLIER));
        RepelBlockItem.MULTIPLIERS.put("max_repel", gameRules.getInt(MAX_REPEL_RANGE_MULTIPLIER));
    }

    public RepelReforged(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        registerPoI(modEventBus);
        modEventBus.addListener(this::addCreative);
        
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGHEST, event -> {
            SpawnablePosition spawnablePosition = event.getSpawnablePosition();
            ServerLevel world = spawnablePosition.getWorld();
            PokemonEntity Pokemon = event.getEntity();
            // Only skip fishing spawns, Pokesnack spawns and the gamerule kill-switch.
            // CobbleBosses spawns bypass this event entirely and are handled by ServerWorldMixin instead.
            String causeName = spawnablePosition.getSpawner().getInfluences().stream().map(influence -> influence.getClass().getSimpleName()).collect(Collectors.joining(", "));
            //RepelReforged.LOGGER.info("Spawn Cause: " + causeName);
            if (event.isCanceled() || 
            world.getGameRules().getInt(REPEL_RANGE) == 0 ||
            spawnablePosition instanceof FishingSpawnablePosition || Pokemon.isUncatchable() ||
            causeName.contains("PokeSnackBlockEntity")) 
            {
                return Unit.INSTANCE;
            }

            BlockPos spawnPos = spawnablePosition.getPosition();
            if (isRepelNearby(world, spawnPos)) 
            {
                event.cancel();
            }

            return Unit.INSTANCE;
        });
    }

    public static boolean isRepelNearby(ServerLevel world, BlockPos pos) {
        int repelRange = world.getGameRules().getInt(REPEL_RANGE);
        int superMultiplier = world.getGameRules().getInt(SUPER_REPEL_RANGE_MULTIPLIER);
        int maxMultiplier = world.getGameRules().getInt(MAX_REPEL_RANGE_MULTIPLIER);
        int maxRange = repelRange * Math.max(superMultiplier, maxMultiplier);

        return world.getPoiManager().getInSquare(
                poi -> poi.is(REPEL_POI.unwrapKey().orElseThrow()),
                pos,
                maxRange,
                PoiManager.Occupancy.ANY
            ).anyMatch(matchPos -> {
            if (world.getBlockState(matchPos.getPos()).getBlock() instanceof RepelBlock repelBlock) {
                int repelLevel = repelBlock.getBlockLevel();
                int xRange = Math.abs(matchPos.getPos().getX() - pos.getX());
                int yRange = Math.abs(matchPos.getPos().getY() - pos.getY());
                int zRange = Math.abs(matchPos.getPos().getZ() - pos.getZ());
                if (repelLevel >= 1 && xRange < repelRange && yRange < repelRange && zRange < repelRange) 
                {
                    return true;
                } 
                else 
                {
                    int superRepelRange = repelRange * superMultiplier;
                    if (repelLevel >= 2 && xRange < superRepelRange && yRange < superRepelRange && zRange < superRepelRange)
                    {
                        return true;
                    } 
                    else 
                    {
                        return repelLevel >= 3;
                    }
                }
            }
            return false;
        });
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.REPEL_BLOCK);
            event.accept(ModBlocks.REPEL_BLOCK_1);
            event.accept(ModBlocks.REPEL_BLOCK_2);
        }
    }


}
