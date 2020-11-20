package com.hexdragon.enchre.mixin.item;

import com.hexdragon.enchre.registry.RegMain;
import net.minecraft.block.BlockState;
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

@Mixin(Item.class)
public abstract class ItemMixin extends net.minecraftforge.registries.ForgeRegistryEntry<net.minecraft.item.Item> implements IItemProvider, net.minecraftforge.common.extensions.IForgeItem {

    // 为物品名称添加附魔等级
    @Shadow public String getTranslationKey(ItemStack stack) {return "";}
    @Overwrite public ITextComponent getDisplayName(ItemStack stack) {
        return new StringTextComponent(new TranslationTextComponent(this.getTranslationKey(stack)).getString() + "test");
    }

}
