package player;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera {
    private final Vector3f position = new Vector3f();
    private float yaw;
    private float pitch;

    public Vector3f position() {
        return position;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Matrix4f viewMatrix() {
        return new Matrix4f()
                .rotateX((float) Math.toRadians(pitch))
                .rotateY((float) Math.toRadians(yaw))
                .translate(-position.x, -position.y, -position.z);
    }

    public Vector3f forward(Vector3f dest) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        dest.x = (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        dest.y = (float) (-Math.sin(pitchRad));
        dest.z = (float) (-Math.cos(pitchRad) * Math.cos(yawRad));
        return dest.normalize();
    }

    public Vector3f right(Vector3f dest) {
        float yawRad = (float) Math.toRadians(yaw + 90.0f);
        dest.x = (float) Math.sin(yawRad);
        dest.y = 0.0f;
        dest.z = (float) -Math.cos(yawRad);
        return dest.normalize();
    }
}
