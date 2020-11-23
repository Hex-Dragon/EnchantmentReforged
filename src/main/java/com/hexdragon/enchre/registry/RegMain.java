package com.hexdragon.enchre.registry;

import com.hexdragon.enchre.Main;
import com.hexdragon.enchre.block.enchantingtable.EnchantmentContainerRe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RegMain {

    // Container
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Main.MODID);
    public static final RegistryObject<ContainerType<EnchantmentContainerRe>> containerEnchantment = CONTAINERS.register("enchantment", () -> IForgeContainerType.create((int windowId, PlayerInventory inv, PacketBuffer data) -> new EnchantmentContainerRe(windowId, inv)));

    // 附魔
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Main.MODID);
    // public static final RegistryObject<Enchantment> enchDecay = ENCHANTMENTS.register("decay_curse", DecayCurseEnchantment::new);

}
