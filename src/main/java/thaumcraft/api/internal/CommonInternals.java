package thaumcraft.api.internal;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;

import thaumcraft.api.ThaumcraftApi;

/** Internal catalogs of the API. Do not use these directly. */
public class CommonInternals {
    public static final HashMap<ResourceLocation, Object> craftingRecipeCatalog = new HashMap<>();
    public static final HashMap<ResourceLocation, Object> craftingRecipeCatalogFake = new HashMap<>();

    public static final ArrayList<ThaumcraftApi.SmeltBonus> smeltingBonus = new ArrayList<>();

    public static Object getCatalogRecipe(ResourceLocation key) {
        return craftingRecipeCatalog.get(key);
    }

    public static Object getCatalogRecipeFake(ResourceLocation key) {
        return craftingRecipeCatalogFake.get(key);
    }
}
