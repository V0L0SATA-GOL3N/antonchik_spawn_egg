package com.example.antonchik.entity.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses a Wavefront {@code .mtl} library, keeping only the diffuse map ({@code map_Kd}) of each
 * material. The remaining PBR maps ({@code map_Ns}, {@code map_Bump}, {@code map_refl}, …) are
 * ignored because Minecraft 1.12.2's fixed-function pipeline cannot use them without a shader mod.
 *
 * <p>Diffuse filenames are resolved as entity textures under a fixed base path, e.g. material
 * {@code Bodymat} with {@code map_Kd Remy_Body_Diffuse.png} becomes
 * {@code antonchik:textures/entity/anton/Remy_Body_Diffuse.png}.
 */
public class MtlLib
{
    private final Map<String, ResourceLocation> diffuse = new HashMap<String, ResourceLocation>();

    private MtlLib() {}

    /**
     * @param location    the {@code .mtl} resource
     * @param modid       texture domain for the resolved {@link ResourceLocation}s
     * @param texturePath base path (relative to the domain) that {@code map_Kd} filenames sit under,
     *                    e.g. {@code "textures/entity/anton/"}
     */
    public static MtlLib load(ResourceLocation location, String modid, String texturePath) throws IOException
    {
        MtlLib lib = new MtlLib();
        InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
        try
        {
            lib.parse(in, modid, texturePath);
        }
        finally
        {
            in.close();
        }
        return lib;
    }

    private void parse(InputStream in, String modid, String texturePath) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        String current = null;
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#')
            {
                continue;
            }

            String[] tokens = line.split("\\s+");
            if ("newmtl".equals(tokens[0]) && tokens.length > 1)
            {
                current = tokens[1];
            }
            else if ("map_Kd".equals(tokens[0]) && current != null && tokens.length > 1)
            {
                // The filename is the last token (earlier tokens may be options like "-bm 1.0").
                String file = basename(tokens[tokens.length - 1]);
                diffuse.put(current, new ResourceLocation(modid, texturePath + file));
            }
        }
    }

    /** Strips any directory portion of a path, handling both {@code /} and {@code \} separators. */
    private static String basename(String path)
    {
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /** Diffuse texture for a material, or {@code null} if the material has none. */
    public ResourceLocation getDiffuse(String material)
    {
        return material == null ? null : diffuse.get(material);
    }
}
