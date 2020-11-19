package com.hexdragon.enchre.registry;

import com.hexdragon.enchre.Main;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD) @OnlyIn(Dist.CLIENT)
public class RegClient {

    @SubscribeEvent public static void clientSetup(final FMLClientSetupEvent e) {
        // 注册容器的 Screen
        // ScreenManager.registerFactory(RegMain.containerGrindstone.get(), GrindstoneScreenRe::new);
    }

}
