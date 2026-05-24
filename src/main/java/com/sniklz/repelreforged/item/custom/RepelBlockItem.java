package com.sniklz.repelreforged.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;

import com.sniklz.repelreforged.RepelReforged;

public class RepelBlockItem extends BlockItem {
    @SuppressWarnings("unused")
    private final String repelType;
    public static int RANGE;
    private final int BlockLevel;
    public static HashMap<String, Integer> MULTIPLIERS = new HashMap<>();

    public RepelBlockItem(Block block, Properties properties, int blockLevel) {
        super(block, properties);
        this.BlockLevel = blockLevel;
        switch (blockLevel) {
            case 1: this.repelType = "repel"; break;
            case 2: this.repelType = "super_repel"; break;
            case 3: this.repelType = "max_repel"; break;
            default: this.repelType = "repel";
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 1. Get the level from the context safely
        Level level = context.level();
        if (level == null) return; // Tooltip might be requested where level isn't available

        // 2. Fetch the values directly from the source of truth
        int baseRange = level.getGameRules().getInt(RepelReforged.REPEL_RANGE);
        
        int multiplier = switch (this.BlockLevel) {
            case 2 -> level.getGameRules().getInt(RepelReforged.SUPER_REPEL_RANGE_MULTIPLIER);
            case 3 -> level.getGameRules().getInt(RepelReforged.MAX_REPEL_RANGE_MULTIPLIER);
            default -> 1;
        };

        int totalRange = baseRange * multiplier;
        
        if (totalRange > 0) {
            Component rangeText = Component.literal(String.valueOf(totalRange)).withStyle(ChatFormatting.LIGHT_PURPLE);
            tooltip.add(Component.translatable("message.repelreforged.repel_tooltip", rangeText).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("message.repelreforged.repel_right_click_tooltip").withStyle(ChatFormatting.GRAY));
    }
}
