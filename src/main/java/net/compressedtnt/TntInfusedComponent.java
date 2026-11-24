package net.compressedtnt;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * Data component added to any tool / armor combined with 8 TNT
 * (8 TNT around the item in the crafting grid).
 *
 * <ul>
 *  <li>armor pieces: each piece absorbs 22.5% of explosion damage,
 *      a full set reduces it by 90%;</li>
 *  <li>tools &amp; swords: work exactly like normal, but every use has a
 *      4-8% chance to trigger a small (~5 block) explosion.</li>
 * </ul>
 */
public record TntInfusedComponent(float power) {
        public static final TntInfusedComponent DEFAULT = new TntInfusedComponent(1.0f);

        public static final Codec<TntInfusedComponent> CODEC =
                        Codec.FLOAT.xmap(TntInfusedComponent::new, TntInfusedComponent::power);

        public static final PacketCodec<ByteBuf, TntInfusedComponent> PACKET_CODEC =
                        PacketCodecs.FLOAT.xmap(TntInfusedComponent::new, TntInfusedComponent::power);
}
