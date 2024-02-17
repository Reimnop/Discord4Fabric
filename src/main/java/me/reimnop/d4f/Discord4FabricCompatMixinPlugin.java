package me.reimnop.d4f;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Discord4FabricCompatMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<String> namespaces = Arrays.asList(mixinClassName.split("\\."));
        int compatIndex = namespaces.indexOf("compat");
        if (compatIndex != -1) {
            // mod id is the namespace after compat
            String modId = namespaces.get(compatIndex + 1);
            boolean modLoaded = FabricLoader.getInstance().isModLoaded(modId);
            Discord4Fabric.LOGGER.info(String.format("Compat mixin for mod '%s' is %s", modId, modLoaded ? "ENABLED" : "DISABLED"));
            return modLoaded;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
