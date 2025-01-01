package mustapelto.deepmoblearning.common.metadata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.util.DMLRHelper;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class MetadataManager {
    private static final String INTERNAL_PATH = "/settings/";
    private static final String DATA_MODEL_FILE = "DataModels.json";
    private static final String DATA_MODEL_TIER_FILE = "DataModelTiers.json";
    private static final String LIVING_MATTER_FILE = "LivingMatter.json";
    private static final String LIVING_MATTER_RECIPES_FILE = "LivingMatterRecipes.json";

    private static File configDir;

    private static ImmutableMap<String, MetadataDataModel> dataModelStore;
    private static ImmutableSortedMap<Integer, MetadataDataModelTier> dataModelTierStore;
    private static ImmutableMap<String, MetadataLivingMatter> livingMatterStore;

    private static ImmutableList<JsonObject> livingMatterRecipesJson;

    public static void init(FMLPreInitializationEvent event) throws IOException {
        DMLRelearned.logger.info("Loading data from JSON config files...");
        configDir = new File(event.getModConfigurationDirectory(), DMLConstants.ModInfo.CONFIG_PATH);

        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new IOException("Could not create mod config directory!");
        }

        copyConfigFile(DATA_MODEL_FILE)
                .flatMap(MetadataManager::readConfigFile)
                .ifPresent(MetadataManager::initDataModelStore);

        copyConfigFile(DATA_MODEL_TIER_FILE)
                .flatMap(MetadataManager::readConfigFile)
                .ifPresent(MetadataManager::initDataModelTierStore);

        copyConfigFile(LIVING_MATTER_FILE)
                .flatMap(MetadataManager::readConfigFile)
                .ifPresent(MetadataManager::initLivingMatterStore);

        copyConfigFile(LIVING_MATTER_RECIPES_FILE)
                .flatMap(MetadataManager::readConfigFile)
                .ifPresent(MetadataManager::readLivingMatterRecipes);
    }

    public static void finalizeData() {
        DMLRelearned.logger.info("Finalizing config data...");
        dataModelStore.values().forEach(MetadataDataModel::finalizeData);
        dataModelTierStore.values().forEach(MetadataDataModelTier::finalizeData);
        livingMatterStore.values().forEach(MetadataLivingMatter::finalizeData);
    }

    //
    // Copy and read JSON files
    //

    private static Optional<File> copyConfigFile(String filename) {
        File file = new File(configDir, filename);
        if (!file.exists()) {
            try (InputStream input = DMLRelearned.class.getResourceAsStream(INTERNAL_PATH + filename)) {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                DMLRelearned.logger.error("Error extracting default config file \"{}\" from mod jar!", filename);
                return Optional.empty();
            }
        }
        return Optional.of(file);
    }

    private static Optional<JsonArray> readConfigFile(File file) {
        JsonElement result;
        InputStreamReader inputStreamReader;
        String filename = file.getName();
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            DMLRelearned.logger.error("Config file \"{}\" not found! Error message: {}", filename, e.getMessage());
            return Optional.empty();
        }

        try (JsonReader reader = new JsonReader(inputStreamReader)) {
            JsonParser parser = new JsonParser();
            reader.setLenient(true);
            result = parser.parse(reader);
        } catch (Exception e) {
            if (e instanceof IOException)
                DMLRelearned.logger.error("Error reading config file \"{}\"! Error message: {}", filename, e.getMessage());
            else if (e instanceof JsonSyntaxException)
                DMLRelearned.logger.error("Invalid JSON in config file \"{}\"! Error message: {}", filename, e.getMessage());
            else
                DMLRelearned.logger.error("Exception while reading config file \"{}\"! Error message: {}", filename, e.getMessage());
            return Optional.empty();
        }

        if (!result.isJsonArray()) {
            DMLRelearned.logger.error("Error parsing config file \"{}\": root element must be an array!", filename);
            return Optional.empty();
        }

        return Optional.of(result.getAsJsonArray());
    }

    //
    // JSON parse
    //

    private static void initDataModelStore(JsonArray json) {
        ImmutableMap.Builder<String, MetadataDataModel> builder = ImmutableMap.builder();

        for (int i = 0; i < json.size(); i++) {
            JsonElement entry = json.get(i);
            if (!entry.isJsonObject()) {
                DMLRelearned.logger.warn(getInvalidEntryString(i, "Data Model"));
                continue;
            }

            MetadataDataModel metadata;
            try {
                metadata = new MetadataDataModel(entry.getAsJsonObject());
            } catch (IllegalArgumentException e) {
                DMLRelearned.logger.warn(getInvalidObjectString(i, "Data Model"));
                continue;
            }
            builder.put(metadata.getID(), metadata);
        }

        dataModelStore = builder.build();
    }

    private static void initDataModelTierStore(JsonArray json) {
        ImmutableSortedMap.Builder<Integer, MetadataDataModelTier> builder = ImmutableSortedMap.naturalOrder();

        for (int i = 0; i < json.size(); i++) {
            JsonElement entry = json.get(i);
            if (!entry.isJsonObject()) {
                DMLRelearned.logger.warn(getInvalidEntryString(i, "Data Model Tier"));
                continue;
            }

            MetadataDataModelTier metadata;
            try {
                metadata = new MetadataDataModelTier(entry.getAsJsonObject());
            } catch (IllegalArgumentException e) {
                DMLRelearned.logger.warn(getInvalidObjectString(i, "Data Model Tier"));
                continue;
            }
            builder.put(metadata.getTier(), metadata);
        }

        dataModelTierStore = builder.build();
    }

    private static void initLivingMatterStore(JsonArray json) {
        ImmutableMap.Builder<String, MetadataLivingMatter> builder = ImmutableMap.builder();

        for (int i = 0; i < json.size(); i++) {
            JsonElement entry = json.get(i);
            if (!entry.isJsonObject()) {
                DMLRelearned.logger.warn(getInvalidEntryString(i, "Living Matter"));
                continue;
            }

            MetadataLivingMatter metadata;
            try {
                metadata = new MetadataLivingMatter(entry.getAsJsonObject());
            } catch (IllegalArgumentException e) {
                DMLRelearned.logger.warn(getInvalidObjectString(i, "Living Matter"));
                continue;
            }
            builder.put(metadata.getID(), metadata);
        }

        livingMatterStore = builder.build();
    }

    private static void readLivingMatterRecipes(JsonArray array) {
        ImmutableList.Builder<JsonObject> builder = ImmutableList.builder();

        for (int i = 0; i < array.size(); i++) {
            JsonElement entry = array.get(i);
            if (entry.isJsonObject())
                builder.add(entry.getAsJsonObject());
            else
                DMLRelearned.logger.warn(getInvalidEntryString(i, "Living Matter Recipe"));
        }

        livingMatterRecipesJson = builder.build();
    }

    private static String getInvalidEntryString(int index, String configName) {
        return String.format("Invalid entry at index %s in %s config (root array elements must be objects)", index, configName);
    }

    private static String getInvalidObjectString(int index, String configName) {
        return String.format("Invalid object structure at index %s in %s config (invalid or missing keys)", index, configName);
    }

    //
    // Data access (General)
    //

    public static ImmutableList<IRecipe> getCraftingRecipes() {
        ImmutableList.Builder<IRecipe> builder = ImmutableList.builder();

        // Get Data Model recipes from metadata
        dataModelStore.values().forEach(entry -> {
            if (DMLRHelper.isModLoaded(entry.getModID()))
                entry.getCraftingRecipe().ifPresent(builder::add);
        });

        // Construct Living Matter recipes from JSON data
        JsonContext ctx = new JsonContext(DMLConstants.ModInfo.ID);

        int recipeCount = 0;
        for(JsonObject entry : livingMatterRecipesJson) {
            IRecipe recipe;
            try {
                recipe = CraftingHelper.getRecipe(entry, ctx);
            } catch (Exception e) {
                DMLRelearned.logger.warn("Error reading Living Matter recipe entry! Error message: {}", e.getMessage());
                continue;
            }
            recipe.setRegistryName(new ResourceLocation("deepmoblearning:living_matter_conversion_" + recipeCount));
            builder.add(recipe);
            recipeCount++;
        }

        return builder.build();
    }

    //
    // Data Access (Data Models)
    //

    public static ImmutableList<MetadataDataModel> getDataModelMetadataList() {
        return dataModelStore.values().asList();
    }

    public static Optional<MetadataDataModel> getDataModelMetadata(String id) {
        MetadataDataModel entry = dataModelStore.get(id);
        return (entry != null) ? Optional.of(entry) : Optional.empty();
    }

    public static ImmutableMap<String, ResourceLocation> getDataModelTextures() {
        ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();

        dataModelStore.forEach((k, v) -> {
            ResourceLocation dataModelTexture = v.getDataModelTexture();
            if (!dataModelTexture.equals(DMLConstants.DefaultModels.DATA_MODEL))
                builder.put(k, dataModelTexture);
        });

        return builder.build();
    }

    public static ImmutableMap<String, ResourceLocation> getPristineMatterTextures() {
        ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();

        dataModelStore.forEach((k, v) -> {
            ResourceLocation pristineMatterTexture = v.getPristineMatterTexture();
            if (!pristineMatterTexture.equals(DMLConstants.DefaultModels.PRISTINE_MATTER))
                builder.put(k, pristineMatterTexture);
        });

        return builder.build();
    }

    public static ImmutableList<String> getAvailableTrials() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        dataModelStore.values().forEach(entry -> {
            if (entry.getTrialData().hasEntity())
                builder.add(entry.getDisplayName());
        });

        return builder.build();
    }

    //
    // Data Access (Data Model Tiers)
    //

    public static Optional<MetadataDataModelTier> getDataModelTierData(int tier) {
        MetadataDataModelTier entry = dataModelTierStore.get(tier);
        return (entry != null) ? Optional.of(entry) : Optional.empty();
    }

    public static int getMaxDataModelTier() {
        return dataModelTierStore.isEmpty() ? -1 : dataModelTierStore.lastKey();
    }

    public static boolean isMaxDataModelTier(int tier) {
        int maxTier = getMaxDataModelTier();
        return maxTier == -1 || tier >= maxTier;
    }

    public static int getMinDataModelTier() {
        return dataModelTierStore.isEmpty() ? -1 : dataModelTierStore.firstKey();
    }

    public static boolean isMinDataModelTier(int tier) {
        int minTier = getMinDataModelTier();
        return minTier == -1 || tier <= minTier;
    }

    public static int getNextDataModelTier(int current) {
        if (dataModelTierStore.isEmpty())
            return -1;
        Integer next = dataModelTierStore.higherKey(current);
        return (next != null) ? next : dataModelTierStore.lastKey();
    }

    public static int getPrevDataModelTier(int current) {
        if (dataModelTierStore.isEmpty())
            return -1;
        Integer next = dataModelTierStore.lowerKey(current);
        return (next != null) ? next : dataModelTierStore.firstKey();
    }

    //
    // Data Access (Living Matter)
    //

    public static ImmutableList<MetadataLivingMatter> getLivingMatterMetadataList() {
        return livingMatterStore.values().asList();
    }

    public static ImmutableMap<String, ResourceLocation> getLivingMatterTextures() {
        ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();

        livingMatterStore.forEach((k, v) -> {
            ResourceLocation livingMatterTexture = v.getLivingMatterTexture();
            if (!livingMatterTexture.equals(DMLConstants.DefaultModels.LIVING_MATTER))
                builder.put(k, livingMatterTexture);
        });

        return builder.build();
    }
}
