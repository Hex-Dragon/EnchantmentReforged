package com.hexdragon.enchre.mixin.item;

import com.hexdragon.corere.item.EnchantmentHelperRe;
import com.hexdragon.enchre.registry.RegMain;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.FileReader;
import java.util.Map;

@Mixin(Item.class)
public abstract class ItemMixin extends net.minecraftforge.registries.ForgeRegistryEntry<net.minecraft.item.Item> implements IItemProvider, net.minecraftforge.common.extensions.IForgeItem {

    // 为物品名称添加附魔等级：例如为带有时运 II 的镐重命名为 “铁镐 +2”
    @Shadow public String getTranslationKey(ItemStack stack) {return null;}
    @Overwrite public ITextComponent getDisplayName(ItemStack stack) {
        int level = EnchantmentHelperRe.getEnchantmentLevel(stack);
        if (level > 0) {
            return new StringTextComponent(new TranslationTextComponent(this.getTranslationKey(stack)).getString() + " +" + level);
        } else {
            return new TranslationTextComponent(this.getTranslationKey(stack));
        }
    }

}
