package com.naizo.elementals.core;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

/** Compact, copyable codes for sharing an allocation, like "RSP1-AbCd...". */
public final class BuildCode {
    public static final String PREFIX = "RSP1-";

    public record Decoded(List<String> nodes, boolean sameTree) {
    }

    private BuildCode() {
    }

    public static String encode(SkillTree tree, Collection<String> allocated) {
        List<Integer> indices = new ArrayList<>();
        for (String id : allocated) {
            int index = tree.indexOf(id);
            if (index >= 0) {
                indices.add(index);
            }
        }
        indices.sort(Integer::compareTo);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long hash = tree.hash();
        for (int i = 0; i < 4; i++) {
            out.write((int) (hash >>> (8 * i)) & 0xFF);
        }
        writeVar(out, indices.size());
        int previous = 0;
        for (int index : indices) {
            writeVar(out, index - previous);
            previous = index;
        }
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
    }

    public static Decoded decode(SkillTree tree, String code) {
        String trimmed = code == null ? "" : code.trim();
        if (!trimmed.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Not a Remnants build code");
        }
        byte[] bytes;
        try {
            bytes = Base64.getUrlDecoder().decode(trimmed.substring(PREFIX.length()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The build code is damaged");
        }
        if (bytes.length < 5) {
            throw new IllegalArgumentException("The build code is too short");
        }
        long hash = 0;
        for (int i = 0; i < 4; i++) {
            hash |= (long) (bytes[i] & 0xFF) << (8 * i);
        }
        int[] position = {4};
        int count = readVar(bytes, position);
        List<String> nodes = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < count; i++) {
            index += readVar(bytes, position);
            String id = tree.idAt(index);
            if (id != null) {
                nodes.add(id);
            }
        }
        return new Decoded(nodes, hash == tree.hash());
    }

    private static void writeVar(ByteArrayOutputStream out, int value) {
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value);
    }

    private static int readVar(byte[] bytes, int[] position) {
        int value = 0;
        int shift = 0;
        while (true) {
            if (position[0] >= bytes.length) {
                throw new IllegalArgumentException("The build code is damaged");
            }
            int b = bytes[position[0]++];
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return value;
            }
            shift += 7;
            if (shift > 28) {
                throw new IllegalArgumentException("The build code is damaged");
            }
        }
    }
}
