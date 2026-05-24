package com.sniklz.repelreforged.block;

import com.sniklz.repelreforged.RepelReforged;
import com.sniklz.repelreforged.block.custom.RepelBlock;
import com.sniklz.repelreforged.item.ModItems;
import com.sniklz.repelreforged.item.custom.RepelBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RepelReforged.MODID);

    public static final DeferredBlock<Block> REPEL_BLOCK =
            registerBlock("repel", () -> new RepelBlock(BlockBehaviour.Properties.of().strength(0.3f).sound(SoundType.METAL), 1), 1); // <- dont beat me for this cringe
    public static final DeferredBlock<Block> REPEL_BLOCK_1 =
            registerBlock("super_repel", () -> new RepelBlock(BlockBehaviour.Properties.of().strength(0.3f).sound(SoundType.METAL), 2),2);
    public static final DeferredBlock<Block> REPEL_BLOCK_2 =
            registerBlock("max_repel", () -> new RepelBlock(BlockBehaviour.Properties.of().strength(0.3f).sound(SoundType.METAL), 3),3);

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, int blockLevel)  {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerRebelBlockItem(name, toReturn, blockLevel);
        return toReturn;
    }

    public static <T extends Block> void registerRebelBlockItem(String name, DeferredBlock<T> block, int blockLevel) {
        ModItems.ITEMS.register(name, () -> new RepelBlockItem(block.get(), new Item.Properties(), blockLevel));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
