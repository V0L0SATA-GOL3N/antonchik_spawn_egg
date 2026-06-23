package com.example.antonchik.proxy;

import com.example.antonchik.entity.EntityAntonMob;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommonProxy
{
    public void preInit()
    {
    }

    public void init()
    {
        registerSpawns();
    }

    private void registerSpawns()
    {
        Biome[] biomes = ForgeRegistries.BIOMES.getValuesCollection().toArray(new Biome[0]);

        // Natural world spawning. Registered under MONSTER so it spawns continuously like a zombie;
        // because the mob extends EntityCreature (not EntityMob) there is no light-level check, so
        // it appears in both day and night.
        EntityRegistry.addSpawn(EntityAntonMob.class, 5, 1, 2, EnumCreatureType.MONSTER, biomes);
        EntitySpawnPlacementRegistry.setPlacementType(
            EntityAntonMob.class, EntityLiving.SpawnPlacementType.ON_GROUND);
    }
}
