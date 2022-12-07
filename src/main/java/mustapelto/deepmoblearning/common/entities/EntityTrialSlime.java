package mustapelto.deepmoblearning.common.entities;

import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.world.World;

public class EntityTrialSlime extends EntitySlime {

    public EntityTrialSlime(World world) {
        super(world);
        setSlimeSize(3, true);
    }

    @Override
    public void setDead() {
        isDead = true;
    }
}
