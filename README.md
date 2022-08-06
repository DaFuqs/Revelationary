![Revelationary Banner](/images/banner.png "Revelationary's Banner")

## Overview
Data Driven Block and Item Revelation system. Discover as you go!

Revelationary adds a way to gate the discovery of new blocks in the world via data pack, or API (for other mods). Using Revelationary, you can design your packs / mods progression in a way the player does not feel overwhelmed when starting a new playthrough. They can only perceive these worldgen blocks when they unlocked a specific advancement, making the block visible for them.

Example: The player sees a specific ore as stone. As they defeat the enderdragon they can now see and mine your ore in the world - it was always there from the start. Individual for each player and very performant.

Find lots of examples for data packs & the API in Revelationary's wiki: https://github.com/DaFuqs/Revelationary/wiki


## Registering Revelations via Data Pack
Have to be placed in the folder `resources/data/<<mod_id>>/revelations`

```json
{
  "advancement": "spectrum:milestones/reveal_quitoxic_reeds",
  "block_states": {
    "minecraft:grass": "minecraft:beacon",
    "minecraft:tall_grass": "minecraft:obsidian",
    "minecraft:tall_grass[half=upper]": "minecraft:netherite_block"
  },
  "items": {
    "minecraft:nether_star": "minecraft:gunpowder"
  }
}
```

## Registering a "revelation aware" block or item

```java
public class CloakedItem extends Item implements RevelationAware {
	
	Identifier cloakAdvancementIdentifier;
	Item cloakItem;
	
	public CloakedItem(Settings settings, Identifier cloakAdvancementIdentifier, Item cloakItem) {
		super(settings);
		this.cloakAdvancementIdentifier = cloakAdvancementIdentifier;
		this.cloakItem = cloakItem;
		
		registerCloak();
	}
	
	@Override
	public Identifier getCloakAdvancementIdentifier() {
		return cloakAdvancementIdentifier;
	}
	
	@Override
	public Hashtable<BlockState, BlockState> getBlockStateCloaks() {
		return new Hashtable<>();
	}
	
	@Override
	public Pair<Item, Item> getItemCloak() {
		return new Pair<>(this, cloakItem);
	}
	
	@Override
	public void onCloak() { }
	
	@Override
	public void onUncloak() { }
	
}
```

## Registering a Callback when Revelations happen

```java
RevelationHolder.registerRevelationCallback(new RevelationHolder.UncloakCallback() {
    @Override
    public void trigger(List<Identifier> advancements, List<Block> blocks, List<Item> items) {
        for(Block block : blocks) {
            if(Registry.BLOCK.getId(block).getNamespace().equals(MOD_ID)) {
                ...
                <I dunno. Like show a popup or something. You tell me>
                ...
                break
            }
        }
    }
});
```

# Discord
You will find a lot of helpful people on Spectrum's Discord. There always are friendly and helpful people around. Swing around too, if you like!

https://discord.com/invite/EXU9XFXT8a