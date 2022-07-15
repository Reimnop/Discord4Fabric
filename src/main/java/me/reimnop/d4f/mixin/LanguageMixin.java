package me.reimnop.d4f.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

@Mixin(Language.class)
public class LanguageMixin {

    @SuppressWarnings({"InvalidInjectorMethodSignature", "rawtypes"})
    @Inject(
            method = "create",
            at = @At(
                    target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;",
                    remap = false,
                    value = "INVOKE",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void create(CallbackInfoReturnable<Language> cir, ImmutableMap.Builder builder, BiConsumer biConsumer, String string, Object var3) {
        Discord4Fabric.LOGGER.info("Attempting to load modded language files");

        Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
        mods.forEach(mod -> {
            ModMetadata metadata = mod.getMetadata();
            if (metadata instanceof LoaderModMetadata loaderModMetadata) {
                Optional<? extends EntrypointMetadata> optional = loaderModMetadata.getEntrypoints("main").stream().findFirst();
                if (optional.isPresent()) {
                    EntrypointMetadata entrypointMetadata = optional.get();
                    try {
                        InputStream inputStream = FabricLauncherBase
                                .getClass(entrypointMetadata.getValue())
                                .getResourceAsStream("/assets/" + metadata.getId() + "/lang/en_us.json");

                        if (inputStream == null) {
                            return;
                        }

                        Language.load(inputStream, biConsumer);

                        inputStream.close();
                    } catch (JsonParseException | IOException | ClassNotFoundException e) {
                        Utils.logException(e);
                    }
                }
            }
        });
    }
}
