package com.hexdragon.enchre;

import com.hexdragon.enchre.registry.RegMain;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class EnchRe {
    public EnchRe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RegMain.ENCHANTMENTS.register(modEventBus);
        RegMain.CONTAINERS.register(modEventBus);
    }
}