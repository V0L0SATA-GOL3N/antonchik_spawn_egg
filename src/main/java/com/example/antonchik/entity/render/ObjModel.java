package com.example.antonchik.entity.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal self-contained Wavefront OBJ loader and renderer.
 *
 * <p>Parses geometry (positions {@code v}, texture coords {@code vt}, normals {@code vn} and
 * polygonal faces {@code f}) from a {@code .obj} resource and compiles it into OpenGL display
 * lists for fast per-frame rendering. Faces are split into {@link Group groups} by {@code usemtl}
 * so the renderer can bind a different texture per material — the entity texture is bound directly
 * rather than through Forge's block-atlas OBJ pipeline, which keeps this reliable for entities.
 *
 * <p>Faces with more than three vertices are triangulated with a simple fan, so quads and n-gons
 * are supported. OBJ indices may be 1-based positive or negative (relative to the end of the list).
 */
public class ObjModel
{
    /** A run of faces that share a single material (from {@code usemtl}). */
    public static class Group
    {
        /** Material name from {@code usemtl}, or {@code null} for faces declared before any material. */
        public final String material;
        private final List<int[][]> faces = new ArrayList<int[][]>();

        private Group(String material)
        {
            this.material = material;
        }
    }

    /** Receives each group's material name so the caller can bind the matching texture. */
    public interface MaterialBinder
    {
        void bind(String material);
    }

    private final List<float[]> positions = new ArrayList<float[]>();
    private final List<float[]> texCoords = new ArrayList<float[]>();
    private final List<float[]> normals = new ArrayList<float[]>();
    private final List<Group> groups = new ArrayList<Group>();

    private Group currentGroup;

    private ObjModel() {}

    /**
     * Loads and parses an OBJ from the active resource manager.
     *
     * @param location e.g. {@code new ResourceLocation("antonchik", "models/entity/antonchik.obj")}
     */
    public static ObjModel load(ResourceLocation location) throws IOException
    {
        ObjModel model = new ObjModel();
        InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
        try
        {
            model.parse(in);
        }
        finally
        {
            in.close();
        }
        return model;
    }

    private void parse(InputStream in) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#')
            {
                continue;
            }

            String[] tokens = line.split("\\s+");
            String type = tokens[0];

            if ("v".equals(type))
            {
                positions.add(new float[] {
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                });
            }
            else if ("vt".equals(type))
            {
                texCoords.add(new float[] {
                    Float.parseFloat(tokens[1]),
                    tokens.length > 2 ? Float.parseFloat(tokens[2]) : 0.0F
                });
            }
            else if ("vn".equals(type))
            {
                normals.add(new float[] {
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                });
            }
            else if ("usemtl".equals(type))
            {
                currentGroup = new Group(tokens.length > 1 ? tokens[1] : null);
                groups.add(currentGroup);
            }
            else if ("f".equals(type))
            {
                if (currentGroup == null)
                {
                    // Faces before any usemtl: collect them under a default (untextured) group.
                    currentGroup = new Group(null);
                    groups.add(currentGroup);
                }
                int count = tokens.length - 1;
                int[][] face = new int[count][];
                for (int i = 0; i < count; i++)
                {
                    face[i] = parseVertex(tokens[i + 1]);
                }
                currentGroup.faces.add(face);
            }
        }
    }

    /** Parses one {@code v}, {@code v/vt}, {@code v//vn} or {@code v/vt/vn} face vertex into 0-based indices. */
    private int[] parseVertex(String token)
    {
        String[] parts = token.split("/", -1);
        int pos = resolve(parts[0], positions.size());
        int tex = parts.length > 1 && !parts[1].isEmpty() ? resolve(parts[1], texCoords.size()) : -1;
        int norm = parts.length > 2 && !parts[2].isEmpty() ? resolve(parts[2], normals.size()) : -1;
        return new int[] { pos, tex, norm };
    }

    /** Converts a 1-based or negative (relative) OBJ index into a 0-based array index. */
    private static int resolve(String value, int size)
    {
        int index = Integer.parseInt(value);
        return index < 0 ? size + index : index - 1;
    }

    /**
     * Renders every material group, asking {@code binder} to bind the appropriate texture before
     * each group is drawn. Geometry is tessellated directly each call (no display list) so it is
     * robust against driver-specific display-list quirks.
     */
    public void render(MaterialBinder binder)
    {
        for (Group group : groups)
        {
            binder.bind(group.material);
            draw(group);
        }
    }

    /** Human-readable load summary for logging, e.g. {@code "5 groups, 60956 faces"}. */
    public String summary()
    {
        int faceCount = 0;
        for (Group group : groups)
        {
            faceCount += group.faces.size();
        }
        return groups.size() + " groups, " + faceCount + " faces";
    }

    private void draw(Group group)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int[][] face : group.faces)
        {
            // Triangle fan: (0, i, i+1) covers triangles, quads and n-gons.
            for (int i = 1; i + 1 < face.length; i++)
            {
                addVertex(buffer, face[0]);
                addVertex(buffer, face[i]);
                addVertex(buffer, face[i + 1]);
            }
        }

        tessellator.draw();
    }

    private void addVertex(BufferBuilder buffer, int[] vertex)
    {
        float[] pos = vertex[0] >= 0 && vertex[0] < positions.size() ? positions.get(vertex[0]) : null;
        buffer.pos(pos != null ? pos[0] : 0.0F, pos != null ? pos[1] : 0.0F, pos != null ? pos[2] : 0.0F);

        if (vertex[1] >= 0 && vertex[1] < texCoords.size())
        {
            float[] uv = texCoords.get(vertex[1]);
            // OBJ uses a bottom-left UV origin; flip V to match Minecraft's top-left textures.
            buffer.tex(uv[0], 1.0F - uv[1]);
        }
        else
        {
            buffer.tex(0.0F, 0.0F);
        }

        if (vertex[2] >= 0 && vertex[2] < normals.size())
        {
            float[] normal = normals.get(vertex[2]);
            buffer.normal(normal[0], normal[1], normal[2]);
        }
        else
        {
            buffer.normal(0.0F, 1.0F, 0.0F);
        }

        buffer.endVertex();
    }
}
