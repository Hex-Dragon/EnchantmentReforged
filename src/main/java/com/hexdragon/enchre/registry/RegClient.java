package com.hexdragon.enchre.registry;

import com.hexdragon.enchre.Main;
import com.hexdragon.enchre.block.enchantingtable.EnchantmentContainerRe;
import com.hexdragon.enchre.block.enchantingtable.EnchantmentScreenRe;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD) @OnlyIn(Dist.CLIENT)
public class RegClient {

    @SubscribeEvent public static void clientSetup(final FMLClientSetupEvent e) {
        // 注册容器的 Screen
        ScreenManager.registerFactory(RegMain.containerEnchantment.get(), EnchantmentScreenRe::new);
    }

}
