package com.hexdragon.enchre.mixin.block;
import com.hexdragon.enchre.block.enchantingtable.EnchantmentContainerRe;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import javax.annotation.Nullable;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends ContainerBlock {
    protected EnchantingTableBlockMixin(Properties builder) {super(builder);}

    // 将附魔台的处理事件替换为 Mod 所提供的事件
    @Overwrite @Nullable public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof EnchantingTableTileEntity) {
            return new SimpleNamedContainerProvider((id, inventory, player) -> new EnchantmentContainerRe(id, inventory, IWorldPosCallable.of(worldIn, pos)), new TranslationTextComponent("gui.enchanting.title"));
        } else {
            return null;
        }
    }

}
