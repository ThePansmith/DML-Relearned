package mustapelto.deepmoblearning.common.metadata;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.DMLRegistry;
import mustapelto.deepmoblearning.common.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class MetadataDataModel extends Metadata {
    // JSON Keys
    private static final String DATA_MODEL_ID = "id";
    private static final String MOD_ID = "mod";
    private static final String DISPLAY_NAME = "displayName";
    private static final String DISPLAY_NAME_PLURAL = "displayNamePlural";
    private static final String LIVING_MATTER = "livingMatter";
    private static final String SIMULATION_RF_COST = "simulationRFCost";
    private static final String EXTRA_TOOLTIP = "extraTooltip";
    private static final String CRAFTING_INGREDIENTS = "craftingIngredients";
    private static final String ASSOCIATED_MOBS = "associatedMobs";
    private static final String LOOT_ITEMS = "lootItems";
    private static final String TRIAL = "trial";
    private static final String DEEP_LEARNER_DISPLAY = "deepLearnerDisplay";

    // Validation
    private static final String[] REQUIRED_KEYS = new String[] {
            DATA_MODEL_ID, LIVING_MATTER, CRAFTING_INGREDIENTS, ASSOCIATED_MOBS, LOOT_ITEMS
    };

    // Default Values
    private static final String DEFAULT_DATA_MODEL_ID = "";
    private static final String DEFAULT_MOD_ID = "minecraft";
    private static final String DEFAULT_DISPLAY_NAME_PLURAL = "%ss";
    private static final String DEFAULT_LIVING_MATTER = "";
    private static final int DEFAULT_SIMULATION_RF_COST = 256;
    private static final String DEFAULT_EXTRA_TOOLTIP = "";

    // Data from JSON
    private final String dataModelID;
    private final String modID;
    private final boolean isEnabled;
    private final String displayName; // Used in Data Model and Pristine Matter display name, and Deep Learner GUI. Default: metadataID
    private final String displayNamePlural; // Plural form of display name. Used in Deep Learner GUI. Default: displayName + "s"
    private final String livingMatterString; // Living Matter type gained from simulating this Model.
    private final int simulationRFCost; // Cost to simulate this Model in RF/t. Default: 256
    private final String extraTooltip; // Extra tooltip to display on Data Model item. Default: ""
    private final ImmutableList<String> craftingIngredientStrings; // Ingredients to craft this model (in addition to a Blank Model). Default: none
    private final ImmutableList<String> associatedMobStrings; // Mobs that give this model data when killed
    private final ImmutableList<String> lootItemStrings; // List of possible loot items from this Model's Pristine Matter. Default: ["minecraft:stone"]
    private final TrialData trialData; // Data for Trials attuned to this Model
    private final DeepLearnerDisplayData deepLearnerDisplayData; // Data used in Deep Learner display

    // Calculated data
    private final String defaultRegistryString; // [modID]:[metadataID]
    private ImmutableList<ResourceLocation> associatedMobs; // List of mobs that increase Model data.
    private ImmutableList<ItemStack> lootItems; // List of actual ItemStacks that can be selected as "loot"
    private ItemStack livingMatter; // Living Matter data associated with this Data Model
    private ItemStack pristineMatter; // Pristine Matter associated with this Data Model

    public MetadataDataModel(JsonObject data) throws IllegalArgumentException {
        if (isInvalidJson(data, REQUIRED_KEYS)) {
            throw new IllegalArgumentException("Invalid Data Model JSON entry!");
        }

        dataModelID = getString(data, DATA_MODEL_ID)
                .orElse(DEFAULT_DATA_MODEL_ID);
        modID = getString(data, MOD_ID)
                .orElse(DEFAULT_MOD_ID);
        isEnabled = DMLRHelper.isModLoaded(modID);

        defaultRegistryString = DMLRHelper.getRegistryString(modID, dataModelID);

        displayName = getString(data, DISPLAY_NAME)
                .orElse(StringHelper.uppercaseFirst(dataModelID));
        displayNamePlural = getString(data, DISPLAY_NAME_PLURAL)
                .orElse(String.format(DEFAULT_DISPLAY_NAME_PLURAL, displayName));
        livingMatterString = getString(data, LIVING_MATTER)
                .orElse(DEFAULT_LIVING_MATTER);
        simulationRFCost = getInt(data, SIMULATION_RF_COST, 0, DMLConstants.SimulationChamber.ENERGY_IN_MAX)
                .orElse(DEFAULT_SIMULATION_RF_COST);
        extraTooltip = getString(data, EXTRA_TOOLTIP)
                .orElse(DEFAULT_EXTRA_TOOLTIP);
        craftingIngredientStrings = getStringList(data, CRAFTING_INGREDIENTS)
                .orElse(ImmutableList.of());
        associatedMobStrings = getStringList(data, ASSOCIATED_MOBS)
                .orElse(ImmutableList.of());
        lootItemStrings = getStringList(data, LOOT_ITEMS)
                .orElse(ImmutableList.of());

        JsonObject trialDataJSON = getJsonObject(data, TRIAL)
                .orElse(null);
        if (trialDataJSON == null) {
            trialData = new TrialData(this);
            DMLRelearned.logger.warn("Missing Trial JSON entry in config for Data Model {}:{}. Using default values.", modID, dataModelID);
        } else {
            trialData = new TrialData(trialDataJSON, this);
        }

        JsonObject deepLearnerDisplayJSON = getJsonObject(data, DEEP_LEARNER_DISPLAY)
                .orElse(null);
        if (deepLearnerDisplayJSON == null) {
            deepLearnerDisplayData = new DeepLearnerDisplayData(this);
            DMLRelearned.logger.warn("Missing Deep Learner display JSON object in entry {}:{}. Using default values.", modID, dataModelID);
        } else {
            deepLearnerDisplayData = new DeepLearnerDisplayData(deepLearnerDisplayJSON, this);
        }
    }

    @Override
    public void finalizeData() {
        if (!DMLRHelper.isModLoaded(modID) || !isEnabled)
            return;

        ImmutableList.Builder<ResourceLocation> mobListBuilder = ImmutableList.builder();
        boolean hasValidMobs = false;
        for (String entry : associatedMobStrings) {
            if (DMLRHelper.isRegisteredEntity(entry)) {
                mobListBuilder.add(new ResourceLocation(entry));
                hasValidMobs = true;
            } else
                DMLRelearned.logger.warn("Invalid entry \"{}\" in Associated Mobs list for Data Model: {}. No registered entity of this name found!", entry, dataModelID);
        }

        if (!hasValidMobs) {
            String defaultMob = DMLRHelper.getRegistryString(modID, dataModelID);
            if (DMLRHelper.isRegisteredEntity(defaultMob)) {
                mobListBuilder.add(new ResourceLocation(defaultMob));
                DMLRelearned.logger.warn("No valid Associated Mob entries for Data Model: {}. Using default mob ({}).", dataModelID, defaultMob);
            } else {
                DMLRelearned.logger.error("No valid Associated Mob entries for Data Model: {}. Model will not be able to gain data!", dataModelID);
            }
        }
        associatedMobs = mobListBuilder.build();

        // Build loot item ItemStack list
        lootItems = ItemStackDefinitionHelper.itemStackListFromStringList(lootItemStrings);
        if (lootItems.isEmpty())
            DMLRelearned.logger.error("No valid Loot Items found for Data Model: {}. This Model's Pristine Matter won't be able to produce any loot!", dataModelID);

        // Get associated Living Matter
        livingMatter = DMLRegistry.getLivingMatter(livingMatterString);
        pristineMatter = DMLRegistry.getPristineMatter(dataModelID);

        trialData.finalizeData();
        deepLearnerDisplayData.finalizeData();
    }

    @Override
    public String getID() {
        return dataModelID;
    }

    public String getModID() {
        return modID;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getDataModelRegistryID() {
        return "data_model_" + dataModelID;
    }

    public String getPristineMatterRegistryID() {
        return "pristine_matter_" + dataModelID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNamePlural() {
        return displayNamePlural;
    }

    public int getSimulationRFCost() {
        return simulationRFCost;
    }

    public ItemStack getLivingMatter(int size) {
        ItemStack result = livingMatter.copy();
        result.setCount(size);
        return result;
    }

    public ItemStack getLivingMatter() {
        return getLivingMatter(1);
    }

    public ItemStack getPristineMatter(int size) {
        ItemStack result = pristineMatter.copy();
        result.setCount(size);
        return result;
    }

    public ItemStack getPristineMatter() {
        return getPristineMatter(1);
    }

    public String getExtraTooltip() {
        return extraTooltip;
    }

    public TrialData getTrialData() {
        return trialData;
    }

    public DeepLearnerDisplayData getDeepLearnerDisplayData() {
        return deepLearnerDisplayData;
    }

    public ResourceLocation getDataModelTexture() {
        try {
            // Will throw FileNotFoundException if texture file doesn't exist in mod jar or resource packs
            ResourceLocation locationFromId = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/items/" + getDataModelRegistryID() + ".png");
            Minecraft.getMinecraft().getResourceManager().getAllResources(locationFromId);
            return new ResourceLocation(DMLConstants.ModInfo.ID, "items/" + getDataModelRegistryID());
        } catch (IOException e) {
            // File not found -> use default model and output info
            DMLRelearned.logger.info("Data Model texture not found for entry {}:{}. Using default texture.", modID, dataModelID);
            return DMLConstants.DefaultModels.DATA_MODEL;
        }
    }

    public ResourceLocation getPristineMatterTexture() {
        try {
            // Will throw FileNotFoundException if texture file doesn't exist in mod jar or resource packs
            ResourceLocation locationFromId = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/items/" + getPristineMatterRegistryID() + ".png");
            Minecraft.getMinecraft().getResourceManager().getAllResources(locationFromId);
            return new ResourceLocation(DMLConstants.ModInfo.ID, "items/" + getPristineMatterRegistryID());
        } catch (IOException e) {
            // File not found -> use default model and output info
            DMLRelearned.logger.info("Pristine Matter texture not found for entry {}:{}. Using default texture.", modID, dataModelID);
            return DMLConstants.DefaultModels.PRISTINE_MATTER;
        }
    }

    public boolean isAssociatedMob(EntityLivingBase entity) {
        EntityEntry entityEntry = EntityRegistry.getEntry(entity.getClass());
        if (entityEntry == null)
            return false;

        ResourceLocation registryName = entityEntry.getRegistryName();
        if (registryName == null)
            return false;

        return associatedMobs.contains(registryName);
    }

    public ImmutableList<ItemStack> getLootItems() {
        if (lootItems == null)
            return ImmutableList.of();
        return lootItems;
    }

    public ItemStack getLootItem(int index) {
        if (index >= 0 && index < lootItems.size())
            return lootItems.get(index).copy();

        return ItemStack.EMPTY;
    }

    public boolean hasLootItem(ItemStack item) {
        for (ItemStack lootItem : lootItems) {
            if (ItemStack.areItemStacksEqual(item, lootItem))
                return true;
        }
        return false;
    }

    public int getLootItemIndex(ItemStack item) {
        if (lootItems.isEmpty() || !hasLootItem(item))
            return -1;

        for (int i = 0; i < lootItems.size(); i++) {
            if (ItemStack.areItemStacksEqual(lootItems.get(i), item))
                return i;
        }

        return -1;
    }

    public Optional<IRecipe> getCraftingRecipe() {
        ItemStack output = DMLRegistry.getDataModel(dataModelID);
        if (output.isEmpty())
            return Optional.empty();

        NonNullList<Ingredient> ingredients = NonNullList.create();
        Ingredient blankModel = CraftingHelper.getIngredient(new ItemStack(DMLRegistry.ITEM_DATA_MODEL_BLANK));
        ingredients.add(blankModel);

        boolean isOreRecipe = false;
        ImmutableList<Ingredient> customIngredients = ItemStackDefinitionHelper.ingredientListFromStringList(craftingIngredientStrings);
        for (Ingredient ingredient : customIngredients) {
            if (ingredient.equals(Ingredient.EMPTY)) {
                DMLRelearned.logger.warn("Invalid Data Model crafting ingredient. Skipping.");
                continue;
            }
            if (ingredient instanceof OreIngredient)
                isOreRecipe = true;

            ingredients.add(ingredient);
        }

        ResourceLocation recipeGroup = new ResourceLocation(DMLConstants.ModInfo.ID, "data_models");
        IRecipe result;
        if (isOreRecipe)
            result = new ShapelessOreRecipe(recipeGroup, output, ingredients);
        else
            result = new ShapelessRecipes(recipeGroup.toString(), output, ingredients);

        result.setRegistryName(output.getItem().getRegistryName());
        return Optional.of(result);
    }

    public static class TrialData {
        private static final String ENTITIES = "entities";
        private static final String MOBS_PER_WAVE = "mobsPerWave";
        private static final String SPAWN_DELAY = "spawnDelay";
        private static final String REWARDS = "rewards";

        private static final int DEFAULT_MOBS_PER_WAVE = 4;
        private static final double DEFAULT_SPAWN_DELAY = 2d;

        private final MetadataDataModel container;

        private final ImmutableList<WeightedString> entityStrings; // Unvalidated list of entities to spawn for trial
        private final ImmutableList<Integer> mobsPerWave; // How many mobs to spawn each wave. Default: 4
        private final double spawnDelay; // Time between wave spawns. Default: 2
        private final ImmutableList<String> rewardStrings; // List of rewards for a Trial with this mob. Default: []

        private ImmutableList<WeightedString> entities; // Validated list of entities to spawn for trial
        private ImmutableList<ItemStack> rewards; // List of actual ItemStacks that are received as trial reward

        /** Default constructor, used if Data Model JSON entry doesn't have Trial entry
         *
         * @param container MetadataDataModel that contains this instance
         */
        public TrialData(MetadataDataModel container) {
            this.container = container;

            entityStrings = ImmutableList.of();
            mobsPerWave = ImmutableList.of();
            spawnDelay = 2;
            rewardStrings = ImmutableList.of();
        }

        public TrialData(JsonObject data, MetadataDataModel container) {
            this.container = container;

            entityStrings = getWeightedStringList(data, ENTITIES)
                    .orElse(ImmutableList.of());
            mobsPerWave = getIntList(data, MOBS_PER_WAVE)
                    .orElse(ImmutableList.of(DEFAULT_MOBS_PER_WAVE));
            spawnDelay = getDouble(data, SPAWN_DELAY, 0, 20)
                    .orElse(DEFAULT_SPAWN_DELAY);
            rewardStrings = getStringList(data, REWARDS)
                    .orElse(ImmutableList.of());
        }

        public void finalizeData() {
            buildValidatedEntityList();

            // Build list of trial rewards
            rewards = ItemStackDefinitionHelper.itemStackListFromStringList(rewardStrings);
        }

        private void buildValidatedEntityList() {
            DMLRelearned.logger.info("Registering Trial for {}", container.getDisplayName());
            // Build weighted list of trial entities
            boolean hasValidEntities = false;
            ImmutableList.Builder<WeightedString> builder = ImmutableList.builder();
            for (WeightedString entry : entityStrings) {
                String entityString = entry.getValue();
                if (!DMLRHelper.isRegisteredEntity(entityString)) {
                    DMLRelearned.logger.warn("Invalid entry \"{}\" in Trial entity list for Data Model: {}. No registered entity of this name found!", entityString, container.dataModelID);
                    continue;
                }
                DMLRelearned.logger.info("Registering Trial entity {} with weight {}", entityString, entry.itemWeight);
                builder.add(new WeightedString(entityString, entry.itemWeight));
                hasValidEntities = true;
            }

            if (!hasValidEntities) {
                // Try using categoryID:metadataID
                if (DMLRHelper.isRegisteredEntity(container.defaultRegistryString)) {
                    builder.add(new WeightedString(container.defaultRegistryString, 100));
                    DMLRelearned.logger.info("No valid entries in Trial entity list for Data Model: {}. Using default entity.", container.dataModelID);
                } else {
                    DMLRelearned.logger.info("No Trial available for {}", container.getDisplayName());
                }
            }

            entities = builder.build();
        }

        public boolean hasEntity() {
            return entities.size() > 0;
        }

        public Optional<Entity> getRandomEntity(World world) {
            String entityName = WeightedRandom.getRandomItem(ThreadLocalRandom.current(), entities).getValue();
            ResourceLocation entityResource = new ResourceLocation(entityName);
            Entity entity = EntityList.createEntityByIDFromName(entityResource, world);
            return (entity != null) ? Optional.of(entity) : Optional.empty();
        }

        public double getSpawnDelay() {
            return spawnDelay;
        }

        public ImmutableList<ItemStack> getRewards() {
            if (rewards == null)
                return ImmutableList.of();
            return rewards;
        }

        public int getMobsPerWave(int wave) {
            if (wave < 0)
                return 0;
            else if (wave >= mobsPerWave.size())
                return mobsPerWave.get(mobsPerWave.size() - 1);
            else
                return mobsPerWave.get(wave);
        }
    }

    public static class DeepLearnerDisplayData {
        private static final String HEARTS = "hearts";
        private static final String MOB_TRIVIA = "mobTrivia";
        private static final String ENTITY = "entity";
        private static final String ENTITY_HELD_ITEM = "entityHeldItem";
        private static final String ENTITY_SCALE = "entityScale";
        private static final String ENTITY_OFFSET_X = "entityOffsetX";
        private static final String ENTITY_OFFSET_Y = "entityOffsetY";
        private static final String EXTRA_ENTITY = "extraEntity";
        private static final String EXTRA_ENTITY_IS_CHILD = "extraEntityIsChild";
        private static final String EXTRA_ENTITY_OFFSET_X = "extraEntityOffsetX";
        private static final String EXTRA_ENTITY_OFFSET_Y = "extraEntityOffsetY";

        private static final int DEFAULT_HEARTS = 0;
        private static final String DEFAULT_MOB_TRIVIA = "Nothing is known about this mob.";
        private static final int DEFAULT_ENTITY_SCALE = 40;
        private static final int DEFAULT_ENTITY_OFFSET_X = 0;
        private static final int DEFAULT_ENTITY_OFFSET_Y = 0;
        private static final boolean DEFAULT_EXTRA_ENTITY_IS_CHILD = false;
        private static final int DEFAULT_EXTRA_ENTITY_OFFSET_X = 0;
        private static final int DEFAULT_EXTRA_ENTITY_OFFSET_Y = 0;

        private final int hearts; // Number of hearts. 0 will show as obfuscated text. Default: 10
        private final ImmutableList<String> mobTrivia; // Mob trivia text. Default: "Nothing is known about this mob."
        private final ResourceLocation entity; // Registry name of displayed entity. Default: "modID:metadataID"
        private final ResourceLocation entityHeldItem; // Registry name of item held by displayed entity. Default: ""
        private final int entityScale; // Scale of displayed entity. Default: 40
        private final int entityOffsetX; // X offset of displayed entity. Default: 0
        private final int entityOffsetY; // Y offset of displayed entity. Default: 0
        private final ResourceLocation extraEntityName; // Registry name of additional displayed entity. Default: ""
        private final boolean extraEntityIsChild; // Is additional displayed entity a child (e.g. zombie)? Default: false
        private final int extraEntityOffsetX; // X offset of additional entity. Default: 0
        private final int extraEntityOffsetY; // Y offset of additional entity. Default: 0

        private ResourceLocation entityValidated;

        private final MetadataDataModel container;

        public DeepLearnerDisplayData(MetadataDataModel container) {
            this.container = container;

            hearts = 0;
            mobTrivia = ImmutableList.of();
            entity = null;
            entityHeldItem = null;
            entityScale = 1;
            entityOffsetX = 0;
            entityOffsetY = 0;
            extraEntityName = null;
            extraEntityIsChild = false;
            extraEntityOffsetX = 0;
            extraEntityOffsetY = 0;
        }

        public DeepLearnerDisplayData(JsonObject data, MetadataDataModel container) {
            this.container = container;

            hearts = getInt(data, HEARTS, 0, Integer.MAX_VALUE)
                    .orElse(DEFAULT_HEARTS);
            mobTrivia = getStringList(data, MOB_TRIVIA)
                    .orElse(ImmutableList.of(DEFAULT_MOB_TRIVIA));

            entity = getResourceLocation(data, ENTITY)
                    .orElse(null);

            entityHeldItem = getResourceLocation(data, ENTITY_HELD_ITEM)
                    .orElse(null);
            entityScale = getInt(data, ENTITY_SCALE, 0, 200)
                    .orElse(DEFAULT_ENTITY_SCALE);
            entityOffsetX = getInt(data, ENTITY_OFFSET_X, -200, 200)
                    .orElse(DEFAULT_ENTITY_OFFSET_X);
            entityOffsetY = getInt(data, ENTITY_OFFSET_Y, -200, 200)
                    .orElse(DEFAULT_ENTITY_OFFSET_Y);
            extraEntityName = getResourceLocation(data, EXTRA_ENTITY)
                    .orElse(null);
            extraEntityIsChild = getBoolean(data, EXTRA_ENTITY_IS_CHILD)
                    .orElse(DEFAULT_EXTRA_ENTITY_IS_CHILD);
            extraEntityOffsetX = getInt(data, EXTRA_ENTITY_OFFSET_X, -200, 200)
                    .orElse(DEFAULT_EXTRA_ENTITY_OFFSET_X);
            extraEntityOffsetY = getInt(data, EXTRA_ENTITY_OFFSET_Y, -200, 200)
                    .orElse(DEFAULT_EXTRA_ENTITY_OFFSET_Y);
        }

        public void finalizeData() {
            if (entity == null && !container.associatedMobs.isEmpty())
                entityValidated = container.associatedMobs.get(0);
            else
                entityValidated = entity;
        }

        public int getHearts() {
            return hearts;
        }

        public ImmutableList<String> getMobTrivia() {
            return mobTrivia;
        }

        public Optional<Entity> getEntity(World world) {
            if (!EntityList.isRegistered(entityValidated))
                return Optional.empty();

            Entity entity = EntityList.createEntityByIDFromName(entityValidated, world);
            if (entity instanceof EntityLiving && entityHeldItem != null) {
                Item heldItem = Item.getByNameOrId(entityHeldItem.toString());
                if (heldItem != null)
                    ((EntityLiving) entity).setHeldItem(EnumHand.MAIN_HAND, new ItemStack(heldItem));
            }

            return entity != null ? Optional.of(entity) : Optional.empty();
        }

        public int getEntityScale() {
            return entityScale;
        }

        public int getEntityOffsetX() {
            return entityOffsetX;
        }

        public int getEntityOffsetY() {
            return entityOffsetY;
        }

        public Optional<Entity> getExtraEntity(World world) {
            if (!EntityList.isRegistered(extraEntityName))
                return Optional.empty();

            Entity entity = EntityList.createEntityByIDFromName(extraEntityName, world);
            if (extraEntityIsChild) {
                if (entity instanceof EntityZombie) {
                    ((EntityZombie) entity).setChild(true);
                } else if (entity instanceof EntityAgeable) {
                    ((EntityAgeable) entity).setGrowingAge(-1000000); // big number -> won't grow up while Deep Learner is open
                }
            }

            return entity != null ? Optional.of(entity) : Optional.empty();
        }

        public int getExtraEntityOffsetX() {
            return extraEntityOffsetX;
        }

        public int getExtraEntityOffsetY() {
            return extraEntityOffsetY;
        }
    }
}
