package de.dafuqs.revelationary.compat.wthit;

import de.dafuqs.revelationary.api.revelations.*;
import mcp.mobius.waila.api.*;

public class RevelationaryWthitPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        registrar.addOverride(new CloakedBlockComponentProvider(), RevelationAware.class);
    }
}
