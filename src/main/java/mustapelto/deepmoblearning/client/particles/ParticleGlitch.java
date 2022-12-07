package mustapelto.deepmoblearning.client.particles;

import net.minecraft.client.particle.ParticlePortal;
import net.minecraft.world.World;

public class ParticleGlitch extends ParticlePortal {

    public ParticleGlitch(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        particleRed = 0.0F;
        particleGreen = 0.81F;
        particleBlue = 0.8F;
    }
}
