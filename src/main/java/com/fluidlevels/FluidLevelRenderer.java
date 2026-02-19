package com.fluidlevels;

//? if >=1.21.10 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;*/
//?}
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

public class FluidLevelRenderer {

    private static final int RENDER_DISTANCE = 32;
    private static final int WATER_TEXT_COLOR = 0xFF88DDFF;
    private static final int LAVA_TEXT_COLOR = 0xFFFFFF44;

    public static void render(WorldRenderContext context) {
        if (!FluidLevelsClient.isEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        //? if >=1.21.10 {
        MatrixStack matrices = context.matrices();
        //?} else {
        /*MatrixStack matrices = context.matrixStack();*/
        //?}
        if (matrices == null) {
            return;
        }

        Camera camera = client.gameRenderer.getCamera();
        //? if >=1.21.10 {
        Vec3d cameraPos = camera.getCameraPos();
        //?} else {
        /*Vec3d cameraPos = camera.getPos();*/
        //?}
        float cameraYaw = camera.getYaw();
        World world = client.world;
        BlockPos playerPos = client.player.getBlockPos();

        for (int x = -RENDER_DISTANCE; x <= RENDER_DISTANCE; x++) {
            for (int y = -RENDER_DISTANCE / 2; y <= RENDER_DISTANCE / 2; y++) {
                for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    if (state.getBlock() instanceof FluidBlock) {
                        int level = state.get(Properties.LEVEL_15);

                        boolean isWater = state.isOf(Blocks.WATER);
                        boolean isLava = state.isOf(Blocks.LAVA);
                        if (!isWater && !isLava) {
                            continue;
                        }

                        String displayText;
                        if (level == 0) {
                            displayText = "S";
                        } else if (level >= 8) {
                            displayText = String.valueOf(level);
                        } else {
                            displayText = String.valueOf(8 - level);
                        }
                        int color = isWater ? WATER_TEXT_COLOR : LAVA_TEXT_COLOR;
                        boolean falling = level >= 8;

                        BlockState aboveState = world.getBlockState(pos.up());
                        boolean fluidAbove = aboveState.getBlock() instanceof FluidBlock;

                        if (falling && fluidAbove) {
                            renderSideLabel(matrices, pos, cameraPos, displayText, color);
                        } else if (!fluidAbove) {
                            renderTopLabel(matrices, cameraYaw, pos, cameraPos, displayText, color);
                        }
                    }
                }
            }
        }
    }

    private static void renderTopLabel(MatrixStack matrices, float cameraYaw,
                                        BlockPos pos, Vec3d cameraPos, String text, int color) {
        double x = pos.getX() + 0.5 - cameraPos.x;
        double y = pos.getY() + 0.95 - cameraPos.y;
        double z = pos.getZ() + 0.5 - cameraPos.z;

        double distance = Math.sqrt(x * x + y * y + z * z);
        if (distance > RENDER_DISTANCE) {
            return;
        }

        matrices.push();
        matrices.translate(x, y, z);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - cameraYaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

        float scale = 0.035f;
        if (distance > 10) {
            scale *= (float)(distance / 10.0) * 0.7f;
        }
        matrices.scale(scale, -scale, scale);

        drawText(matrices, text, color);
        matrices.pop();
    }

    private static void renderSideLabel(MatrixStack matrices,
                                         BlockPos pos, Vec3d cameraPos, String text, int color) {
        double dx = cameraPos.x - (pos.getX() + 0.5);
        double dz = cameraPos.z - (pos.getZ() + 0.5);

        double bx, bz;
        float faceYaw;

        if (Math.abs(dx) > Math.abs(dz)) {
            bx = pos.getX() + (dx > 0 ? 1.01 : -0.01) - cameraPos.x;
            bz = pos.getZ() + 0.5 - cameraPos.z;
            faceYaw = dx > 0 ? -90 : 90;
        } else {
            bx = pos.getX() + 0.5 - cameraPos.x;
            bz = pos.getZ() + (dz > 0 ? 1.01 : -0.01) - cameraPos.z;
            faceYaw = dz > 0 ? 0 : 180;
        }

        double by = pos.getY() + 0.5 - cameraPos.y;

        double distance = Math.sqrt(bx * bx + by * by + bz * bz);
        if (distance > RENDER_DISTANCE) {
            return;
        }

        matrices.push();
        matrices.translate(bx, by, bz);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(faceYaw));

        float scale = 0.035f;
        if (distance > 10) {
            scale *= (float)(distance / 10.0) * 0.7f;
        }
        matrices.scale(scale, -scale, scale);

        drawText(matrices, text, color);
        matrices.pop();
    }

    private static void drawText(MatrixStack matrices, String text, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float textWidth = textRenderer.getWidth(text);
        float xOffset = -textWidth / 2;

        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        textRenderer.draw(
            text,
            xOffset,
            -textRenderer.fontHeight / 2.0f,
            color,
            false,
            matrix,
            immediate,
            TextRenderer.TextLayerType.POLYGON_OFFSET,
            0x00000000,
            0xF000F0
        );

        immediate.draw();
    }
}
