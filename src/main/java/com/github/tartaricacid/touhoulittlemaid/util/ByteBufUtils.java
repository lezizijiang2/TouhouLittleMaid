package com.github.tartaricacid.touhoulittlemaid.util;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ByteBufUtils {
    public static final StreamCodec<ByteBuf, Set<String>> STRING_SET_CODEC = ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8);
    public static final StreamCodec<ByteBuf, List<String>> STRING_LIST_CODEC = ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8);
    public static final StreamCodec<ByteBuf, Map<String, List<String>>> SITES_CODEC = ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, STRING_LIST_CODEC);
    public static final StreamCodec<ByteBuf, Object2FloatOpenHashMap<String>> OBJECT_2_FLOAT_OPEN_HASH_MAP_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buffer, Object2FloatOpenHashMap<String> map) {
            buffer.writeInt(map.size());
            map.forEach((key, value) -> {
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                buffer.writeInt(bytes.length);
                buffer.writeBytes(bytes);
                buffer.writeFloat(value);
            });
        }

        @Override
        public Object2FloatOpenHashMap<String> decode(ByteBuf buffer) {
            int size = buffer.readInt();
            Object2FloatOpenHashMap<String> map = new Object2FloatOpenHashMap<>();
            for (int i = 0; i < size; i++) {
                byte[] bytes = new byte[buffer.readInt()];
                buffer.readBytes(bytes);
                map.put(new String(bytes, StandardCharsets.UTF_8), buffer.readFloat());
            }
            return map;
        }
    };
}
