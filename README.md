<img src="/images/icon.png" alt="Revelationary's Icon" width="200" />

# Revelations
"[a] revelation is the revealing or disclosing of some form of truth or knowledge"

If you are the designer of a progression based mod or modpack you have come to the right place.

With Revelationary, you are able to use Data Packs or the Revelation API to gradually reveal blocks in the world as the player progresses. A plain stone filled cleft might be revealed as bursting with life and ore as the player defeats a boss or reaches an important milestone.

As long as a player does not have the necessary progress the blocks and items you register with Revelationary via API or Data Pack will just have cryptic names and in case of blocks in the world, will drop the same drops as the block it is disguised as, giving the player a seamless experience without getting flooded with unknown resources that they are not able to use at the start. Instead you can introduce resources just as they are needed.

Limitations
Besides visuals and drops, disguised blocks retain all the properties of the original block: Hitbox, if light passes through and everything else. Keep that in mind when searching for fitting candidates. A perfect disguise for your Ore would be stone or deepslate, since these share a lot of similarities. Similarly you could disguise your elusive plant with a poppy: Both have a small hitbox and players can walk through them.

If you are a programmer you can use the Revelation API to dynamically change your blocks properties depending on if they are visible to the player, or not. This way you could even disguise your flower as air: Giving it an empty hitbox when disguised, your players might not even know it is there until they can see them!

# Advancement API
Revelationary provides utilities to make the handling of advancements easier - both client- and serverside.

# Documentation and examples
Find everything you need to know in [Revelationary's Wiki](https://github.com/DaFuqs/Revelationary/wiki) or take a look at [the examples](https://github.com/DaFuqs/Revelationary/blob/master/examples/example_revelations.json)


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