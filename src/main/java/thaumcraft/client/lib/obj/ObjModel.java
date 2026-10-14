package thaumcraft.client.lib.obj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import thaumcraft.Thaumcraft;

/**
 * Replaces AdvancedModelLoader / WavefrontObject (IModelCustom): a Wavefront OBJ split in groups ("g" lines) that is
 * drawn face by face into a VertexConsumer. Faces use their own normal and the original's tiny UV inset.
 * Triangles go out as quads with the last vertex repeated, so they work with the entity render types.
 */
@OnlyIn(Dist.CLIENT)
public class ObjModel {
    private static final float TEXTURE_OFFSET = 5.0E-4f;
    private static final Map<ResourceLocation, ObjModel> CACHE = new HashMap<>();

    private final Map<String, List<Face>> groups = new LinkedHashMap<>();

    private record Face(float[][] pos, float[][] uv, Vector3f normal) {
    }

    /** loadModel: flipV like the original's parser (1 - v); the golem press renderer undid it with a flipped texture matrix. */
    public static ObjModel load(ResourceLocation location, boolean flipV) {
        return CACHE.computeIfAbsent(location, l -> parse(l, flipV));
    }

    /** Drops the cache, for resource reloads. */
    public static void clearCache() {
        CACHE.clear();
    }

    private static ObjModel parse(ResourceLocation location, boolean flipV) {
        ObjModel model = new ObjModel();
        List<float[]> verts = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        String group = "default";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Minecraft.getInstance().getResourceManager().open(location), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] t = line.split("\\s+");
                switch (t[0]) {
                    case "v" -> verts.add(new float[]{Float.parseFloat(t[1]), Float.parseFloat(t[2]), Float.parseFloat(t[3])});
                    case "vt" -> {
                        float v = Float.parseFloat(t[2]);
                        uvs.add(new float[]{Float.parseFloat(t[1]), flipV ? 1.0f - v : v});
                    }
                    case "g" -> group = t.length > 1 ? t[1].toLowerCase() : "default";
                    case "f" -> {
                        int n = t.length - 1;
                        float[][] pos = new float[n][];
                        float[][] uv = new float[n][];
                        for (int i = 0; i < n; ++i) {
                            String[] idx = t[i + 1].split("/");
                            pos[i] = verts.get(index(idx[0], verts.size()));
                            uv[i] = idx.length > 1 && !idx[1].isEmpty() ? uvs.get(index(idx[1], uvs.size())) : new float[]{0, 0};
                        }
                        model.addFace(group, pos, uv);
                    }
                    default -> {
                    }
                }
            }
        } catch (Exception e) {
            Thaumcraft.LOGGER.error("Could not load OBJ model {}", location, e);
        }
        return model;
    }

    private static int index(String s, int size) {
        int i = Integer.parseInt(s);
        return i < 0 ? size + i : i - 1;
    }

    private void addFace(String group, float[][] pos, float[][] uv) {
        List<Face> faces = groups.computeIfAbsent(group, g -> new ArrayList<>());
        // Fan out anything bigger than a quad.
        if (pos.length <= 4) {
            faces.add(makeFace(pos, uv));
        } else {
            for (int i = 1; i < pos.length - 1; ++i) {
                faces.add(makeFace(new float[][]{pos[0], pos[i], pos[i + 1]}, new float[][]{uv[0], uv[i], uv[i + 1]}));
            }
        }
    }

    private static Face makeFace(float[][] pos, float[][] uv) {
        // Face.calculateFaceNormal
        Vector3f a = new Vector3f(pos[1][0] - pos[0][0], pos[1][1] - pos[0][1], pos[1][2] - pos[0][2]);
        Vector3f b = new Vector3f(pos[2][0] - pos[0][0], pos[2][1] - pos[0][1], pos[2][2] - pos[0][2]);
        Vector3f n = a.cross(b);
        if (n.lengthSquared() > 0) n.normalize();
        // Face.addFaceForRender: pull each UV a little towards the face's UV center.
        float au = 0, av = 0;
        for (float[] t : uv) {
            au += t[0];
            av += t[1];
        }
        au /= uv.length;
        av /= uv.length;
        float[][] uv2 = new float[uv.length][];
        for (int i = 0; i < uv.length; ++i) {
            float ou = uv[i][0] > au ? -TEXTURE_OFFSET : TEXTURE_OFFSET;
            float ov = uv[i][1] > av ? -TEXTURE_OFFSET : TEXTURE_OFFSET;
            uv2[i] = new float[]{uv[i][0] + ou, uv[i][1] + ov};
        }
        return new Face(pos, uv2, n);
    }

    public Set<String> getPartNames() {
        return groups.keySet();
    }

    public void renderAll(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        for (List<Face> faces : groups.values()) {
            render(faces, pose, buffer, light, overlay, color);
        }
    }

    public void renderPart(String name, PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        List<Face> faces = groups.get(name.toLowerCase());
        if (faces != null) render(faces, pose, buffer, light, overlay, color);
    }

    public void renderAllExcept(String name, PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        String n = name.toLowerCase();
        for (Map.Entry<String, List<Face>> e : groups.entrySet()) {
            if (!e.getKey().equals(n)) render(e.getValue(), pose, buffer, light, overlay, color);
        }
    }

    private static void render(List<Face> faces, PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        PoseStack.Pose last = pose.last();
        for (Face f : faces) {
            int n = f.pos.length;
            for (int i = 0; i < 4; ++i) {
                int k = Math.min(i, n - 1);
                buffer.addVertex(last, f.pos[k][0], f.pos[k][1], f.pos[k][2]).setColor(color).setUv(f.uv[k][0], f.uv[k][1])
                        .setOverlay(overlay).setLight(light).setNormal(last, f.normal.x(), f.normal.y(), f.normal.z());
            }
        }
    }
}
