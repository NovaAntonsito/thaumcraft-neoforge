package thaumcraft.client.fx.beams;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Stands in for the gle library's glePolyCone (CoreGLE) used by the bolts and streams: a tube of the given sides
 * through the points, with a radius and a color per point. As in gle the first and last points only give the
 * direction of the ends. The texture goes once around the tube and runs along it by texScale per point, shifted by
 * texOffset, so a stream that shrinks from its start keeps its pattern in place.
 */
public final class PolyCone {
    private PolyCone() {
    }

    /** ox, oy, oz: where the points start, relative to the camera. */
    public static void draw(VertexConsumer vc, double ox, double oy, double oz, double[][] points, float[][] colours, double[] radii, int sides,
                            float texScale, float texOffset) {
        int n = points.length;
        if (n < 4) return;
        float[][][] rings = new float[n][][];
        double[] u = null;
        for (int i = 1; i < n - 1; ++i) {
            double tx = points[i + 1][0] - points[i - 1][0];
            double ty = points[i + 1][1] - points[i - 1][1];
            double tz = points[i + 1][2] - points[i - 1][2];
            double tl = Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl < 1.0E-9) {
                tx = 0.0;
                ty = 1.0;
                tz = 0.0;
            } else {
                tx /= tl;
                ty /= tl;
                tz /= tl;
            }
            // Carries the side vector along the tube so the rings do not twist.
            if (u == null) {
                u = Math.abs(ty) < 0.9 ? cross(tx, ty, tz, 0.0, 1.0, 0.0) : cross(tx, ty, tz, 1.0, 0.0, 0.0);
            } else {
                double d = u[0] * tx + u[1] * ty + u[2] * tz;
                u = new double[] { u[0] - tx * d, u[1] - ty * d, u[2] - tz * d };
            }
            double ul = Math.sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
            if (ul < 1.0E-9) {
                u = Math.abs(ty) < 0.9 ? cross(tx, ty, tz, 0.0, 1.0, 0.0) : cross(tx, ty, tz, 1.0, 0.0, 0.0);
                ul = Math.sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
            }
            u = new double[] { u[0] / ul, u[1] / ul, u[2] / ul };
            double[] v = cross(tx, ty, tz, u[0], u[1], u[2]);
            double r = radii[i];
            rings[i] = new float[sides + 1][];
            for (int s = 0; s <= sides; ++s) {
                double ang = s * Math.PI * 2.0 / sides;
                double c = Math.cos(ang) * r, sn = Math.sin(ang) * r;
                rings[i][s] = new float[] { (float) (ox + points[i][0] + u[0] * c + v[0] * sn), (float) (oy + points[i][1] + u[1] * c + v[1] * sn),
                        (float) (oz + points[i][2] + u[2] * c + v[2] * sn) };
            }
        }
        for (int i = 1; i < n - 2; ++i) {
            float v0 = i * texScale + texOffset;
            float v1 = (i + 1) * texScale + texOffset;
            float[] c0 = colours[i], c1 = colours[i + 1];
            for (int s = 0; s < sides; ++s) {
                float u0 = s / (float) sides, u1 = (s + 1) / (float) sides;
                vertex(vc, rings[i][s], u0, v0, c0);
                vertex(vc, rings[i + 1][s], u0, v1, c1);
                vertex(vc, rings[i + 1][s + 1], u1, v1, c1);
                vertex(vc, rings[i][s + 1], u1, v0, c0);
            }
        }
    }

    private static void vertex(VertexConsumer vc, float[] p, float u, float v, float[] c) {
        vc.addVertex(p[0], p[1], p[2]).setUv(u, v).setColor(c[0], c[1], c[2], c[3]);
    }

    private static double[] cross(double ax, double ay, double az, double bx, double by, double bz) {
        return new double[] { ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx };
    }
}
