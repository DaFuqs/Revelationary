package de.dafuqs.revelationary.config;

import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;

@Config(name = "Revelationary")
public class RevelarionaryConfig implements ConfigData {
	
	@Comment("Use target block/item name instead of scattering it")
	public boolean UseTargetBlockOrItemNameInsteadOfScatter = false;

	@Comment("Name for cloaked Blocks")
	public String NameForCloakedBlocks = "";

	@Comment("Name for cloaked Items")
	public String NameForCloakedItems = "";
	
}
