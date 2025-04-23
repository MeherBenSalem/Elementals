package com.naizo.elementals.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class MainConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Double> BURN_TIMER_1;
	public static final ForgeConfigSpec.ConfigValue<Double> BURN_TIMER_2;
	public static final ForgeConfigSpec.ConfigValue<Double> EXPLOSION_POWER;
	public static final ForgeConfigSpec.ConfigValue<Double> CDR_S2;
	public static final ForgeConfigSpec.ConfigValue<Double> ABSORPTION_LVL;
	public static final ForgeConfigSpec.ConfigValue<Double> CDR_S3;
	public static final ForgeConfigSpec.ConfigValue<Double> DAMAGE_INFERNO;
	public static final ForgeConfigSpec.ConfigValue<Double> CDR_S4;
	static {
		BUILDER.push("Spells");
		BUILDER.push("Signature Spell");
		BURN_TIMER_1 = BUILDER.comment("The time the spell wil burn the enemy when its at level 0").define("burn_timer_1", (double) 3);
		BURN_TIMER_2 = BUILDER.comment("The time the spell wil burn the enemy when its at level 1+").define("burn_timer_2", (double) 5);
		BUILDER.pop();
		BUILDER.push("Explosive Orb");
		EXPLOSION_POWER = BUILDER.comment("the power of the explosion").define("explosion_power", (double) 6);
		CDR_S2 = BUILDER.comment("the damage dealt to the targets").define("cdr", (double) 100);
		BUILDER.pop();
		BUILDER.push("Ember Shield");
		ABSORPTION_LVL = BUILDER.comment("the level of the absorption buff").define("absorption_lvl", (double) 3);
		CDR_S3 = BUILDER.comment("the damage dealt to the targets").define("cdr", (double) 200);
		BUILDER.pop();
		BUILDER.push("Inferno Wave");
		DAMAGE_INFERNO = BUILDER.comment("the damage dealt to the targets").define("damage", (double) 3);
		CDR_S4 = BUILDER.comment("the damage dealt to the targets").define("cdr", (double) 300);
		BUILDER.pop();
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
