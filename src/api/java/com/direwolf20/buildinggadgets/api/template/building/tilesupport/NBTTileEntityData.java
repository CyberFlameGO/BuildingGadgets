package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import com.direwolf20.buildinggadgets.api.BuildinggadgetsAPI;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.template.serialisation.SerialisationSupport;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class NBTTileEntityData implements ITileEntityData {
    private final CompoundNBT nbt;

    public NBTTileEntityData(CompoundNBT nbt) {
        this.nbt = Objects.requireNonNull(nbt);
    }

    @Override
    public ITileDataSerializer getSerializer() {
        return SerialisationSupport.nbtTileDataSerializer();
    }

    @Override
    public boolean placeIn(IBuildContext context, BlockState state, BlockPos position) {
        BuildinggadgetsAPI.LOG.trace("Placing {} with Tile NBT at {}.", state, position);
        context.getWorld().setBlockState(position, state, 0);
        TileEntity te = context.getWorld().getTileEntity(position);
        if (te != null) {
            try {
                te.read(getNBTModifiable());
            } catch (Exception e) {
                BuildinggadgetsAPI.LOG.debug("Failed to apply Tile NBT Data to {} at {} in Context {}", state, position, context);
                BuildinggadgetsAPI.LOG.debug(e);
            }
        }
        return true;
    }

    public CompoundNBT getNBT() {
        return nbt.copy();
    }

    protected CompoundNBT getNBTModifiable() {
        return nbt;
    }

}
