scoreboard players set chk_water tnttest 1
# the water source outside the tier-2 crater must have flowed into it
# (physics fix: fluids work normally after the explosion)
execute unless block 214 -63 0 minecraft:water run scoreboard players set chk_water tnttest 0
execute unless block 213 -63 0 minecraft:water run scoreboard players set chk_water tnttest 0
say CHECK_WATER_DONE
