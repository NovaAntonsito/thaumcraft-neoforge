package thaumcraft.client.renderers.block;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.Thaumcraft;
import thaumcraft.common.blocks.world.ore.BlockCrystal;

/**
 * Port of CrystalModel: builds the cluster from models/obj/crystal.obj, which holds eight crystal shards. For every
 * face that touches solid rock it picks growth + 1 shards at random (seeded by the position) and turns them to grow
 * out of that face. A crystal walled in on all six sides draws nothing, like the original.
 * <p>
 * The 1.12 model got the touching faces from IExtendedBlockState; here they come from {@link #getModelData}.
 * Loaded from block models with {@code "loader": "thaumcraft:crystal"}.
 */
@OnlyIn(Dist.CLIENT)
public class CrystalModel implements IDynamicBakedModel {
    public static final ResourceLocation LOADER_ID = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "crystal");
    private static final ResourceLocation MESH = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "models/obj/crystal.obj");
    /** Bit d is set when the face in Direction.from3DDataValue(d) touches solid rock. */
    private static final ModelProperty<Integer> FACES = new ModelProperty<>();
    /** getPackedLightmapCoords: the original lit crystals with at least block light 180 (about 11 of 15). */
    private static final int MIN_BLOCK_LIGHT = 180;

    private final TextureAtlasSprite tex;
    private final Mesh sourceMesh;

    public CrystalModel(TextureAtlasSprite tex) {
        this.tex = tex;
        this.sourceMesh = Mesh.load(MESH);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        int faces = 0;
        for (Direction d : Direction.values()) {
            if (BlockCrystal.drawAt(level, pos, d)) {
                faces |= 1 << d.get3DDataValue();
            }
        }
        return ModelData.builder().with(FACES, faces).build();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {
        if (side != null || state == null || !(state.getBlock() instanceof BlockCrystal crystal) || sourceMesh == null) {
            return List.of();
        }
        Integer faces = data.get(FACES);
        if (faces == null || faces == 0x3F) {
            return List.of();
        }
        long seed = rand.nextLong();
        int m = crystal.getGrowth(state) + 1;
        List<BakedQuad> ret = new ArrayList<>();
        if (has(faces, Direction.UP)) {
            Mesh mod = sourceMesh.pick(m, seed);
            mod.rotate(180.0, new Vector3f(1, 0, 0), new Vector3f(0, 1, 1));
            mod.bake(tex, ret);
        }
        if (has(faces, Direction.DOWN)) {
            sourceMesh.pick(m, seed + 5L).bake(tex, ret);
        }
        if (has(faces, Direction.EAST)) {
            Mesh mod = sourceMesh.pick(m, seed + 10L);
            mod.rotate(90.0, new Vector3f(1, 0, 0), new Vector3f(0, 0, 0));
            mod.rotate(270.0, new Vector3f(0, 1, 0), new Vector3f(1, 1, 0));
            mod.bake(tex, ret);
        }
        if (has(faces, Direction.WEST)) {
            Mesh mod = sourceMesh.pick(m, seed + 15L);
            mod.rotate(90.0, new Vector3f(1, 0, 0), new Vector3f(0, 0, 0));
            mod.rotate(90.0, new Vector3f(0, 1, 0), new Vector3f(0, 1, 1));
            mod.bake(tex, ret);
        }
        if (has(faces, Direction.NORTH)) {
            Mesh mod = sourceMesh.pick(m, seed + 20L);
            mod.rotate(90.0, new Vector3f(1, 0, 0), new Vector3f(0, 1, 0));
            mod.bake(tex, ret);
        }
        if (has(faces, Direction.SOUTH)) {
            Mesh mod = sourceMesh.pick(m, seed + 25L);
            mod.rotate(90.0, new Vector3f(1, 0, 0), new Vector3f(0, 0, 0));
            mod.rotate(180.0, new Vector3f(0, 1, 0), new Vector3f(1, 1, 1));
            mod.bake(tex, ret);
        }
        return ret;
    }

    private static boolean has(int faces, Direction d) {
        return (faces & (1 << d.get3DDataValue())) != 0;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return tex;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    /** The OBJ mesh: one shared vertex list and the eight shards as lists of quads (triangles repeat their last vertex). */
    private static final class Mesh {
        private List<Vector3f> positions = new ArrayList<>();
        private final List<float[]> texCoords = new ArrayList<>();
        /** Each part is a list of faces; each face is four {position index, uv index} pairs. */
        private final List<List<int[][]>> parts = new ArrayList<>();

        static Mesh load(ResourceLocation location) {
            Mesh mesh = new Mesh();
            List<int[][]> part = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Minecraft.getInstance().getResourceManager().open(location), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] t = line.split("\\s+");
                    switch (t[0]) {
                        case "v" -> mesh.positions.add(new Vector3f(Float.parseFloat(t[1]), Float.parseFloat(t[2]), Float.parseFloat(t[3])));
                        case "vt" -> mesh.texCoords.add(new float[]{Float.parseFloat(t[1]), Float.parseFloat(t[2])});
                        // MeshLoader starts a new part on every usemtl.
                        case "usemtl" -> mesh.parts.add(part = new ArrayList<>());
                        case "f" -> {
                            if (part == null) mesh.parts.add(part = new ArrayList<>());
                            int[][] face = new int[4][];
                            for (int i = 0; i < 4; ++i) {
                                String[] idx = t[Math.min(i + 1, t.length - 1)].split("/");
                                face[i] = new int[]{Integer.parseInt(idx[0]) - 1, idx.length > 1 ? Integer.parseInt(idx[1]) - 1 : -1};
                            }
                            part.add(face);
                        }
                        default -> {
                        }
                    }
                }
            } catch (Exception e) {
                Thaumcraft.LOGGER.error("Could not load the crystal mesh {}", location, e);
                return null;
            }
            return mesh;
        }

        /** A copy that keeps {@code count} of the shards, chosen like Collections.shuffle(0..7, new Random(seed)). */
        Mesh pick(int count, long seed) {
            List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
            Collections.shuffle(c, new Random(seed));
            Mesh mm = new Mesh();
            for (Vector3f v : positions) mm.positions.add(new Vector3f(v));
            mm.texCoords.addAll(texCoords);
            for (int a = 0; a < count && a < c.size(); ++a) {
                if (c.get(a) < parts.size()) mm.parts.add(parts.get(c.get(a)));
            }
            return mm;
        }

        /** MeshModel.rotate: turns every vertex around the axis (through the origin), then moves it by offset. */
        void rotate(double degrees, Vector3f axis, Vector3f offset) {
            Quaternionf q = new Quaternionf().rotationAxis((float) Math.toRadians(degrees), axis.x, axis.y, axis.z);
            List<Vector3f> p = new ArrayList<>(positions.size());
            for (Vector3f v : positions) {
                p.add(q.transform(new Vector3f(v)).add(offset));
            }
            positions = p;
        }

        void bake(TextureAtlasSprite sprite, List<BakedQuad> out) {
            for (List<int[][]> part : parts) {
                for (int[][] face : part) {
                    out.add(bakeQuad(face, sprite));
                }
            }
        }

        private BakedQuad bakeQuad(int[][] face, TextureAtlasSprite sprite) {
            Vector3f p0 = positions.get(face[0][0]);
            Vector3f p1 = positions.get(face[1][0]);
            Vector3f p2 = positions.get(face[2][0]);
            Vector3f normal = new Vector3f(p1).sub(p0).cross(new Vector3f(p2).sub(p0));
            if (normal.lengthSquared() > 0.0f) normal.normalize();
            QuadBakingVertexConsumer baker = new QuadBakingVertexConsumer();
            baker.setSprite(sprite);
            baker.setTintIndex(0);
            baker.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
            baker.setShade(true);
            baker.setHasAmbientOcclusion(true);
            for (int[] vertex : face) {
                Vector3f pos = positions.get(vertex[0]);
                float[] uv = vertex[1] >= 0 ? texCoords.get(vertex[1]) : new float[]{0.0f, 0.0f};
                baker.addVertex(pos.x, pos.y, pos.z)
                        .setColor(255, 255, 255, 255)
                        .setUv(sprite.getU(uv[0]), sprite.getV(uv[1]))
                        .setUv2(MIN_BLOCK_LIGHT, 0)
                        .setNormal(normal.x, normal.y, normal.z);
            }
            return baker.bakeQuad();
        }
    }

    public static final class Geometry implements IUnbakedGeometry<Geometry> {
        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
                               ModelState modelState, ItemOverrides overrides) {
            return new CrystalModel(spriteGetter.apply(context.getMaterial("particle")));
        }
    }

    public static final class Loader implements IGeometryLoader<Geometry> {
        public static final Loader INSTANCE = new Loader();

        @Override
        public Geometry read(JsonObject json, JsonDeserializationContext context) {
            return new Geometry();
        }
    }
}
