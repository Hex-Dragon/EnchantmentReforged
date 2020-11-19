package com.hexdragon.enchre.registry;

import com.hexdragon.enchre.Main;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RegMain {

    // 附魔
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Main.MODID);
    // public static final RegistryObject<Enchantment> enchDecay = ENCHANTMENTS.register("decay_curse", DecayCurseEnchantment::new);

}
