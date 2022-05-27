/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.common.blockentities.BlastFurnaceBlockEntity;
import net.dries007.tfc.common.blocks.devices.BlastFurnaceBlock;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.container.BlastFurnaceContainer;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;

public class BlastFurnaceScreen extends BlockEntityScreen<BlastFurnaceBlockEntity, BlastFurnaceContainer>
{
    private static final ResourceLocation BLAST_FURNACE = Helpers.identifier("textures/gui/blast_furnace.png");

    public BlastFurnaceScreen(BlastFurnaceContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BLAST_FURNACE);

        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(poseStack, partialTicks, mouseX, mouseY);

        final int capacity = blockEntity.getCapacity();
        final int fuelCount = blockEntity.getFuelCount();
        final int inputCount = blockEntity.getInputCount();

        final int maximumCapacity = TFCConfig.SERVER.blastFurnaceCapacity.get() * TFCConfig.SERVER.blastFurnaceMaxChimneyHeight.get();

        final boolean lit = blockEntity.getBlockState().getValue(BlastFurnaceBlock.LIT);

        // Render the two meters: ore and fuel
        renderCapacityLimitedFillMeter(poseStack, 42, 22, lit ? 216 : 226, maximumCapacity, capacity, inputCount);
        renderCapacityLimitedFillMeter(poseStack, 124, 22, 206, maximumCapacity, capacity, fuelCount);

        // Render temperature indicator
        final int temperature = (int) (51 * blockEntity.getTemperature() / Heat.maxVisibleTemperature());
        if (temperature > 0)
        {
            blit(poseStack, leftPos + 8, topPos + 78 - Math.min(51, temperature), 176, 0, 15, 5);
        }

        // Render output fluid tank
        final FluidStack fluid = blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            .map(c -> c.getFluidInTank(0))
            .orElse(FluidStack.EMPTY);
        final TextureAtlasSprite sprite = getAndBindFluidSprite(fluid);
        final int startY = 53;
        final int endY = 84;
        final int fillHeight = (int) Math.ceil((float) (endY - startY) * fluid.getAmount() / TFCConfig.SERVER.blastFurnaceFluidCapacity.get());

        fillAreaWithSprite(sprite, poseStack, 70, 106, endY, fillHeight);

        resetToBackgroundSprite();
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY)
    {
        super.renderTooltip(poseStack, mouseX, mouseY);

        final int capacity = blockEntity.getCapacity();
        final int fuelCount = blockEntity.getFuelCount();
        final int inputCount = blockEntity.getInputCount();

        if (isMouseIn(42, 22, 10, 66, mouseX, mouseY))
        {
            renderTooltip(poseStack, new TranslatableComponent("tfc.tooltip.blast_furnace_ore", inputCount, capacity), mouseX, mouseY);
        }
        if (isMouseIn(124, 22, 10, 66, mouseX, mouseY))
        {
            renderTooltip(poseStack, new TranslatableComponent("tfc.tooltip.blast_furnace_fuel", fuelCount, capacity), mouseX, mouseY);
        }
    }

    private void renderCapacityLimitedFillMeter(PoseStack poseStack, int x, int y, int fillU, int maximum, int capacity, int content)
    {
        if (capacity == 0)
        {
            // No capacity, so render a full dotted region.
            blit(poseStack, leftPos + x, topPos + y, 246, 0, 10, 66);
        }
        else if (content == 0)
        {
            // If we have capacity but no content, we render just an top section of the empty content bar.
            final int emptyHeight = 1 + (64 * capacity) / maximum;
            blit(poseStack, leftPos + x, topPos + y + 64 - emptyHeight, 236, 0, 10, 1 + emptyHeight);
        }
        else
        {
            // Both capacity and content are > 0, so we render the top section of an empty content bar, and the bottom section of a full content bar
            final int emptyHeight = 1 + (64 * capacity) / maximum;
            final int fillHeight = (64 * content) / maximum;

            blit(poseStack, leftPos + x, topPos + y + 64 - emptyHeight, 236, 0, 10, 1 + emptyHeight - fillHeight);
            blit(poseStack, leftPos + x, topPos + y + 65 - fillHeight, fillU, 0, 10, fillHeight);
        }
    }

    private boolean isMouseIn(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}