package com.hexdragon.enchre.block.enchantingtable;
import com.hexdragon.enchre.registry.RegMain;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

public class EnchantmentContainerRe extends Container {

    // 物品栏与数据存储
    private final IInventory tableInventory = new Inventory(2) {
        public void markDirty() {
            super.markDirty();
            EnchantmentContainerRe.this.onCraftMatrixChanged(this);
        }
    };
    private final IWorldPosCallable worldPosCallable;
    private final Random rand = new Random();
    private final IntReferenceHolder xpSeed = IntReferenceHolder.single();

    // 构造函数
    public EnchantmentContainerRe(int id, PlayerInventory playerInventory) {this(id, playerInventory, IWorldPosCallable.DUMMY);}
    public EnchantmentContainerRe(int id, PlayerInventory playerInventory, IWorldPosCallable worldPosCallable) {
        super(RegMain.containerEnchantment.get(), id);
        this.worldPosCallable = worldPosCallable;

        // 追踪数据变化，以自动 makeDirty
        this.trackInt(IntReferenceHolder.create(this.enchantLevels, 0));
        this.trackInt(IntReferenceHolder.create(this.enchantLevels, 1));
        this.trackInt(IntReferenceHolder.create(this.enchantLevels, 2));
        this.trackInt(this.xpSeed).set(playerInventory.player.getXPSeed());
        this.trackInt(IntReferenceHolder.create(this.guiEnchantId, 0));
        this.trackInt(IntReferenceHolder.create(this.guiEnchantId, 1));
        this.trackInt(IntReferenceHolder.create(this.guiEnchantId, 2));
        this.trackInt(IntReferenceHolder.create(this.guiEnchantLevel, 0));
        this.trackInt(IntReferenceHolder.create(this.guiEnchantLevel, 1));
        this.trackInt(IntReferenceHolder.create(this.guiEnchantLevel, 2));

        // 添加被附魔物品格
        this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
            public boolean isItemValid(ItemStack stack) {
                return true;
            }
            public int getSlotStackLimit() {
                return 1;
            }
        });
        // 添加青金石物品格
        this.addSlot(new Slot(this.tableInventory, 1, 15, 17) {
            public boolean isItemValid(ItemStack stack) {
                return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(stack.getItem());
            }
        });
        // 添加背包物品格
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

    }

    // 附魔参数
    public final int[] enchantLevels = new int[3]; // 显示在右下角的每个附魔选项的等级
    public final int[] guiEnchantId = new int[]{-1, -1, -1}; // GUI 指向中显示的附魔 ID
    public final int[] guiEnchantLevel = new int[]{-1, -1, -1}; // GUI 指向中显示的附魔等级

    // 更新附魔选项的等级与 GUI 显示信息
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        if (inventoryIn != this.tableInventory) return;

        // 检查输入物品
        ItemStack inputItem = inventoryIn.getStackInSlot(0);
        if (inputItem.isEmpty() || !inputItem.isEnchantable()) {
            for(int i = 0; i < 3; ++i) {
                this.enchantLevels[i] = 0;
                this.guiEnchantId[i] = -1;
                this.guiEnchantLevel[i] = -1;
            }
            return;
        }

        this.worldPosCallable.consume((world, pos) -> {

            // 获取书架提供的总附魔能力值
            int power = 0;
            for(int k = -1; k <= 1; ++k) {
                for(int l = -1; l <= 1; ++l) {
                    // TODO : 让火把、雪等方块不会干扰书架的附魔能力值传递（由于代码判断间隔方块必须是空气）
                    if ((k != 0 || l != 0) && world.isAirBlock(pos.add(l, 0, k)) && world.isAirBlock(pos.add(l, 1, k))) {
                        power += getPower(world, pos.add(l * 2, 0, k * 2));
                        power += getPower(world, pos.add(l * 2, 1, k * 2));
                        if (l != 0 && k != 0) {
                            power += getPower(world, pos.add(l * 2, 0, k));
                            power += getPower(world, pos.add(l * 2, 1, k));
                            power += getPower(world, pos.add(l, 0, k * 2));
                            power += getPower(world, pos.add(l, 1, k * 2));
                        }
                    }
                }
            }

            // 设置随机数种子
            this.rand.setSeed(this.xpSeed.get());

            // 获取附魔
            for(int id = 0; id < 3; ++id) {
                // 清空其他参数
                this.guiEnchantId[id] = -1;
                this.guiEnchantLevel[id] = -1;
                // 根据物品与附魔编号，获取各个附魔的预计等级
                this.enchantLevels[id] = EnchantmentHelper.calcItemStackEnchantability(this.rand, id, power, inputItem);
                if (this.enchantLevels[id] < id + 1)  this.enchantLevels[id] = 0;
                this.enchantLevels[id] = net.minecraftforge.event.ForgeEventFactory.onEnchantmentLevelSet(world, pos, id, power, inputItem, enchantLevels[id]);
                if (this.enchantLevels[id] <= 0) continue;
                // 根据等级，获取实际会附加的附魔列表
                List<EnchantmentData> list = this.getEnchantmentList(inputItem, id, this.enchantLevels[id]);
                if (list.isEmpty()) continue;
                // 从附魔列表中抽取一项用于 GUI 显示
                EnchantmentData enchantmentdata = list.get(this.rand.nextInt(list.size()));
                this.guiEnchantId[id] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
                this.guiEnchantLevel[id] = enchantmentdata.enchantmentLevel;
            }

            // 提交更改
            this.detectAndSendChanges();

        });
    }

    // 实际为物品进行附魔（服务端触发）
    public boolean enchantItem(PlayerEntity playerIn, int id) {
        ItemStack inputItem = this.tableInventory.getStackInSlot(0);
        ItemStack lapisItem = this.tableInventory.getStackInSlot(1);

        // 前置条件检查
        int cost = id + 1; // 青金石与等级花费
        if ((lapisItem.isEmpty() || lapisItem.getCount() < cost) && !playerIn.abilities.isCreativeMode) {
            // 检查青金石是否足够
            return false;
        } else if (this.enchantLevels[id] <= 0 || inputItem.isEmpty() || (playerIn.experienceLevel < cost || playerIn.experienceLevel < this.enchantLevels[id]) && !playerIn.abilities.isCreativeMode) {
            // 检查玩家等级是否足够
            return false;
        }
        // 检查准备附魔的列表是否为空
        this.worldPosCallable.consume((world, pos) -> {
            ItemStack outputItem = inputItem;
            List<EnchantmentData> enchList = this.getEnchantmentList(inputItem, id, this.enchantLevels[id]); // 附魔列表
            if (enchList.isEmpty()) return;

            // 如果输入为书，则将输出替换为附魔书
            boolean isBook = inputItem.getItem() == Items.BOOK;
            if (isBook) {
                outputItem = new ItemStack(Items.ENCHANTED_BOOK);
                CompoundNBT compoundnbt = inputItem.getTag();
                if (compoundnbt != null) outputItem.setTag(compoundnbt.copy());
                this.tableInventory.setInventorySlotContents(0, outputItem);
            }
            // 为输出物品添加附魔
            for(int j = 0; j < enchList.size(); ++j) {
                EnchantmentData enchantmentdata = enchList.get(j);
                if (isBook) {
                    EnchantedBookItem.addEnchantment(outputItem, enchantmentdata);
                } else {
                    outputItem.addEnchantment(enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
                }
            }
            // 扣除玩家等级，并重置附魔随机种子
            playerIn.onEnchant(inputItem, cost);
            // 扣除青金石
            if (!playerIn.abilities.isCreativeMode) {
                lapisItem.shrink(cost);
                if (lapisItem.isEmpty()) this.tableInventory.setInventorySlotContents(1, ItemStack.EMPTY);
            }

            // 改变统计信息，触发 Trigger
            playerIn.addStat(Stats.ENCHANT_ITEM);
            if (playerIn instanceof ServerPlayerEntity) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity)playerIn, outputItem, cost);
            }
            // 更新结果
            this.tableInventory.markDirty();
            this.xpSeed.set(playerIn.getXPSeed());
            this.onCraftMatrixChanged(this.tableInventory);
            world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);

        });
        return true;
    }

    // 为 [ItemStack、附魔位与等级] 提供固定的 [实际附魔列表] 输出
    // 其中没有随机因素，相同输入会给出固定的输出（这基于玩家的一个种子）
    private List<EnchantmentData> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {
        this.rand.setSeed(this.xpSeed.get() + enchantSlot);
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(this.rand, stack, level, false);
        if (stack.getItem() == Items.BOOK && list.size() > 1) list.remove(this.rand.nextInt(list.size()));
        return list;
    }

    /*********************************
     *       无需修改的代码
     ********************************/

    // 获取某个方块的附魔能力值
    private float getPower(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
        return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
    }

    // 获取青金石数量
    @OnlyIn(Dist.CLIENT) public int getLapisAmount() {
        ItemStack itemstack = this.tableInventory.getStackInSlot(1);
        return itemstack.isEmpty() ? 0 : itemstack.getCount();
    }

    // 获取 xpSeed
    @OnlyIn(Dist.CLIENT) public int getXpSeed() {
        return this.xpSeed.get();
    }

    // 在容器关闭时返还物品
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.worldPosCallable.consume((p_217004_2_, p_217004_3_) -> {
            this.clearContainer(playerIn, playerIn.world, this.tableInventory);
        });
    }

    // 玩家是否能与其交互
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(this.worldPosCallable, playerIn, Blocks.ENCHANTING_TABLE);
    }

    // 按 Shift+左键 时转移物品
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 1) {
                if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.getItem() == Items.LAPIS_LAZULI) {
                if (!this.mergeItemStack(itemstack1, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.inventorySlots.get(0).getHasStack() || !this.inventorySlots.get(0).isItemValid(itemstack1)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack2 = itemstack1.copy();
                itemstack2.setCount(1);
                itemstack1.shrink(1);
                this.inventorySlots.get(0).putStack(itemstack2);
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

}
