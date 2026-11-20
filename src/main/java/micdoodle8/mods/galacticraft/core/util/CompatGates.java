package micdoodle8.mods.galacticraft.core.util;

import cpw.mods.fml.common.Loader;
import micdoodle8.mods.galacticraft.core.Constants;

/**
 * Decides which content path Galacticraft runs.
 *
 * <p>
 * The GTNH fork gates its content on <code>dreamcraft</code> (NewHorizonsCoreMod, i.e. "this is the GTNH pack") but
 * identifies GregTech by two other mod ids (<code>gregtech</code> and <code>gregtech_nh</code>). Those are three
 * different questions and conflating them is what broke non-GTNH packs. This class is the single place that answers
 * them.
 *
 * <p>
 * Two orthogonal questions:
 * <ul>
 * <li>{@link #useLegacyContent()} &mdash; are we running the original Galacticraft content (recipes, benches, NEI)
 * rather than the GTNH/corerod replacements?</li>
 * <li>{@link #useGregTechItems()} &mdash; are GregTech items actually present, so a GT-shaped slot whitelist can be
 * satisfied?</li>
 * </ul>
 *
 * <p>
 * Defaults reproduce the GTNH fork's behaviour exactly (legacy content iff <code>dreamcraft</code> is absent), so this
 * change is inert inside the GTNH pack. The {@link ConfigManagerCore#gtnhCompatMode} option exists so both paths can be
 * exercised from a single dev instance.
 */
public final class CompatGates {

    /** Config value: pick automatically from the loaded mods. */
    public static final int MODE_AUTO = -1;
    /** Config value: always run the original Galacticraft content. */
    public static final int MODE_LEGACY = 0;
    /** Config value: always run the GTNH content. */
    public static final int MODE_GTNH = 1;

    private CompatGates() {}

    /** True when the GTNH pack mod (NewHorizonsCoreMod) is present. */
    public static boolean isGTNHPack() {
        return Loader.isModLoaded(Constants.MOD_ID_NEW_HORIZONS_CORE_MOD);
    }

    /**
     * True when GregTech is present. Covers both the standalone (non-GTNH) mod id and the GTNH fork's id, because slot
     * whitelists that look up <code>gt.metaitem.01</code> work with either.
     */
    public static boolean isGregTechLoaded() {
        return Loader.isModLoaded(Constants.MOD_ID_GREGTECH) || Loader.isModLoaded(Constants.MOD_ID_GREGTECH_NH);
    }

    /**
     * True when the original Galacticraft recipes/benches should be registered instead of the GTNH-supplied ones. This
     * is the replacement for the fork's bare <code>!Loader.isModLoaded("dreamcraft")</code> checks.
     */
    public static boolean useLegacyContent() {
        switch (ConfigManagerCore.gtnhCompatMode) {
            case MODE_LEGACY:
                return true;
            case MODE_GTNH:
                return false;
            default:
                return !isGTNHPack();
        }
    }

    /**
     * True when a GregTech-shaped slot whitelist can be satisfied. The GTNH fork guards these with
     * <code>&& dreamcraft</code>, which leaves the slots accepting <em>nothing</em> in a pack that has GregTech but not
     * the GTNH coremod.
     */
    public static boolean useGregTechItems() {
        return isGregTechLoaded();
    }
}
