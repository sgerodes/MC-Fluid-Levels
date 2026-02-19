package com.fluidlevels;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? if >=1.21.10 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;*/
//?}
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
//? if >=1.21.10 {
import net.minecraft.util.Identifier;
//?}
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidLevelsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("FluidLevels");

    private static boolean enabled = true;
    private static KeyBinding toggleKey;

    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Fluid Levels mod initialized");

        //? if >=1.21.10 {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fluidlevels.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            KeyBinding.Category.create(Identifier.of("fluidlevels", "main"))
        ));
        //?} else {
        /*toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fluidlevels.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            "category.fluidlevels.main"
        ));*/
        //?}

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.sendMessage(
                        Text.literal("Fluid Levels: " + (enabled ? "ON" : "OFF")),
                        true
                    );
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(FluidLevelRenderer::render);
    }
}
