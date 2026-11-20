package micdoodle8.mods.galacticraft.core.recipe.legacy;

/**
 * The cargo-add-on combinations shared by every NASA-bench rocket.
 *
 * <p>
 * Tier 1, Tier 2 and Tier 3 all offer the same eight variants: the bare rocket plus every non-empty subset of the three
 * add-on slots, with the output metadata equal to the number of chests (0 &ndash; 3, i.e. no cargo, 18, 36 and 54
 * slots). Keeping the table here means the three recipe sets cannot disagree about it, and it can be asserted directly
 * in a unit test.
 */
public final class LegacyCargoLayout {

    /**
     * Cargo layout of each variant as a bitmask over the bench's add-on slots: bit 0 = first add-on slot, bit 1 =
     * second, bit 2 = third.
     */
    public static final int[] MASKS = { 0x0, 0x1, 0x2, 0x4, 0x3, 0x5, 0x6, 0x7 };

    /** Output metadata (cargo tier) of each variant, matching {@link #MASKS}. */
    public static final int[] OUTPUT_METAS = { 0, 1, 1, 1, 2, 2, 2, 3 };

    /** Number of variants. */
    public static final int COUNT = MASKS.length;

    private LegacyCargoLayout() {}

    /** True when the variant puts a chest in add-on slot {@code bit} (0-based). */
    public static boolean hasChest(int mask, int bit) {
        return (mask & 1 << bit) != 0;
    }
}
