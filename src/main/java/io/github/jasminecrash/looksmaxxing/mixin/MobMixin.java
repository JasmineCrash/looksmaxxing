package io.github.jasminecrash.looksmaxxing.mixin;

import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommandGoal;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
	//add a custom behavior goal to mobs that can be triggered by modifying the goal (usually it'll just do nothing)
	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectRTSGoals(CallbackInfo ci) {
		Mob self = (Mob)(Object) this;
		//Looksmaxxing.LOGGER.info("injectRTSGoals firing for " + self.getPlainTextName());
		if (!self.level().isClientSide()) {
			self.goalSelector.addGoal(-100, new RTSCommandGoal(self));
		}
	}
}