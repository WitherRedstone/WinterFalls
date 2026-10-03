package com.chinaex123.winter_falls.client;

import com.chinaex123.winter_falls.Winterfalls;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Winterfalls.MOD_ID, dist = Dist.CLIENT)
public class ClientSetup {

    public ClientSetup(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(ClientFogHandler::onRenderFog);
        NeoForge.EVENT_BUS.addListener(ClientFogHandler::onComputeFogColor);
    }
}