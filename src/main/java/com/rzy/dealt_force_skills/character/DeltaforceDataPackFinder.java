package com.rzy.dealt_force_skills.character;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.PathPackResources;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum DeltaforceDataPackFinder implements net.minecraft.server.packs.repository.RepositorySource {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(DealtForceSkillsMod.MODID + "/deltaforce_packs");

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(INSTANCE);
        }
    }

    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        CharacterBranchPackManager.bootstrapDefaults();
        Path root = CharacterBranchPackManager.rootDirectory();
        if (Files.notExists(root)) {
            return;
        }
        try (var stream = Files.list(root)) {
            stream.filter(Files::isDirectory)
                    .filter(DeltaforceDataPackFinder::isDataPack)
                    .sorted((left, right) -> left.getFileName().toString()
                            .compareToIgnoreCase(right.getFileName().toString()))
                    .forEach(path -> createPack(path, onLoad));
        } catch (IOException error) {
            LOGGER.warn("Unable to scan deltaforce data-pack directory {}", root, error);
        }
    }

    private static boolean isDataPack(Path path) {
        return Files.isRegularFile(path.resolve("pack.mcmeta")) && Files.isDirectory(path.resolve("data"));
    }

    private static void createPack(Path path, Consumer<Pack> onLoad) {
        String folderName = path.getFileName().toString();
        String id = DealtForceSkillsMod.MODID + "/deltaforce/" + sanitizePackId(folderName);
        Pack pack = Pack.readMetaAndCreate(
                id,
                Component.literal("Delta Force " + folderName),
                true,
                packId -> new PathPackResources(packId, false, path),
                PackType.SERVER_DATA,
                Pack.Position.TOP,
                PackSource.BUILT_IN
        );
        if (pack != null) {
            onLoad.accept(pack);
            LOGGER.info("Added deltaforce data pack {}", path);
        } else {
            LOGGER.warn("Ignoring deltaforce data pack with invalid metadata: {}", path);
        }
    }

    private static String sanitizePackId(String value) {
        StringBuilder result = new StringBuilder();
        value.toLowerCase(Locale.ROOT).codePoints().forEach(codePoint -> {
            if ((codePoint >= 'a' && codePoint <= 'z') || (codePoint >= '0' && codePoint <= '9')) {
                result.appendCodePoint(codePoint);
            } else if (result.length() == 0 || result.charAt(result.length() - 1) != '_') {
                result.append('_');
            }
        });
        String sanitized = result.toString().replaceAll("^_+|_+$", "");
        return sanitized.isBlank() ? "pack" : sanitized;
    }
}
